package com.brightpessimist.imex.network;

import java.util.List;

public class MarketData {

    public long timestamp;
    public long vat;
    public List<TradeableItem> items;
    public List<TradeableFluid> fluids;

    public static class TradeableItem {

        public String modId; // e.g., "minecraft" or "gregtech"
        public String itemName; // e.g., "iron_ingot" or "gt.metaitem.01"
        public int metadata; // e.g., 0, or GT meta damage value
        public double buyPrice; // Price to buy from the bay (in your currency)
        public double sellPrice; // Price to sell to the bay

        // Optional: For GregTech Material-based identification
        public String gtMaterialName; // e.g., "ingotIron", "dustGold"
    }

    public static class TradeableFluid {

        public String fluidName; // e.g., "water", "lava", "fluid.nitrogen"
        public double buyPrice;
        public double sellPrice;
    }
}
