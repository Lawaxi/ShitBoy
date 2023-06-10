package net.lawaxi.model;

import net.mamoe.mirai.message.data.Message;

public class Pocket48SenderMessage {

    private final boolean canJoin;
    private final Message title;
    private final Message[] message;
    private boolean specific = false;//第一条消息可以合并


    public Pocket48SenderMessage(boolean canJoin, Message title, Message[] message) {
        this.canJoin = canJoin;
        this.title = title;
        this.message = message;
    }

    public boolean canJoin() {
        return canJoin;
    }

    public Message[] getMessage() {
        return message;
    }

    public Message getTitle() {
        return title;
    }

    public Message[] getUnjointMessage() {
        message[0] = title.plus(message[0]);
        return message;
    }

    public boolean isSpecific() {
        return specific;
    }

    public Pocket48SenderMessage setSpecific() {
        this.specific = true;
        return this;
    }
}
