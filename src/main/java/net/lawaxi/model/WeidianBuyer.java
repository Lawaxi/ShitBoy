package net.lawaxi.model;

public class WeidianBuyer {
    public final long id;
    public final String name;
    public double contribution;

    public WeidianBuyer(long id, String name) {
        this.id = id;
        this.name = name;
        this.contribution = 0;
    }

    public WeidianBuyer(long id, String name, double contribution) {
        this.id = id;
        this.name = name;
        this.contribution = contribution;
    }

    public void add(double contribution) {
        this.contribution += contribution;
    }
}
