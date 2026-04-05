package com.gildedrose;

class BackstagePassUpdater implements ItemUpdater {

    @Override
    public void update(Item item) {
        if (item.sellIn <= 5) {
            item.quality = Math.min(50, item.quality + 3);
        } else if (item.sellIn <= 10) {
            item.quality = Math.min(50, item.quality + 2);
        } else {
            item.quality = Math.min(50, item.quality + 1);
        }

        item.sellIn -= 1;

        if (item.sellIn < 0) {
            item.quality = 0;
        }
    }
}
