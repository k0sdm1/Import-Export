package com.brightpessimist.imex.market;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.server.MinecraftServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.brightpessimist.imex.network.MarketData;
import com.brightpessimist.imex.network.MarketGoods;
import com.brightpessimist.imex.util.GoodsAvailable;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public final class MarketCache {

    private static final Logger LOG = LogManager.getLogger("IMEX-Market");

    private static final long MANUAL_COOLDOWN_MS = 10_000L; // refresh button
    private static final long AUTO_COOLDOWN_MS = 30_000L; // retry / background
    private static final long STALE_MS = 60_000L; // auto refresh age

    private static volatile MarketSnapshot snapshot = MarketSnapshot.EMPTY;
    private static volatile long lastAttempt = 0L;
    private static final AtomicBoolean IN_FLIGHT = new AtomicBoolean(false);
    private static final Queue<Runnable> MAIN_THREAD = new ConcurrentLinkedQueue<>();

    private static final ExecutorService IO = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "IMEX-Market-IO");
        t.setDaemon(true);
        return t;
    });

    private static final GoodsAvailable API = new GoodsAvailable();

    private MarketCache() {}

    /** Called from the sync value getter on the server, and never blocks. */
    public static MarketSnapshot get() {
        return snapshot;
    }

    public static void refreshIfStale() {
        if (System.currentTimeMillis() - snapshot.fetchedAt > STALE_MS) requestRefresh(false);
    }

    /** Server only; no-op on the client. */
    public static void requestRefresh(boolean manual) {
        if (FMLCommonHandler.instance()
            .getEffectiveSide()
            .isClient()) return;
        if (MinecraftServer.getServer() == null) return;

        long now = System.currentTimeMillis();
        if (now - lastAttempt < (manual ? MANUAL_COOLDOWN_MS : AUTO_COOLDOWN_MS)) return;
        if (!IN_FLIGHT.compareAndSet(false, true)) return;
        lastAttempt = now;

        // publish a "loading" snapshot so every open GUI shows it immediately
        MarketSnapshot old = snapshot;
        snapshot = new MarketSnapshot(old.goods, old.revision, old.fetchedAt, "loading");

        IO.execute(() -> {
            MarketData data = null;
            String error = null;
            try {
                data = API.fetch();
            } catch (Throwable t) {
                error = t.getClass()
                    .getSimpleName() + ": "
                    + t.getMessage();
                LOG.error("Market request failed", t);
            }
            final MarketData result = data;
            final String err = error;

            // registry / ItemStack creation must happen on the server thread
            MAIN_THREAD.add(() -> {
                try {
                    MarketSnapshot cur = snapshot;
                    if (result != null) {
                        MarketGoods goods = API.convert(result);
                        snapshot = new MarketSnapshot(goods, cur.revision + 1, System.currentTimeMillis(), "ok");
                    } else {
                        snapshot = new MarketSnapshot(cur.goods, cur.revision, cur.fetchedAt, "error: " + err);
                    }
                } finally {
                    IN_FLIGHT.set(false);
                }
            });
        });
    }

    private static void onTickEnd() {
        Runnable r;
        while ((r = MAIN_THREAD.poll()) != null) r.run();

        MinecraftServer server = MinecraftServer.getServer();
        if (server != null && server.getCurrentPlayerCount() > 0
            && System.currentTimeMillis() - snapshot.fetchedAt > STALE_MS) {
            requestRefresh(false);
        }
    }

    /** Register once: FMLCommonHandler.instance().bus().register(new MarketCache.Ticker()); */
    public static class Ticker {

        @SubscribeEvent
        public void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase == TickEvent.Phase.END) onTickEnd();
        }
    }

    public static void runOnServerThread(Runnable task) {
        MAIN_THREAD.add(task);
    }
}
