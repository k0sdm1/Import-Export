package com.brightpessimist.imex.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.brightpessimist.imex.market.MarketCache;
import com.brightpessimist.imex.market.MarketSnapshot;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;

public final class TradingConfig {

    public static int vat = 22;
    public static int exportCycleTicks = 100;
    public static int importCycleTicks = 200;
    public static int defaultItemBatch = 64; // max items of ONE type pulled from ME per cycle
    public static int defaultFluidBatchMb = 8000; // max mB of ONE fluid pulled from ME per cycle
    public static int importFluidChunkMb = 16000; // split contract fluids so they fit output hatches
    public static int importItemBatch = 0; // items of one type per import cycle; 0 = unlimited
    public static int importFluidBatchMb = 0; // mB of one fluid per import cycle; 0 = unlimited

    private static Item currencyItem = Items.emerald;
    private static int currencyMeta = 0;

    private static final Map<String, Entry> ITEM_PRICES = new HashMap<>();
    private static final Map<String, Entry> FLUID_PRICES = new HashMap<>();

    private TradingConfig() {}

    public static final class Entry {

        public final long price;
        public final int batch; // <= 0 -> use default

        Entry(long price, int batch) {
            this.price = price;
            this.batch = batch;
        }
    }

    private static File goods_file;

    /** Call once in preInit: TradingConfig.init(event.getModConfigurationDirectory()); */
    public static void init(File configDir) {
        File file = new File(new File(configDir, "imex"), "imex_trading.json");
        goods_file = new File(new File(configDir, "imex"), "imex_goods.json");
        try {
            if (!file.exists()) {
                // noinspection ResultOfMethodCallIgnored
                file.getParentFile()
                    .mkdirs();
                try (Writer w = new FileWriter(file)) {
                    w.write(DEFAULT_JSON);
                }
            }
            if (!goods_file.exists()) {
                // noinspection ResultOfMethodCallIgnored
                file.getParentFile()
                    .mkdirs();
                try (Writer w = new FileWriter(goods_file)) {
                    w.write(DEFAULT_GOODS_JSON);
                }
            }
            try (Reader r = new FileReader(file)) {
                parse(
                    new JsonParser().parse(r)
                        .getAsJsonObject());
            }
        } catch (Exception e) {
            GTLog.conditionalLogger("[IMEX] Failed to load imex_trading.json, using defaults: " + e);
        }
    }

    private static void parse(JsonObject root) {
        exportCycleTicks = getInt(root, "exportCycleTicks", exportCycleTicks);
        importCycleTicks = getInt(root, "importCycleTicks", importCycleTicks);
        defaultItemBatch = getInt(root, "defaultItemBatch", defaultItemBatch);
        defaultFluidBatchMb = getInt(root, "defaultFluidBatchMb", defaultFluidBatchMb);
        importFluidChunkMb = getInt(root, "importFluidChunkMb", importFluidChunkMb);
        importItemBatch = getInt(root, "importItemBatch", importItemBatch);
        importFluidBatchMb = getInt(root, "importFluidBatchMb", importFluidBatchMb);

        if (root.has("currency")) {
            String[] parts = root.get("currency")
                .getAsString()
                .split(":");
            if (parts.length >= 2) {
                Item item = GameRegistry.findItem(parts[0], parts[1]);
                if (item != null) {
                    currencyItem = item;
                    currencyMeta = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
                }
            }
        }

        ITEM_PRICES.clear();
        if (root.has("items")) {
            for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("items")
                .entrySet()) {
                JsonObject o = e.getValue()
                    .getAsJsonObject();
                ITEM_PRICES.put(e.getKey(), new Entry(getLong(o, "price", 0), getInt(o, "batch", 0)));
            }
        }
        FLUID_PRICES.clear();
        if (root.has("fluids")) {
            for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("fluids")
                .entrySet()) {
                JsonObject o = e.getValue()
                    .getAsJsonObject();
                FLUID_PRICES.put(e.getKey(), new Entry(getLong(o, "pricePerBucket", 0), getInt(o, "batchMb", 0)));
            }
        }
    }

    public static JsonObject get_goods_json() {
        try (Reader r = new FileReader(goods_file)) {
            return new JsonParser().parse(r)
                .getAsJsonObject();
        } catch (Exception e) {
            throw new IllegalArgumentException(e.toString());
        }
    }

    private static String itemKey(ItemStack stack, boolean wildcard) {
        GameRegistry.UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(stack.getItem());
        if (uid == null) return null;
        return uid.modId + ":" + uid.name + ":" + (wildcard ? "*" : stack.getItemDamage());
    }

    private static Entry itemEntry(ItemStack stack) {
        String exact = itemKey(stack, false);
        if (exact == null) return null;
        Entry e = ITEM_PRICES.get(exact);
        return e != null ? e : ITEM_PRICES.get(itemKey(stack, true));
    }

    /** Price per single item. 0 (or missing) = NOT sellable -> acts as the export whitelist. */
    public static long getItemPrice(ItemStack stack) {
        Entry e = itemEntry(stack);
        return e == null ? 0 : e.price;
    }

    public static int getItemBatch(ItemStack stack) {
        Entry e = itemEntry(stack);
        return e == null || e.batch <= 0 ? defaultItemBatch : e.batch;
    }

    /** Price per 1000 mB. 0 (or missing) = NOT sellable. */
    public static long getFluidPricePerBucket(FluidStack fluid) {
        Entry e = FLUID_PRICES.get(
            fluid.getFluid()
                .getName());
        return e == null ? 0 : e.price;
    }

    public static int getFluidBatchMb(FluidStack fluid) {
        Entry e = FLUID_PRICES.get(
            fluid.getFluid()
                .getName());
        return e == null || e.batch <= 0 ? defaultFluidBatchMb : e.batch;
    }

    public static double getItemSellPrice(ItemStack stack) {
        MarketSnapshot snapshot = MarketCache.get();
        for (var item : snapshot.goods.items) {
            if (GTUtility.areStacksEqual(item.itemStack, stack, true)) return item.sellPrice;
        }
        return 0;
    }

    public static double getItemBuyPrice(ItemStack stack) {
        MarketSnapshot snapshot = MarketCache.get();
        for (var item : snapshot.goods.items) {
            if (GTUtility.areStacksEqual(item.itemStack, stack, true)) return item.buyPrice;
        }
        return 0;
    }

    public static double getFluidSellPrice(FluidStack stack) {
        MarketSnapshot snapshot = MarketCache.get();
        for (var item : snapshot.goods.fluids) {
            if (GTUtility.areFluidsEqual(item.fluidStack, stack, true)) return item.sellPrice;
        }
        return 0;
    }

    public static double getFluidBuyPrice(FluidStack stack) {
        MarketSnapshot snapshot = MarketCache.get();
        for (var item : snapshot.goods.fluids) {
            if (GTUtility.areFluidsEqual(item.fluidStack, stack, true)) return item.buyPrice;
        }
        return 0;
    }

    public static ItemStack createCurrencyStack(int amount) {
        return currencyItem == null ? null : new ItemStack(currencyItem, amount, currencyMeta);
    }

    private static int getInt(JsonObject o, String key, int def) {
        return o.has(key) ? o.get(key)
            .getAsInt() : def;
    }

    private static long getLong(JsonObject o, String key, long def) {
        return o.has(key) ? o.get(key)
            .getAsLong() : def;
    }

    private static final String DEFAULT_JSON = """
        {
          "exportCycleTicks": 100,
          "importCycleTicks": 200,
          "defaultItemBatch": 64,
          "defaultFluidBatchMb": 8000,
          "currency": "minecraft:emerald:0",
          "items": {
            "minecraft:iron_ingot:0": { "price": 12 },
            "minecraft:gold_ingot:0": { "price": 48, "batch": 32 },
            "gregtech:gt.metaitem.01:*": { "price": 5, "batch": 128 }
          },
          "fluids": {
            "water": { "pricePerBucket": 1 },
            "lava": { "pricePerBucket": 4, "batchMb": 4000 }
          }
        }
        """;

    private static final String DEFAULT_GOODS_JSON = """
            {
                "timestamp": 1690000000000,
                "vat": 22,
                "items": [
                    {
                        "modId": "gregtech",
                        "itemName": "gt.metaitem.01",
                        "metadata": 11316,
                        "buyPrice": 1,
                        "sellPrice": 64.12
                    },
                    {
                        "modId": "minecraft",
                        "itemName": "egg",
                        "metadata": 0,
                        "buyPrice": 0,
                        "sellPrice": 50
                    },
                    {
                        "modId": "minecraft",
                        "itemName": "wheat",
                        "metadata": 0,
                        "buyPrice": 0,
                        "sellPrice": 25
                    },
                    {
                        "modId": "minecraft",
                        "itemName": "log",
                        "metadata": 1,
                        "buyPrice": 0,
                        "sellPrice": 30
                    },
                    {
                        "modId": "gregtech",
                        "itemName": "gt.metaitem.01",
                        "metadata": 11317,
                        "buyPrice": 11317,
                        "sellPrice": 64.00
                    },
                    {
                        "modId": "gregtech",
                        "itemName": "gt.metaitem.01",
                        "metadata": 11318,
                        "buyPrice": 11318,
                        "sellPrice": 64.00
                    },
                    {
                        "modId": "gregtech",
                        "itemName": "gt.metaitem.01",
                        "metadata": 11319,
                        "buyPrice": 11,
                        "sellPrice": 64.00
                    },
                    {
                        "modId": "gregtech",
                        "itemName": "gt.metaitem.01",
                        "metadata": 11320,
                        "buyPrice": 11320,
                        "sellPrice": 64.00
                    },
                    {
                        "modId": "gregtech",
                        "itemName": "gt.metaitem.01",
                        "metadata": 11321,
                        "buyPrice": 11321,
                        "sellPrice": 64.00
                    }
                ],
                "fluids": [
                    {
                        "fluidName": "molten.epoxid",
                        "buyPrice": 2.00,
                        "sellPrice": 0.50
                    }
                ]
            }
        """;
}
