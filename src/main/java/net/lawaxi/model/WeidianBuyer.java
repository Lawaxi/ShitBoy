package net.lawaxi.model;

public class WeidianBuyer {
    public final long id;
    public final String name;
    public int price;

    public WeidianBuyer(long id, String name) {
        this.id = id;
        this.name = name;
        this.price = 0;
    }

    public WeidianBuyer(long id, String name, int price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public void add(int price) {
        this.price += price;
    }
}
