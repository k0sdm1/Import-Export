package com.brightpessimist.imex.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class NBTUtils {

    public static NBTTagCompound getPersistedData(EntityPlayer player, boolean createIfMissing) {
        NBTTagCompound tag = player.getEntityData()
            .getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);

        if (createIfMissing) {
            player.getEntityData()
                .setTag(EntityPlayer.PERSISTED_NBT_TAG, tag);
        }

        return tag;
    }
}
