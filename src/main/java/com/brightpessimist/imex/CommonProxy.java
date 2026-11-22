package com.brightpessimist.imex;

import com.brightpessimist.imex.command.DebugComand;
import com.brightpessimist.imex.config.Config;
import com.brightpessimist.imex.config.TradingConfig;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class CommonProxy {

    public void add_chat_message(String message) {
        return;
    }

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getModConfigurationDirectory());
        TradingConfig.init(event.getModConfigurationDirectory());

        ImportExport.LOG.info(Config.greeting);
        ImportExport.LOG.info("I am MyMod at version " + 1);
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        // FMLCommonHandler.instance().bus().register(new MarketCache.Ticker());
    }

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {}

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new DebugComand());
    }
}
