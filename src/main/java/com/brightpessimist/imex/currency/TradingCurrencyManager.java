package com.brightpessimist.imex.currency;

import java.math.BigInteger;
import java.util.UUID;

import gregtech.api.util.GTLog;

/** Static API analogous to WirelessNetworkManager. Server-side only. */
public final class TradingCurrencyManager {

    private TradingCurrencyManager() {}

    public static BigInteger getBalance(UUID owner) {
        if (owner == null) return BigInteger.ZERO;
        return TradingCurrencyStorage.BALANCES.getOrDefault(owner, BigInteger.ZERO);
    }

    /** Add (positive) or charge (negative). Returns false and changes nothing if balance would drop below 0. */
    public static boolean addBalance(UUID owner, BigInteger amount) {
        if (owner == null || amount == null || amount.signum() == 0) return true;
        BigInteger total = getBalance(owner).add(amount);
        if (total.signum() < 0) return false;
        if (total.signum() == 0) TradingCurrencyStorage.BALANCES.remove(owner);
        else TradingCurrencyStorage.BALANCES.put(owner, total);
        markDirty();
        return true;
    }

    public static boolean addBalance(UUID owner, long amount) {
        return addBalance(owner, BigInteger.valueOf(amount));
    }

    public static boolean tryCharge(UUID owner, BigInteger amount) {
        return addBalance(owner, amount.negate());
    }

    public static boolean tryCharge(UUID owner, long amount) {
        return tryCharge(owner, BigInteger.valueOf(amount));
    }

    private static void markDirty() {
        try {
            if (TradingCurrencyWorldSavedData.INSTANCE != null) {
                TradingCurrencyWorldSavedData.INSTANCE.markDirty();
            }
        } catch (Exception e) {
            GTLog.conditionalLogger("[IMEX] Could not mark currency data dirty: " + e);
        }
    }
}
