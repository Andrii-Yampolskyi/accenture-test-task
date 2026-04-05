package com.gildedrose;

class ConjuredItemUpdater implements ItemUpdater {

    @Override
    public void update(Item item) {
        item.quality = Math.max(0, item.quality - 2);

        item.sellIn -= 1;

        if (item.sellIn < 0) {
            item.quality = Math.max(0, item.quality - 2);
        }
    }
}
