package net.lawaxi.model;

import net.mamoe.mirai.message.data.Message;

public class MessageWithTime {

    private final Message message;
    private final long time;


    public MessageWithTime(Message message, long time) {
        this.message = message;
        this.time = time;
    }

    public Message getMessage() {
        return message;
    }

    public long getTime() {
        return time;
    }
}
