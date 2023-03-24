package net.lawaxi.model;

public class Pocket48Reply {
    private final String nameTo;
    private final String msgTo;
    private final String msgFrom;
    private final boolean isGift;//false为回复消息，true为回复礼物

    public Pocket48Reply(String nameTo, String msgTo, String msgFrom, boolean isGift) {
        this.nameTo = nameTo;
        this.msgTo = msgTo;
        this.msgFrom = msgFrom;
        this.isGift = isGift;
    }

    public String getMsgFrom() {
        return msgFrom;
    }

    public String getMsgTo() {
        return msgTo;
    }

    public String getNameTo() {
        return nameTo;
    }

    public boolean isGift() {
        return isGift;
    }
}
