package net.lawaxi.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;
import net.lawaxi.Shitboy;
import net.lawaxi.model.Pocket48Subscribe;
import net.lawaxi.model.WeidianCookie;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.NormalMember;

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
            setting.set("save_login", "false");
            setting.set("ylg", "false");
            setting.set("admins", "2330234142");
            setting.set("secureGroup", "");

            JSONObject object = new JSONObject();
            object.set("1", 1234567);
            object.set("2", "欢迎新宝宝");
            setting.set("welcome", "[" + object + "]");

            //schedule pattern
            setting.setByGroup("schedule", "pocket48", "* * * * *");
            setting.setByGroup("schedule", "bilibili", "* * * * *");
            setting.setByGroup("schedule", "weibo", "*/5 * * * *");
            setting.setByGroup("schedule_order", "weidian", "*/10 * * * *");
            setting.setByGroup("schedule_item", "weidian", "*/10 * * * *");

            //口袋48
            setting.setByGroup("account", "pocket48", "");
            setting.setByGroup("password", "pocket48", "");
            setting.setByGroup("token", "pocket48", "");

            object = new JSONObject();
            object.set("qqGroup", 1234567);
            object.set("showAtOne", true);
            object.set("starSubs", new long[]{});
            object.set("roomSubs", new long[]{});
            setting.setByGroup("subscribe", "pocket48",
                    "[" + object + "]");

            //bilibili
            object = new JSONObject();
            object.set("qqGroup", 1234567);
            object.set("subscribe", new int[]{});
            setting.setByGroup("subscribe", "bilibili",
                    "[" + object + "]");

            //微博
            object = new JSONObject();
            object.set("qqGroup", 1234567);
            object.set("userSubs", new long[]{});
            object.set("superTopicSubs", new String[]{});
            setting.setByGroup("subscribe", "weibo",
                    "[" + object + "]");

            //微店
            object = new JSONObject();
            object.set("qqGroup", 1234567);
            object.set("cookie", "");
            object.set("autoDeliver", false);
            object.set("highlight", "[]");
            setting.setByGroup("shops", "weidian", "[" + object + "]");

            setting.store();
            Shitboy.INSTANCE.getLogger().info("首次加载已生成 config/net.lawaxi.shitboy/config.setting 配置文件，请先填写口袋48账号密码用于获取房间消息并重启");
        }

        this.setting = new Setting(file, StandardCharsets.UTF_8, false);
        init();
    }

    public Setting getSetting() {
        return setting;
    }

    public void init() {
        properties.enable = setting.getBool("enable", true);
        properties.save_login = setting.getBool("save_login", false);
        properties.ylg = setting.getBool("ylg", false);
        properties.admins = setting.getStrings("admins");
        properties.secureGroup = setting.getStrings("secureGroup");
        if (properties.admins == null)
            properties.admins = new String[]{};
        if (properties.secureGroup == null)
            properties.secureGroup = new String[]{};

        for (Object a :
                JSONUtil.parseArray(setting.getStr("welcome", "[]")).toArray()) {
            JSONObject welcome = JSONUtil.parseObj(a);
            properties.welcome.put(
                    welcome.getLong("1"),
                    welcome.getStr("2")
            );
        }

        //schedule pattern
        properties.pocket48_pattern = setting.getStr("schedule", "pocket48", "* * * * *");
        properties.bilibili_pattern = setting.getStr("schedule", "pocket48", "* * * * *");
        properties.weibo_pattern = setting.getStr("schedule", "weibo", "*/5 * * * *");
        properties.weidian_pattern_order = setting.getStr("schedule_order", "weidian", "*/2 * * * *");
        properties.weidian_pattern_item = setting.getStr("schedule_item", "weidian", "*/5 * * * *");

        //口袋48
        properties.pocket48_account = setting.getStr("account", "pocket48", "");
        properties.pocket48_password = setting.getStr("password", "pocket48", "");
        properties.pocket48_token = setting.getStr("token", "pocket48", "");

        for (Object a :
                JSONUtil.parseArray(setting.getByGroup("subscribe", "pocket48")).toArray()) {
            JSONObject sub = JSONUtil.parseObj(a);
            Object rooms = sub.getBeanList("roomSubs", Long.class);
            Object stars = sub.getBeanList("starSubs", Long.class);

            properties.pocket48_subscribe
                    .put(sub.getLong("qqGroup"),
                            new Pocket48Subscribe(
                                    sub.getBool("showAtOne", true),
                                    rooms == null ? new ArrayList<>() : (List<Long>) rooms,
                                    stars == null ? new ArrayList<>() : (List<Long>) stars
                            ));
        }

        for (Object a :
                JSONUtil.parseArray(setting.getByGroup("roomConnection", "pocket48")).toArray()) {
            JSONObject sid = JSONUtil.parseObj(a);
            properties.pocket48_serverID.put(sid.getLong("roomID"), sid.getLong("serverID"));
        }

        //bilibili
        for (Object a :
                JSONUtil.parseArray(setting.getByGroup("subscribe", "bilibili")).toArray()) {
            JSONObject subs = JSONUtil.parseObj(a);
            if (subs.containsKey("bili_subscribe") && subs.containsKey("bili_subscribe")) {
                Object bili_subs = subs.getBeanList("bili_subscribe", Integer.class);
                properties.bilibili_subscribe
                        .put(subs.getLong("qqGroup"),
                                bili_subs == null ? new ArrayList<>() : (List<Integer>) bili_subs);

                Object live_subs = subs.getBeanList("live_subscribe", Integer.class);
                properties.bililive_subscribe
                        .put(subs.getLong("qqGroup"),
                                live_subs == null ? new ArrayList<>() : (List<Integer>) live_subs);


            } else if (subs.containsKey("subscribe")) { //旧版配置
                properties.bilibili_subscribe
                        .put(subs.getLong("qqGroup"), new ArrayList<>());

                Object live_subs = subs.getBeanList("subscribe", Integer.class);
                properties.bililive_subscribe
                        .put(subs.getLong("qqGroup"),
                                live_subs == null ? new ArrayList<>() : (List<Integer>) live_subs);

            }
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
            boolean doBroadCast = shop.getBool("doBroadCast", true);
            List<Long> highlight = shop.getBeanList("highlight", Long.class);
            List<Long> shielded = shop.getBeanList("shielded", Long.class);
            properties.weidian_cookie.put(g, WeidianCookie.construct(cookie, autoDeliver, doBroadCast,
                    highlight == null ? new ArrayList<>() : highlight,
                    shielded == null ? new ArrayList<>() : shielded));

        }
    }

    //修改配置并更新缓存的方法
    public void swch(boolean on) {
        setting.set("enable", String.valueOf(on));
        setting.store();
        properties.enable = setting.getBool("enable");
    }

    public boolean setWelcome(String welcome, long group) {
        properties.welcome.put(group, welcome);
        saveWelcome();
        return true;
    }

    public boolean closeWelcome(long group) {
        properties.welcome.remove(group);
        saveWelcome();
        return true;
    }

    public boolean setAndSaveToken(String token) {
        properties.pocket48_token = token;
        setting.setByGroup("token", "pocket48", token);
        setting.store();
        return true;
    }

    public String getToken(){
        return setting.getStr("token", "pocket48",properties.pocket48_token);
    }

    public boolean addPocket48RoomSubscribe(long room_id, long group) {
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

    public boolean rmPocket48RoomSubscribe(long room_id, long group) {
        if (!properties.pocket48_subscribe.get(group).getRoomIDs().contains(room_id))
            return false;

        properties.pocket48_subscribe.get(group).getRoomIDs().remove(room_id);
        savePocket48SubscribeConfig();
        return true;
    }

    public boolean addRoomIDConnection(long room_id, long sever_id) {
        if (properties.pocket48_serverID.containsKey(room_id))
            return false;

        properties.pocket48_serverID.put(room_id, sever_id);
        savePocket48RoomIDConnectConfig();
        return true;
    }

    public boolean rmRoomIDConnection(long room_id, long sever_id) {
        if (!properties.pocket48_serverID.containsKey(room_id))
            return false;

        properties.pocket48_serverID.remove(room_id, sever_id);
        savePocket48RoomIDConnectConfig();
        return true;
    }

    public boolean addBililiveSubscribe(int room_id, long group) {
        if (!properties.bilibili_subscribe.containsKey(group)) {
            properties.bilibili_subscribe.put(group, new ArrayList<>());
            properties.bililive_subscribe.put(group, new ArrayList<>());
        }

        if (properties.bililive_subscribe.get(group).contains(room_id))
            return false;

        properties.bililive_subscribe.get(group).add(room_id);
        saveBilibiliConfig();
        return true;
    }

    public boolean rmBililiveSubscribe(int room_id, long group) {
        if (properties.bilibili_subscribe.containsKey(group)) {
            if (properties.bililive_subscribe.get(group).contains(room_id)) {
                properties.bililive_subscribe.get(group).remove((Object) room_id);
                saveBilibiliConfig();
                return true;
            }
        }
        return false;
    }

    public boolean addBilibiliSubscribe(int uid, long group) {
        if (!properties.bilibili_subscribe.containsKey(group)) {
            properties.bilibili_subscribe.put(group, new ArrayList<>());
            properties.bililive_subscribe.put(group, new ArrayList<>());
        }

        if (properties.bilibili_subscribe.get(group).contains(uid))
            return false;

        properties.bilibili_subscribe.get(group).add(uid);
        saveBilibiliConfig();
        return true;
    }

    public boolean rmBilibiliSubscribe(int uid, long group) {
        if (properties.bilibili_subscribe.containsKey(group)) {
            if (properties.bilibili_subscribe.get(group).contains(uid)) {
                properties.bilibili_subscribe.get(group).remove((Object) uid);
                saveBilibiliConfig();
                return true;
            }
        }
        return false;
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
        boolean doBroadcast = true;
        List<Long> highlightItem = new ArrayList<>();
        List<Long> shieldItem = new ArrayList<>();
        if (properties.weidian_cookie.containsKey(group)) {
            autoDeliver = properties.weidian_cookie.get(group).autoDeliver;
            doBroadcast = properties.weidian_cookie.get(group).doBroadcast;
            highlightItem = properties.weidian_cookie.get(group).highlightItem;
            shieldItem = properties.weidian_cookie.get(group).shieldedItem;
        }
        properties.weidian_cookie.put(group, WeidianCookie.construct(cookie, autoDeliver, doBroadcast, highlightItem, shieldItem));
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

    public int switchWeidianDoBroadCast(long group) {
        if (!properties.weidian_cookie.containsKey(group))
            return -1;

        WeidianCookie cookie = properties.weidian_cookie.get(group);
        cookie.doBroadcast = !cookie.doBroadcast;
        saveWeidianConfig();
        return cookie.doBroadcast ? 1 : 0;
    }

    public boolean rmWeidianCookie(long group) {
        if (!properties.weidian_cookie.containsKey(group)) {
            return false;
        }

        properties.weidian_cookie.remove(group);
        saveWeidianConfig();
        return true;
    }

    public int highlightWeidianItem(long group, long itemid) {
        if (!properties.weidian_cookie.containsKey(group)) {
            return -1;
        }

        List<Long> it = properties.weidian_cookie.get(group).highlightItem;
        if (it.contains(itemid)) {
            it.remove(itemid);
        } else {
            it.add(itemid);
        }
        saveWeidianConfig();
        return it.contains(itemid) ? 1 : 0;
    }

    public int shieldWeidianItem(long group, long itemid) {
        if (!properties.weidian_cookie.containsKey(group)) {
            return -1;
        }

        List<Long> it = properties.weidian_cookie.get(group).shieldedItem;
        if (it.contains(itemid)) {
            it.remove(itemid);
        } else {
            it.add(itemid);
        }
        saveWeidianConfig();
        return it.contains(itemid) ? 1 : 0;
    }

    public void saveWelcome() {
        String a = "[";
        for (long group : properties.welcome.keySet()) {
            JSONObject object = new JSONObject();
            object.set("1", group);
            object.set("2", properties.welcome.get(group));
            a += object + ",";
        }
        setting.set("welcome", (a.length() > 1 ? a.substring(0, a.length() - 1) : a) + "]");
        setting.store();
    }

    public void savePocket48SubscribeConfig() {
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

    public void savePocket48RoomIDConnectConfig() {
        String a = "[";
        for (long room_id : properties.pocket48_serverID.keySet()) {
            JSONObject object = new JSONObject();
            object.set("roomID", room_id);
            object.set("serverID", properties.pocket48_serverID.get(room_id));
            a += object + ",";
        }
        setting.setByGroup("roomConnection", "pocket48", (a.length() > 1 ? a.substring(0, a.length() - 1) : a) + "]");
        setting.store();
    }

    public void saveBilibiliConfig() {
        String a = "[";
        for (long group : properties.bilibili_subscribe.keySet()) {
            JSONObject object = new JSONObject();
            object.set("qqGroup", group);
            object.set("bili_subscribe", properties.bilibili_subscribe.get(group));
            object.set("live_subscribe", properties.bililive_subscribe.get(group));
            a += object + ",";
        }
        setting.setByGroup("subscribe", "bilibili", (a.length() > 1 ? a.substring(0, a.length() - 1) : a) + "]");
        setting.store();
    }

    public void saveWeiboConfig() {
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

    public void saveWeidianConfig() {
        String a = "[";
        for (long group : properties.weidian_cookie.keySet()) {
            JSONObject object = new JSONObject();
            object.set("qqGroup", group);
            object.set("cookie", properties.weidian_cookie.get(group).cookie);
            object.set("autoDeliver", properties.weidian_cookie.get(group).autoDeliver);
            object.set("highlight", properties.weidian_cookie.get(group).highlightItem.toString());
            a += object + ",";
        }
        setting.setByGroup("shops", "weidian", (a.length() > 1 ? a.substring(0, a.length() - 1) : a) + "]");
        setting.store();
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

        NormalMember m = group.get(qqID);
        if (m == null)
            return false;

        return m.getPermission() == MemberPermission.ADMINISTRATOR || m.getPermission() == MemberPermission.OWNER;
    }

    public boolean isAdmin(long qqID) {
        for (String a : properties.admins) {
            if (a.equals(String.valueOf(qqID)))
                return true;
        }
        return false;
    }
}
