package net.lawaxi.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.Shitboy;
import net.lawaxi.model.Pocket48RoomInfo;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.IOException;

public class Listener extends SimpleListenerHost {

    @EventHandler()
    public ListeningStatus onGroupMessage(GroupMessageEvent event) {
        Member sender = event.getSender();
        Group group = event.getGroup();
        String message = event.getMessage().contentToString();


        if(message.startsWith("/")){
            Message m = commandSub(message.split(" "),group);
            if(m != null)
                group.sendMessage(new At(sender.getId()).plus(m));
        }

        return ListeningStatus.LISTENING;
    }

    private Message commandSub(String[] args, Group g){
        long group = Long.valueOf(g.getId());

        switch (args[0]){
            case "/口袋":
                if(args.length == 1){
                    return getHelp(1);
                }
                else if(args.length == 2) {
                    switch (args[1]) {
                        case "关注列表": {
                            String out = "本群口袋房间关注列表：\n";
                            if(!Shitboy.INSTANCE.getProperties().pocket48_subscribe.containsKey(group))
                                return new PlainText("暂无");

                            int count = 1;
                            for(int room_id : Shitboy.INSTANCE.getProperties().pocket48_subscribe.get(group).getRoomIDs()){
                                out+=count+". ("+room_id+")";
                                count++;

                                Pocket48RoomInfo roomInfo = Shitboy.INSTANCE.getHandlerPocket48().getRoomInfoByChannelID(room_id);
                                if(roomInfo !=null) {
                                    String roomName = roomInfo.getRoomName();
                                    String ownerName = roomInfo.getOwnerName();
                                    out += roomName + "(" + ownerName + ")\n";
                                }
                                else
                                    out+="未知房间"+room_id+"\n";
                            }
                            return new PlainText(out);
                        }
                        case "查直播":{
                            String out = "";
                            int count = 1;
                            for(Object liveRoom : Shitboy.INSTANCE.getHandlerPocket48().getLiveList()){
                                JSONObject liveRoom1 = JSONUtil.parseObj(liveRoom);
                                JSONObject userInfo =  liveRoom1.getJSONObject("userInfo");

                                String title = liveRoom1.getStr("title");
                                String name = userInfo.getStr("starName");
                                String userId = userInfo.getStr("userId");
                                out+=count+". ("+userId+")"+name+": "+title+"\n";
                                count++;
                            }
                            return new PlainText(out);
                        }
                        case "查录播":{
                            String out = "";
                            int count = 1;
                            for(Object liveRoom : Shitboy.INSTANCE.getHandlerPocket48().getRecordList()){
                                JSONObject liveRoom1 = JSONUtil.parseObj(liveRoom);
                                JSONObject userInfo =  liveRoom1.getJSONObject("userInfo");

                                String title = liveRoom1.getStr("title");
                                String name = userInfo.getStr("starName");
                                String userId = userInfo.getStr("userId");
                                out+=count+". ("+userId+")"+name+": "+title+"\n";
                                count++;
                            }
                            return new PlainText(out);

                        }
                        default:
                            return getHelp(1);
                    }

                }
                else if(args.length == 3) {
                    switch (args[1]) {
                        case "查询": {
                            int star_ID = Integer.valueOf(args[2]);
                            JSONObject info = Shitboy.INSTANCE.getHandlerPocket48().getUserInfo(star_ID);
                            if(info==null)
                                return new PlainText("用户不存在");

                            boolean star = info.getBool("isStar");
                            int followers = info.getInt("followers");
                            int friends = info.getInt("friends");
                            String nickName = info.getStr("nickname");
                            String starName = info.getStr("starName");
                            String avatar = Shitboy.INSTANCE.getHandlerPocket48().SOURCEROOT + info.getStr("avatar");
                            String out = (star ? "【成员】" : "【聚聚】") + nickName + (starName != null ? "(" + starName + ")" : "") + "\n"
                                    + "关注 " + friends + " 粉丝 " + followers + "\n";

                            try {
                                int server_id = Shitboy.INSTANCE.getHandlerPocket48().getServerIDByStarID(star_ID);
                                if (server_id != 0) {
                                    out += "Server_id: " + server_id + "\n";
                                    for (int i : Shitboy.INSTANCE.getHandlerPocket48().getChannelIDBySeverID(server_id)) {
                                        out += "(" + i + ")" + Shitboy.INSTANCE.getHandlerPocket48().getRoomInfoByChannelID(i)
                                                .getRoomName() + "\n";
                                    }
                                }
                            }catch (Exception e){

                            }

                            try {
                                return new PlainText(out).plus(
                                        g.uploadImage(ExternalResource.create(HttpRequest.get(avatar).execute().bodyStream())));
                            } catch (IOException e) {
                                return new PlainText(out);
                            }
                        }
                        case "关注": {
                            Pocket48RoomInfo roomInfo = Shitboy.INSTANCE.getHandlerPocket48().getRoomInfoByChannelID(Integer.valueOf(args[2]));
                            if (roomInfo == null) {
                                return new PlainText("房间ID不存在。查询房间ID请输入/口袋 查询 <成员ID>");
                            }

                            Shitboy.INSTANCE.getConfig().addPocket48RoomSubscribe(Integer.valueOf(args[2]), group);
                            String roomName = roomInfo.getRoomName();
                            String ownerName = roomInfo.getOwnerName();
                            return new PlainText("本群新增关注：" + roomName + "(" + ownerName + ")");
                        }

                        case "取消关注": {
                            if(!Shitboy.INSTANCE.getProperties().pocket48_subscribe.containsKey(group))
                                return new PlainText("本群暂无房间关注，先添加一个吧~");

                            if (Shitboy.INSTANCE.getProperties().pocket48_subscribe.get(group).getRoomIDs().contains(Integer.valueOf(args[2]))) {
                                Pocket48RoomInfo roomInfo = Shitboy.INSTANCE.getHandlerPocket48().getRoomInfoByChannelID(Integer.valueOf(args[2]));
                                Shitboy.INSTANCE.getConfig().rmPocket48RoomSubscribe(Integer.valueOf(args[2]), group);
                                if (roomInfo != null) {
                                    String roomName = roomInfo.getRoomName();
                                    String ownerName = roomInfo.getOwnerName();
                                    return new PlainText("本群取消关注：" + roomName + "(" + ownerName + ")");
                                } else return new PlainText("本群取消关注：未知房间");
                            }
                            return new PlainText("本群没有关注此房间捏~");

                        }
                    }
                }
                else if(args.length == 4) {
                    switch (args[1]) {
                        case "连接": {
                            int room_id = Integer.valueOf(args[2]);
                            int server_id = Integer.valueOf(args[3]);
                            if(testRoomIDWithServerID(room_id,server_id)){
                                Shitboy.INSTANCE.getConfig().addRoomIDConnection(room_id,server_id);
                                return new PlainText("连接成功");
                            }else
                                return new PlainText("您输入的ServerId并不包含此RoomId");

                        }
                    }
                }
            case "/bili":
                if(args.length == 1){
                    return getHelp(2);
                }
                else if(args.length == 2) {
                    switch (args[1]) {
                        case "关注列表": {
                            String out = "本群Bilibili直播间关注列表：\n";
                            if(!Shitboy.INSTANCE.getProperties().bilibili_subscribe.containsKey(group))
                                return new PlainText("暂无");

                            int count = 1;
                            for(int room_id : Shitboy.INSTANCE.getProperties().bilibili_subscribe.get(group)){
                                out+=count+". ("+room_id+")";
                                count++;

                                JSONObject data = Shitboy.INSTANCE.getHandlerBilibili().getLiveData(room_id);
                                if (data.getInt("code") == 0) {
                                    JSONObject info = JSONUtil.parseObj(data.getObj("data"));
                                    String name = Shitboy.INSTANCE.getHandlerBilibili().getNameByMid(info.getInt("uid"));
                                    out+=name+"\n";
                                }
                            }
                            return new PlainText(out);
                        }
                        default:
                            return getHelp(2);
                    }

                }
                else {
                    switch (args[1]) {
                        case "关注": {
                            JSONObject data = Shitboy.INSTANCE.getHandlerBilibili().getLiveData(Integer.valueOf(args[2]));
                            if (data.getInt("code") == 1) {
                                return new PlainText("直播ID不存在。提示：直播ID是直播间链接最后的数字，不是B站用户uid");
                            }

                            Shitboy.INSTANCE.getConfig().addBilibiliLiveSubscribe(Integer.valueOf(args[2]), group);

                            JSONObject info = JSONUtil.parseObj(data.getObj("data"));
                            String name = Shitboy.INSTANCE.getHandlerBilibili().getNameByMid(info.getInt("uid"));
                            return new PlainText("本群新增关注：" + name+"的直播间");
                        }

                        case "取消关注": {
                            if(!Shitboy.INSTANCE.getProperties().bilibili_subscribe.containsKey(group))
                                return new PlainText("本群暂无Bilibili直播间关注，先添加一个吧~");

                            if(Shitboy.INSTANCE.getProperties().bilibili_subscribe.get(group).contains(Integer.valueOf(args[2]))) {
                                JSONObject data = Shitboy.INSTANCE.getHandlerBilibili().getLiveData(Integer.valueOf(args[2]));
                                Shitboy.INSTANCE.getConfig().rmBilibiliLiveSubscribe(Integer.valueOf(args[2]), group);
                                if (data.getInt("code") == 0) {
                                    JSONObject info = JSONUtil.parseObj(data.getObj("data"));
                                    String name = Shitboy.INSTANCE.getHandlerBilibili().getNameByMid(info.getInt("uid"));
                                    return new PlainText("本群取消关注：" + name+"的直播间");
                                }else return new PlainText("本群取消关注：未知直播间");
                            }
                            return new PlainText("本群没有关注此房间捏~");
                        }
                    }
                }
            case "/帮助":
            case "/help":
                return getHelp(0);
        }

        return null;
    }
    private static final int maxID = 2;
    private Message getHelp(int id){
        switch (id){
            case 1:
                return new PlainText( "【口袋48相关】\n"
                        + "/口袋 查询 <成员ID>\n"
                        + "/口袋 查直播\n"
                        + "/口袋 查录播\n"
                        + "/口袋 关注 <房间ID>\n"
                        + "/口袋 取消关注 <房间ID>\n"
                        + "/口袋 关注列表\n"
                        + "/口袋 连接 <加密房间ID> <ServerId>\n"
                        + "注1：不知道房间ID可以在直播的时候先通过查直播获得成员ID，再通过查询获得房间ID\n"
                        + "注2：不知道密码的加密房间如果知道serverId，通过连接功能连接以后照样可以关注并获取消息\n");
            case 2:
                return new PlainText( "【B站直播相关】\n"
                        + "/bili 关注 <直播ID>\n"
                        + "/bili 取消关注 <直播ID>\n"
                        + "/bili 关注列表\n");
            default:
                Message a = getHelp(1);
                for(int i=2;i<=maxID;i++)
                    a = a.plus(getHelp(2));
                return a;
        }
    }

    @EventHandler()
    public ListeningStatus onFriendMessage(FriendMessageEvent event) {
        return ListeningStatus.LISTENING;
    }

    private boolean testRoomIDWithServerID(int room_id, int server_id){
        for (int i : Shitboy.INSTANCE.getHandlerPocket48().getChannelIDBySeverID(server_id)) {
            if(i == room_id)
                return true;
        }
        return false;
    }
}
