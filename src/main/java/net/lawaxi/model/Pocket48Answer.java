package net.lawaxi.model;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

public class Pocket48Answer {
    private final String msgTo;
    private final String bodyFrom;
    private final String answerID;
    private final String questionID;
    private final Pocket48MessageType type;


    public Pocket48Answer(String msgTo, String bodyFrom, String answerID, String questionID, Pocket48MessageType type) {
        this.msgTo = msgTo;
        this.bodyFrom = bodyFrom;
        this.answerID = answerID;
        this.questionID = questionID;
        this.type = type;
    }
    public String getMsgTo() {
        return msgTo;
    }

    private String getBodyFrom() {
        return bodyFrom;
    }

    public String getAnswer() {
        return bodyFrom;
    }

    private final static String ROOT = "https://mp4.48.cn";
    public String getResInfo(){
        JSONObject object = JSONUtil.parseObj(getBodyFrom());
        return ROOT + object.getStr("url");
    }
}
