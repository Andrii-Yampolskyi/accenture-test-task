package com.gildedrose;

import java.util.HashMap;
import java.util.Map;

public class GildedRose {
    private final Item[] items;

    private final Map<String, ItemUpdater> itemUpdaters = new HashMap<>();
    private final ItemUpdater defaultItemUpdater = new NormalItemUpdater();

    public GildedRose(Item[] items) {
        this.items = items;
        itemUpdaters.put("Aged Brie", new AgedBrieUpdater());
        itemUpdaters.put("Sulfuras, Hand of Ragnaros", new SulfurasUpdater());
        itemUpdaters.put("Backstage passes to a TAFKAL80ETC concert", new BackstagePassUpdater());
        itemUpdaters.put("Conjured Mana Cake", new ConjuredItemUpdater());
    }

    public void updateQuality() {
        for (Item item : items) {
            resolveUpdater(item).update(item);
        }
    }

    private ItemUpdater resolveUpdater(Item item) {
        return itemUpdaters.getOrDefault(item.name, defaultItemUpdater);
    }
}
