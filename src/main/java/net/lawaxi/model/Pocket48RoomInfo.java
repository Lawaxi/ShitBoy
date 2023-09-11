package net.lawaxi.model;

import cn.hutool.json.JSONObject;
import net.lawaxi.Shitboy;

public class Pocket48RoomInfo {
    private final static String ROOT_SOURCE = "https://source.48.cn";
    private final String roomName;
    private final String ownerName;//储存question
    private final long severId;
    private final long roomId;
    private long starId = 0;
    private String bgImg;

    public Pocket48RoomInfo(JSONObject roomInfo) {
        this.roomName = roomInfo.getStr("channelName");
        this.ownerName = roomInfo.getStr("ownerName");
        this.severId = roomInfo.getLong("serverId");
        this.roomId = roomInfo.getLong("channelId");
    }

    private static JSONObject getLockedRoomInfo(String question, long serverId, long roomId) {
        JSONObject roomInfo = new JSONObject();
        roomInfo.set("channelName", "?加密房间?");
        roomInfo.set("ownerName", question);
        roomInfo.set("serverId", serverId);
        roomInfo.set("channelId", roomId);
        return roomInfo;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public long getSeverId() {
        return severId;
    }

    public long getRoomId() {
        return roomId;
    }

    public long getChannelId() {
        return roomId;
    }

    public long getStarId() {
        return starId;
    }

    public Pocket48RoomInfo setStarId(long starId) {
        this.starId = starId;
        return this;
    }

    public String getBgImg() {
        if (this.starId != 0 && this.bgImg == null) {
            this.bgImg = Shitboy.INSTANCE.getHandlerPocket48().getUserInfo(starId).getStr("bgImg");
        }
        return ROOT_SOURCE + this.bgImg;
    }

    public static class LockedRoomInfo extends Pocket48RoomInfo {
        public LockedRoomInfo(String question, Long serverId, long roomId) {
            super(Pocket48RoomInfo.getLockedRoomInfo(question, serverId == null ? 0 : serverId, roomId));
        }
    }
}
