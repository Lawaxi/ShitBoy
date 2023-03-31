package net.lawaxi.model;

public class Pocket48RoomInfo {
    private final String roomName;
    private final String ownerName;//储存question

    public Pocket48RoomInfo(String roomName, String ownerName) {
        this.roomName = roomName;
        this.ownerName = ownerName;
    }

    public Pocket48RoomInfo(String question) {
        this.roomName = "?加密房间?";
        this.ownerName = question;
    }


    public String getRoomName() {
        return roomName;
    }

    public String getOwnerName() {
        return ownerName;
    }
}
