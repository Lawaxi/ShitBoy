package net.lawaxi.model;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;

public class WeidianOrder {
    public final long itemID;
    public final String itemName;
    public final long buyerID;
    public final String buyerName;
    public final double price;
    private final String payTime;

    public WeidianOrder(long itemID, String itemName, long buyerID, String buyerName, double price, String payTime) {
        this.itemID = itemID;
        this.itemName = itemName;
        this.buyerID = buyerID;
        this.buyerName = buyerName;
        this.price = price;
        this.payTime = payTime;
    }

    public String getPayTimeStr() {
        return payTime;
    }

    public DateTime getPayTime() {
        return DateUtil.parse(payTime);
    }
}
