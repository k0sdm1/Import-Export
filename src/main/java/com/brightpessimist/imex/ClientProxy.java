package com.brightpessimist.imex;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

public class ClientProxy extends CommonProxy {

    // Override CommonProxy methods here, if you want a different behaviour on the client (e.g. registering renders).
    // Don't forget to call the super methods as well.
    @Override
    public void add_chat_message(String message) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player.worldObj.isRemote) {
            player.addChatMessage(new ChatComponentText(message));
        }
    }
}
