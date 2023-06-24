package net.lawaxi.model;

import cn.hutool.core.date.DateTime;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;

import java.math.BigDecimal;

public class WeidianItemMessage implements WeidianMessage {
    public final long itemId;
    public final String name;
    protected Message message;
    public final WeidianBuyer[] buyers;
    public final int number;
    public final long amountTotal;
    public final long amountAverage;

    public WeidianItemMessage(long itemId, String name, Message message, WeidianBuyer[] buyers, int number, long amountTotal, long amountAverage) {
        this.itemId = itemId;
        this.name = name;
        this.message = message;
        this.buyers = buyers;
        this.number = number;
        this.amountTotal = amountTotal;
        this.amountAverage = amountAverage;
    }

    public WeidianItemMessage setMessage(Message message) {
        this.message = message;
        return this;
    }

    public WeidianItemMessage generateMessage(Image image, int pickAmount) {
        Message m = new PlainText(name + "\n");
        if (image != null) {
            m = m.plus(image);
        }

        if (amountTotal == 0) {
            return this.setMessage(m.plus("人数：0\n进度：0" +
                    "\n" + DateTime.now()));
        } else {
            return this.setMessage(m.plus("人数：" + number +
                    "\n进度：" + new BigDecimal(amountTotal).divide(new BigDecimal("100.0")).toPlainString() +
                    "\n均：" + new BigDecimal(amountAverage).divide(new BigDecimal("100.0")).toPlainString() +
                    "\n" + DateTime.now() +
                    "\n---------" + pickBuyer(buyers, pickAmount)));
        }
    }

    @Override
    public Message getMessage() {
        return this.message;
    }

    public static WeidianItemMessage construct(long itemId, String name, Image image, WeidianBuyer[] buyers, int pickAmount) {
        if (buyers == null) {
            return new WeidianItemMessage(itemId, name, null, null, 0, 0, 0);
        } else {
            long total = 0;
            for (WeidianBuyer buyer : buyers) {
                total += buyer.contribution;
            }
            return new WeidianItemMessage(
                    itemId, name,
                    null,
                    buyers,
                    buyers.length,
                    total,
                    total / buyers.length
            ).generateMessage(image, pickAmount);
        }


    }

    public static String pickBuyer(WeidianBuyer[] buyers, int amount) {
        if (amount <= 0)
            amount = buyers.length;

        String out = "";
        for (int i = 0; i < amount; i++) {
            if (buyers.length >= i + 1) {
                out += "\n" + (i + 1) + ". (" + new BigDecimal(buyers[i].contribution).divide(new BigDecimal("100.0")).toPlainString() + ")" + buyers[i].name;
            } else
                break;
        }
        return out;
    }
}
