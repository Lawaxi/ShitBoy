package net.lawaxi.util.handler;

import cn.hutool.http.HttpRequest;
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

    /*----------------------------------*/

    public final Properties properties;

    public Handler() {
        this.properties = Shitboy.INSTANCE.getProperties();
    }


    protected void logInfo(String msg) {
        properties.logger.info(msg);
    }

    protected void logError(String msg) {
        properties.logger.error(msg);
    }

    protected String post(String url, String body) {
        return setHeader(HttpRequest.post(url))
                .body(body).execute().body();
    }

    protected String get(String url) {
        return setHeader(HttpRequest.get(url))
                .execute().body();
    }

    protected HttpRequest setHeader(HttpRequest request) {
        return request;
    }
}
