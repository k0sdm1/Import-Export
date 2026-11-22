package com.brightpessimist.imex;

import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.brightpessimist.imex.Block.MTETradingBay;
import com.brightpessimist.imex.Block.MTETradingTerminal;
import com.brightpessimist.imex.Item.registry;
import com.brightpessimist.imex.currency.TradingCurrencyWorldSavedData;
import com.brightpessimist.imex.market.MarketCache;
import com.brightpessimist.imex.network.ImexNetwork;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(
    modid = ImportExport.MODID,
    version = "0.0.1 alpha",
    name = "Import / Export",
    acceptedMinecraftVersions = "[1.7.10]")
public class ImportExport {

    public static final String MODID = "imex";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(
        clientSide = "com.brightpessimist.imex.ClientProxy",
        serverSide = "com.brightpessimist.imex.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
        ImexNetwork.init(); // BOTH sides, unconditional
        FMLCommonHandler.instance()
            .bus()
            .register(new MarketCache.Ticker());
    }

    @Mod.EventHandler
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
        MinecraftForge.EVENT_BUS.register(new TradingCurrencyWorldSavedData());
        new MTETradingTerminal(20001, "trading.terminal", "Trading Terminal", 1);
        new MTETradingBay(20002, "trading.bay", "Trading Bay");

        registry.register();
    }

    @Mod.EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }
}
