package net.lawaxi;

import cn.hutool.cron.CronUtil;
import net.lawaxi.command.ShitBoyCommand;
import net.lawaxi.util.ConfigOperator;
import net.lawaxi.util.Listener;
import net.lawaxi.util.ListenerYLG;
import net.lawaxi.util.handler.BilibiliHandler;
import net.lawaxi.util.handler.Pocket48Handler;
import net.lawaxi.util.sender.BilibiliSender;
import net.lawaxi.util.sender.Pocket48Sender;
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

    private Shitboy() {
        super(new JvmPluginDescriptionBuilder("net.lawaxi.shitboy", "0.1.3-test13")
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
        GlobalEventChannel.INSTANCE.registerListenerHost(new ListenerYLG());
        GlobalEventChannel.INSTANCE.parentScope(INSTANCE).subscribeOnce(BotOnlineEvent.class, event -> {
            listenBroadcast();
        });

        getLogger().info("Shit boy!");
    }

    private void initProperties() {
        properties.configData = resolveConfigFile(PropertiesCommon.configDataName);
        properties.logger = getLogger();
        handlerPocket48 = new Pocket48Handler(properties);
        handlerBilibili = new BilibiliHandler(properties);
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

    private void loadConfig() {
        configOperator.load(properties);
    }

    public void registerPermission() {
        PermissionId permissionId = this.getParentPermission().getId();
        for(String a : properties.admins){
            AbstractPermitteeId.ExactUser user =  new AbstractPermitteeId.ExactUser(Long.parseLong(a));

            if (!PermissionService.hasPermission(user, permissionId)) {
                PermissionService.permit(user, permissionId);
            }
        }
    }


    private void listenBroadcast() {
        CronUtil.getScheduler().clear();
        //------------------------------------------------

        //口袋48
        this.handlerPocket48.login(
                properties.pocket48_account,
                properties.pocket48_password
        );

        for (Bot b : Bot.getInstances()) {
            for (long group : properties.pocket48_subscribe.keySet()) {

                HashMap<Integer, Long> endTime = new HashMap<>();//获取房间消息的最晚时间
                handlerPocket48.setCronScheduleID(CronUtil.schedule("*/5 * * * * *", new Runnable() {
                            @Override
                            public void run() {
                                new Pocket48Sender(b, group, endTime).start();
                            }
                        }
                ));
            }

            for (long group : properties.bilibili_subscribe.keySet()) {
                handlerBilibili.setCronScheduleID(CronUtil.schedule("*/2 * * * * *", new Runnable() {
                            @Override
                            public void run() {
                                new BilibiliSender(b, group).start();
                            }
                        }
                ));
            }
        }

        //------------------------------------------------
        if (properties.enable) {
            CronUtil.start();
        } else {
            //停止
        }
    }
}