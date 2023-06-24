package net.lawaxi.handler;

import cn.hutool.http.HttpRequest;
import net.lawaxi.Shitboy;
import net.lawaxi.model.*;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.InputStream;

public class WeidianSenderHandler {

    public WeidianSenderHandler() {
    }

    public InputStream getRes(String resLoc) {
        return HttpRequest.get(resLoc).execute().bodyStream();
    }

    //普链订单播报, pickAmount = 5
    public WeidianItemMessage executeItemMessages(WeidianItem item, Group group) {
        return this.executeItemMessages(item, group, 5);
    }

    public WeidianItemMessage executeItemMessages(WeidianItem item, Group group, int pickAmount) {
        WeidianHandler weidian = Shitboy.INSTANCE.getHandlerWeidian();
        WeidianCookie cookie = Shitboy.INSTANCE.getProperties().weidian_cookie.get(group.getId());

        //统计总值
        long id = item.id;
        WeidianBuyer[] buyers = weidian.getItemBuyer(cookie, id);

        Image image = null;
        if (!item.pic.equals("")) {
            try {
                image = group.uploadImage(ExternalResource.create(getRes(item.pic)));
            } catch (Exception e) {
            }
        }

        return WeidianItemMessage.construct(item.id, item.name, image, buyers, pickAmount);
    }

    public WeidianOrderMessage executeOrderMessage(WeidianOrder order, Group group) {
        return WeidianOrderMessage.construct(order);
    }

}
