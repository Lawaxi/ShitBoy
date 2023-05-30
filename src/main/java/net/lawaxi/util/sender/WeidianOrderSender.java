package net.lawaxi.util.sender;

import net.lawaxi.Shitboy;
import net.lawaxi.handler.WeidianHandler;
import net.lawaxi.handler.WeidianSenderHandler;
import net.lawaxi.model.EndTime;
import net.lawaxi.model.WeidianCookie;
import net.lawaxi.model.WeidianOrder;
import net.mamoe.mirai.Bot;

public class WeidianOrderSender extends Sender {
    private final EndTime endTime;
    private final WeidianSenderHandler handler;

    public WeidianOrderSender(Bot bot, long group, EndTime endTime, WeidianSenderHandler handler) {
        super(bot, group);
        this.endTime = endTime;
        this.handler = handler;
    }

    @Override
    public void run() {
        WeidianHandler weidian = Shitboy.INSTANCE.getHandlerWeidian();
        WeidianCookie cookie = Shitboy.INSTANCE.getProperties().weidian_cookie.get(group.getId());

        WeidianOrder[] orders = weidian.getOrderList(cookie, endTime);
        if (orders == null)
            return;

        for (WeidianOrder order : orders) {
            handler.executeOrderMessage(order, group);
        }
    }

}
