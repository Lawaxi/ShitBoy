package net.lawaxi.handler;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.model.Pocket48Message;
import net.lawaxi.model.Pocket48RoomInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Pocket48Handler extends Handler {

    public static final String ROOT = "https://pocketapi.48.cn";
    public static final String SOURCEROOT = "https://source.48.cn/";
    private static final String APILogin = ROOT + "/user/api/v1/login/app/mobile";
    private static final String APIBalance = ROOT + "/user/api/v1/user/money";
    private static final String APIStar2Server = ROOT + "/im/api/v1/im/server/jump";
    private static final String APIServer2Channel = ROOT + "/im/api/v1/team/last/message/get";
    private static final String APIChannel2Server = ROOT + "/im/api/v1/im/team/room/info";
    private static final String APISearch = ROOT + "/im/api/v1/im/server/search";
    private static final String APIMsgOwner = ROOT + "/im/api/v1/team/message/list/homeowner";
    private static final String APIMsgAll = ROOT + "/im/api/v1/team/message/list/all";
    public static final String APIAnswerDetail = ROOT + "/idolanswer/api/idolanswer/v1/question_answer/detail";
    private static final String APIUserInfo = ROOT + "/user/api/v1/user/info/home";
    private static final String APILiveList = ROOT + "/live/api/v1/live/getLiveList";
    private static final String APIRoomVoice = ROOT + "/im/api/v1/team/voice/operate";

    private final Pocket48HandlerHeader header;

    public Pocket48Handler() {
        super();
        this.header = new Pocket48HandlerHeader(properties);
    }

    //登陆前
    public boolean login(String account, String password) {
        if (isLogin()) {
            logError("口袋48已经登陆");
            return true;
        }

        String s = header.setLoginHeader(HttpRequest.post(APILogin))
                .body(String.format("{\"pwd\":\"%s\",\"mobile\":\"%s\"}", password, account)).execute().body();
        JSONObject object = JSONUtil.parseObj(s);
        if (object.getInt("status") == 200) {
            JSONObject content = JSONUtil.parseObj(object.getObj("content"));
            header.setToken(content.getStr("token"));
            logInfo("口袋48登陆成功");
            return true;
        } else {
            logInfo("口袋48登陆失败");
            logError(object.getStr("message"));
            return false;
        }
    }

    public boolean isLogin() {
        return header.getToken() != null;
    }

    public void logout() {
        header.setToken(null);
    }

    @Override
    protected HttpRequest setHeader(HttpRequest request) {
        return header.setHeader(request);
    }

    /* ----------------------文字获取类---------------------- */

    public int getBalance() {
        String s = post(APIBalance, String.format("{\"token\":\"%s\"}", header.getToken()));
        JSONObject object = JSONUtil.parseObj(s);

        if (object.getInt("status") == 200) {
            JSONObject content = JSONUtil.parseObj(object.getObj("content"));
            return content.getInt("moneyTotal", 0);

        } else {
            logError(object.getStr("message"));
        }
        return 0;
    }

    private final HashMap<Long, String> name = new HashMap<>();

    public String getStarNameByStarID(long starID) {
        if (name.containsKey(starID))
            return name.get(starID);

        JSONObject info = getUserInfo(starID);
        if (info == null)
            return null;

        Object starName = info.getObj("starName");
        String starName_ = starName == null ? "" : (String) starName;
        name.put(starID, starName_);
        return starName_;
    }

    public JSONObject getUserInfo(long starID) {
        String s = post(APIUserInfo, String.format("{\"userId\":%d}", starID));
        JSONObject object = JSONUtil.parseObj(s);

        if (object.getInt("status") == 200) {
            JSONObject content = JSONUtil.parseObj(object.getObj("content"));
            return JSONUtil.parseObj(content.getObj("baseUserInfo"));

        } else {
            logError(object.getStr("message"));
        }
        return null;

    }

    private JSONObject getJumpContent(long starID) {
        String s = post(APIStar2Server, String.format("{\"starId\":%d,\"targetType\":1}", starID));
        JSONObject object = JSONUtil.parseObj(s);

        if (object.getInt("status") == 200) {
            return JSONUtil.parseObj(object.getObj("content"));

        } else {
            logError(object.getStr("message"));
        }
        return null;
    }

    public long getMainChannelIDByStarID(long starID) {
        JSONObject content = getJumpContent(starID);
        if (content != null) {
            Long id = content.getLong("channelId");
            if (id != null) {
                logInfo("获取成功");
                return id;
            }
            logError("没有房间");
        }
        return 0;
    }

    public Long getServerIDByStarID(long starID) {
        JSONObject content = getJumpContent(starID);
        if (content != null) {
            JSONObject serverInfo = JSONUtil.parseObj(content.getObj("jumpServerInfo"));
            if (serverInfo != null) {
                return serverInfo.getLong("serverId");
            }
        }
        return null;
    }

    public Long[] getChannelIDBySeverID(long serverID) {
        String s = post(APIServer2Channel, String.format("{\"serverId\":\"%d\"}", serverID));
        JSONObject object = JSONUtil.parseObj(s);

        if (object.getInt("status") == 200) {
            JSONObject content = JSONUtil.parseObj(object.getObj("content"));
            List<Long> rs = new ArrayList<>();
            for (Object room : content.getBeanList("lastMsgList", Object.class)) {
                rs.add(JSONUtil.parseObj(room).getLong("channelId"));
            }
            return rs.toArray(new Long[0]);

        } else {
            logError(object.getStr("message"));
            return new Long[0];
        }
    }

    public Pocket48RoomInfo getRoomInfoByChannelID(long roomID) {
        String s = post(APIChannel2Server, String.format("{\"channelId\":\"%d\"}", roomID));
        JSONObject object = JSONUtil.parseObj(s);

        if (object.getInt("status") == 200) {
            JSONObject content = JSONUtil.parseObj(object.getObj("content"));
            JSONObject roomInfo = JSONUtil.parseObj(content.getObj("channelInfo"));
            return new Pocket48RoomInfo(roomInfo);

        } else if (object.getInt("status") == 2001
                && object.getStr("message").indexOf("question") != -1
                && properties.pocket48_serverID.containsKey(roomID)) { //只有配置中存有severID的加密房间会被解析
            JSONObject message = JSONUtil.parseObj(object.getObj("message"));
            return new Pocket48RoomInfo(message.getStr("question") + "？",
                    properties.pocket48_serverID.get(roomID), roomID);
        } else {
            logError(object.getStr("message"));
        }
        return null;

    }

    public Object[] search(String content_) {
        String s = post(APISearch, String.format("{\"searchContent\":\"%s\"}", content_));
        JSONObject object = JSONUtil.parseObj(s);

        if (object.getInt("status") == 200) {
            JSONObject content = JSONUtil.parseObj(object.getObj("content"));
            if (content.containsKey("serverApiList")) {
                JSONArray a = content.getJSONArray("serverApiList");
                return a.stream().toArray();
            }

        } else {
            logError(object.getStr("message"));
        }
        return new Object[0];
    }

    public String getAnswerNameTo(String answerID, String questionID) {
        String s = post(APIAnswerDetail, String.format("{\"answerId\":\"%s\",\"questionId\":\"%s\"}", answerID, questionID));
        JSONObject object = JSONUtil.parseObj(s);

        if (object.getInt("status") == 200) {
            JSONObject content = JSONUtil.parseObj(object.getObj("content"));
            return content.getStr("userName");

        } else {
            logError(object.getStr("message"));
        }
        return null;
    }

    /* ----------------------房间类---------------------- */
    //获取最新消息并整理成Pocket48Message[]
    public Pocket48Message[] getMessages(Pocket48RoomInfo roomInfo, HashMap<Long, Long> endTime) {
        long roomID = roomInfo.getRoomId();
        String roomName = roomInfo.getRoomName();
        String ownerName = getOwnerOrTeamName(roomInfo);
        List<Object> msgs = getOriMessages(roomID, roomInfo.getSeverId());
        if (msgs != null) {
            List<Pocket48Message> rs = new ArrayList<>();
            Long latest = null;
            for (Object message : msgs) {
                JSONObject m = JSONUtil.parseObj(message);
                long time = m.getLong("msgTime");
                if (latest == null) {
                    latest = time;
                    if (!endTime.containsKey(roomID))
                        break;
                }

                if (m.getLong("msgTime") <= endTime.get(roomID))
                    break;

                rs.add(Pocket48Message.construct(
                        roomName,
                        ownerName,
                        m,
                        time
                ));
            }
            endTime.put(roomID, latest);
            return rs.toArray(new Pocket48Message[0]);
        }
        return new Pocket48Message[0];
    }

    public Pocket48Message[] getMessages(long roomID, HashMap<Long, Long> endTime) {
        Pocket48RoomInfo roomInfo = getRoomInfoByChannelID(roomID);
        if (roomInfo != null) {
            return getMessages(roomInfo, endTime);
        }
        return new Pocket48Message[0];
    }

    //获取全部消息并整理成Pocket48Message[]
    public Pocket48Message[] getMessages(Pocket48RoomInfo roomInfo) {
        long roomID = roomInfo.getRoomId();
        String roomName = roomInfo.getRoomName();
        String ownerName = getOwnerOrTeamName(roomInfo);
        List<Object> msgs = getOriMessages(roomID, roomInfo.getSeverId());
        if (msgs != null) {
            List<Pocket48Message> rs = new ArrayList<>();
            for (Object message : msgs) {
                rs.add(Pocket48Message.construct(
                        roomName,
                        ownerName,
                        JSONUtil.parseObj(message),
                        JSONUtil.parseObj(message).getLong("msgTime")
                ));
            }
            return rs.toArray(new Pocket48Message[0]);
        }

        return new Pocket48Message[0];
    }

    public Pocket48Message[] getMessages(long roomID) {
        Pocket48RoomInfo roomInfo = getRoomInfoByChannelID(roomID);
        if (roomInfo != null) {
            return getMessages(roomInfo);
        }

        return new Pocket48Message[0];
    }


    //获取未整理的消息
    private List<Object> getOriMessages(long roomID, long serverID) {
        String s = post(APIMsgOwner, String.format("{\"nextTime\":0,\"serverId\":%d,\"channelId\":%d,\"limit\":100}", serverID, roomID));
        JSONObject object = JSONUtil.parseObj(s);

        if (object.getInt("status") == 200) {
            JSONObject content = JSONUtil.parseObj(object.getObj("content"));
            List<Object> out = content.getBeanList("message", Object.class);
            out.sort((a, b) -> JSONUtil.parseObj(b).getLong("msgTime") - JSONUtil.parseObj(a).getLong("msgTime") > 0 ? 1 : 0);//口袋消息好像会乱（？）
            return out;

        } else {
            logError(object.getStr("message"));

        }
        return null;
    }

    public String getOwnerOrTeamName(Pocket48RoomInfo roomInfo) {
        switch (String.valueOf(roomInfo.getSeverId())) {
            case "1148749":
                return "TEAM Z";
            case "1164313":
                return "TEAM X";
            case "1164314":
                return "TEAM NIII";
            case "1181051":
                return "TEAM HII";
            case "1181256":
                return "TEAM G";
            case "1213978":
                return "TEAM NII";
            case "1214171":
                return "TEAM SII";
            case "1115226":
                return "CKG48";
            case "1115413":
                return "BEJ48";
            default:
                return roomInfo.getOwnerName();
        }
    }

    public static final List<Long> voidRoomVoiceList = new ArrayList<>();

    public List<Long> getRoomVoiceList(long roomID, long serverID) {
        String s = post(APIRoomVoice, String.format("{\"channelId\":%d,\"serverId\":%d,\"operateCode\":2}", roomID, serverID));
        JSONObject object = JSONUtil.parseObj(s);
        if (object.getInt("status") == 200) {
            JSONObject content = JSONUtil.parseObj(object.getObj("content"));
            JSONArray a = content.getJSONArray("voiceUserList");
            List<Long> l = new ArrayList<>();
            if (a.size() > 0) {
                for (Object star_ : a.stream().toArray()) {
                    JSONObject star = JSONUtil.parseObj(star_);
                    long starID = star.getLong("userId");
                    l.add(starID);

                    //优化：names的另一种添加途径
                    if (!name.containsKey(starID)) {
                        name.put(starID, star.getStr("nickname"));
                    }
                }
                return l;
            }

        } else {
            logError(object.getStr("message"));
        }
        return voidRoomVoiceList;
    }

    /* ----------------------直播类---------------------- */
    public List<Object> getLiveList() {
        String s = post(APILiveList, "{\"groupId\":0,\"debug\":true,\"next\":0,\"record\":false}");
        JSONObject object = JSONUtil.parseObj(s);

        if (object.getInt("status") == 200) {
            JSONObject content = JSONUtil.parseObj(object.getObj("content"));
            return content.getBeanList("liveList", Object.class);

        } else {
            logError(object.getStr("message"));
        }
        return null;

    }

    public List<Object> getRecordList() {
        String s = post(APILiveList, "{\"groupId\":0,\"debug\":true,\"next\":0,\"record\":true}");
        JSONObject object = JSONUtil.parseObj(s);

        if (object.getInt("status") == 200) {
            JSONObject content = JSONUtil.parseObj(object.getObj("content"));
            return content.getBeanList("liveList", Object.class);

        } else {
            logError(object.getStr("message"));
        }
        return null;

    }

}
