package com.brightpessimist.imex.network;

// import java.util.List;
//
// import net.minecraft.item.ItemStack;
// import net.minecraftforge.fluids.FluidStack;
//
// public class MarketGoods {
//
// public List<TradeableItemStack> items;
// public List<MarketGoods.TradeableFluidStack> fluids;
//
// public static class TradeableItemStack {
//
// public ItemStack itemStack;
// public double buyPrice;
// public double sellPrice;
//
// public TradeableItemStack(ItemStack itemStack, double buyPrice, double sellPrice) {
// this.itemStack = itemStack;
// this.buyPrice = buyPrice;
// this.sellPrice = sellPrice;
// }
// }
//
// public static class TradeableFluidStack {
//
// public FluidStack fluidStack;
// public double buyPrice;
// public double sellPrice;
//
// public TradeableFluidStack(FluidStack fluidStack, double buyPrice, double sellPrice) {
// this.fluidStack = fluidStack;
// this.buyPrice = buyPrice;
// this.sellPrice = sellPrice;
// }
// }
// }

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class MarketGoods {

    public long vat;
    public List<TradeableItemStack> items = new ArrayList<>();
    public List<TradeableFluidStack> fluids = new ArrayList<>();

    public static MarketGoods empty() {
        return new MarketGoods();
    }

    public static class TradeableItemStack {

        public ItemStack itemStack;
        public double buyPrice;
        public double sellPrice;

        public TradeableItemStack(ItemStack itemStack, double buyPrice, double sellPrice) {
            this.itemStack = itemStack;
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
        }
    }

    public static class TradeableFluidStack {

        public FluidStack fluidStack;
        public double buyPrice;
        public double sellPrice;

        public TradeableFluidStack(FluidStack fluidStack, double buyPrice, double sellPrice) {
            this.fluidStack = fluidStack;
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
        }
    }
}
