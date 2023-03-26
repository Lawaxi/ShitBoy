package net.lawaxi.model;

import java.util.List;

public class Pocket48Subscribe {
    private final List<Integer> roomIDs;
    private final List<Integer> starIDs;
    private final boolean showAtOne;

    public Pocket48Subscribe(boolean showAtOne, List<Integer> roomIDs, List<Integer> starIDs) {
        this.roomIDs = roomIDs;
        this.starIDs = starIDs;
        this.showAtOne = showAtOne;
    }

    public List<Integer> getRoomIDs() {
        return roomIDs;
    }

    public List<Integer> getStarIDs() {
        return starIDs;
    }

    public boolean showAtOne() {
        return showAtOne;
    }
}
