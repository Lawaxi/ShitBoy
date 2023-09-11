package net.lawaxi.handler;

import cn.hutool.http.HttpRequest;
import net.lawaxi.util.Properties;

public class Pocket48HandlerHeader {

    private final Properties properties;
    private final String pa = "MTY3OTYzOTUwOTAwMCw3NDEwLEYyMzk2RkJGQTI5ODEwRUFFOEI4NEUyNDQ0RkREQjUzLDIwMjEwNjA5MDE=";
    private final String version = "7.0.4";
    private String token;

    public Pocket48HandlerHeader(Properties properties) {
        this.properties = properties;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    private HttpRequest setDefaultHeader(HttpRequest request) {
        return
                request.header("Content-Type", "application/json;charset=utf-8")
                        .header("Host", "pocketapi.48.cn")
                        .header("pa", pa)
                        .header("User-Agent", "PocketFans201807/7.0.4 (iPhone; iOS 16.2; Scale/3.00)")
                        .header("appInfo", String.format("{\"vendor\":\"apple\",\"deviceId\":\"5857F116-D35C-478E-95B1-8540034740B3\",\"appVersion\":\"%s\",\"appBuild\":\"23011601\",\"osVersion\":\"16.2.0\",\"osType\":\"ios\",\"deviceName\":\"iPhone 13\",\"os\":\"ios\"}", version));
    }

    public HttpRequest setLoginHeader(HttpRequest request) {
        return setDefaultHeader(request);
    }

    public HttpRequest setHeader(HttpRequest request) {
        return setDefaultHeader(request)
                .header("token", token);
    }

}
