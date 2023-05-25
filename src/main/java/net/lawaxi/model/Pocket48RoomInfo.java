package net.lawaxi.model;

import cn.hutool.json.JSONObject;

public class Pocket48RoomInfo {
    private final String roomName;
    private final String ownerName;//储存question
    private final long severId;
    private final long roomId;

    public Pocket48RoomInfo(JSONObject roomInfo) {
        this.roomName = roomInfo.getStr("channelName");
        this.ownerName = roomInfo.getStr("ownerName");
        this.severId = roomInfo.getLong("serverId");
        this.roomId = roomInfo.getLong("channelId");
    }

    public Pocket48RoomInfo(String question, long serverId, long roomId) {
        this.severId = serverId;
        this.roomId = roomId;
        this.ownerName = question;
        this.roomName = "?加密房间?";
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
}
