package com.brightpessimist.imex.util;

// import com.brightpessimist.imex.network.MarketApiClient;
// import com.brightpessimist.imex.network.MarketData;
// import com.brightpessimist.imex.network.MarketGoods;
// import cpw.mods.fml.common.registry.GameRegistry;
// import net.minecraft.item.ItemStack;
// import net.minecraft.util.ChatComponentText;
// import net.minecraftforge.fluids.FluidRegistry;
//
// import java.util.ArrayList;
// import java.util.List;
//
// public class GoodsAvailable {
//
// public MarketGoods get_goods() {
// try {
// List<MarketGoods.TradeableItemStack> items = new ArrayList<>();
// List<MarketGoods.TradeableFluidStack> fluids = new ArrayList<>();
// MarketData md = MarketApiClient.performHttpRequest();
// for (MarketData.TradeableItem item : md.items) {
// if (item != null) {
// items.add(
// new MarketGoods.TradeableItemStack(
// new ItemStack(GameRegistry.findItem(item.modId, item.itemName), 1, item.metadata),
// item.buyPrice,
// item.sellPrice));
// }
// }
// for (MarketData.TradeableFluid fluid : md.fluids) {
// fluids.add(
// new MarketGoods.TradeableFluidStack(
// FluidRegistry.getFluidStack(fluid.fluidName, 1),
// fluid.buyPrice,
// fluid.sellPrice));
// }
//
// MarketGoods mg = new MarketGoods();
// mg.items = items;
// mg.fluids = fluids;
// return mg;
//
// } catch (Exception e) {
// return null;
// }
// }
// }

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.brightpessimist.imex.network.MarketApiClient;
import com.brightpessimist.imex.network.MarketData;
import com.brightpessimist.imex.network.MarketGoods;

import cpw.mods.fml.common.registry.GameRegistry;

public class GoodsAvailable {

    private static final Logger LOG = LogManager.getLogger("IMEX-Market");

    /** Network only. Safe to call from a worker thread. */
    public MarketData fetch() throws Exception {
        return MarketApiClient.performHttpRequest();
    }

    /** Registry access. MAIN THREAD ONLY. */
    public MarketGoods convert(MarketData md) {
        MarketGoods mg = MarketGoods.empty();
        if (md == null) return mg;

        mg.vat = md.vat;

        if (md.items != null) {
            for (MarketData.TradeableItem item : md.items) {
                if (item == null) continue;
                Item i = GameRegistry.findItem(item.modId, item.itemName);
                if (i == null) {
                    LOG.warn("Unknown market item {}:{}", item.modId, item.itemName);
                    continue;
                }
                mg.items.add(
                    new MarketGoods.TradeableItemStack(
                        new ItemStack(i, 1, item.metadata),
                        item.buyPrice,
                        item.sellPrice));
            }
        }
        if (md.fluids != null) {
            for (MarketData.TradeableFluid fluid : md.fluids) {
                if (fluid == null) continue;
                FluidStack fs = FluidRegistry.getFluidStack(fluid.fluidName, 1);
                if (fs == null) {
                    LOG.warn("Unknown market fluid {}", fluid.fluidName);
                    continue;
                }
                mg.fluids.add(new MarketGoods.TradeableFluidStack(fs, fluid.buyPrice, fluid.sellPrice));
            }
        }
        return mg;
    }

    /** Kept for compatibility – main thread only. */
    public MarketGoods get_goods() {
        try {
            return convert(fetch());
        } catch (Exception e) {
            LOG.error("Market request failed", e);
            return null;
        }
    }
}
