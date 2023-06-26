package net.lawaxi.model;


import net.lawaxi.Shitboy;

public class EndTime {
    public long time;

    public EndTime(long time) {
        this.time = time;
    }

    public EndTime() {
        this(Shitboy.START_TIME);
    }
}