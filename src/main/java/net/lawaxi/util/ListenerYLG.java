package net.lawaxi.util;

import cn.hutool.extra.pinyin.engine.pinyin4j.Pinyin4jEngine;
import net.lawaxi.Shitboy;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageRecallEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;

import java.util.ArrayList;
import java.util.List;

public class ListenerYLG extends SimpleListenerHost {

    private final Pinyin4jEngine engine = new Pinyin4jEngine();
    private final List<xenonMessage> xenon = new ArrayList<>();

    @EventHandler()
    public ListeningStatus onGroupMessage(GroupMessageEvent event) {
        Member sender = event.getSender();
        Group group = event.getGroup();
        String message = event.getMessage().contentToString();

        if (sender instanceof NormalMember) {
            long qqID = sender.getId();

            //单推人
            if (qqID == 1004297982L) {
                String t = engine.getPinyin(message, " ");
                if (t.indexOf("wo wai fu") != -1 || t.indexOf("wo lao po") != -1)
                    group.sendMessage("傻逼");
            }

            //小豆芽
            if (qqID == 2901878527L) {
                if (message.toLowerCase().indexOf("zf") != -1 || message.indexOf("朕妃") != -1)
                    group.sendMessage("傻逼");
            }

            //gethigher
            if (qqID == 2080539637 && group.getId() == 755732123) {
                if (xenon.size() == 5)
                    xenon.remove(0);
                xenon.add(new xenonMessage(event.getTime(), message));
            }

            //群cp——单糖&乌冬面
            if (qqID == 2901878527L || qqID == 1004297982L || qqID == 1145572010L || qqID == 2432980874L) {
                for (Message m : event.getMessage()) {
                    if (m instanceof At) {
                        if ((qqID == 2901878527L && ((At) m).getTarget() == 1004297982L)//单糖
                                || (qqID == 1004297982L && ((At) m).getTarget() == 2901878527L)
                                || (qqID == 1145572010L && ((At) m).getTarget() == 2432980874L)//乌冬面
                                || (qqID == 2432980874L && ((At) m).getTarget() == 1145572010L)) {
                            group.sendMessage("祝福");
                        }
                    }
                }
            }

        }

        if (message.equals("查群主") && group.getId() == 755732123) {
            String o = "";
            for (xenonMessage m : xenon) {
                o += "· " + m.message + "\n";
            }
            group.sendMessage(
                    new At(sender.getId()).plus("群主最近五条消息:\n" + o));
        }

        return ListeningStatus.LISTENING;
    }

    //群主实时撤回播报
    @EventHandler()
    public ListeningStatus onGroupRecall(MessageRecallEvent.GroupRecall event) {
        if (event.getGroup().getId() == 755732123 && event.getAuthorId() == 2080539637) {
            for (xenonMessage m : xenon) {
                if (m.time == (int) event.getMessageTime()) {
                    event.getGroup().sendMessage("【群主刚刚撤回了】\n" + m.message);
                    break;
                }
            }
        }

        return ListeningStatus.LISTENING;
    }

    private class xenonMessage {
        public final int time;
        public final String message;

        public xenonMessage(int time, String message) {
            this.time = time;
            this.message = message;
        }
    }
}
