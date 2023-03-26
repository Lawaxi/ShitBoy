package net.lawaxi.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;
import net.lawaxi.Properties;
import net.lawaxi.model.Pocket48Subscribe;

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
            setting.set("admins", "2330234142");

            //pocket48
            setting.setByGroup("account", "pocket48", "");
            setting.setByGroup("password", "pocket48", "");

            JSONObject object = new JSONObject();
            object.set("qqGroup", 764687233);
            object.set("showAtOne", true);
            object.set("starSubs", new int[]{45285669, 70385975, 64422016});
            object.set("roomSubs", new int[]{1262731, 1361829});
            setting.setByGroup("subscribe", "pocket48",
                    "[" + object + "]");

            //bilibili
            object = new JSONObject();
            object.set("qqGroup", 764687233);
            object.set("subscribe", new int[]{21452505, 23771189});
            setting.setByGroup("subscribe", "bilibili",
                    "[" + object + "]");

            setting.store();
        }

        this.setting = new Setting(file, StandardCharsets.UTF_8, false);
        init();
    }

    public Setting getSetting() {
        return setting;
    }

    public void init() {
        properties.enable = setting.getBool("enable");
        properties.admins = setting.getStrings("admins");

        //pocket48
        properties.pocket48_account = setting.getByGroup("account", "pocket48");
        properties.pocket48_password = setting.getByGroup("password", "pocket48");

        for (Object a :
                JSONUtil.parseArray(setting.getByGroup("subscribe", "pocket48")).toArray()) {
            JSONObject subs = JSONUtil.parseObj(a);
            Object rooms = subs.getBeanList("roomSubs", Integer.class);
            Object stars = subs.getBeanList("starSubs", Integer.class);

            properties.pocket48_subscribe
                    .put(subs.getLong("qqGroup"),
                            new Pocket48Subscribe(
                                    subs.getBool("showAtOne",true),
                                    rooms == null ? new ArrayList<>() : (List<Integer>) rooms,
                                    stars == null ? new ArrayList<>() : (List<Integer>) stars
                            ));
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
    }

    public void addPocket48RoomSubscribe(int room_id, long group){
        properties.pocket48_subscribe.get(group).getRoomIDs().add(room_id);
        savePocket48Config();
    }

    public void rmPocket48RoomSubscribe(int room_id, long group){
        properties.pocket48_subscribe.get(group).getRoomIDs().remove((Object) room_id);
        savePocket48Config();
    }

    public void addBilibiliLiveSubscribe(int room_id, long group){
        properties.bilibili_subscribe.get(group).add(room_id);
        saveBilibiliConfig();
    }

    public void rmBilibiliLiveSubscribe(int room_id, long group){
        properties.bilibili_subscribe.get(group).remove((Object) room_id);
        saveBilibiliConfig();
    }

    private void savePocket48Config(){
        String a = "[";
        for(long group : properties.pocket48_subscribe.keySet()){
            JSONObject object = new JSONObject();
            Pocket48Subscribe subscribe = properties.pocket48_subscribe.get(group);
            object.set("qqGroup", group);
            object.set("showAtOne", subscribe.showAtOne());
            object.set("starSubs", subscribe.getStarIDs().toArray());
            object.set("roomSubs", subscribe.getRoomIDs().toArray());
            a += object+",";
        }
        setting.setByGroup("subscribe", "pocket48", (a.length()>1 ? a.substring(0,a.length()-1) : a)+"]");
        setting.store();
    }

    private void saveBilibiliConfig(){
        String a = "[";
        for(long group : properties.bilibili_subscribe.keySet()){
            JSONObject object = new JSONObject();
            object.set("qqGroup", group);
            object.set("subscribe", properties.bilibili_subscribe.get(group));
            a += object+",";
        }
        setting.setByGroup("subscribe", "bilibili", (a.length()>1 ? a.substring(0,a.length()-1) : a)+"]");
        setting.store();
    }

    public void swch(boolean on) {
        setting.set("enable", String.valueOf(on));
        properties.enable = setting.getBool("enable");
    }
}
