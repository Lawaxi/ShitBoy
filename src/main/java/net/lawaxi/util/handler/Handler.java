package net.lawaxi.util.handler;

import net.lawaxi.Properties;
import net.lawaxi.Shitboy;

public class Handler {

    private String cronScheduleID;

    public void setCronScheduleID(String cronScheduleID) {
        this.cronScheduleID = cronScheduleID;
    }

    public String getCronScheduleID() {
        return cronScheduleID;
    }

    public final Properties properties;

    public Handler() {
        this.properties = Shitboy.INSTANCE.getProperties();
    }
}
