package com.brightpessimist.imex.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public final class ImexNetwork {

    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("imex");

    /** call in preInit */
    public static void init() {
        CHANNEL.registerMessage(PacketRefreshMarket.Handler.class, PacketRefreshMarket.class, 0, Side.SERVER);
    }
}
