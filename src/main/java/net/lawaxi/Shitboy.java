package net.lawaxi;

import cn.hutool.cron.CronUtil;
import net.lawaxi.command.ShitBoyCommand;
import net.lawaxi.util.ConfigOperator;
import net.lawaxi.util.Listener;
import net.lawaxi.util.ListenerYLG;
import net.lawaxi.util.handler.BilibiliHandler;
import net.lawaxi.util.handler.Pocket48Handler;
import net.lawaxi.util.handler.WeiboHandler;
import net.lawaxi.util.sender.BilibiliSender;
import net.lawaxi.util.sender.Pocket48Sender;
import net.lawaxi.util.sender.WeiboSender;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.permission.AbstractPermitteeId;
import net.mamoe.mirai.console.permission.PermissionId;
import net.mamoe.mirai.console.permission.PermissionService;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.BotOnlineEvent;

import java.util.HashMap;


public final class Shitboy extends JavaPlugin {
    public static final Shitboy INSTANCE = new Shitboy();
    private final ConfigOperator configOperator = new ConfigOperator();
    private final Properties properties = new Properties();
    private Pocket48Handler handlerPocket48;
    private BilibiliHandler handlerBilibili;
    private WeiboHandler handlerWeibo;

    private Shitboy() {
        super(new JvmPluginDescriptionBuilder("net.lawaxi.shitboy", "0.1.5-test8" +
                "")
                .name("shitboy")
                .author("delay")
                .info("易拉罐人日常刚需")
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
    }

    private void initProperties() {
        properties.configData = resolveConfigFile(PropertiesCommon.configDataName);
        properties.logger = getLogger();
        handlerPocket48 = new Pocket48Handler();
        handlerBilibili = new BilibiliHandler();
        handlerWeibo = new WeiboHandler();
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
        this.handlerPocket48.login(
                properties.pocket48_account,
                properties.pocket48_password
        );

        try {
            this.handlerWeibo.updateLoginToSuccess();
            getLogger().info("微博Cookie更新成功");

        } catch (Exception e) {
            getLogger().info("微博Cookie更新失败");
        }

        //服务

        //endTime: 已发送房间消息的最晚时间 & status: 上次检测的开播状态
        HashMap<Long, HashMap<Integer, Long>> pocket48_room = new HashMap<>();
        HashMap<Long, HashMap<Integer, Boolean>> bilibili_live = new HashMap<>();
        HashMap<Long, HashMap<String, Long>> weibo = new HashMap<>(); //同时包含超话和个人(long -> String)

        //服务
        for (Bot b : Bot.getInstances()) {
            handlerPocket48.setCronScheduleID(CronUtil.schedule("*/5 * * * * *", new Runnable() {
                        @Override
                        public void run() {
                            for (long group : properties.pocket48_subscribe.keySet()) {
                                if (!pocket48_room.containsKey(group))//放到Runnable里面是因为可能实时更新新的群
                                    pocket48_room.put(group, new HashMap<>());

                                new Pocket48Sender(b, group, pocket48_room.get(group)).start();

                            }

                        }
                    }
            ));

            handlerBilibili.setCronScheduleID(CronUtil.schedule("* * * * * *", new Runnable() {
                        @Override
                        public void run() {
                            for (long group : properties.bilibili_subscribe.keySet()) {
                                if (!bilibili_live.containsKey(group))
                                    bilibili_live.put(group, new HashMap<>());

                                new BilibiliSender(b, group, bilibili_live.get(group)).start();
                            }
                        }
                    }
            ));

            handlerWeibo.setCronScheduleID(CronUtil.schedule("*/5 * * * * *", new Runnable() {
                        @Override
                        public void run() {
                            for (long group : properties.weibo_user_subscribe.keySet()) {
                                if (!weibo.containsKey(group))
                                    weibo.put(group, new HashMap<>());

                                new WeiboSender(b, group, weibo.get(group)).start();
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