package net.lawaxi.util;

import cn.hutool.http.HttpUtil;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                        || message.indexOf("wives") != -1 || message.indexOf("外敷") != -1))
                    group.sendMessage("傻逼");

                //在单推人发布错误详情时指正
                if (message.indexOf("b23.tv") != -1) {
                    Matcher matcher = Pattern.compile("(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]").matcher(message);
                    while (matcher.find()) {
                        try {
                            String a1 = "<title data-vue-meta=\"true\">";
                            String a2 = "_哔哩哔哩_bilibili</title>";
                            String info = HttpUtil.get(matcher.group());
                            String title = info.substring(info.indexOf(a1) + a1.length(), info.indexOf(a2));
                            group.sendMessage("真实标题为：" + title);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            //群主
            if(qqID == 2080539637){
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

        if(message.equals("查群主")){
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
