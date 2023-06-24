package net.lawaxi.model;

import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;

public class WeidianOrderMessage implements WeidianMessage {
    protected Message message;
    public final String name_buyer;
    public final long buyerId;
    public final long itemId;
    public final String name_item;
    public final double price;
    public final String payTime;

    public WeidianOrderMessage(long buyerId, long itemId, String nameBuyer, String nameItem, Message message, double price, String payTime) {
        this.message = message;
        name_buyer = nameBuyer;
        this.buyerId = buyerId;
        this.itemId = itemId;
        name_item = nameItem;
        this.price = price;
        this.payTime = payTime;
    }

    public WeidianOrderMessage setMessage(Message message) {
        this.message = message;
        return this;
    }

    public WeidianOrderMessage generateMessage() {
        return this.setMessage(new PlainText("感谢" + name_buyer + "在" + name_item + "中支持了" + price + "！"
                + "\n" + payTime));
    }

    @Override
    public Message getMessage() {
        return this.message;
    }


    public Message getMessage(WeidianBuyer[] buyers) {
        for (int i = 0; i < buyers.length; i++) {
            if (this.buyerId == buyers[i].id) {
                return this.message.plus("\n当前总计：" + buyers[i].contribution + " 排名：" + (i + 1));
            }
        }
        return this.message;
    }

    public static WeidianOrderMessage construct(WeidianOrder order) {
        return new WeidianOrderMessage(
                order.buyerID,
                order.itemID,
                order.buyerName,
                order.itemName,
                null,
                order.price,
                order.getPayTimeStr()
        ).generateMessage();
    }

}
