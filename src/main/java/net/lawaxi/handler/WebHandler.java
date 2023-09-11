package net.lawaxi.handler;

import cn.hutool.http.HttpRequest;
import net.lawaxi.Shitboy;
import net.lawaxi.util.Properties;

public class WebHandler {

    public final Properties properties;
    private String cronScheduleID;

    public WebHandler() {
        this.properties = Shitboy.INSTANCE.getProperties();
    }

    /*----------------------------------*/

    public String getCronScheduleID() {
        return cronScheduleID;
    }

    public void setCronScheduleID(String cronScheduleID) {
        this.cronScheduleID = cronScheduleID;
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

    public HttpRequest setHeader_Public(HttpRequest request) {
        if (!properties.save_login)
            return setHeader(request);
        else return request;
    }

    protected HttpRequest setHeader(HttpRequest request) {
        return request.setReadTimeout(20000);
    }
}
