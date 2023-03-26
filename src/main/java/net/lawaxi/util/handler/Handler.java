package net.lawaxi.util.handler;

import net.lawaxi.Properties;

public class Handler {

    private String cronScheduleID;

    public void setCronScheduleID(String cronScheduleID) {
        this.cronScheduleID = cronScheduleID;
    }

    public String getCronScheduleID() {
        return cronScheduleID;
    }

    public final Properties properties;

    public Handler(Properties properties) {
        this.properties = properties;
    }
}
