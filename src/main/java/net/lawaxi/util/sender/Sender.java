package net.lawaxi.util.sender;

import cn.hutool.http.HttpRequest;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;

import java.io.InputStream;

public class Sender extends Thread { //异步进程
    public final Bot bot;
    public final Group group;

    public Sender(Bot bot, long group) {
        this.bot = bot;
        this.group = bot.getGroup(group);
    }

    public Sender(Bot bot, Group group) {
        this.bot = bot;
        this.group = group;
    }

    public InputStream getRes(String resLoc) {
        return HttpRequest.get(resLoc).execute().bodyStream();
    }
}
