package net.lawaxi.util;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;

import java.util.ArrayList;
import java.util.List;

public class ListenerYLG extends SimpleListenerHost {

    private final List<String> xenon = new ArrayList<>();
    @EventHandler()
    public ListeningStatus onGroupMessage(GroupMessageEvent event) {
        Member sender = event.getSender();
        Group group = event.getGroup();
        String message = event.getMessage().contentToString();

        if (sender instanceof NormalMember) {
            long qqID = sender.getId();
            if (qqID == 1004297982L) {
                //在单推人发送如何评价我wife时回复傻逼
                if ((message.indexOf("wife") != -1
                        || message.indexOf("wives") != -1
                        || message.indexOf("外敷") != -1
                        || message.indexOf("老婆") != -1))
                    group.sendMessage("傻逼");
            }

            //群主
            if(qqID == 2080539637 && group.getId() == 755732123){
                if(xenon.size()==5)
                    xenon.remove(0);
                xenon.add(message);
            }

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

        if(message.equals("查群主") && group.getId() == 755732123){
            String o = "";
            for(String m : xenon){
                o+="· "+m+"\n";
            }
            group.sendMessage(
                    new At(sender.getId()).plus("机器人自动保存群主前五条消息\n"+o));
        }

        return ListeningStatus.LISTENING;
    }
}
