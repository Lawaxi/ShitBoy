package net.lawaxi.util.sender;

import cn.hutool.http.HttpRequest;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.message.data.AtAll;
import net.mamoe.mirai.message.data.Message;

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

    public Message toNotification(Message m) {
        if (this.group.getBotAsMember().getPermission() == MemberPermission.ADMINISTRATOR)
            return AtAll.INSTANCE.plus("\n").plus(m);
        return m;
    }
}
