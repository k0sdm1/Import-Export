package com.brightpessimist.imex.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fluids.FluidRegistry;

import com.brightpessimist.imex.ImportExport;
import com.brightpessimist.imex.config.Config;
import com.brightpessimist.imex.currency.TradingCurrencyManager;
import com.brightpessimist.imex.network.MarketApiClient;
import com.brightpessimist.imex.network.MarketData;
import com.brightpessimist.imex.network.MarketGoods;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

import cpw.mods.fml.common.registry.GameRegistry;

public class DebugComand extends CommandBase {

    private static final Gson GSON = new Gson();

    private final List<String> aliases = Lists.newArrayList(ImportExport.MODID, "imex", "importexport");

    @Override
    public String getCommandName() {
        return "imex";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/imex debug returns debug data";
    }

    @Override
    public List<String> getCommandAliases() {
        return aliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (sender instanceof EntityPlayer player) {
            if (args.length == 0) {
                player.addChatMessage(new ChatComponentText("Available arguments: balance, debug"));
                return;
            }
            if (args.length == 1) {
                switch (args[0]) {
                    case "balance":
                        player.addChatMessage(
                            new ChatComponentText(
                                "Your balance: " + TradingCurrencyManager.getBalance(player.getUniqueID())));
                        break;
                    case "debug":
                        if (!player.getDisplayName()
                            .equals("Developer")) {
                            player.addChatMessage(new ChatComponentText("You are not the developer to use this."));
                            return;
                        }
                        try {
                            debug2(player);
                        } catch (Exception e) {
                            player.addChatMessage(new ChatComponentText("Error! " + e.toString()));
                        }
                        break;
                    default:
                        player.addChatMessage(new ChatComponentText("Unknows argument: " + args[0]));
                        break;
                }
                return;
            }
            if (!player.worldObj.isRemote) {
                player.addChatMessage(new ChatComponentText("debug successfull" + args));
            }
        }
    }

    private void debug2(EntityPlayer player) {

        List<MarketGoods.TradeableItemStack> items = new ArrayList<>();
        List<MarketGoods.TradeableFluidStack> fluids = new ArrayList<>();

        try {
            MarketData md = MarketApiClient.performHttpRequest();
            player.addChatMessage(new ChatComponentText(GSON.toJson(md)));

            for (MarketData.TradeableItem item : md.items) {
                if (item != null) {
                    // player.addChatMessage(new ChatComponentText("item: " + stack.toString() + " sold for " +
                    // item.sellPrice + " bought for " + item.buyPrice));
                    items.add(
                        new MarketGoods.TradeableItemStack(
                            new ItemStack(GameRegistry.findItem(item.modId, item.itemName), 1, item.metadata),
                            item.buyPrice,
                            item.sellPrice));
                }
            }
            for (MarketData.TradeableFluid fluid : md.fluids) {
                // player.addChatMessage(new ChatComponentText("fluid " + _fluid + " sold for " + fluid.sellPrice + "
                // bought for " + fluid.buyPrice));
                fluids.add(
                    new MarketGoods.TradeableFluidStack(
                        FluidRegistry.getFluidStack(fluid.fluidName, 1),
                        fluid.buyPrice,
                        fluid.sellPrice));
            }

            MarketGoods mg = new MarketGoods();
            mg.items = items;
            mg.fluids = fluids;

        } catch (Exception e) {
            player.addChatMessage(new ChatComponentText(e.toString()));
        }
    }

    private void debug(EntityPlayer player) {
        player.addChatMessage(new ChatComponentText("debug called"));
        URL url = null;
        try {
            url = URI.create(Config.api_url)
                .toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            conn.setRequestMethod("GET");
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        int status = 0;
        try {
            status = conn.getResponseCode();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Response Code: " + status);

        // 5. Read from getInputStream() on success, or getErrorStream() on error
        InputStream stream = null;
        try {
            stream = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line)
                    .append("\n");
            }
            System.out.println("Response Body:\n" + response);
            player.addChatMessage(new ChatComponentText("Response Body:\n" + response));
        } catch (Exception e) {
            player.addChatMessage(new ChatComponentText(e.toString()));
        }
        conn.disconnect();
    }
}
