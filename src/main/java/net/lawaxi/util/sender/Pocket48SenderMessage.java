package net.lawaxi.util.sender;

import net.mamoe.mirai.message.data.Message;

public class Pocket48SenderMessage {

    private final boolean canJoin;
    private final Message title;
    private final Message[] message;


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

}
