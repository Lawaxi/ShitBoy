package net.lawaxi.model;

import java.util.ArrayList;
import java.util.List;

public class WeidianCookie {
    public final String cookie;
    public final String wdtoken;
    public boolean autoDeliver;
    public List<Long> highlightItem;
    public boolean invalid = false;

    private WeidianCookie(String cookie, String wdtoken, boolean autoDeliver, List<Long> highlightItem) {
        this.cookie = cookie;
        this.wdtoken = wdtoken;
        this.autoDeliver = autoDeliver;
        this.highlightItem = highlightItem;
    }

    public static WeidianCookie construct(String cookie, boolean autoDeliver, List<Long> highlightItem) {
        for (String p : cookie.split("; ")) {
            if (p.startsWith("wdtoken")) {
                return new WeidianCookie(cookie, p.substring(p.indexOf("=") + 1), autoDeliver, highlightItem);
            }
        }
        return null;
    }


    public static WeidianCookie construct(String cookie) {
        return construct(cookie, false, new ArrayList<>());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WeidianCookie) {
            return ((WeidianCookie) obj).cookie.equals(this.cookie)
                    && ((WeidianCookie) obj).autoDeliver == this.autoDeliver;
        }
        return false;
    }
}
