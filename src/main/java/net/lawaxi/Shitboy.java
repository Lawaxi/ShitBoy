package net.lawaxi;

import cn.hutool.cron.CronUtil;
import cn.hutool.extra.pinyin.engine.pinyin4j.Pinyin4jEngine;
import net.lawaxi.command.ShitBoyCommand;
import net.lawaxi.handler.*;
import net.lawaxi.model.EndTime;
import net.lawaxi.model.Pocket48SenderCache;
import net.lawaxi.util.ConfigOperator;
import net.lawaxi.util.Properties;
import net.lawaxi.util.PropertiesCommon;
import net.lawaxi.util.sender.*;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.permission.AbstractPermitteeId;
import net.mamoe.mirai.console.permission.PermissionId;
import net.mamoe.mirai.console.permission.PermissionService;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.BotOnlineEvent;

import java.util.Date;
import java.util.HashMap;
import java.util.List;


public final class Shitboy extends JavaPlugin {
    public static final Shitboy INSTANCE = new Shitboy();
    private final ConfigOperator configOperator = new ConfigOperator();
    private final Properties properties = new Properties();
    public Pocket48Handler handlerPocket48;
    public BilibiliHandler handlerBilibili;
    public WeiboHandler handlerWeibo;
    public WeidianHandler handlerWeidian;
    public WeidianSenderHandler handlerWeidianSender;

    private Shitboy() {
        super(new JvmPluginDescriptionBuilder("net.lawaxi.shitboy", "0.1.7-test14" +
                "")
                .name("shitboy")
                .author("delay")
                .info("010号机器人")
                .build());
    }

    @Override
    public void onEnable() {
        initProperties();
        loadConfig();
        registerPermission();
        registerCommand();
        GlobalEventChannel.INSTANCE.registerListenerHost(new Listener());
        if (properties.ylg)
            GlobalEventChannel.INSTANCE.registerListenerHost(new ListenerYLG());
        GlobalEventChannel.INSTANCE.parentScope(INSTANCE).subscribeOnce(BotOnlineEvent.class, event -> {
            listenBroadcast();
        });

        getLogger().info("Shit boy!");
        getLogger().info(new Pinyin4jEngine().getPinyin("拼音测试", " "));
    }

    private void initProperties() {
        properties.configData = resolveConfigFile(PropertiesCommon.configDataName);
        properties.logger = getLogger();
        handlerPocket48 = new Pocket48Handler();
        handlerBilibili = new BilibiliHandler();
        handlerWeibo = new WeiboHandler();
        handlerWeidian = new WeidianHandler();
        handlerWeidianSender = new WeidianSenderHandler();
    }

    private void registerCommand() {
        CommandManager.INSTANCE.registerCommand(new ShitBoyCommand(), false);
    }

    public ConfigOperator getConfig() {
        return configOperator;
    }

    public Properties getProperties() {
        return properties;
    }

    public Pocket48Handler getHandlerPocket48() {
        return handlerPocket48;
    }

    public BilibiliHandler getHandlerBilibili() {
        return handlerBilibili;
    }

    public WeiboHandler getHandlerWeibo() {
        return handlerWeibo;
    }

    public WeidianHandler getHandlerWeidian() {
        return handlerWeidian;
    }

    public WeidianSenderHandler getHandlerWeidianSender() {
        return handlerWeidianSender;
    }

    private void loadConfig() {
        configOperator.load(properties);
    }

    public void registerPermission() {
        PermissionId permissionId = this.getParentPermission().getId();
        for (String a : properties.admins) {
            AbstractPermitteeId.ExactUser user = new AbstractPermitteeId.ExactUser(Long.parseLong(a));

            if (!PermissionService.hasPermission(user, permissionId)) {
                PermissionService.permit(user, permissionId);
            }
        }
    }


    private void listenBroadcast() {
        CronUtil.getScheduler().clear();
        //------------------------------------------------

        //口袋48登录
        boolean pocket48_has_login = false;
        if (!properties.pocket48_token.equals("")) {
            this.handlerPocket48.login(properties.pocket48_token, false);
            pocket48_has_login = true;
        } else if (!(properties.pocket48_account.equals("") || properties.pocket48_password.equals(""))) {
            pocket48_has_login = this.handlerPocket48.login(
                    properties.pocket48_account,
                    properties.pocket48_password
            );
        } else {
            getLogger().info("开启口袋48播报需填写config/net.lawaxi.shitboy/config.setting并重启");
        }

        boolean weibo_has_login = false;
        try {
            this.handlerWeibo.updateLoginToSuccess();
            weibo_has_login = true;
            getLogger().info("微博Cookie更新成功");

        } catch (Exception e) {
            getLogger().info("微博Cookie更新失败");
        }

        //服务

        //endTime: 已发送房间消息的最晚时间
        HashMap<Long, HashMap<Long, Long>> pocket48RoomEndTime = new HashMap<>();
        HashMap<Long, HashMap<String, Long>> weiboEndTime = new HashMap<>(); //同时包含超话和个人(long -> String)
        HashMap<Long, EndTime> weidianEndTime = new HashMap<>();
        //status: 上次检测的开播状态
        HashMap<Long, HashMap<Long, List<Long>>> pocket48VoiceStatus = new HashMap<>();
        HashMap<Long, HashMap<Integer, Boolean>> bilibiliLiveStatus = new HashMap<>();

        //服务
        for (Bot b : Bot.getInstances()) {
            if (pocket48_has_login) {
                handlerPocket48.setCronScheduleID(CronUtil.schedule(properties.pocket48_pattern, new Runnable() {
                            @Override
                            public void run() {
                                HashMap<Long, Pocket48SenderCache> cache = new HashMap();

                                for (long group : properties.pocket48_subscribe.keySet()) {
                                    if (b.getGroup(group) == null)
                                        continue;

                                    if (!pocket48RoomEndTime.containsKey(group))//放到Runnable里面是因为可能实时更新新的群
                                    {
                                        pocket48RoomEndTime.put(group, new HashMap<>());
                                        pocket48VoiceStatus.put(group, new HashMap<>());
                                    }

                                    new Pocket48Sender(b, group, pocket48RoomEndTime.get(group), pocket48VoiceStatus.get(group), cache).start();

                                }

                            }
                        }
                ));
            }

            handlerBilibili.setCronScheduleID(CronUtil.schedule(properties.bilibili_pattern, new Runnable() {
                        @Override
                        public void run() {
                            for (long group : properties.bilibili_subscribe.keySet()) {
                                if (b.getGroup(group) == null)
                                    continue;

                                if (!bilibiliLiveStatus.containsKey(group))
                                    bilibiliLiveStatus.put(group, new HashMap<>());

                                new BilibiliSender(b, group, bilibiliLiveStatus.get(group)).start();
                            }
                        }
                    }
            ));

            if (weibo_has_login) {
                handlerWeibo.setCronScheduleID(CronUtil.schedule(properties.weibo_pattern, new Runnable() {
                            @Override
                            public void run() {
                                for (long group : properties.weibo_user_subscribe.keySet()) {
                                    if (b.getGroup(group) == null)
                                        continue;

                                    if (!weiboEndTime.containsKey(group))
                                        weiboEndTime.put(group, new HashMap<>());

                                    new WeiboSender(b, group, weiboEndTime.get(group)).start();
                                }
                            }
                        }
                ));
            }

            //微店订单播报
            CronUtil.schedule(properties.weidian_pattern_order, new Runnable() {
                        @Override
                        public void run() {
                            getLogger().info("10");
                            for (long group : properties.weidian_cookie.keySet()) {
                                if (b.getGroup(group) == null)
                                    continue;

                                if (!weidianEndTime.containsKey(group))
                                    weidianEndTime.put(group, new EndTime(new Date().getTime()));

                                new WeidianOrderSender(b, group, weidianEndTime.get(group), handlerWeidianSender).start();
                            }
                        }
                    }
            );

            //微店排名统计
            handlerWeidian.setCronScheduleID(CronUtil.schedule(properties.weidian_pattern_item, new Runnable() {
                        @Override
                        public void run() {
                            getLogger().info("5");
                            for (long group : properties.weidian_cookie.keySet()) {
                                if (b.getGroup(group) == null)
                                    continue;

                                new WeidianSender(b, group, handlerWeidianSender).start();
                            }
                        }
                    }
            ));
        }

        //------------------------------------------------
        if (properties.enable) {
            CronUtil.start();
        } else {
            //停止
        }
    }
}