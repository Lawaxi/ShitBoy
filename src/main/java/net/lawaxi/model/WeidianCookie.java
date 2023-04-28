package net.lawaxi.model;

public class WeidianCookie {
    public final String cookie;
    public final String wdtoken;
    public boolean autoDeliver;

    private WeidianCookie(String cookie, String wdtoken, boolean autoDeliver) {
        this.cookie = cookie;
        this.wdtoken = wdtoken;
        this.autoDeliver = autoDeliver;
    }

    public static WeidianCookie construct(String cookie, boolean autoDeliver) {
        for (String p : cookie.split("; ")) {
            if (p.startsWith("wdtoken")) {
                return new WeidianCookie(cookie, p.substring(p.indexOf("=") + 1), autoDeliver);
            }
        }
        return null;
    }


    public static WeidianCookie construct(String cookie) {
        return construct(cookie, false);
    }
}
