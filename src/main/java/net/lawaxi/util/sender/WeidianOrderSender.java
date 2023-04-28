package net.lawaxi.util.sender;

import net.lawaxi.Shitboy;
import net.lawaxi.model.EndTime;
import net.lawaxi.model.WeidianCookie;
import net.lawaxi.model.WeidianOrder;
import net.lawaxi.util.handler.WeidianHandler;
import net.mamoe.mirai.Bot;

public class WeidianOrderSender extends Sender {
    private final EndTime endTime;

    public WeidianOrderSender(Bot bot, long group, EndTime endTime) {
        super(bot, group);
        this.endTime = endTime;
    }

    @Override
    public void run() {
        WeidianHandler weidian = Shitboy.INSTANCE.getHandlerWeidian();
        WeidianCookie cookie = Shitboy.INSTANCE.getProperties().weidian_cookie.get(group.getId());

        for (WeidianOrder order : weidian.getOrderList(cookie, endTime)) {
            group.sendMessage("感谢" + order.buyerName + "在" + order.itemName + "中支持了" + order.price + "！");
        }
    }

}
