package net.lawaxi.util.sender;

import cn.hutool.json.JSONObject;
import net.lawaxi.Shitboy;
import net.lawaxi.util.handler.BilibiliHandler;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class BilibiliSender extends Sender {
    private static final String roomUrl = "https://live.bilibili.com/";

    private final HashMap<Integer, Boolean> status;

    public BilibiliSender(Bot bot, long group, HashMap<Integer, Boolean> status) {
        super(bot, group);
        this.status = status;
    }

    @Override
    public void run() {
        BilibiliHandler bili = Shitboy.INSTANCE.getHandlerBilibili();
        List<Integer> subscribe = Shitboy.INSTANCE.getProperties().bilibili_subscribe.get(group.getId());

        for (Integer room : subscribe) {
            JSONObject info = bili.shouldMention(room, status);
            if (info != null) {
                String title = info.getStr("title");
                String description = info.getStr("description");
                String cover = info.getStr("user_cover");
                String name = bili.getNameByMid(info.getInt("uid"));

                try {
                    group.sendMessage(toNotification(new PlainText("【" + name + "开播啦~】\n" + title)
                            .plus(group.uploadImage(ExternalResource.create(getRes(cover))))
                            .plus(new PlainText(roomUrl + room))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
