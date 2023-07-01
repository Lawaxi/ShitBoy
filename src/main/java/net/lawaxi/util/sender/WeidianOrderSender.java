package net.lawaxi.util.sender;

import net.lawaxi.Shitboy;
import net.lawaxi.handler.WeidianHandler;
import net.lawaxi.handler.WeidianSenderHandler;
import net.lawaxi.model.*;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.message.data.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WeidianOrderSender extends Sender {
    private final EndTime endTime;
    private final WeidianSenderHandler handler;
    private final HashMap<WeidianCookie, WeidianOrder[]> cache;

    public WeidianOrderSender(Bot bot, long group, EndTime endTime, WeidianSenderHandler handler, HashMap<WeidianCookie, WeidianOrder[]> cache) {
        super(bot, group);
        this.endTime = endTime;
        this.handler = handler;
        this.cache = cache;
    }

    @Override
    public void run() {
        WeidianHandler weidian = Shitboy.INSTANCE.getHandlerWeidian();
        WeidianCookie cookie = Shitboy.INSTANCE.getProperties().weidian_cookie.get(group_id);

        if (!cache.containsKey(cookie)) {
            cache.put(cookie, weidian.getOrderList(cookie, endTime));
        }

        WeidianOrder[] orders = cache.get(cookie);
        if (orders == null || bot == null)
            return;

        List<WeidianMessage> messages = new ArrayList<>();
        List<Long> itemIDs = new ArrayList<>();
        HashMap<Long, WeidianBuyer[]> itemBuyers = new HashMap<>();

        for (int i = orders.length - 1; i >= 0; i--) {
            long id = orders[i].itemID;
            if (!itemIDs.contains(id))
                itemIDs.add(id);
            //订单信息
            messages.add(handler.executeOrderMessage(orders[i], group));
        }

        WeidianItem[] items = weidian.getItems(cookie);
        for (Long id : itemIDs) {
            WeidianItem item = weidian.searchItem(items, id);
            if (item != null) {
                if (cookie.highlightItem.contains(id)) {//特殊链
                    itemBuyers.put(id, weidian.getItemBuyer(cookie, id));
                } else { //普链
                    WeidianItemMessage itemMessage = handler.executeItemMessages(item, group);
                    messages.add(itemMessage); //普链商品信息附在最后
                    itemBuyers.put(id, itemMessage.buyers);
                }
            }
        }

        List<Message> messages1 = new ArrayList<>();
        for (WeidianMessage message : messages) {
            if (message instanceof WeidianOrderMessage) {
                long id = ((WeidianOrderMessage) message).itemId;
                messages1.add(((WeidianOrderMessage) message).getMessage(itemBuyers.get(id)));
            } else {
                messages1.add(message.getMessage());
            }
        }

        Message t = combine(messages1);
        if (t != null)
            group.sendMessage(t);
    }

}
