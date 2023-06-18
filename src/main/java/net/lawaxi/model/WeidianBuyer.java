package net.lawaxi.model;

import java.math.BigDecimal;

public class WeidianBuyer {
    public final long id;
    public final String name;
    public int contribution; //以分为单位

    public WeidianBuyer(long id, String name) {
        this.id = id;
        this.name = name;
        this.contribution = 0;
    }

    public WeidianBuyer(long id, String name, int contribution) {
        this.id = id;
        this.name = name;
        this.contribution = contribution;
    }

    public void add(int contribution) {
        this.contribution += contribution;
    }

    public double getContributionYuan() {
        return new BigDecimal(contribution).divide(new BigDecimal(100)).doubleValue();
    }
}
