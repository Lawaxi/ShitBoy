package net.lawaxi.handler;

import cn.hutool.core.date.DateTime;
import cn.hutool.http.HttpRequest;
import net.lawaxi.Shitboy;
import net.lawaxi.model.WeidianBuyer;
import net.lawaxi.model.WeidianCookie;
import net.lawaxi.model.WeidianItem;
import net.lawaxi.model.WeidianOrder;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.InputStream;
import java.math.BigDecimal;

public class WeidianSenderHandler {

    public WeidianSenderHandler() {
    }

    public InputStream getRes(String resLoc) {
        return HttpRequest.get(resLoc).execute().bodyStream();
    }

    //普链订单播报, pickAmount = 5
    public Message executeItemMessages(WeidianItem item, Group group) {
        return this.executeItemMessages(item, group, 5);
    }

    public Message executeItemMessages(WeidianItem item, Group group, int pickAmount) {
        WeidianHandler weidian = Shitboy.INSTANCE.getHandlerWeidian();
        WeidianCookie cookie = Shitboy.INSTANCE.getProperties().weidian_cookie.get(group.getId());
        //统计总值
        long id = item.id;
        int total = 0;
        WeidianBuyer[] buyers = weidian.getItemBuyer(cookie, id);

        Message m = new PlainText(item.name + "\n");
        if (!item.pic.equals("")) {
            try {
                m = m.plus(group.uploadImage(ExternalResource.create(getRes(item.pic))));
            } catch (Exception e) {
            }
        }

        //无人购买
        if (buyers == null || buyers.length == 0)
            return m.plus("人数：0\n进度：0" +
                    "\n" + DateTime.now());

        //有人购买
        for (WeidianBuyer buyer : buyers) {
            total += buyer.contribution;
        }
        return m.plus("人数：" + buyers.length +
                "\n进度：" + new BigDecimal(total).divide(new BigDecimal("100.0")).toPlainString() +
                "\n均：" + new BigDecimal(total / buyers.length).divide(new BigDecimal("100.0")).toPlainString() +
                "\n" + DateTime.now() +
                "\n---------" + pickBuyer(buyers, pickAmount));
    }

    private String pickBuyer(WeidianBuyer[] buyers, int amount) {
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

    public Message executeOrderMessage(WeidianOrder order, Group group) {
        return new PlainText("感谢" + order.buyerName + "在" + order.itemName + "中支持了" + order.price + "！"
                + "\n" + order.getPayTimeStr());
    }

}
