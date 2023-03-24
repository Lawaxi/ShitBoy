package net.lawaxi.model;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

public class Pocket48Message {
    private final String room;
    private final String name;
    private final String nickName;
    private final Pocket48MessageType type;
    private final String body;

    public Pocket48Message(String room, String name, String nickName, String type, String body) {
        this.room = room;
        this.name = name;
        this.nickName = nickName;
        this.type = Pocket48MessageType.valueOf(type);
        this.body = body;
    }

    public static final Pocket48Message construct(String roomName, String ownerName, JSONObject m){
        JSONObject extInfo = JSONUtil.parseObj(m.getObj("extInfo"));
        JSONObject user = JSONUtil.parseObj(extInfo.getObj("user"));
        return new Pocket48Message(
                roomName,
                ownerName,
                user.getStr("nickName"),
                m.getStr("msgType"),
                m.getStr("bodys")
        );
    }

    public String getRoom() {
        return room;
    }

    public String getName() {
        return name;
    }

    public String getNickName() {
        return nickName;
    }

    public String getBody() {
        return body;
    }

    public Pocket48MessageType getType() {
        return type;
    }

    //IMAGE,AUDIO
    public String getResLoc(){
        if(getType() == Pocket48MessageType.EXPRESSIMAGE){
            return JSONUtil.parseObj(JSONUtil.parseObj(getBody()).getObj("expressImgInfo")).getStr("emotionRemote");
        }
        return JSONUtil.parseObj(getBody()).getStr("url");
    }

    public Pocket48Reply getReply(){
        boolean isGift = getType() == Pocket48MessageType.GIFTREPLY;
        if(!isGift && getType() != Pocket48MessageType.REPLY)//非回复消息
            return null;

        JSONObject object = JSONUtil.parseObj(getBody());
        JSONObject content = JSONUtil.parseObj(object.getObj(
                isGift ? "giftReplyInfo" : "replyInfo"));
        return new Pocket48Reply(
                content.getStr("replyName"),
                content.getStr("replyText"),
                content.getStr("text"),
                isGift
        );
    }

    public Pocket48LivePush getLivePush(){
        if(getType() != Pocket48MessageType.LIVEPUSH)
            return null;

        JSONObject object = JSONUtil.parseObj(getBody());
        JSONObject content = JSONUtil.parseObj(object.getObj(
                "livePushInfo"));
        return new Pocket48LivePush(
                content.getStr("liveCover"),
                content.getStr("liveTitle"),
                content.getStr("liveId")
        );
    }

    public Pocket48Answer getAnswer(){
        if(getType() != Pocket48MessageType.FLIPCARD
        && getType() != Pocket48MessageType.FLIPCARD_AUDIO
        && getType() != Pocket48MessageType.FLIPCARD_VIDEO)
            return null;

        JSONObject object = JSONUtil.parseObj(getBody());
        JSONObject content = JSONUtil.parseObj(object.getObj(
                "filpCardInfo"));
        return new Pocket48Answer(
                content.getStr("qustion"),
                content.getStr("answer"),
                content.getStr("answerID"),
                content.getStr("questionID"),
                getType()
        );
    }
}
