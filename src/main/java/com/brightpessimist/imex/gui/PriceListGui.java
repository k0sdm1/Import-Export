package com.brightpessimist.imex.gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.brightpessimist.imex.market.MarketCache;
import com.brightpessimist.imex.market.MarketSnapshot;
import com.brightpessimist.imex.network.ImexNetwork;
import com.brightpessimist.imex.network.MarketGoods;
import com.brightpessimist.imex.network.PacketRefreshMarket;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.GenericSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.FluidDisplayWidget;
import com.cleanroommc.modularui.widgets.ItemDisplayWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import cpw.mods.fml.common.FMLCommonHandler;

public final class PriceListGui {

    private static final int W = 200, H = 240;
    private static final int LIST_X = 6, LIST_Y = 40, LIST_W = 188, LIST_H = 104;
    private static final int ROW_W = 180, ROW_H = 20;
    private static final int NAME_X = 20, NAME_W = 88, BUY_X = 112, SELL_X = 148;
    private static final String[] MODES = { "All", "Items", "Fluids" };

    private PriceListGui() {}

    /** Everything here is per-open and per-side. Never put this on the Item. */
    private static final class State {

        MarketSnapshot snapshot = MarketSnapshot.EMPTY;
        List<Entry> entries = Collections.emptyList();
        String filter = "";
        int mode; // 0 all, 1 items, 2 fluids
        int refreshCounter;
        int visible;
        ListWidget<IWidget, ?> list;
    }

    private static final class Entry {

        final ItemStack stack;
        final FluidStack fluid;
        final String name, shown, id, key;
        final double buy, sell;

        Entry(ItemStack stack, FluidStack fluid, String name, String id, double buy, double sell) {
            this.stack = stack;
            this.fluid = fluid;
            this.name = name;
            this.id = id;
            this.buy = buy;
            this.sell = sell;
            this.shown = fit(name, NAME_W);
            this.key = (name + " " + id).toLowerCase(Locale.ROOT);
        }
    }

    public static ModularPanel build(PlayerInventoryGuiData data, PanelSyncManager syncManager, UISettings settings) {
        final State state = new State();
        final boolean client = FMLCommonHandler.instance()
            .getEffectiveSide()
            .isClient();

        if (!client) MarketCache.refreshIfStale();

        // ---- global market data: server -> client, auto-detected every tick ----
        syncManager.syncValue(
            "market",
            GenericSyncValue.builder(MarketSnapshot.class)
                .getter(MarketCache::get)
                // .setter(snap -> {
                // state.snapshot = snap;
                // state.entries = client ? makeEntries(snap) : Collections.emptyList();
                // rebuild(state);
                // })
                .setter(snap -> {
                    MarketSnapshot prev = state.snapshot;
                    state.snapshot = snap;
                    if (!client) return;
                    // same data, only a re-send / status ping -> do NOT touch the widget tree
                    if (snap.revision == prev.revision && snap.fetchedAt == prev.fetchedAt
                        && snap.status.equals(prev.status)) return;
                    state.entries = makeEntries(snap);
                    rebuild(state);
                })
                .serializer((buf, snap) -> snap.writeTo(buf))
                .deserializer(MarketSnapshot::read)
                .build());

        // ---- refresh request: client -> server ----
        // final IntSyncValue refresh = new IntSyncValue(() -> state.refreshCounter, value -> {
        // state.refreshCounter = value;
        // MarketCache.requestRefresh(true); // no-op on client
        // });
        // syncManager.syncValue("refresh", refresh);

        // ---- search text (filtering itself is client side) ----
        // final StringSyncValue filter = new StringSyncValue(() -> state.filter, value -> {
        // state.filter = value == null ? "" : value;
        // rebuild(state);
        // });

        final StringSyncValue filter = new StringSyncValue(() -> state.filter, value -> {
            String v = value == null ? "" : value;
            if (v.equals(state.filter)) return;
            state.filter = v;
            if (client) rebuild(state);
        }).allowC2S();

        // final StringSyncValue filter = new StringSyncValue(() -> state.filter, value -> {
        // String v = value == null ? "" : value;
        // if (v.equals(state.filter)) return;
        // state.filter = v;
        // rebuild(state);
        // });
        syncManager.syncValue("filter", filter);

        ModularPanel panel = new ModularPanel("imex_pricelist").size(W, H);

        panel.child(
            new TextFieldWidget().pos(6, 6)
                .size(118, 14)
                .setTextColor(0xFFE0E0E0)
                .value(filter));

        panel.child(
            new ButtonWidget<>().pos(128, 6)
                .size(32, 14)
                .overlay(IKey.dynamic(() -> MODES[state.mode]))
                .addTooltipLine(IKey.str("Cycle: all / items / fluids"))
                .onMousePressed(button -> {
                    state.mode = (state.mode + 1) % MODES.length;
                    rebuild(state);
                    return true;
                }));

        // panel.child(new ButtonWidget<>()
        // .pos(164, 6).size(30, 14)
        // .overlay(IKey.dynamic(() -> state.snapshot.isLoading() ? "..." : "Sync"))
        // .addTooltipLine(IKey.str("Pull the current price list"))
        // .addTooltipLine(IKey.str("Shared by all players, 10s cooldown"))
        // .onMousePressed(button -> {
        // refresh.setIntValue(refresh.getIntValue() + 1);
        // return true;
        // }));
        panel.child(
            new ButtonWidget<>().pos(164, 6)
                .size(30, 14)
                .overlay(IKey.dynamic(() -> state.snapshot.isLoading() ? "..." : "Sync"))
                .addTooltipLine(IKey.str("Pull the current price list"))
                .onMousePressed(button -> {
                    ImexNetwork.CHANNEL.sendToServer(new PacketRefreshMarket());
                    return true;
                }));

        // column headers
        panel.child(
            IKey.str("Item / Fluid")
                .color(0xFF909090)
                .asWidget()
                .pos(LIST_X + NAME_X, 30));
        panel.child(
            IKey.str("Buy")
                .color(0xFF909090)
                .asWidget()
                .pos(LIST_X + BUY_X, 30));
        panel.child(
            IKey.str("Sell")
                .color(0xFF909090)
                .asWidget()
                .pos(LIST_X + SELL_X, 30));

        state.list = new ListWidget<>().pos(LIST_X, LIST_Y)
            .size(LIST_W, LIST_H);
        panel.child(state.list);

        panel.child(
            IKey.dynamic(() -> statusLine(state))
                .color(0xFF808080)
                .asWidget()
                .pos(6, 148));

        panel.bindPlayerInventory(); // remove this and shrink H by ~82 if you don't want the inventory

        if (client) {
            state.entries = makeEntries(state.snapshot);
            rebuild(state);
        }
        return panel;
    }

    // ---------------- rows ----------------

    private static List<Entry> makeEntries(MarketSnapshot snap) {
        List<Entry> out = new ArrayList<>();
        for (MarketGoods.TradeableItemStack t : snap.goods.items) {
            if (t == null || t.itemStack == null || t.itemStack.getItem() == null) continue;
            String id = String.valueOf(Item.itemRegistry.getNameForObject(t.itemStack.getItem()));
            out.add(new Entry(t.itemStack, null, t.itemStack.getDisplayName(), id, t.buyPrice, t.sellPrice));
        }
        for (MarketGoods.TradeableFluidStack f : snap.goods.fluids) {
            if (f == null || f.fluidStack == null || f.fluidStack.getFluid() == null) continue;
            String id = "fluid:" + FluidRegistry.getFluidName(f.fluidStack);
            out.add(new Entry(null, f.fluidStack, f.fluidStack.getLocalizedName(), id, f.buyPrice, f.sellPrice));
        }
        out.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
        return out;
    }

    private static void rebuild(State state) {
        if (state.list == null || !FMLCommonHandler.instance()
            .getEffectiveSide()
            .isClient()) return;

        state.list.removeAll();
        String q = state.filter.trim()
            .toLowerCase(Locale.ROOT);
        int count = 0;
        for (Entry e : state.entries) {
            if (state.mode == 1 && e.stack == null) continue;
            if (state.mode == 2 && e.fluid == null) continue;
            if (!q.isEmpty() && !e.key.contains(q)) continue;
            state.list.child(row(e));
            count++;
        }
        state.visible = count;
        state.list.scheduleResize();
    }

    private static IWidget row(Entry e) {
        ParentWidget<?> row = new ParentWidget<>().size(ROW_W, ROW_H);
        if (e.stack != null) {
            row.child(
                new ItemDisplayWidget().pos(1, 2)
                    .size(16, 16)
                    .item(e.stack));
        } else {
            row.child(
                new FluidDisplayWidget().pos(1, 2)
                    .size(16, 16)
                    .value(e.fluid)
                    .displayAmount(false));
        }
        row.child(
            IKey.str(e.shown)
                .color(Color.BLACK.main)
                .asWidget()
                .pos(NAME_X, 6));
        row.child(
            IKey.str(price(e.buy))
                .color(Color.RED.main)
                .asWidget()
                .pos(BUY_X, 6));
        row.child(
            IKey.str(price(e.sell))
                .color(Color.GREEN.darker(1))
                .asWidget()
                .pos(SELL_X, 6));

        row.addTooltipLine(IKey.str(e.name));
        row.addTooltipLine(IKey.str("Buy:  " + exact(e.buy)));
        row.addTooltipLine(IKey.str("Sell: " + exact(e.sell)));
        row.addTooltipLine(
            IKey.str(e.id)
                .color(0xFF808080));
        return row;
    }

    private static String statusLine(State state) {
        MarketSnapshot s = state.snapshot;
        if (s.isLoading()) return state.visible + " entries | loading...";
        if (s.status.startsWith("error")) return fit(s.status, W - 12);
        if (s.fetchedAt == 0) return "no data - press Sync";
        long age = (System.currentTimeMillis() - s.fetchedAt) / 1000L;
        return state.visible + " entries | updated " + age + "s ago";
    }

    // ---------------- formatting helpers (client only) ----------------

    private static String fit(String s, int width) {
        if (s == null) return "";
        if (!FMLCommonHandler.instance()
            .getEffectiveSide()
            .isClient()) return s;
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        if (fr.getStringWidth(s) <= width) return s;
        String cut = s;
        while (cut.length() > 1 && fr.getStringWidth(cut + "...") > width) {
            cut = cut.substring(0, cut.length() - 1);
        }
        return cut + "...";
    }

    private static String price(double v) {
        if (v <= 0) return "-";
        if (v >= 1e9) return String.format(Locale.ROOT, "%.1fB", v / 1e9);
        if (v >= 1e6) return String.format(Locale.ROOT, "%.1fM", v / 1e6);
        if (v >= 1e3) return String.format(Locale.ROOT, "%.1fk", v / 1e3);
        if (v == Math.floor(v)) return String.valueOf((long) v);
        return String.format(Locale.ROOT, "%.2f", v);
    }

    private static String exact(double v) {
        return v <= 0 ? "not tradeable" : String.format(Locale.ROOT, "%,.2f", v);
    }
}
