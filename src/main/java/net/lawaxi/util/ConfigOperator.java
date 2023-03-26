package net.lawaxi.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;
import net.lawaxi.Properties;
import net.lawaxi.model.Pocket48Subscribe;

import java.io.File;
import java.nio.charset.StandardCharsets;

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
            properties.pocket48_subscribe
                    .put(subs.getLong("qqGroup"),
                            new Pocket48Subscribe(
                                    subs.getBeanList("roomSubs", Integer.class),
                                    subs.getBeanList("starSubs", Integer.class)
                            ));
        }

        //bilibili
        for (Object a :
                JSONUtil.parseArray(setting.getByGroup("subscribe", "bilibili")).toArray()) {
            JSONObject subs = JSONUtil.parseObj(a);
            properties.bilibili_subscribe
                    .put(subs.getLong("qqGroup"),
                            subs.getBeanList("subscribe", Integer.class));
        }
    }

    public void swch(boolean on) {
        setting.set("enable", String.valueOf(on));
        properties.enable = setting.getBool("enable");
    }
}
