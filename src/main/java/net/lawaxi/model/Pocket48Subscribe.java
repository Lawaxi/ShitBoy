package net.lawaxi.model;

import java.util.List;

public class Pocket48Subscribe {
    private final List<Long> roomIDs;
    private final List<Long> starIDs;
    private final boolean showAtOne;

    public Pocket48Subscribe(boolean showAtOne, List<Long> roomIDs, List<Long> starIDs) {
        this.roomIDs = roomIDs;
        this.starIDs = starIDs;
        this.showAtOne = showAtOne;
    }

    public List<Long> getRoomIDs() {
        return roomIDs;
    }

    public List<Long> getStarIDs() {
        return starIDs;
    }

    public boolean showAtOne() {
        return showAtOne;
    }
}
