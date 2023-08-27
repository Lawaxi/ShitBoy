package net.lawaxi.model;

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

    private final static String ROOT_SOURCE = "https://source.48.cn";

    public String getResInfo() {
        if (type == Pocket48MessageType.FLIPCARD_AUDIO || type == Pocket48MessageType.FLIPCARD_VIDEO)
            return ROOT + JSONUtil.parseObj(getBodyFrom()).getStr("url");
        return null;
    }

    public String getExt() {
        String rec = getResInfo();
        return rec.substring(rec.lastIndexOf(".") + 1);
    }

    public long getDuration() {
        if (type == Pocket48MessageType.FLIPCARD_AUDIO || type == Pocket48MessageType.FLIPCARD_VIDEO)
            JSONUtil.parseObj(getBodyFrom()).getLong("duration");
        return 0;
    }

    public String getPreviewImg() {
        if (type == Pocket48MessageType.FLIPCARD_VIDEO)
            return ROOT_SOURCE + JSONUtil.parseObj(getBodyFrom()).getStr("previewImg");
        return null;
    }

    public String getQuestionID() {
        return questionID;
    }

    public String getAnswerID() {
        return answerID;
    }
}
