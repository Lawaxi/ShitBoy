package net.lawaxi.handler;

import cn.hutool.http.HttpRequest;
import net.lawaxi.Shitboy;
import net.lawaxi.model.WeidianBuyer;
import net.lawaxi.model.WeidianCookie;
import net.lawaxi.model.WeidianItem;
import net.lawaxi.model.WeidianOrder;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.InputStream;

public class WeidianSenderHandler {

    private final Bot bot;

    public WeidianSenderHandler(Bot bot) {
        this.bot = bot;
    }

    public Bot getBot() {
        return bot;
    }

    public InputStream getRes(String resLoc) {
        return HttpRequest.get(resLoc).execute().bodyStream();
    }

    public void executeItemMessages(WeidianItem item, Group group) {
        WeidianHandler weidian = Shitboy.INSTANCE.getHandlerWeidian();
        WeidianCookie cookie = Shitboy.INSTANCE.getProperties().weidian_cookie.get(group.getId());

        //统计总值
        long id = item.id;
        double total = 0;
        WeidianBuyer[] buyers = weidian.getItemBuyer(cookie, id);
        if (buyers == null || buyers.length == 0)
            return;

        for (WeidianBuyer buyer : buyers) {
            total += buyer.contribution;
        }

        //发送信息
        Message m = new PlainText("【微店链】\n" + item.name + "\n");
        if (!item.pic.equals("")) {
            try {
                m = m.plus(group.uploadImage(ExternalResource.create(getRes(item.pic))));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        group.sendMessage(m.plus("进度：" + total + "\n---------" + pickBuyer(buyers, 5)));
    }

    private String pickBuyer(WeidianBuyer[] buyers, int amount) {
        String out = "";
        for (int i = 0; i < amount; i++) {
            if (buyers.length >= i + 1) {
                out += "\n" + (i + 1) + ". (" + buyers[i].contribution + ")" + buyers[i].name;
            } else
                break;
        }
        return out;
    }

    public void executeOrderMessage(WeidianOrder order, Group group) {
        group.sendMessage("感谢" + order.buyerName + "在" + order.itemName + "中支持了" + order.price + "！");
    }

}
