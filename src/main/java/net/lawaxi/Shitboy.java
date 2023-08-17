package net.lawaxi;

import cn.hutool.cron.Scheduler;
import net.lawaxi.command.ShitBoyCommand;
import net.lawaxi.handler.*;
import net.lawaxi.model.EndTime;
import net.lawaxi.model.Pocket48SenderCache;
import net.lawaxi.model.WeidianCookie;
import net.lawaxi.model.WeidianOrder;
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

import java.util.HashMap;
import java.util.List;

public final class Shitboy extends JavaPlugin {
    public static final String ID = "net.lawaxi.shitboy";
    public static final String VERSION = "0.1.11-dev4";

    /*
    本项目是一个mirai-console插件 用于SNH48 Group应援群播报
    最初用于小团体群聊”YLG48“ 播报群友关注的xox 同时也有了ListenerYLG类中的特殊功能
    后来也用于扶贫一些底边应援群提供播报功能
    本插件支持多个应援群/同一应援群多个xox/多平台播报

     */
    /*
    欢迎学习代码或开发拓展插件
    基本思路：定时创建Sender对象异步进行播报 Sender对象调用Handler对象获取信息
    拓展插件可以通过替换Handler对象以达到提供不同的信息给Sender对象

     */

    public static final Shitboy INSTANCE = new Shitboy();
    private final ConfigOperator configOperator = new ConfigOperator();
    private final Properties properties = new Properties();
    public Pocket48Handler handlerPocket48;
    public BilibiliHandler handlerBilibili;
    public WeiboHandler handlerWeibo;
    public WeidianHandler handlerWeidian;
    public WeidianSenderHandler handlerWeidianSender;

    private Shitboy() {
        super(new JvmPluginDescriptionBuilder(ID, VERSION +
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

        //------------------------------------------------
        //服务

        //口袋48登录
        final boolean pocket48_has_login;
        if (!properties.pocket48_token.equals("")) {
            this.handlerPocket48.login(properties.pocket48_token, false);
            pocket48_has_login = true;
        } else if (!(properties.pocket48_account.equals("") || properties.pocket48_password.equals(""))) {
            pocket48_has_login = this.handlerPocket48.login(
                    properties.pocket48_account,
                    properties.pocket48_password
            );
        } else {
            pocket48_has_login = false;
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
        boolean finalWeibo_has_login = weibo_has_login;
        listenBroadcast(pocket48_has_login, finalWeibo_has_login);

        getLogger().info("Shit boy!");
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


    private void listenBroadcast(boolean pocket48_has_login, boolean weibo_has_login) {

        //endTime: 已发送房间消息的最晚时间
        HashMap<Long, HashMap<Long, Long>> pocket48RoomEndTime = new HashMap<>();
        HashMap<Long, HashMap<String, Long>> weiboEndTime = new HashMap<>(); //同时包含超话和个人(long -> String)
        HashMap<Long, HashMap<Integer, Long>> bilibiliEndTime = new HashMap<>();
        HashMap<Long, EndTime> weidianEndTime = new HashMap<>();
        //status: 上次检测的开播状态
        HashMap<Long, HashMap<Long, List<Long>>> pocket48VoiceStatus = new HashMap<>();
        HashMap<Long, HashMap<Integer, Boolean>> bilibiliLiveStatus = new HashMap<>();

        Scheduler sb = new Scheduler();

        //服务
        if (pocket48_has_login) {
            handlerPocket48.setCronScheduleID(sb.schedule(properties.pocket48_pattern, new Runnable() {
                        @Override
                        public void run() {
                            if (getHandlerPocket48().isLogin()) {
                                HashMap<Long, Pocket48SenderCache> cache = new HashMap();

                                for (Bot b : Bot.getInstances()) {
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
                            } else {
                                getLogger().warning("口袋48已退出登录，请在控制台使用指令\"/shitboy login <token>\"或\"/shitboy login <账号> <密码>\"登录");
                            }

                        }
                    }
            ));
        }


        handlerBilibili.setCronScheduleID(sb.schedule(properties.bilibili_pattern, new Runnable() {
                    @Override
                    public void run() {
                        for (Bot b : Bot.getInstances()) {
                            for (long group : properties.bilibili_subscribe.keySet()) {
                                if (b.getGroup(group) == null)
                                    continue;

                                if (!bilibiliLiveStatus.containsKey(group)) {
                                    bilibiliLiveStatus.put(group, new HashMap<>());
                                    bilibiliEndTime.put(group, new HashMap<>());
                                }

                                new BilibiliSender(b, group, bilibiliEndTime.get(group), bilibiliLiveStatus.get(group)).start();
                            }
                        }
                    }
                }
        ));

        if (weibo_has_login) {
            handlerWeibo.setCronScheduleID(sb.schedule(properties.weibo_pattern, new Runnable() {
                        @Override
                        public void run() {
                            for (Bot b : Bot.getInstances()) {
                                for (long group : properties.weibo_user_subscribe.keySet()) {
                                    if (b.getGroup(group) == null)
                                        continue;

                                    if (!weiboEndTime.containsKey(group))
                                        weiboEndTime.put(group, new HashMap<>());

                                    new WeiboSender(b, group, weiboEndTime.get(group)).start();
                                }
                            }
                        }
                    }
            ));
        }

        //微店订单播报
        sb.schedule(properties.weidian_pattern_order, new Runnable() {
                    @Override
                    public void run() {

                        HashMap<WeidianCookie, WeidianOrder[]> cache = new HashMap();

                        for (Bot b : Bot.getInstances()) {
                            for (long group : properties.weidian_cookie.keySet()) {
                                if (!properties.weidian_cookie.get(group).doBroadcast)
                                    continue;

                                if (b.getGroup(group) == null)
                                    continue;

                                if (!weidianEndTime.containsKey(group))
                                    weidianEndTime.put(group, new EndTime());

                                new WeidianOrderSender(b, group, weidianEndTime.get(group), handlerWeidianSender, cache).start();
                            }
                        }

                        //机器人不在线/不播报也自动发货
                        for (long group : properties.weidian_cookie.keySet()) {
                            WeidianCookie cookie = properties.weidian_cookie.get(group);
                            if (cookie.autoDeliver && !cache.containsKey(cookie)) {
                                new WeidianOrderSender(null, group, new EndTime(), handlerWeidianSender, cache).start();
                            }
                        }
                    }
                }
        );

        //微店商品播报
        handlerWeidian.setCronScheduleID(sb.schedule(properties.weidian_pattern_item, new Runnable() {
                    @Override
                    public void run() {
                        for (Bot b : Bot.getInstances()) {
                            for (long group : properties.weidian_cookie.keySet()) {
                                if (b.getGroup(group) == null)
                                    continue;

                                new WeidianItemSender(b, group, handlerWeidianSender).start();
                            }
                        }
                    }
                }
        ));

        //------------------------------------------------
        if (properties.enable) {
            sb.start();
        } else {
            //停止
        }
    }
}