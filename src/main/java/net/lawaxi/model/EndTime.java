package net.lawaxi.model;


import cn.hutool.core.date.DateTime;

public class EndTime {
    public long time;

    public EndTime(long time) {
        this.time = time;
    }

    public EndTime() {
        this(newTime());
    }

    public static long newTime() {
        return DateTime.now().getTime();
    }
}