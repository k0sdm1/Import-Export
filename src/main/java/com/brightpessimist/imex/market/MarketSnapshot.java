package com.brightpessimist.imex.market;

import java.io.IOException;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;

import com.brightpessimist.imex.network.MarketGoods;

public final class MarketSnapshot {

    public static final MarketSnapshot EMPTY = new MarketSnapshot(MarketGoods.empty(), 0L, 0L, "no data");

    public final MarketGoods goods;
    public final long revision;
    public final long fetchedAt;
    public final String status; // "ok" | "loading" | "error: ..."

    public MarketSnapshot(MarketGoods goods, long revision, long fetchedAt, String status) {
        this.goods = goods;
        this.revision = revision;
        this.fetchedAt = fetchedAt;
        this.status = status == null ? "" : status;
    }

    public boolean isLoading() {
        return "loading".equals(status);
    }

    // ---------------- networking ----------------

    public void writeTo(PacketBuffer buf) {
        NBTTagCompound root = new NBTTagCompound();
        root.setLong("rev", revision);
        root.setLong("time", fetchedAt);
        root.setString("status", status);

        NBTTagList items = new NBTTagList();
        for (MarketGoods.TradeableItemStack t : goods.items) {
            if (t == null || t.itemStack == null || t.itemStack.getItem() == null) continue;
            NBTTagCompound e = new NBTTagCompound();
            e.setTag("stack", t.itemStack.writeToNBT(new NBTTagCompound()));
            e.setDouble("buy", t.buyPrice);
            e.setDouble("sell", t.sellPrice);
            items.appendTag(e);
        }
        root.setTag("items", items);

        NBTTagList fluids = new NBTTagList();
        for (MarketGoods.TradeableFluidStack f : goods.fluids) {
            if (f == null || f.fluidStack == null) continue;
            NBTTagCompound e = new NBTTagCompound();
            e.setTag("stack", f.fluidStack.writeToNBT(new NBTTagCompound()));
            e.setDouble("buy", f.buyPrice);
            e.setDouble("sell", f.sellPrice);
            fluids.appendTag(e);
        }
        root.setTag("fluids", fluids);

        try {
            buf.writeNBTTagCompoundToBuffer(root);
        } catch (IOException ex) {
            throw new RuntimeException("Cannot write market snapshot", ex);
        }
    }

    public static MarketSnapshot read(PacketBuffer buf) {
        try {
            NBTTagCompound root = buf.readNBTTagCompoundFromBuffer();
            if (root == null) return EMPTY;
            MarketGoods goods = MarketGoods.empty();

            NBTTagList items = root.getTagList("items", 10);
            for (int i = 0; i < items.tagCount(); i++) {
                NBTTagCompound e = items.getCompoundTagAt(i);
                ItemStack stack = ItemStack.loadItemStackFromNBT(e.getCompoundTag("stack"));
                if (stack == null || stack.getItem() == null) continue;
                goods.items.add(new MarketGoods.TradeableItemStack(stack, e.getDouble("buy"), e.getDouble("sell")));
            }

            NBTTagList fluids = root.getTagList("fluids", 10);
            for (int i = 0; i < fluids.tagCount(); i++) {
                NBTTagCompound e = fluids.getCompoundTagAt(i);
                FluidStack fs = FluidStack.loadFluidStackFromNBT(e.getCompoundTag("stack"));
                if (fs == null) continue;
                goods.fluids.add(new MarketGoods.TradeableFluidStack(fs, e.getDouble("buy"), e.getDouble("sell")));
            }

            return new MarketSnapshot(goods, root.getLong("rev"), root.getLong("time"), root.getString("status"));
        } catch (Exception ex) {
            return EMPTY;
        }
    }
}
