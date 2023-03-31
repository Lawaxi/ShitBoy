package net.lawaxi;

import net.lawaxi.model.Pocket48Subscribe;
import net.mamoe.mirai.utils.MiraiLogger;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class Properties {

    public MiraiLogger logger;
    public File configData;
    public Boolean enable = true;
    public String[] admins;

    //pocket48
    public String pocket48_account;
    public String pocket48_password;
    public HashMap<Long, Pocket48Subscribe> pocket48_subscribe = new HashMap<>();
    public HashMap<Integer,Integer> pocket48_serverID = new HashMap<>();//加密房间的severID记录

    //bilibili
    public HashMap<Long, List<Integer>> bilibili_subscribe = new HashMap<>();
}
