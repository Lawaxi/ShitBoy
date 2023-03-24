package net.lawaxi.util;

import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpUtil;
import net.lawaxi.Shitboy;
import net.lawaxi.model.Pocket48Message;
import net.lawaxi.model.Pocket48Subscribe;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

import java.util.HashMap;

public class Pocket48TimeTask extends Thread{
    private Bot bot;
    private String group;
    private HashMap<Integer,Long> endTime;

    public Pocket48TimeTask(Bot bot, String group, HashMap<Integer, Long> endTime) {
        this.bot = bot;
        this.group = group;
        this.endTime = endTime;
    }

    @Override
    public void run() {
        Pocket48Subscribe subscribe = Shitboy.INSTANCE.getProperties().pocket48_subscribe.get(group);
        Pocket48Handler pocket = Shitboy.INSTANCE.getHandlerPocket48();

        for(int roomID : subscribe.getRoomIDs()) {
            for (Pocket48Message message : pocket.getNewMessages(roomID, endTime)) {
                bot.getGroup(Long.parseLong(group)).sendMessage(pharseMessage(message));
            }
        }
    }

    public Message pharseMessage(Pocket48Message message){
        String name = "【"+message.getNickName() + "@"+ message.getRoom()+"】\n";
        switch (message.getType()){
            case TEXT:
                return new PlainText(name + message.getBody());
            case AUDIO:
                break;
            case IMAGE:
                break;
            case VIDEO:
                break;
            case EXPRESSIMAGE:
                break;
            case REPLY:
                break;
            case GIFTREPLY:
                break;
            case LIVEPUSH:
                break;
            case FLIPCARD:
                break;
            case FLIPCARD_AUDIO:
                break;
            case FLIPCARD_VIDEO:
                break;
            case PASSWORD_REDPACKAGE:
                break;
            case VOTE:
                break;
        }

        return new PlainText("不支持的消息");
    }
}
