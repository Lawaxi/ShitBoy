package net.lawaxi.model;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.Shitboy;
import net.lawaxi.util.handler.Pocket48Handler;

public class Pocket48Message {
    private final String room;
    private final String ownerName;
    private final String nickName;
    private final String starName;
    private final Pocket48MessageType type;
    private final String body;
    private final long time;

    public Pocket48Message(String room, String ownerName, String nickName, String starName, String type, String body, long time) {
        this.room = room;
        this.ownerName = ownerName;
        this.nickName = nickName;
        this.starName = starName;
        this.type = Pocket48MessageType.valueOf(type);
        this.body = body;
        this.time = time;
    }

    public static final Pocket48Message construct(String roomName, String ownerName, JSONObject m, long time) {
        JSONObject extInfo = JSONUtil.parseObj(m.getObj("extInfo"));
        JSONObject user = JSONUtil.parseObj(extInfo.getObj("user"));
        return new Pocket48Message(
                roomName,
                ownerName,
                user.getStr("nickName"),
                Shitboy.INSTANCE.getHandlerPocket48().getStarNameByStarID(user.getInt("userId")),
                m.getStr("msgType"),
                m.getStr("bodys"),
                time);
    }

    public String getRoom() {
        return room;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getNickName() {
        return nickName;
    }

    public String getStarName() {
        return starName;
    }

    public String getBody() {
        return body;
    }

    public Pocket48MessageType getType() {
        return type;
    }

    public long getTime() {
        return time;
    }

    //IMAGE,AUDIO
    public String getResLoc() {
        if (getType() == Pocket48MessageType.EXPRESSIMAGE) {
            return JSONUtil.parseObj(JSONUtil.parseObj(getBody()).getObj("expressImgInfo")).getStr("emotionRemote");
        }
        return JSONUtil.parseObj(getBody()).getStr("url");
    }

    public Pocket48Reply getReply() {
        boolean isGift = getType() == Pocket48MessageType.GIFTREPLY;
        if (!isGift && getType() != Pocket48MessageType.REPLY)//非回复消息
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

    public Pocket48LivePush getLivePush() {
        if (getType() != Pocket48MessageType.LIVEPUSH)
            return null;

        JSONObject object = JSONUtil.parseObj(getBody());
        JSONObject content = JSONUtil.parseObj(object.getObj(
                "livePushInfo"));
        return new Pocket48LivePush(
                Pocket48Handler.SOURCEROOT + content.getStr("liveCover").substring(1),
                content.getStr("liveTitle"),
                content.getStr("liveId")
        );
    }

    public Pocket48Answer getAnswer() {
        if (getType() != Pocket48MessageType.FLIPCARD
                && getType() != Pocket48MessageType.FLIPCARD_AUDIO
                && getType() != Pocket48MessageType.FLIPCARD_VIDEO)
            return null;

        JSONObject object = JSONUtil.parseObj(getBody());
        JSONObject content = JSONUtil.parseObj(object.getObj(
                "filpCardInfo"));
        return new Pocket48Answer(
                content.getStr("question"),
                content.getStr("answer"),
                content.getStr("answerId"),
                content.getStr("questionId"),
                getType()
        );
    }
}
