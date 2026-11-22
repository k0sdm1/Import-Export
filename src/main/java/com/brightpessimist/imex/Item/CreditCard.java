package com.brightpessimist.imex.Item;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import com.brightpessimist.imex.currency.TradingCurrencyManager;
import com.brightpessimist.imex.currency.TradingCurrencyStorage;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;

public class CreditCard extends Item implements IGuiHolder<PlayerInventoryGuiData> {

    public CreditCard() {
        this.setUnlocalizedName("creditcard");
        this.setTextureName("imex:creditcard");
        this.setMaxStackSize(1);
    }

    @Override
    public ModularPanel buildUI(PlayerInventoryGuiData data, PanelSyncManager syncManager, UISettings settings) {
        ModularPanel panel = new ModularPanel("imex_creditcard").size(142, 90);
        final UUID playerUUID = data.getPlayer()
            .getUniqueID();

        // 2. Create a StringSyncHandler to bridge the Server and Client
        StringSyncValue balanceSync = new StringSyncValue(
            () -> TradingCurrencyManager.getBalance(playerUUID)
                .toString(),
            (newVisible) -> {});

        // 3. Register the sync handler with the PanelSyncManager
        syncManager.syncValue("card_balance", balanceSync);

        panel.child(
            IKey.str("Your balance")
                .asWidget()
                .padding(5))
            .child(
                IKey.dynamic(
                    // () -> {
                    // return TradingCurrencyManager.getBalance(
                    // data.getPlayer()
                    // .getUniqueID())
                    // .toString();
                    // })
                    balanceSync::getStringValue)
                    .asWidget()
                    .center());
        return panel;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            if (!stack.hasTagCompound()) initCard(stack, player);
            if (!player.getUniqueID()
                .equals(getOwnerID(stack))) {
                player.addChatMessage(new ChatComponentText("This card is not yours."));
                return stack;
            }
            if (player.isSneaking()) {
                player.addChatMessage(
                    new ChatComponentText(
                        "CVV code: " + stack.getTagCompound()
                            .getString("CVV")));
                return stack;
            }
            GuiFactories.playerInventory()
                .openFromMainHand(player);
        }
        return stack;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean huh) {
        super.addInformation(stack, player, tooltip, huh);
        if (stack.hasTagCompound()) {
            tooltip.add("Your new very best friend - the Credit Card!");
            tooltip.add("Provides an interface to your bank account.");
            tooltip.add("Place this card into the Trading Bay controller slot to provide your account for it.");
            tooltip.add(
                "Card ID: " + stack.getTagCompound()
                    .getString("UniqueId"));
            if (player.getUniqueID()
                .equals(getOwnerID(stack))) {
                tooltip.add("This card is owned by you.");
            } else {
                tooltip.add(
                    "This card is owned by " + stack.getTagCompound()
                        .getString("OwnerName"));
            }
        } else {
            tooltip.add("This is a new card without ID.");
        }
    }

    private void initCard(ItemStack stack, EntityPlayer player) {
        NBTTagCompound tag = new NBTTagCompound();
        if (!TradingCurrencyStorage.BALANCES.containsKey(player.getUniqueID())) {
            player.addChatMessage(
                new ChatComponentText("Welcome bonus from the Pessimistic Cash bank! You got 1000 credits."));
            TradingCurrencyManager.addBalance(player.getUniqueID(), 1000);
        }
        Random r = new Random();
        // final int cvv = r.nextInt(1000);
        // String cvv_code;
        // if (cvv < 10) {
        // cvv_code = "00" + cvv;
        // } else if (cvv < 100) {
        // cvv_code = "0" + cvv;
        // } else {
        // cvv_code = Integer.toString(cvv);
        // }
        String cvv_code = String.format("%03d", r.nextInt(1000));

        tag.setString(
            "UniqueId",
            UUID.randomUUID()
                .toString());
        tag.setString(
            "OwnerUniqueId",
            player.getUniqueID()
                .toString());
        tag.setString("CVV", cvv_code);
        tag.setString("OwnerName", player.getDisplayName());
        stack.setTagCompound(tag);
    }

    public static UUID getOwnerID(ItemStack item) {
        NBTTagCompound tag = item.stackTagCompound;
        return UUID.fromString(tag.getString("OwnerUniqueId"));
    }
}
