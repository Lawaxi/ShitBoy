package net.lawaxi.model;

import java.util.List;

public class Pocket48Subscribe {
    private final List<Integer> roomIDs;
    private final List<Integer> starIDs;

    public Pocket48Subscribe(List<Integer> roomIDs, List<Integer> starIDs) {
        this.roomIDs = roomIDs;
        this.starIDs = starIDs;
    }

    public List<Integer> getRoomIDs() {
        return roomIDs;
    }

    public List<Integer> getStarIDs() {
        return starIDs;
    }
}
