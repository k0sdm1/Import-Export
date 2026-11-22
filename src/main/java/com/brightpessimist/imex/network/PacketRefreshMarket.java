package com.brightpessimist.imex.network;

import com.brightpessimist.imex.market.MarketCache;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketRefreshMarket implements IMessage {

    public PacketRefreshMarket() {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<PacketRefreshMarket, IMessage> {

        @Override
        public IMessage onMessage(PacketRefreshMarket msg, MessageContext ctx) {
            // 1.7.10: onMessage runs on the Netty thread, NOT on the server thread!
            MarketCache.runOnServerThread(() -> MarketCache.requestRefresh(true));
            return null;
        }
    }
}
