package net.lawaxi.model;

import net.lawaxi.Shitboy;
import net.lawaxi.handler.Pocket48Handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static net.lawaxi.model.EndTime.newTime;

public class Pocket48SenderCache {

    public final Pocket48RoomInfo roomInfo;
    public Pocket48Message[] messages;
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

        if (!endTime.containsKey(roomID)) {
            endTime.put(roomID, newTime());
        }

        return new Pocket48SenderCache(roomInfo, pocket.getMessages(roomInfo, endTime),
                pocket.getRoomVoiceList(roomID, roomInfo.getSeverId()));
    }

    public void addMessage(Pocket48Message message) {
        List<Pocket48Message> messages1 = new ArrayList(Arrays.asList(this.messages));
        messages1.add(message);
        this.messages = messages1.toArray(new Pocket48Message[0]);
    }
}
