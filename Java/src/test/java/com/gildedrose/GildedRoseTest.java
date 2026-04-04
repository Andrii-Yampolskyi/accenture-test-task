package com.gildedrose;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GildedRoseTest {

    @ParameterizedTest(name = "Normal item: sellIn={0} quality={1} -> sellIn={2} quality={3}")
    @CsvSource({
        "10, 20,  9, 19",  // basic degradation
        " 1, 10,  0,  9",  // last day before sell date
        " 0, 20, -1, 18",  // sell date passed, double degradation
        "-1, 20, -2, 18",  // already past sell date
        " 5,  0,  4,  0",  // quality floor at 0, before sell date
        " 0,  0, -1,  0",  // quality floor at 0, after sell date
        " 0,  1, -1,  0",  // quality 1 after sell date, doesn't go negative
    })
    void normalItems(int sellIn, int quality, int expectedSellIn, int expectedQuality) {
        verifyUpdate("+5 Dexterity Vest", sellIn, quality, expectedSellIn, expectedQuality);
    }

    @ParameterizedTest(name = "Aged Brie type: sellIn={0} quality={1} -> sellIn={2} quality={3}")
    @CsvSource({
        " 2,  0,  1,  1",  // basic quality increase
        "10, 30,  9, 31",  // increase when sellIn > 0
        " 0, 10, -1, 12",  // double increase after sell date
        "-1, 10, -2, 12",  // already past sell date
        " 5, 50,  4, 50",  // quality capped at 50, before sell date
        " 0, 50, -1, 50",  // quality capped at 50, after sell date
        " 0, 49, -1, 50",  // quality 49 after sell date, caps at 50 not 51
    })
    void agedBrieItem(int sellIn, int quality, int expectedSellIn, int expectedQuality) {
        verifyUpdate("Aged Brie", sellIn, quality, expectedSellIn, expectedQuality);
    }

    @ParameterizedTest(name = "Sulfuras type: sellIn={0} quality={1} -> sellIn={2} quality={3}")
    @CsvSource({
        " 5, 80,  5, 80",  // positive sellIn, no change
        " 0, 80,  0, 80",  // sellIn at 0, no change
        "-1, 80, -1, 80",  // negative sellIn, no change
    })
    void sulfurasItem(int sellIn, int quality, int expectedSellIn, int expectedQuality) {
        verifyUpdate("Sulfuras, Hand of Ragnaros", sellIn, quality, expectedSellIn, expectedQuality);
    }

    @ParameterizedTest(name = "Backstage: sellIn={0} quality={1} -> sellIn={2} quality={3}")
    @CsvSource({
        "15, 20, 14, 21",  // +1 when sellIn > 10
        "11, 20, 10, 21",  // +1 at exactly sellIn=11
        "10, 20,  9, 22",  // +2 at sellIn=10 boundary
        " 7, 20,  6, 22",  // +2 in the 6-10 range
        " 6, 20,  5, 22",  // +2 at sellIn=6
        " 5, 20,  4, 23",  // +3 at sellIn=5 boundary
        " 3, 20,  2, 23",  // +3 in the 1-5 range
        " 1, 20,  0, 23",  // +3 at sellIn=1
        " 0, 20, -1,  0",  // drops to 0 after concert
        "-1, 20, -2,  0",  // already past concert
        "15, 50, 14, 50",  // quality cap at 50, sellIn > 10
        "10, 49,  9, 50",  // quality cap at 50, sellIn=10 quality=49
        " 5, 48,  4, 50",  // quality cap at 50, sellIn=5 quality=48
        " 5, 49,  4, 50",  // quality cap at 50, sellIn=5 quality=49
    })
    void backstagePassesItem(int sellIn, int quality, int expectedSellIn, int expectedQuality) {
        verifyUpdate("Backstage passes to a TAFKAL80ETC concert", sellIn, quality, expectedSellIn, expectedQuality);
    }

    @ParameterizedTest(name = "Conjured: sellIn={0} quality={1} -> sellIn={2} quality={3}")
    @CsvSource({
        "10, 20,  9, 18",  // double degradation before sell date
        " 0, 20, -1, 16",  // quadruple degradation after sell date
        " 5,  1,  4,  0",  // quality floor at 0 before sell date
        " 0,  1, -1,  0",  // quality floor at 0 after sell date
        " 5,  0,  4,  0",  // quality already 0
        " 0,  3, -1,  0",  // quality 3 after sell date, clamps to 0
    })
    void conjuredItems(int sellIn, int quality, int expectedSellIn, int expectedQuality) {
        verifyUpdate("Conjured Mana Cake", sellIn, quality, expectedSellIn, expectedQuality);
    }

    @Test
    void multiDayRegressionForNormalItem() {
        Item[] items = new Item[]{new Item("+5 Dexterity Vest", 3, 10)};
        GildedRose app = new GildedRose(items);

        app.updateQuality(); // day 1: sellIn=2, quality=9
        assertEquals(2, items[0].sellIn);
        assertEquals(9, items[0].quality);

        app.updateQuality(); // day 2: sellIn=1, quality=8
        assertEquals(1, items[0].sellIn);
        assertEquals(8, items[0].quality);

        app.updateQuality(); // day 3: sellIn=0, quality=7
        assertEquals(0, items[0].sellIn);
        assertEquals(7, items[0].quality);

        app.updateQuality(); // day 4: sellIn=-1, quality=5 (double degradation)
        assertEquals(-1, items[0].sellIn);
        assertEquals(5, items[0].quality);
    }

    @Test
    void multipleItemsAreIndependent() {
        Item[] items = new Item[]{
            new Item("+5 Dexterity Vest", 10, 20),
            new Item("Aged Brie", 2, 0),
            new Item("Sulfuras, Hand of Ragnaros", 0, 80)
        };
        GildedRose app = new GildedRose(items);
        app.updateQuality();

        assertEquals(19, items[0].quality);
        assertEquals(1, items[1].quality);
        assertEquals(80, items[2].quality);
    }

    private void verifyUpdate(
        String name,
        int sellIn,
        int quality,
        int expectedSellIn,
        int expectedQuality
    ) {
        Item[] items = new Item[]{new Item(name, sellIn, quality)};
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals(expectedSellIn, items[0].sellIn, "sellIn");
        assertEquals(expectedQuality, items[0].quality, "quality");
    }
}
