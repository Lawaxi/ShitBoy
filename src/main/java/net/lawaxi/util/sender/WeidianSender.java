package net.lawaxi.util.sender;

import net.lawaxi.Shitboy;
import net.lawaxi.handler.WeidianHandler;
import net.lawaxi.handler.WeidianSenderHandler;
import net.lawaxi.model.WeidianCookie;
import net.lawaxi.model.WeidianItem;
import net.mamoe.mirai.Bot;

public class WeidianSender extends Sender {

    private final WeidianSenderHandler handler;

    public WeidianSender(Bot bot, long group, WeidianSenderHandler handler) {
        super(bot, group);
        this.handler = handler;
    }


    @Override
    public void run() {
        WeidianHandler weidian = Shitboy.INSTANCE.getHandlerWeidian();
        WeidianCookie cookie = Shitboy.INSTANCE.getProperties().weidian_cookie.get(group.getId());

        WeidianItem[] items = weidian.getItems(cookie);
        if (items == null)
            return;

        for (WeidianItem item : items) {
            handler.executeItemMessages(item, group);
        }
    }


}
