package net.lawaxi.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;
import net.lawaxi.Properties;
import net.lawaxi.model.Pocket48Subscribe;
import net.lawaxi.model.WeidianCookie;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ConfigOperator {

    private Setting setting;
    private Properties properties;

    public void load(Properties properties) {

        this.properties = properties;
        File file = properties.configData;
        if (!file.exists()) {
            FileUtil.touch(file);
            Setting setting = new Setting(file, StandardCharsets.UTF_8, false);
            setting.set("enable", "true");
            setting.set("ylg", "true");
            setting.set("admins", "2330234142");
            setting.set("secureGroup", "");

            //口袋48
            setting.setByGroup("account", "pocket48", "12345678901");
            setting.setByGroup("password", "pocket48", "123456");

            JSONObject object = new JSONObject();
            object.set("qqGroup", 1234567);
            object.set("showAtOne", true);
            object.set("starSubs", new int[]{45285669, 70385975, 64422016});
            object.set("roomSubs", new int[]{1262731, 1361829});
            setting.setByGroup("subscribe", "pocket48",
                    "[" + object + "]");

            //bilibili
            object = new JSONObject();
            object.set("qqGroup", 1234567);
            object.set("subscribe", new int[]{21452505, 23771189});
            setting.setByGroup("subscribe", "bilibili",
                    "[" + object + "]");

            //微博
            object = new JSONObject();
            object.set("qqGroup", 1234567);
            object.set("userSubs", new long[]{5460950220L, 7824231810L});
            object.set("superTopicSubs", new String[]{"100808d965430a8faf6226034e42c56dca4a2b"});
            setting.setByGroup("subscribe", "weibo",
                    "[" + object + "]");

            //微店
            object = new JSONObject();
            object.set("qqGroup", 1234567);
            object.set("cookie", "");
            object.set("autoDeliver", false);
            setting.setByGroup("shops", "weidian", "[" + object + "]");

            setting.store();
        }

        this.setting = new Setting(file, StandardCharsets.UTF_8, false);
        init();
    }

    public Setting getSetting() {
        return setting;
    }

    public void init() {
        properties.enable = setting.getBool("enable", true);
        properties.ylg = setting.getBool("ylg", true);
        properties.admins = setting.getStrings("admins");
        properties.secureGroup = setting.getStrings("secureGroup");
        if (properties.admins == null)
            properties.admins = new String[]{};
        if (properties.secureGroup == null)
            properties.secureGroup = new String[]{};

        //口袋48
        properties.pocket48_account = setting.getByGroup("account", "pocket48");
        properties.pocket48_password = setting.getByGroup("password", "pocket48");

        for (Object a :
                JSONUtil.parseArray(setting.getByGroup("subscribe", "pocket48")).toArray()) {
            JSONObject sub = JSONUtil.parseObj(a);
            Object rooms = sub.getBeanList("roomSubs", Integer.class);
            Object stars = sub.getBeanList("starSubs", Integer.class);

            properties.pocket48_subscribe
                    .put(sub.getLong("qqGroup"),
                            new Pocket48Subscribe(
                                    sub.getBool("showAtOne", true),
                                    rooms == null ? new ArrayList<>() : (List<Integer>) rooms,
                                    stars == null ? new ArrayList<>() : (List<Integer>) stars
                            ));
        }

        for (Object a :
                JSONUtil.parseArray(setting.getByGroup("roomConnection", "pocket48")).toArray()) {
            JSONObject sid = JSONUtil.parseObj(a);
            properties.pocket48_serverID.put(sid.getInt("roomID"), sid.getInt("serverID"));
        }

        //bilibili
        for (Object a :
                JSONUtil.parseArray(setting.getByGroup("subscribe", "bilibili")).toArray()) {
            JSONObject subs = JSONUtil.parseObj(a);

            Object subss = subs.getBeanList("subscribe", Integer.class);
            properties.bilibili_subscribe
                    .put(subs.getLong("qqGroup"),
                            subss == null ? new ArrayList<>() : (List<Integer>) subss);
        }

        //微博
        for (Object a :
                JSONUtil.parseArray(setting.getByGroup("subscribe", "weibo")).toArray()) {
            JSONObject subs = JSONUtil.parseObj(a);

            long g = subs.getLong("qqGroup");
            List userSubs = subs.getBeanList("userSubs", Long.class);
            properties.weibo_user_subscribe.put(g, userSubs == null ? new ArrayList<>() : userSubs);

            List sTopicSubs = subs.getBeanList("superTopicSubs", String.class);
            properties.weibo_superTopic_subscribe.put(g, sTopicSubs == null ? new ArrayList<>() : sTopicSubs);

        }

        //微店
        for (Object a :
                JSONUtil.parseArray(setting.getByGroup("shops", "weidian")).toArray()) {
            JSONObject shop = JSONUtil.parseObj(a);

            long g = shop.getLong("qqGroup");
            String cookie = shop.getStr("cookie", "");
            boolean autoDeliver = shop.getBool("autoDeliver", false);
            properties.weidian_cookie.put(g, WeidianCookie.construct(cookie, autoDeliver));

        }
    }

    //修改配置并更新缓存的方法
    public boolean addPocket48RoomSubscribe(int room_id, long group) {
        if (!properties.pocket48_subscribe.containsKey(group)) {
            properties.pocket48_subscribe.put(group, new Pocket48Subscribe(
                    true, new ArrayList<>(), new ArrayList<>()
            ));
        }

        if (properties.pocket48_subscribe.get(group).getRoomIDs().contains(room_id))
            return false;

        properties.pocket48_subscribe.get(group).getRoomIDs().add(room_id);
        savePocket48SubscribeConfig();
        return true;
    }

    public boolean rmPocket48RoomSubscribe(int room_id, long group) {
        if (!properties.pocket48_subscribe.get(group).getRoomIDs().contains(room_id))
            return false;

        properties.pocket48_subscribe.get(group).getRoomIDs().remove((Object) room_id);
        savePocket48SubscribeConfig();
        return true;
    }

    public boolean addRoomIDConnection(int room_id, int sever_id) {
        if (properties.pocket48_serverID.containsKey(room_id))
            return false;

        properties.pocket48_serverID.put(room_id, sever_id);
        savePocket48RoomIDConnectConfig();
        return true;
    }

    public boolean rmRoomIDConnection(int room_id, int sever_id) {
        if (!properties.pocket48_serverID.containsKey(room_id))
            return false;

        properties.pocket48_serverID.remove(room_id, sever_id);
        savePocket48RoomIDConnectConfig();
        return true;
    }

    public boolean addBilibiliLiveSubscribe(int room_id, long group) {
        if (!properties.bilibili_subscribe.containsKey(group)) {
            properties.bilibili_subscribe.put(group, new ArrayList<>());
        }

        if (properties.bilibili_subscribe.get(group).contains(room_id))
            return false;

        properties.bilibili_subscribe.get(group).add(room_id);
        saveBilibiliConfig();
        return true;
    }

    public boolean rmBilibiliLiveSubscribe(int room_id, long group) {
        if (!properties.bilibili_subscribe.get(group).contains(room_id))
            return false;

        properties.bilibili_subscribe.get(group).remove((Object) room_id);
        saveBilibiliConfig();
        return true;
    }


    public boolean addWeiboUserSubscribe(long id, long group) {
        if (!properties.weibo_user_subscribe.containsKey(group)) {
            properties.weibo_user_subscribe.put(group, new ArrayList<>());
            properties.weibo_superTopic_subscribe.put(group, new ArrayList<>());
        }

        if (properties.weibo_user_subscribe.get(group).contains(id))
            return false;

        properties.weibo_user_subscribe.get(group).add(id);
        saveWeiboConfig();
        return true;
    }

    public boolean rmWeiboUserSubscribe(long id, long group) {
        if (!properties.weibo_user_subscribe.get(group).contains(id))
            return false;

        properties.weibo_user_subscribe.get(group).remove(id);
        saveWeiboConfig();
        return true;
    }

    public boolean addWeiboSTopicSubscribe(String id, long group) {
        if (!properties.weibo_user_subscribe.containsKey(group)) {
            properties.weibo_user_subscribe.put(group, new ArrayList<>());
            properties.weibo_superTopic_subscribe.put(group, new ArrayList<>());
        }

        if (properties.weibo_superTopic_subscribe.get(group).contains(id))
            return false;

        properties.weibo_superTopic_subscribe.get(group).add(id);
        saveWeiboConfig();
        return true;
    }

    public boolean rmWeiboSTopicSubscribe(String id, long group) {
        if (!properties.weibo_superTopic_subscribe.get(group).contains(id))
            return false;

        properties.weibo_superTopic_subscribe.get(group).remove(id);
        saveWeiboConfig();
        return true;
    }

    public boolean setWeidianCookie(String cookie, long group) {
        boolean autoDeliver = false;
        if (properties.weidian_cookie.containsKey(group)) {
            autoDeliver = properties.weidian_cookie.get(group).autoDeliver;
        }
        properties.weidian_cookie.put(group, WeidianCookie.construct(cookie, autoDeliver));
        saveWeidianConfig();
        return true;
    }

    public int switchWeidianAutoDeliver(long group) {
        if (!properties.weidian_cookie.containsKey(group))
            return -1;

        WeidianCookie cookie = properties.weidian_cookie.get(group);
        cookie.autoDeliver = !cookie.autoDeliver;
        saveWeidianConfig();
        return cookie.autoDeliver ? 1 : 0;
    }

    public boolean rmWeidianCookie(long group) {
        if (!properties.weidian_cookie.containsKey(group)) {
            return false;
        }

        properties.weidian_cookie.remove(group);
        saveWeidianConfig();
        return true;
    }

    private void savePocket48SubscribeConfig() {
        String a = "[";
        for (long group : properties.pocket48_subscribe.keySet()) {
            JSONObject object = new JSONObject();
            Pocket48Subscribe subscribe = properties.pocket48_subscribe.get(group);
            object.set("qqGroup", group);
            object.set("showAtOne", subscribe.showAtOne());
            object.set("starSubs", subscribe.getStarIDs().toArray());
            object.set("roomSubs", subscribe.getRoomIDs().toArray());
            a += object + ",";
        }
        setting.setByGroup("subscribe", "pocket48", (a.length() > 1 ? a.substring(0, a.length() - 1) : a) + "]");
        setting.store();
    }

    private void savePocket48RoomIDConnectConfig() {
        String a = "[";
        for (int room_id : properties.pocket48_serverID.keySet()) {
            JSONObject object = new JSONObject();
            object.set("roomID", room_id);
            object.set("serverID", properties.pocket48_serverID.get(room_id));
            a += object + ",";
        }
        setting.setByGroup("roomConnection", "pocket48", (a.length() > 1 ? a.substring(0, a.length() - 1) : a) + "]");
        setting.store();
    }

    private void saveBilibiliConfig() {
        String a = "[";
        for (long group : properties.bilibili_subscribe.keySet()) {
            JSONObject object = new JSONObject();
            object.set("qqGroup", group);
            object.set("subscribe", properties.bilibili_subscribe.get(group));
            a += object + ",";
        }
        setting.setByGroup("subscribe", "bilibili", (a.length() > 1 ? a.substring(0, a.length() - 1) : a) + "]");
        setting.store();
    }

    private void saveWeiboConfig() {
        String a = "[";
        for (long group : properties.weibo_user_subscribe.keySet()) {
            JSONObject object = new JSONObject();
            object.set("qqGroup", group);
            object.set("userSubs", properties.weibo_user_subscribe.get(group));
            object.set("superTopicSubs", properties.weibo_superTopic_subscribe.get(group));
            a += object + ",";
        }
        setting.setByGroup("subscribe", "weibo", (a.length() > 1 ? a.substring(0, a.length() - 1) : a) + "]");
        setting.store();
    }

    private void saveWeidianConfig() {
        String a = "[";
        for (long group : properties.weidian_cookie.keySet()) {
            JSONObject object = new JSONObject();
            object.set("qqGroup", group);
            object.set("cookie", properties.weidian_cookie.get(group).cookie);
            object.set("autoDeliver", properties.weidian_cookie.get(group).autoDeliver);
            a += object + ",";
        }
        setting.setByGroup("shops", "weidian", (a.length() > 1 ? a.substring(0, a.length() - 1) : a) + "]");
        setting.store();
    }

    public void swch(boolean on) {
        setting.set("enable", String.valueOf(on));
        properties.enable = setting.getBool("enable");
    }

    public boolean isAdmin(Group group, long qqID) {
        for (String g : properties.secureGroup) {
            if (g.equals(String.valueOf(group.getId())))
                return true;
        }

        for (String a : properties.admins) {
            if (a.equals(String.valueOf(qqID)))
                return true;
        }

        return group.get(qqID).getPermission() == MemberPermission.ADMINISTRATOR;
    }
}
