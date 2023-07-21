package net.lawaxi.model;

import java.util.ArrayList;
import java.util.List;

public class WeidianCookie {
    public final String cookie;
    public final String wdtoken;
    public boolean autoDeliver; //是否自动发货
    public boolean doBroadcast; //是否播报
    public List<Long> highlightItem; //特殊商品
    public List<Long> shieldedItem; //屏蔽商品(如周边)
    public boolean invalid = false;

    private WeidianCookie(String cookie, String wdtoken, boolean autoDeliver, boolean doBroadcast, List<Long> highlightItem, List<Long> shieldedItem) {
        this.cookie = cookie;
        this.wdtoken = wdtoken;
        this.autoDeliver = autoDeliver;
        this.doBroadcast = doBroadcast;
        this.highlightItem = highlightItem;
        this.shieldedItem = shieldedItem;
    }

    public static WeidianCookie construct(String cookie, boolean autoDeliver, boolean doBroadcast, List<Long> highlightItem, List<Long> shieldedItem) {
        for (String p : cookie.split("; ")) {
            if (p.startsWith("wdtoken")) {
                return new WeidianCookie(cookie, p.substring(p.indexOf("=") + 1), autoDeliver, doBroadcast, highlightItem, shieldedItem);
            }
        }
        return null;
    }

    public static WeidianCookie construct(String cookie) {
        return construct(cookie, false, true, new ArrayList<>(), new ArrayList<>());
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
