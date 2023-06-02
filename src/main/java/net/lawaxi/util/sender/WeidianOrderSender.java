package net.lawaxi.util.sender;

import net.lawaxi.Shitboy;
import net.lawaxi.handler.WeidianHandler;
import net.lawaxi.handler.WeidianSenderHandler;
import net.lawaxi.model.EndTime;
import net.lawaxi.model.WeidianCookie;
import net.lawaxi.model.WeidianItem;
import net.lawaxi.model.WeidianOrder;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.message.data.Message;

import java.util.ArrayList;
import java.util.List;

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

        //合并发送（将普链ItemMessages附在最后）
        List<Message> messages = new ArrayList<>();
        List<Long> itemIDs = new ArrayList<>();
        for (int i = 0; i < orders.length; i++) {
            long id = orders[i].itemID;
            if (!itemIDs.contains(id) && !cookie.highlightItem.contains(id))
                itemIDs.add(id);
            messages.add(handler.executeOrderMessage(orders[i], group));
        }
        if (itemIDs.size() > 0) {
            WeidianItem[] items = weidian.getItems(cookie);
            for (Long id : itemIDs) {
                WeidianItem item = weidian.searchItem(items, id);
                if (item != null)
                    messages.add(handler.executeItemMessages(item, group));
            }
        }

        Message t = combine(messages);
        if (t != null)
            group.sendMessage(t);
    }

}
