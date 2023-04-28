package net.lawaxi.model;

import java.util.ArrayList;
import java.util.List;

public class WeidianItem {
    public final long id;
    public final String name;
    public final String pic;
    public List<WeidianItemSku> skus;

    public WeidianItem(long id, String name, String pic) {
        this.id = id;
        this.name = name;
        this.pic = pic;
    }

    public WeidianItem addSkus(long id, String name, String pic) {
        if (this.skus == null)
            this.skus = new ArrayList<>();

        this.skus.add(new WeidianItemSku(id, name, pic));
        return this;
    }


    public class WeidianItemSku {
        public final long id;
        public final String title;
        public final String pic;

        public WeidianItemSku(long id, String title, String pic) {
            this.id = id;
            this.title = title;
            this.pic = pic;
        }
    }
}
