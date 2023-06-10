package net.lawaxi.model;

import net.lawaxi.Shitboy;
import net.lawaxi.handler.Pocket48Handler;

import java.util.HashMap;
import java.util.List;

public class Pocket48SenderCache {

    public final Pocket48RoomInfo roomInfo;
    public final Pocket48Message[] messages;
    public final List<Long> voiceList;

    public Pocket48SenderCache(Pocket48RoomInfo roomInfo, Pocket48Message[] messages, List<Long> voiceList) {
        this.roomInfo = roomInfo;
        this.messages = messages;
        this.voiceList = voiceList;
    }

    public static Pocket48SenderCache create(long roomID, HashMap<Long, Long> endTime) {
        Pocket48Handler pocket = Shitboy.INSTANCE.getHandlerPocket48();

        Pocket48RoomInfo roomInfo = pocket.getRoomInfoByChannelID(roomID);
        if (roomInfo == null)
            return null;

        return new Pocket48SenderCache(roomInfo, pocket.getMessages(roomInfo, endTime),
                pocket.getRoomVoiceList(roomID, roomInfo.getSeverId()));
    }
}
