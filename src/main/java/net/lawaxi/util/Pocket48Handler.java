package net.lawaxi.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.Properties;
import net.lawaxi.model.Pocket48Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Pocket48Handler {

    public static final String ROOT = "https://pocketapi.48.cn";
    private static final String APILogin = ROOT + "/user/api/v1/login/app/mobile";
    private static final String APIStar2Server = ROOT + "/im/api/v1/im/server/jump";
    private static final String APIServer2Channel = ROOT + "/im/api/v1/team/last/message/get";
    private static final String APIChannel2Server = ROOT + "/im/api/v1/im/team/room/info";
    private static final String APIMsgOwner = ROOT + "/im/api/v1/team/message/list/homeowner";
    private static final String APIMsgAll = ROOT + "/im/api/v1/team/message/list/all";
    public static final String APIAnswerDetail = ROOT + "/idolanswer/api/idolanswer/v1/question_answer/detail";

    private String cronScheduleID;

    public void setCronScheduleID(String cronScheduleID) {
        this.cronScheduleID = cronScheduleID;
    }

    public String getCronScheduleID() {
        return cronScheduleID;
    }

    private Properties properties;
    private Pocket48HandlerHeader header;

    public Pocket48Handler(Properties properties){
        this.properties = properties;
        this.header = new Pocket48HandlerHeader(properties);
    }

    private void logInfo(String msg){
        properties.logger.info(msg);
    }

    private void logError(String msg){
        properties.logger.error(msg);
    }

    private String post(String url, String body){
        return header.setHeader(HttpRequest.post(url))
                .body(body).execute().body();
    }
    private String get(String url){
        return header.setHeader(HttpRequest.get(url))
                .execute().body();
    }

    //登陆前
    public boolean login(String account, String password) {
        if (isLogin()) {
            logError("已经登陆");
            return true;
        }

        String s = header.setLoginHeader(HttpRequest.post(APILogin))
                .body(String.format("{\"pwd\":\"%s\",\"mobile\":\"%s\"}", password, account)).execute().body();
        JSONObject object = JSONUtil.parseObj(s);
        if (object.getInt("status") == 200) {
            JSONObject content = JSONUtil.parseObj(object.getObj("content"));
            header.setToken(content.getStr("token"));
            logInfo("登陆成功");
            return true;
        } else {
            logError(object.getStr("message"));
            return false;
        }
    }

    public boolean isLogin(){
        return header.getToken() != null;
    }

    public void logout(){
        header.setToken(null);
    }


    //登陆后

    private JSONObject getJumpContent(int starID){
        String s = post(APIStar2Server, String.format("{\"starId\":%d,\"targetType\":1}", starID));
        JSONObject object = JSONUtil.parseObj(s);

        if (object.getInt("status") == 200) {
            return JSONUtil.parseObj(object.getObj("content"));

        } else {
            logError(object.getStr("message"));
        }
        return null;
    }

    public int getMainChannelIDByStarID(int starID){
        JSONObject content = getJumpContent(starID);
        if(content != null) {
            Integer id = content.getInt("channelId");
            if (id != null) {
                logInfo("获取成功");
                return id;
            }
            logError("没有房间");
        }
        return 0;
    }

    public int getServerIDByStarID(int starID){
        JSONObject content = getJumpContent(starID);
        if(content != null) {
            JSONObject serverInfo = JSONUtil.parseObj(content.getObj("jumpServerInfo"));
            if(serverInfo != null){
                logInfo("获取成功");
                return  serverInfo.getInt("serverId");
            }
            logError("没有服务器");

        }
        return 0;
    }
    public Integer[] getChannelIDBySeverID(int serverID){
        String s = post(APIServer2Channel, String.format("{\"serverId\":\"%d\"}", serverID));
        JSONObject object = JSONUtil.parseObj(s);

        if (object.getInt("status") == 200) {
            JSONObject content = JSONUtil.parseObj(object.getObj("content"));
            List<Integer> rs = new ArrayList<>();
            properties.logger.info(object.getObj("content").toString());

            for(Object room : content.getBeanList("lastMsgList", Object.class)){
                rs.add(JSONUtil.parseObj(room).getInt("channelId"));
            }
            return  rs.toArray(new Integer[0]);

        }else{
            logError(object.getStr("message"));
            return new Integer[0];
        }
    }

    public JSONObject getRoomInfoByChannelID(int roomID){
        String s = post(APIChannel2Server, String.format("{\"channelId\":\"%d\"}", roomID));
        JSONObject object = JSONUtil.parseObj(s);

        if (object.getInt("status") == 200) {
            JSONObject content =  JSONUtil.parseObj(object.getObj("content"));
            JSONObject roomInfo =  JSONUtil.parseObj(content.getObj("channelInfo"));
            properties.logger.info(content.getObj("channelInfo").toString());
            return roomInfo;

        } else {
            logError(object.getStr("message"));
        }
        return null;

    }

    public Pocket48Message[] getNewMessages(int roomID, HashMap<Integer,Long> endTime){
        JSONObject roomInfo = getRoomInfoByChannelID(roomID);

        if(roomInfo != null) {
            String roomName = roomInfo.getStr("channelName");
            String ownerName = roomInfo.getStr("ownerName");
            List<Object> msgs = getOMessages(roomID);
            if (msgs != null) {
                List<Pocket48Message> rs = new ArrayList<>();
                Long latest = null;
                for (Object message : msgs) {
                    JSONObject m = JSONUtil.parseObj(message);
                    if(latest == null){
                        latest = m.getLong("msgTime");
                        if(!endTime.containsKey(roomID))
                            break;
                    }

                    if(m.getLong("msgTime") <= endTime.get(roomID))
                        break;

                    rs.add(Pocket48Message.construct(
                            roomName,
                            ownerName,
                            m
                    ));
                }
                endTime.put(roomID,latest);
                return rs.toArray(new Pocket48Message[0]);
            }
        }
        return new Pocket48Message[0];
    }

    public Pocket48Message[] getMessages(int roomID){
        JSONObject roomInfo = getRoomInfoByChannelID(roomID);
        if(roomInfo != null) {
            String roomName = roomInfo.getStr("channelName");
            String ownerName = roomInfo.getStr("ownerName");
            List<Object> msgs = getOMessages(roomID);
            if (msgs != null) {
                List<Pocket48Message> rs = new ArrayList<>();
                for (Object message : msgs) {
                    rs.add(Pocket48Message.construct(
                            roomName,
                            ownerName,
                            JSONUtil.parseObj(message)
                    ));
                }
                return rs.toArray(new Pocket48Message[0]);
            }
        }

        return new Pocket48Message[0];
    }

    private List<Object> getOMessages(int roomID){
        JSONObject roomInfo = getRoomInfoByChannelID(roomID);
        if(roomInfo != null) {
            int serverID = roomInfo.getInt("serverId");
            String s = post(APIMsgOwner, String.format("{\"nextTime\":0,\"serverId\":%d,\"channelId\":%d,\"limit\":100}", serverID, roomID));
            JSONObject object = JSONUtil.parseObj(s);

            if (object.getInt("status") == 200) {
                JSONObject content = JSONUtil.parseObj(object.getObj("content"));
                return content.getBeanList("message", Object.class);

            } else {
                logError(object.getStr("message"));

            }
        }
        return null;
    }

    public String getAnswerNameTo(String answerID, String questionID){
        String s = post(APIAnswerDetail, String.format("{\"answerId\":\"%s\",\"questionId\":\"%s\"}", answerID,questionID));
        JSONObject object = JSONUtil.parseObj(s);

        if (object.getInt("status") == 200) {
            JSONObject content =  JSONUtil.parseObj(object.getObj("content"));
            return content.getStr("userName");

        } else {
            logError(object.getStr("message"));
        }
        return null;
    }
}
