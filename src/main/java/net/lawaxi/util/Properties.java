package net.lawaxi.util;

import net.lawaxi.model.Pocket48Subscribe;
import net.lawaxi.model.WeidianCookie;
import net.mamoe.mirai.utils.MiraiLogger;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class Properties {

    public MiraiLogger logger;
    public File configData;
    public Boolean enable = true;
    public Boolean ylg = true;
    public String[] admins;
    public String[] secureGroup;
    public HashMap<Long, String> welcome = new HashMap<>();

    //口袋48
    public String pocket48_pattern;
    public String pocket48_account;
    public String pocket48_password;
    public String pocket48_token;
    public HashMap<Long, Pocket48Subscribe> pocket48_subscribe = new HashMap<>();
    public HashMap<Long, Long> pocket48_serverID = new HashMap<>();//加密房间的severID记录

    //bilibili
    public String bilibili_pattern;
    public HashMap<Long, List<Integer>> bilibili_subscribe = new HashMap<>();

    //微博
    public String weibo_pattern;
    public HashMap<Long, List<Long>> weibo_user_subscribe = new HashMap<>();
    public HashMap<Long, List<String>> weibo_superTopic_subscribe = new HashMap<>();

    //微店
    public String weidian_pattern_order;
    public String weidian_pattern_item;
    public HashMap<Long, WeidianCookie> weidian_cookie = new HashMap<>();

}
