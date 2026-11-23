package com.brightpessimist.imex.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Iterables;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
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
import org.apache.commons.lang3.StringUtils;

public class DebugComand extends CommandBase {

    private static final Gson GSON = new Gson();

    private final List<String> aliases = Lists.newArrayList(ImportExport.MODID, "imex", "importexport");

    private final List<String> args_completions = Lists.newArrayList("debug", "balance", "add", "remove");

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

    protected boolean isOp(EntityPlayer player) {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152596_g(player.getGameProfile());
    }

    protected EntityPlayer getPlayerByName(String username) {
        return MinecraftServer.getServer().getConfigurationManager().func_152612_a(username);
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return index == 2;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            return Collections.emptyList();
        } else if (isUsernameIndex(args, args.length - 1)) {
            return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
        }
        return getListOfStringsFromIterableMatchingLastWord(args, Iterables.transform(args_completions, Object::toString));
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
            }
            if (args.length >= 2) {
                if (args[0].equals("balance")) {
                    if (args[1].equals("add")) {
                        if (!isOp(player)) {
                            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Not enough permission to set balance"));
                            return;
                        }
                        if (args.length == 2) {
                            player.addChatMessage(new ChatComponentText("Not enough arguments, expected balance add [player] amount"));
                            return;
                        }
                        if (args.length == 3) {
                            if (StringUtils.isNumeric(args[2])) {
                                TradingCurrencyManager.addBalance(player.getUniqueID(), new BigInteger(args[2]));
                                player.addChatMessage(new ChatComponentText("Added " + args[2] + " to your balance."));
                            }
                        }
                        if (args.length == 4) {
                            EntityPlayer target = getPlayerByName(args[2]);
                            if (target == null) {
                                player.addChatMessage(new ChatComponentText("Player with name " + args[2] + " is not found."));
                                return;
                            }
                            if (!StringUtils.isNumeric(args[3])) {
                                player.addChatMessage(new ChatComponentText(args[3] + " not a number."));
                                return;
                            }
                            TradingCurrencyManager.addBalance(target.getUniqueID(), new BigInteger(args[3]));
                            player.addChatMessage(new ChatComponentText("Added " + args[3] + " to " + args[2]));
                            return;
                        }
                    }
                    if (args[1].equals("remove")) {
                        if (!isOp(player)) {
                            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Not enough permission to set balance"));
                            return;
                        }
                        if (args.length == 2) {
                            player.addChatMessage(new ChatComponentText("Not enough arguments, expected balance remove [player] amount"));
                            return;
                        }
                        if (args.length == 3) {
                            if (StringUtils.isNumeric(args[2])) {
                                boolean result = TradingCurrencyManager.tryCharge(player.getUniqueID(), new BigInteger(args[2]));
                                if (result) {
                                    player.addChatMessage(new ChatComponentText("Removed " + args[2] + " to your balance."));
                                } else {
                                    TradingCurrencyManager.tryCharge(player.getUniqueID(), TradingCurrencyManager.getBalance(player.getUniqueID()));
                                    player.addChatMessage(new ChatComponentText("Tried to remove more than player had, balance set to 0"));
                                }
                            }
                        }
                        if (args.length == 4) {
                            EntityPlayer target = getPlayerByName(args[2]);
                            if (target == null) {
                                player.addChatMessage(new ChatComponentText("Player with name " + args[2] + " is not found."));
                                return;
                            }
                            if (!StringUtils.isNumeric(args[3])) {
                                player.addChatMessage(new ChatComponentText(args[3] + " not a number."));
                                return;
                            }
                            boolean result = TradingCurrencyManager.tryCharge(target.getUniqueID(), new BigInteger(args[2]));
                            if (result) {
                                player.addChatMessage(new ChatComponentText("Removed " + args[2] + " to your balance."));
                            } else {
                                TradingCurrencyManager.tryCharge(target.getUniqueID(), TradingCurrencyManager.getBalance(target.getUniqueID()));
                                player.addChatMessage(new ChatComponentText("Tried to remove more than player had, balance set to 0"));
                            }
                            return;
                        }
                    }
                } else {
                    player.addChatMessage(new ChatComponentText("Unknows argument: " + args[0]));
                    return;
                }
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
