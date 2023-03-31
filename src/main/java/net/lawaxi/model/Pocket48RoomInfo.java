package net.lawaxi.model;

import cn.hutool.json.JSONObject;

public class Pocket48RoomInfo {
    private final String roomName;
    private final String ownerName;//储存question
    private final int severId;

    public Pocket48RoomInfo(JSONObject roomInfo) {
        this.roomName = roomInfo.getStr("channelName");
        this.ownerName = roomInfo.getStr("ownerName");
        this.severId = roomInfo.getInt("serverId");
    }

    public Pocket48RoomInfo(String question, int serverId) {
        this.severId = serverId;
        this.ownerName = question;
        this.roomName = "?加密房间?";
    }


    public String getRoomName() {
        return roomName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public int getSeverId() {
        return severId;
    }


}
