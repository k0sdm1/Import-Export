package com.brightpessimist.imex.Item;

// import com.cleanroommc.modularui.api.IGuiHolder;
// import com.cleanroommc.modularui.factory.GuiFactories;
// import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
// import com.cleanroommc.modularui.screen.ModularPanel;
// import com.cleanroommc.modularui.screen.UISettings;
// import com.cleanroommc.modularui.value.sync.PanelSyncManager;
// import net.minecraft.entity.player.EntityPlayer;
// import net.minecraft.item.Item;
// import net.minecraft.item.ItemStack;
// import net.minecraft.world.World;
//
// public class PriceList extends Item implements IGuiHolder<PlayerInventoryGuiData> {
// public PriceList() {
// super();
// this.setUnlocalizedName("pricelist");
// this.setTextureName("imex:pricelist");
// this.setMaxStackSize(1);
// }
//
// @Override
// public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
// if (!world.isRemote) {
// GuiFactories.playerInventory()
// .openFromMainHand(player);
// }
// return stack;
// }
//
// @Override
// public ModularPanel buildUI(PlayerInventoryGuiData data, PanelSyncManager syncManager, UISettings settings) {
// ModularPanel panel = new ModularPanel("panel").size(176, 100);
// return panel;
// }
// }

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.brightpessimist.imex.gui.PriceListGui;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

public class PriceList extends Item implements IGuiHolder<PlayerInventoryGuiData> {

    public PriceList() {
        this.setUnlocalizedName("pricelist");
        this.setTextureName("imex:pricelist");
        this.setMaxStackSize(1);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            GuiFactories.playerInventory()
                .openFromMainHand(player);
        }
        return stack;
    }

    @Override
    public ModularPanel buildUI(PlayerInventoryGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return PriceListGui.build(data, syncManager, settings);
    }
}
