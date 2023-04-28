package net.lawaxi.model;

public class WeidianOrder {
    public final long itemID;
    public final String itemName;
    public final long buyerID;
    public final String buyerName;
    public final int price;
    public final String payTime;

    public WeidianOrder(long itemID, String itemName, long buyerID, String buyerName, int price, String payTime) {
        this.itemID = itemID;
        this.itemName = itemName;
        this.buyerID = buyerID;
        this.buyerName = buyerName;
        this.price = price;
        this.payTime = payTime;
    }
}
