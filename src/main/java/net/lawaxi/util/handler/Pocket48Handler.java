package net.lawaxi.util.handler;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.Properties;
import net.lawaxi.model.Pocket48Message;
import net.lawaxi.model.Pocket48RoomInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Pocket48Handler extends Handler {

    public static final String ROOT = "https://pocketapi.48.cn";
    public static final String SOURCEROOT = "https://source.48.cn/";
    private static final String APILogin = ROOT + "/user/api/v1/login/app/mobile";
    private static final String APIStar2Server = ROOT + "/im/api/v1/im/server/jump";
    private static final String APIServer2Channel = ROOT + "/im/api/v1/team/last/message/get";
    private static final String APIChannel2Server = ROOT + "/im/api/v1/im/team/room/info";
    private static final String APIMsgOwner = ROOT + "/im/api/v1/team/message/list/homeowner";
    private static final String APIMsgAll = ROOT + "/im/api/v1/team/message/list/all";
    public static final String APIAnswerDetail = ROOT + "/idolanswer/api/idolanswer/v1/question_answer/detail";
    private static final String APIUserInfo = ROOT + "/user/api/v1/user/info/home";
    private static final String APIRoomInfo = ROOT + "/im/api/v1/im/team/room/info";
    private static final String APILiveList = ROOT + "/live/api/v1/live/getLiveList";

    private final Pocket48HandlerHeader header;

    public Pocket48Handler(Properties properties) {
        super(properties);
        this.header = new Pocket48HandlerHeader(properties);
    }

    private void logInfo(String msg) {
        properties.logger.info(msg);
    }

    private void logError(String msg) {
        properties.logger.error(msg);
    }

    private String post(String url, String body) {
        return header.setHeader(HttpRequest.post(url))
                .body(body).execute().body();
    }

    private String get(String url) {
        return header.setHeader(HttpRequest.get(url))
                .execute().body();
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


    //登陆后

    private JSONObject getJumpContent(int starID) {
        String s = post(APIStar2Server, String.format("{\"starId\":%d,\"targetType\":1}", starID));
        JSONObject object = JSONUtil.parseObj(s);

        if (object.getInt("status") == 200) {
            return JSONUtil.parseObj(object.getObj("content"));

        } else {
            logError(object.getStr("message"));
        }
        return null;
    }

    public int getMainChannelIDByStarID(int starID) {
        JSONObject content = getJumpContent(starID);
        if (content != null) {
            Integer id = content.getInt("channelId");
            if (id != null) {
                logInfo("获取成功");
                return id;
            }
            logError("没有房间");
        }
        return 0;
    }

    public int getServerIDByStarID(int starID) {
        JSONObject content = getJumpContent(starID);
        if (content != null) {
            JSONObject serverInfo = JSONUtil.parseObj(content.getObj("jumpServerInfo"));
            if (serverInfo != null) {
                logInfo("获取成功");
                return serverInfo.getInt("serverId");
            }
            logError("没有服务器");

        }
        return 0;
    }

    public Integer[] getChannelIDBySeverID(int serverID) {
        String s = post(APIServer2Channel, String.format("{\"serverId\":\"%d\"}", serverID));
        JSONObject object = JSONUtil.parseObj(s);

        if (object.getInt("status") == 200) {
            JSONObject content = JSONUtil.parseObj(object.getObj("content"));
            List<Integer> rs = new ArrayList<>();
            properties.logger.info(object.getObj("content").toString());

            for (Object room : content.getBeanList("lastMsgList", Object.class)) {
                rs.add(JSONUtil.parseObj(room).getInt("channelId"));
            }
            return rs.toArray(new Integer[0]);

        } else {
            logError(object.getStr("message"));
            return new Integer[0];
        }
    }

    public Pocket48RoomInfo getRoomInfoByChannelID(int roomID) {
        String s = post(APIChannel2Server, String.format("{\"channelId\":\"%d\"}", roomID));
        JSONObject object = JSONUtil.parseObj(s);

        if (object.getInt("status") == 200) {
            JSONObject content = JSONUtil.parseObj(object.getObj("content"));
            JSONObject roomInfo = JSONUtil.parseObj(content.getObj("channelInfo"));
            return new Pocket48RoomInfo(roomInfo);

        }else if(object.getInt("status") == 2001
        && object.getStr("message").indexOf("question") != -1
        && properties.pocket48_serverID.containsKey(roomID)){ //只有配置中存有severID的加密房间会被解析
            JSONObject message = JSONUtil.parseObj(object.getObj("message"));
            return new Pocket48RoomInfo(message.getStr("question")+"？", properties.pocket48_serverID.get(roomID));
        }
        else {
            logError(object.getStr("message"));
        }
        return null;

    }

    public Pocket48Message[] getNewMessages(int roomID, HashMap<Integer, Long> endTime) {
        Pocket48RoomInfo roomInfo = getRoomInfoByChannelID(roomID);

        if (roomInfo != null) {
            String roomName = roomInfo.getRoomName();
            String ownerName = roomInfo.getOwnerName();
            List<Object> msgs = getOMessages(roomID,roomInfo.getSeverId());
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
        }
        return new Pocket48Message[0];
    }

    public Pocket48Message[] getMessages(int roomID) {
        Pocket48RoomInfo roomInfo = getRoomInfoByChannelID(roomID);
        if (roomInfo != null) {
            String roomName = roomInfo.getRoomName();
            String ownerName = roomInfo.getOwnerName();
            List<Object> msgs = getOMessages(roomID,roomInfo.getSeverId());
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
        }

        return new Pocket48Message[0];
    }

    private List<Object> getOMessages(int roomID, int serverID) {
        String s = post(APIMsgOwner, String.format("{\"nextTime\":0,\"serverId\":%d,\"channelId\":%d,\"limit\":100}", serverID, roomID));
        JSONObject object = JSONUtil.parseObj(s);

        if (object.getInt("status") == 200) {
            JSONObject content = JSONUtil.parseObj(object.getObj("content"));
            return content.getBeanList("message", Object.class);

        } else {
            logError(object.getStr("message"));

        }
        return null;
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

    private HashMap<Integer, String> name = new HashMap<>();

    public String getStarNameByStarID(int starID) {
        if (name.containsKey(starID))
            return name.get(starID);

        JSONObject info = getUserInfo(starID);
        if(info == null)
            return null;

        Object starName = info.getObj("starName");
        String starName_ = starName == null ? "" : (String) starName;
        name.put(starID, starName_);
        return starName_;
    }

    public JSONObject getUserInfo(int starID) {
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

    public JSONObject getRoomInfo(int channelID) {
        String s = post(APIRoomInfo, String.format("{\"channelId\":\"%d\"}", channelID));
        JSONObject object = JSONUtil.parseObj(s);

        if (object.getInt("status") == 200) {
            JSONObject content = JSONUtil.parseObj(object.getObj("content"));
            return JSONUtil.parseObj(content.getObj("channelInfo"));

        } else {
            logError(object.getStr("message"));
        }
        return null;

    }

    //获取一个JSONObejct表
    public List<Object> getLiveList(){
        String s = post(APILiveList, String.format("{\"groupId\":0,\"debug\":true,\"next\":0,\"record\":false}"));
        JSONObject object = JSONUtil.parseObj(s);

        if (object.getInt("status") == 200) {
            JSONObject content = JSONUtil.parseObj(object.getObj("content"));
            return content.getBeanList("liveList",Object.class);

        } else {
            logError(object.getStr("message"));
        }
        return null;

    }

    public List<Object> getRecordList(){
        String s = post(APILiveList, String.format("{\"groupId\":0,\"debug\":true,\"next\":0,\"record\":true}"));
        JSONObject object = JSONUtil.parseObj(s);

        if (object.getInt("status") == 200) {
            JSONObject content = JSONUtil.parseObj(object.getObj("content"));
            return content.getBeanList("liveList",Object.class);

        } else {
            logError(object.getStr("message"));
        }
        return null;

    }
}
