package net.lawaxi.util.sender;

import net.lawaxi.Shitboy;
import net.lawaxi.model.WeidianBuyer;
import net.lawaxi.model.WeidianCookie;
import net.lawaxi.model.WeidianItem;
import net.lawaxi.util.handler.WeidianHandler;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

public class WeidianSender extends Sender {

    public WeidianSender(Bot bot, long group) {
        super(bot, group);
    }


    @Override
    public void run() {
        WeidianHandler weidian = Shitboy.INSTANCE.getHandlerWeidian();
        WeidianCookie cookie = Shitboy.INSTANCE.getProperties().weidian_cookie.get(group.getId());

        WeidianItem[] items = weidian.getItems(cookie);
        if (items == null)
            return;

        for (WeidianItem item : items) {
            long id = item.id;
            double total = 0;
            WeidianBuyer[] buyers = weidian.getItemBuyer(cookie, id);
            if (buyers == null || buyers.length == 0)
                continue;

            for (WeidianBuyer buyer : buyers) {
                total += buyer.contribution;
            }

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
}
