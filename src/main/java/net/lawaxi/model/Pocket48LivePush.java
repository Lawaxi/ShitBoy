package net.lawaxi.model;

public class Pocket48LivePush {
    private final String Cover;
    private final String Title;
    private final String ID;


    public Pocket48LivePush(String cover, String title, String id) {
        Cover = cover;
        Title = title;
        ID = id;
    }

    public String getCover() {
        return Cover;
    }

    public String getTitle() {
        return Title;
    }

    public String getID() {
        return ID;
    }
}
