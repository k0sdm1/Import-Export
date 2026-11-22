package com.brightpessimist.imex.currency;

import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.WorldEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class TradingCurrencyWorldSavedData extends WorldSavedData {

    public static TradingCurrencyWorldSavedData INSTANCE;
    private static final String DATA_NAME = "IMEX_TradingCurrencyWorldSavedData";
    private static final String TAG = "IMEX_CurrencyBalances";

    public TradingCurrencyWorldSavedData() {
        super(DATA_NAME);
    }

    public TradingCurrencyWorldSavedData(String name) {
        super(name);
    }

    private static void loadInstance(World world) {
        TradingCurrencyStorage.BALANCES.clear();
        MapStorage storage = world.mapStorage;
        INSTANCE = (TradingCurrencyWorldSavedData) storage.loadData(TradingCurrencyWorldSavedData.class, DATA_NAME);
        if (INSTANCE == null) {
            INSTANCE = new TradingCurrencyWorldSavedData();
            storage.setData(DATA_NAME, INSTANCE);
        }
        INSTANCE.markDirty();
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (!event.world.isRemote && event.world.provider.dimensionId == 0) {
            loadInstance(event.world);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList(TAG, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound t = list.getCompoundTagAt(i);
            try {
                UUID owner = UUID.fromString(t.getString("owner"));
                BigInteger balance = new BigInteger(t.getByteArray("balance"));
                if (balance.signum() > 0) TradingCurrencyStorage.BALANCES.put(owner, balance);
            } catch (RuntimeException ignored) {
                // malformed entry; keep loading the rest
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<UUID, BigInteger> e : TradingCurrencyStorage.BALANCES.entrySet()) {
            if (e.getValue() == null || e.getValue()
                .signum() <= 0) continue;
            NBTTagCompound t = new NBTTagCompound();
            t.setString(
                "owner",
                e.getKey()
                    .toString());
            t.setByteArray(
                "balance",
                e.getValue()
                    .toByteArray());
            list.appendTag(t);
        }
        nbt.setTag(TAG, list);
    }
}
