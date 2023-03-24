package net.lawaxi;

import cn.hutool.cron.CronUtil;
import net.lawaxi.command.ShitBoyCommand;
import net.lawaxi.util.Pocket48Handler;
import net.lawaxi.util.ConfigOperator;
import net.lawaxi.util.Pocket48TimeTask;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;

import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.permission.AbstractPermitteeId;
import net.mamoe.mirai.console.permission.PermissionId;
import net.mamoe.mirai.console.permission.PermissionService;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.BotOnlineEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


public final class Shitboy extends JavaPlugin {
    public static final Shitboy INSTANCE = new Shitboy();
    private ConfigOperator configOperator = new ConfigOperator();
    private Properties properties = new Properties();
    private Pocket48Handler handlerPocket48;

    private Shitboy() {
        super(new JvmPluginDescriptionBuilder("net.lawaxi.shitboy", "0.1.0")
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

        getLogger().info("Shit boy!");
        GlobalEventChannel.INSTANCE.parentScope(INSTANCE).subscribeOnce(BotOnlineEvent.class, event -> {
            listenBroadcast();
        });
    }

    private void initProperties() {
        properties.configData = resolveConfigFile(PropertiesCommon.configDataName);
        properties.logger = getLogger();
        handlerPocket48 = new Pocket48Handler(properties);
    }

    private void registerCommand() {
        CommandManager.INSTANCE.registerCommand(new ShitBoyCommand(),false);
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

    private void loadConfig(){
        configOperator.load(properties);
    }

    public void registerPermission() {
        //将配置中的管理员字符数组转成权限被许可人列表
        List<AbstractPermitteeId.ExactUser> users = Arrays.stream(properties.admins)
                .map(admin-> new AbstractPermitteeId.ExactUser(Long.parseLong(admin)))
                .collect(Collectors.toList());
        //获取插件对应的权限
        PermissionId permissionId = this.getParentPermission().getId();
        //将为注册的管理员设置权限
        users.forEach(user->{
            if (!PermissionService.hasPermission(user,permissionId)) {
                PermissionService.permit(user,permissionId);
            }
        });
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
            for(String group : properties.pocket48_subscribe.keySet()) {

                HashMap<Integer,Long> endTime = new HashMap<>();//获取房间消息的最晚时间
                handlerPocket48.setCronScheduleID(CronUtil.schedule("* * * * * *", new Runnable() {
                            @Override
                            public void run() {
                                new Pocket48TimeTask(b, group,endTime).start();
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