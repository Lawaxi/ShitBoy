package net.lawaxi.handler;

import cn.hutool.http.HttpRequest;
import net.lawaxi.Shitboy;
import net.lawaxi.model.WeidianBuyer;
import net.lawaxi.model.WeidianCookie;
import net.lawaxi.model.WeidianItem;
import net.lawaxi.model.WeidianOrder;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.InputStream;

public class WeidianSenderHandler {

    public WeidianSenderHandler() {
    }

    public InputStream getRes(String resLoc) {
        return HttpRequest.get(resLoc).execute().bodyStream();
    }

    public Message executeItemMessages(WeidianItem item, Group group) {
        WeidianHandler weidian = Shitboy.INSTANCE.getHandlerWeidian();
        WeidianCookie cookie = Shitboy.INSTANCE.getProperties().weidian_cookie.get(group.getId());
        return getItemMessage(item, cookie, 5, group);
    }

    private String pickBuyer(WeidianBuyer[] buyers, int amount) {
        if (amount <= 0)
            amount = buyers.length;

        String out = "";
        for (int i = 0; i < amount; i++) {
            if (buyers.length >= i + 1) {
                out += "\n" + (i + 1) + ". (" + buyers[i].contribution + ")" + buyers[i].name;
            } else
                break;
        }
        return out;
    }

    public Message executeOrderMessage(WeidianOrder order, Group group) {
        return new PlainText("感谢" + order.buyerName + "在" + order.itemName + "中支持了" + order.price + "！");
    }

    public Message getItemMessage(WeidianItem item, WeidianCookie cookie, int pickAmount, Contact contact) {
        WeidianHandler weidian = Shitboy.INSTANCE.getHandlerWeidian();
        //统计总值
        long id = item.id;
        double total = 0;
        WeidianBuyer[] buyers = weidian.getItemBuyer(cookie, id);
        if (buyers == null || buyers.length == 0)
            return null;

        for (WeidianBuyer buyer : buyers) {
            total += buyer.contribution;
        }

        //double精度修正
        total = (double) Math.round(total * 100) / 100;

        //发送信息
        Message m = new PlainText(item.name + "\n");
        if (!item.pic.equals("")) {
            try {
                m = m.plus(contact.uploadImage(ExternalResource.create(getRes(item.pic))));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return m.plus(
                "人数：" + buyers.length +
                        "\n进度：" + total +
                        "\n均：" + (double) Math.round((total / buyers.length) * 100) / 100 +
                        "\n---------" + pickBuyer(buyers, pickAmount));
    }

}
