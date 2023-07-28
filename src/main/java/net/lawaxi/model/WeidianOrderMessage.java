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
        return this.setMessage(new PlainText("感谢" + name_buyer + "在" + name_item + "中支持了" + price + "！"));
    }

    @Override
    public Message getMessage() {
        return this.message;
    }


    public Message getMessage(WeidianBuyer[] buyers) {
        for (int i = 0; i < buyers.length; i++) {
            if (this.buyerId == buyers[i].id) {
                //向前找
                int j = i;
                while (j >= 0 && buyers[j].contribution == buyers[i].contribution) {
                    j--; //排名（0起）为j+1
                }

                //向后找
                boolean tied = j != i - 1;
                if (!tied) {
                    if (buyers.length > j + 2) {
                        tied = buyers[j + 1].contribution == buyers[j + 2].contribution;
                    }
                }

                return this.message.plus("\n共" + (buyers[i].contribution / 100.0) + " 排名" + (tied ? "并列" : "") + (j + 2)
                        + (i == 0 ? "" : " 距离上一名" + (buyers[j].contribution - buyers[j + 1].contribution) / 100.0)
                        + "\n" + payTime);
            }
        }
        return this.message.plus("\n" + payTime);
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
