package net.lawaxi;

import net.lawaxi.model.Pocket48Subscribe;
import net.mamoe.mirai.utils.MiraiLogger;

import java.io.File;
import java.util.*;

public class Properties {

    public MiraiLogger logger;
    public File configData;
    public Boolean enable = true;

    public String [] admins;

    public String pocket48_account;
    public String pocket48_password;
    public HashMap<String, Pocket48Subscribe> pocket48_subscribe = new HashMap<>();
}
