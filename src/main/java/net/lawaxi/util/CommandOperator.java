package net.lawaxi.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.Shitboy;
import net.lawaxi.model.Pocket48RoomInfo;
import net.lawaxi.model.WeidianCookie;
import net.lawaxi.util.handler.Pocket48Handler;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.UserMessageEvent;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.IOException;

public class CommandOperator {
    public CommandOperator() {

    }

    public Message executePublic(String[] args, Group g, long senderID) {
        long group = Long.valueOf(g.getId());

        switch (args[0]) {
            case "/口袋":
                if (args.length == 1) {
                    return getHelp(2);
                } else if (args.length == 2) {
                    switch (args[1]) {
                        case "关注列表": {
                            String out = "本群口袋房间关注列表：\n";
                            if (!Shitboy.INSTANCE.getProperties().pocket48_subscribe.containsKey(group))
                                return new PlainText("暂无");

                            int count = 1;
                            for (int room_id : Shitboy.INSTANCE.getProperties().pocket48_subscribe.get(group).getRoomIDs()) {

                                try {
                                    out += count + ". (" + room_id + ")";
                                    count++;

                                    Pocket48RoomInfo roomInfo = Shitboy.INSTANCE.getHandlerPocket48().getRoomInfoByChannelID(room_id);
                                    if (roomInfo != null) {
                                        String roomName = roomInfo.getRoomName();
                                        String ownerName = roomInfo.getOwnerName();
                                        out += roomName + "(" + ownerName + ")\n";
                                    } else
                                        out += "未知房间" + room_id + "\n";

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    out += "null\n";
                                }
                            }
                            return new PlainText(out);
                        }
                        case "余额": {
                            return new PlainText("" + Shitboy.INSTANCE.getHandlerPocket48().getBalance());
                        }
                        /*
                        case "查直播": {
                            String out = "";
                            int count = 1;
                            for (Object liveRoom : Shitboy.INSTANCE.getHandlerPocket48().getLiveList()) {
                                JSONObject liveRoom1 = JSONUtil.parseObj(liveRoom);
                                JSONObject userInfo = liveRoom1.getJSONObject("userInfo");

                                String title = liveRoom1.getStr("title");
                                String name = userInfo.getStr("starName");
                                String userId = userInfo.getStr("userId");
                                out += count + ". (" + userId + ")" + name + ": " + title + "\n";
                                count++;
                            }
                            return new PlainText(out);
                        }
                        case "查录播": {
                            String out = "";
                            int count = 1;
                            for (Object liveRoom : Shitboy.INSTANCE.getHandlerPocket48().getRecordList()) {
                                JSONObject liveRoom1 = JSONUtil.parseObj(liveRoom);
                                JSONObject userInfo = liveRoom1.getJSONObject("userInfo");

                                String title = liveRoom1.getStr("title");
                                String name = userInfo.getStr("starName");
                                String userId = userInfo.getStr("userId");
                                out += count + ". (" + userId + ")" + name + ": " + title + "\n";
                                count++;
                            }
                            return new PlainText(out);

                        }*/
                        default:
                            return getHelp(2);
                    }

                } else if (args.length == 3) {
                    switch (args[1]) {
                        case "搜索": {
                            Object[] servers = Shitboy.INSTANCE.getHandlerPocket48().search(args[2]);
                            String out = "关键词：" + args[2] + "\n";

                            if (servers.length == 0)
                                return new PlainText(out + "仅支持搜索在团小偶像/队伍名");

                            int count = 1;
                            for (Object server_ : servers) {
                                JSONObject server = JSONUtil.parseObj(server_);
                                String name = server.getStr("serverDefaultName");
                                String serverName = server.getStr("serverName");
                                int starId = server.getInt("serverOwner");
                                Integer serverId = server.getInt("serverId");

                                out += count + ". " + name + "(" + serverName + ")\nid: " + starId + "\n";
                                try {
                                    out += informationFromPocketServerId(serverId);
                                } catch (Exception e) {
                                    out += serverId == null ? "" : "Server_id: " + serverId + "\n房间信息获取失败\n";
                                }
                                count++;
                            }
                            return new PlainText(out);
                        }
                        case "查询": {
                            int star_ID = Integer.valueOf(args[2]);
                            JSONObject info = Shitboy.INSTANCE.getHandlerPocket48().getUserInfo(star_ID);
                            if (info == null)
                                return new PlainText("用户不存在");

                            boolean star = info.getBool("isStar");
                            int followers = info.getInt("followers");
                            int friends = info.getInt("friends");
                            String nickName = info.getStr("nickname");
                            String starName = info.getStr("starName");
                            String avatar = Pocket48Handler.SOURCEROOT + info.getStr("avatar");
                            String out = (star ? "【成员】" : "【聚聚】") + nickName + (starName != null ? "(" + starName + ")" : "") + "\n"
                                    + "关注 " + friends + " 粉丝 " + followers + "\n";

                            //Server
                            Integer serverId = Shitboy.INSTANCE.getHandlerPocket48().getServerIDByStarID(star_ID);
                            try {
                                out += informationFromPocketServerId(serverId);
                            } catch (Exception e) {
                                out += serverId == null ? "" : "Server_id: " + serverId + "\n房间信息获取失败\n";
                            }

                            //头像
                            try {
                                return new PlainText(out).plus(
                                        g.uploadImage(ExternalResource.create(HttpRequest.get(avatar).execute().bodyStream())));
                            } catch (IOException e) {
                                return new PlainText(out);
                            }
                        }
                        case "查询2": {
                            int server_id = Integer.valueOf(args[2]);
                            if (server_id != 0) {
                                try {
                                    return new PlainText(informationFromPocketServerId(server_id));
                                } catch (Exception e) {
                                    return new PlainText("Server_id不存在或房间信息获取失败");
                                }
                            }
                            return new PlainText("请输入合法的Server_id");
                        }
                        case "关注": {
                            if (!Shitboy.INSTANCE.getConfig().isAdmin(g, senderID))
                                return new PlainText("权限不足喵");

                            Pocket48RoomInfo roomInfo = Shitboy.INSTANCE.getHandlerPocket48().getRoomInfoByChannelID(Integer.valueOf(args[2]));
                            if (roomInfo == null) {
                                return new PlainText("房间ID不存在。查询房间ID请输入/口袋 查询 <成员ID>");
                            }

                            if (Shitboy.INSTANCE.getConfig().addPocket48RoomSubscribe(Integer.valueOf(args[2]), group)) {
                                String roomName = roomInfo.getRoomName();
                                String ownerName = roomInfo.getOwnerName();
                                return new PlainText("本群新增关注：" + roomName + "(" + ownerName + ")");
                            } else
                                return new PlainText("本群已经关注过这个房间了");
                        }

                        case "取消关注": {
                            if (!Shitboy.INSTANCE.getConfig().isAdmin(g, senderID))
                                return new PlainText("权限不足喵");

                            if (!Shitboy.INSTANCE.getProperties().pocket48_subscribe.containsKey(group))
                                return new PlainText("本群暂无房间关注，先添加一个吧~");

                            if (Shitboy.INSTANCE.getConfig().rmPocket48RoomSubscribe(Integer.valueOf(args[2]), group)) {
                                Pocket48RoomInfo roomInfo = Shitboy.INSTANCE.getHandlerPocket48().getRoomInfoByChannelID(Integer.valueOf(args[2]));
                                if (roomInfo != null) {
                                    String roomName = roomInfo.getRoomName();
                                    String ownerName = roomInfo.getOwnerName();
                                    return new PlainText("本群取消关注：" + roomName + "(" + ownerName + ")");
                                } else return new PlainText("本群取消关注：未知房间");
                            } else
                                return new PlainText("本群没有关注此房间捏~");

                        }
                    }
                } else if (args.length == 4) {
                    if (args[1].equals("连接")) {
                        int room_id = Integer.valueOf(args[2]);
                        int server_id = Integer.valueOf(args[3]);
                        if (testRoomIDWithServerID(room_id, server_id)) {
                            if (Shitboy.INSTANCE.getConfig().addRoomIDConnection(room_id, server_id))
                                return new PlainText("连接成功");
                            else
                                return new PlainText("建立过此连接");
                        } else
                            return new PlainText("您输入的ServerId并不包含此RoomId");
                    }
                }
            case "/bili":
                if (args.length == 1) {
                    return getHelp(3);
                } else if (args.length == 2) {
                    if (args[1].equals("关注列表")) {
                        String out = "本群Bilibili直播间关注列表：\n";
                        if (!Shitboy.INSTANCE.getProperties().bilibili_subscribe.containsKey(group))
                            return new PlainText("暂无");

                        int count = 1;
                        for (int room_id : Shitboy.INSTANCE.getProperties().bilibili_subscribe.get(group)) {
                            out += count + ". (" + room_id + ")";
                            count++;

                            JSONObject data = Shitboy.INSTANCE.getHandlerBilibili().getLiveData(room_id);
                            if (data.getInt("code") == 0) {
                                JSONObject info = JSONUtil.parseObj(data.getObj("data"));
                                String name = Shitboy.INSTANCE.getHandlerBilibili().getNameByMid(info.getInt("uid"));
                                out += name + "\n";
                            }
                        }
                        return new PlainText(out);
                    }
                    return getHelp(3);

                } else {
                    switch (args[1]) {
                        case "关注": {
                            if (!Shitboy.INSTANCE.getConfig().isAdmin(g, senderID))
                                return new PlainText("权限不足喵");

                            JSONObject data = Shitboy.INSTANCE.getHandlerBilibili().getLiveData(Integer.valueOf(args[2]));
                            if (data.getInt("code") == 1) {
                                return new PlainText("直播ID不存在。提示：直播ID是直播间链接最后的数字，不是B站用户uid");
                            }

                            if (Shitboy.INSTANCE.getConfig().addBilibiliLiveSubscribe(Integer.valueOf(args[2]), group)) {
                                JSONObject info = JSONUtil.parseObj(data.getObj("data"));
                                String name = Shitboy.INSTANCE.getHandlerBilibili().getNameByMid(info.getInt("uid"));
                                return new PlainText("本群新增关注：" + name + "的直播间");
                            } else
                                return new PlainText("本群已经关注过这个直播间了");
                        }

                        case "取消关注": {
                            if (!Shitboy.INSTANCE.getConfig().isAdmin(g, senderID))
                                return new PlainText("权限不足喵");

                            if (!Shitboy.INSTANCE.getProperties().bilibili_subscribe.containsKey(group))
                                return new PlainText("本群暂无Bilibili直播间关注，先添加一个吧~");

                            if (Shitboy.INSTANCE.getConfig().rmBilibiliLiveSubscribe(Integer.valueOf(args[2]), group)) {
                                JSONObject data = Shitboy.INSTANCE.getHandlerBilibili().getLiveData(Integer.valueOf(args[2]));
                                if (data.getInt("code") == 0) {
                                    JSONObject info = JSONUtil.parseObj(data.getObj("data"));
                                    String name = Shitboy.INSTANCE.getHandlerBilibili().getNameByMid(info.getInt("uid"));
                                    return new PlainText("本群取消关注：" + name + "的直播间");
                                } else return new PlainText("本群取消关注：未知直播间");
                            } else
                                return new PlainText("本群没有关注此房间捏~");
                        }
                    }
                }
            case "/超话":
                if (args.length == 1) {
                    return getHelp(4);
                } else if (args.length == 2) {
                    if (args[1].equals("关注列表")) {
                        String out = "本群微博超话关注列表：\n";
                        if (!Shitboy.INSTANCE.getProperties().weibo_superTopic_subscribe.containsKey(group))
                            return new PlainText("暂无");

                        int count = 1;
                        for (String id : Shitboy.INSTANCE.getProperties().weibo_superTopic_subscribe.get(group)) {
                            String a = Shitboy.INSTANCE.getHandlerWeibo().getSuperTopicRes(id);
                            if (a == null)
                                out += count + ". 不存在的超话\n";
                            else {
                                a = a.substring(a.indexOf("onick']='") + "onick']='".length());
                                String name = a.substring(0, a.indexOf("';"));
                                out += count + ". " + name + "\n";
                            }
                            count++;
                        }
                        return new PlainText(out);
                    }
                    return getHelp(4);

                } else {
                    switch (args[1]) {
                        case "关注": {
                            if (!Shitboy.INSTANCE.getConfig().isAdmin(g, senderID))
                                return new PlainText("权限不足喵");

                            String a = Shitboy.INSTANCE.getHandlerWeibo().getSuperTopicRes(args[2]);
                            if (a == null)
                                return new PlainText("超话id不存在。");
                            else {
                                if (Shitboy.INSTANCE.getConfig().addWeiboSTopicSubscribe(args[2], group)) {
                                    a = a.substring(a.indexOf("onick']='") + "onick']='".length());
                                    String name = a.substring(0, a.indexOf("';"));
                                    return new PlainText("本群新增超话关注：" + name);
                                } else return new PlainText("本群已经关注过这个超话了");
                            }
                        }

                        case "取消关注": {
                            if (!Shitboy.INSTANCE.getConfig().isAdmin(g, senderID))
                                return new PlainText("权限不足喵");

                            if (!Shitboy.INSTANCE.getProperties().weibo_superTopic_subscribe.containsKey(group))
                                return new PlainText("本群暂无超话关注，先添加一个吧~");

                            if (Shitboy.INSTANCE.getConfig().rmWeiboSTopicSubscribe(args[2], group)) {
                                String a = Shitboy.INSTANCE.getHandlerWeibo().getSuperTopicRes(args[2]);
                                if (a == null)
                                    return new PlainText("本群取消关注超话：未知");
                                else {
                                    a = a.substring(a.indexOf("onick']='") + "onick']='".length());
                                    String name = a.substring(0, a.indexOf("';"));
                                    return new PlainText("本群取消关注超话：" + name);
                                }
                            } else
                                return new PlainText("本群没有关注此超话捏~");
                        }
                    }
                }
            case "/微博":
                if (args.length == 1) {
                    return getHelp(5);
                } else if (args.length == 2) {
                    if (args[1].equals("关注列表")) {
                        String out = "本群微博关注列表：\n";
                        if (!Shitboy.INSTANCE.getProperties().weibo_user_subscribe.containsKey(group))
                            return new PlainText("暂无");

                        int count = 1;
                        for (long id : Shitboy.INSTANCE.getProperties().weibo_user_subscribe.get(group)) {
                            String name = Shitboy.INSTANCE.getHandlerWeibo().getUserName(id);
                            out += count + ". " + name + "\n";
                            count++;
                        }
                        return new PlainText(out);
                    }
                    return getHelp(5);

                } else {
                    switch (args[1]) {
                        case "关注": {
                            if (!Shitboy.INSTANCE.getConfig().isAdmin(g, senderID))
                                return new PlainText("权限不足喵");

                            String name = Shitboy.INSTANCE.getHandlerWeibo().getUserName(Long.valueOf(args[2]));
                            if (name.equals("未知用户"))
                                return new PlainText("博主id不存在。");
                            else {
                                if (Shitboy.INSTANCE.getConfig().addWeiboUserSubscribe(Long.valueOf(args[2]), group))
                                    return new PlainText("本群新增微博关注：" + name);
                                else
                                    return new PlainText("本群已经关注过这个博主了");
                            }
                        }

                        case "取消关注": {
                            if (!Shitboy.INSTANCE.getConfig().isAdmin(g, senderID))
                                return new PlainText("权限不足喵");

                            if (!Shitboy.INSTANCE.getProperties().weibo_user_subscribe.containsKey(group))
                                return new PlainText("本群暂无微博关注，先添加一个吧~");

                            if (Shitboy.INSTANCE.getConfig().rmWeiboUserSubscribe(Long.valueOf(args[2]), group))
                                return new PlainText("本群取消关注超话：" +
                                        Shitboy.INSTANCE.getHandlerWeibo().getUserName(Long.valueOf(args[2])));
                            else
                                return new PlainText("本群没有关注此超话捏~");
                        }
                    }
                }
            case "/帮助":
            case "/help":
            case "/?":
                return getHelp(0);
        }

        return null;
    }

    private static final int maxID = 6;

    public Message getHelp(int id) {
        switch (id) {
            case 1:
                return new PlainText("【通用】\n"
                        + "(私聊) /欢迎 <群id> 欢迎词(填写“取消”关闭)\n");
            case 2:
                return new PlainText("【口袋48相关】\n"
                        + "/口袋 搜索 <在团小偶像或队伍名>\n"
                        + "/口袋 查询 <ID>\n"
                        + "/口袋 查询2 <Server_id>\n"
                        + "/口袋 关注 <房间ID>\n"
                        + "/口袋 取消关注 <房间ID>\n"
                        + "/口袋 关注列表\n"
                        + "/口袋 连接 <加密房间ID> <ServerId>\n"
                        + "注1：关注步骤：搜索名字，关注房间\n"
                        + "注2：不知道密码的加密房间如果知道Server_Id，通过连接功能连接以后照样可以关注并获取消息\n");
            case 3:
                return new PlainText("【B站直播相关】\n"
                        + "/bili 关注 <直播ID>\n"
                        + "/bili 取消关注 <直播ID>\n"
                        + "/bili 关注列表\n");
            case 4:
                return new PlainText("【微博超话相关】\n"
                        + "/超话 关注 <超话ID>\n"
                        + "/超话 取消关注 <超话ID>\n"
                        + "/超话 关注列表\n");
            case 5:
                return new PlainText("【微博相关】\n"
                        + "/微博 关注 <UID>\n"
                        + "/微博 取消关注 <UID>\n"
                        + "/微博 关注列表\n");
            case 6:
                return new PlainText("【微店相关】\n"
                        + "(私聊)/微店 <群id> cookie <Cookie>\n"
                        + "(私聊)/微店 <群id> 自动发货\n"
                        + "(私聊)/微店 <群id> 关闭\n"
                        + "PK功能未完成，敬请期待\n");
            default:
                Message a = getHelp(1);
                for (int i = 2; i <= maxID; i++)
                    a = a.plus(getHelp(i));
                return a;
        }
    }

    public Message executePrivate(String message, UserMessageEvent event) {
        String[] args = splitPrivateCommand(message);
        if (args == null)
            return null; //指令判断

        long groupId = Long.valueOf(args[1]);
        Message test = testPermission(groupId, event);
        if (test != null)
            return test; //权限检测

        switch (args[0]) {
            case "/微店": {
                if (args[2].startsWith("cookie")) {
                    if (args[2].contains(" ")) {
                        String cookie = args[2].substring(args[2].indexOf(" ") + 1);
                        Shitboy.INSTANCE.getConfig().setWeidianCookie(cookie, groupId);
                        WeidianCookie cookie1 = Shitboy.INSTANCE.getProperties().weidian_cookie.get(groupId);
                        return new PlainText("设置Cookie成功，当前自动发货为：" + (cookie1.autoDeliver ? "开启" : "关闭") + "。您可以通过/微店 自动发货 " + groupId + "切换");

                    }
                    return new PlainText("请输入Cookie");
                }

                String[] argsIn = args[2].split(" ");
                if (argsIn.length == 2) {
                    switch (argsIn[0]) {
                        case "自动发货": {
                            int a = Shitboy.INSTANCE.getConfig().switchWeidianAutoDeliver(groupId);
                            if (a == -1)
                                return new PlainText("该群未设置Cookie");
                            else
                                return new PlainText("自动发货设为：" + (a == 1 ? "开启" : "关闭"));
                        }
                        case "关闭": {
                            if (Shitboy.INSTANCE.getConfig().rmWeidianCookie(groupId))
                                return new PlainText("该群微店播报重置");
                            else
                                return new PlainText("该群微店播报设置为空");
                        }
                    }
                }
            }
            case "/欢迎": {
                if (!args[2].equals("取消")) {
                    Shitboy.INSTANCE.getConfig().setWelcome(args[2], groupId);
                    return new PlainText("设置成功");
                } else {
                    Shitboy.INSTANCE.getConfig().closeWelcome(groupId);
                }
            }
            default: {
                return getHelp(0);
            }
        }
    }

    private String[] splitPrivateCommand(String command) {
        if (command.contains(" ")) {
            String[] out = new String[3];
            out[0] = command.substring(0, command.indexOf(" "));
            command = command.substring(command.indexOf(" ") + 1);
            if (command.contains(" ")) {
                out[1] = command.substring(0, command.indexOf(" "));
                out[2] = command.substring(command.indexOf(" ") + 1);
                return out;
            }
        }
        return null;
    }

    //私聊权限检测
    public Message testPermission(long groupId, UserMessageEvent event) {
        Group group = event.getBot().getGroup(groupId);
        if (group == null) {
            return new PlainText("群号不存在或机器人不在群");
        }

        if (!Shitboy.INSTANCE.getConfig().isAdmin(group, event.getSender().getId())) {
            return new PlainText("权限不足喵");
        }
        return null;
    }

    /* 函数工具 */
    private boolean testRoomIDWithServerID(int room_id, int server_id) {
        for (int i : Shitboy.INSTANCE.getHandlerPocket48().getChannelIDBySeverID(server_id)) {
            if (i == room_id)
                return true;
        }
        return false;
    }

    private String informationFromPocketServerId(int server_id) throws Exception {
        String out = "Server_id: " + server_id + "\n";
        for (Integer i : Shitboy.INSTANCE.getHandlerPocket48().getChannelIDBySeverID(server_id)) {
            out += i != null ? "(" + i + ")" + Shitboy.INSTANCE.getHandlerPocket48().getRoomInfoByChannelID(i).getRoomName() + "\n" : "";
        }
        return out;
    }
}
