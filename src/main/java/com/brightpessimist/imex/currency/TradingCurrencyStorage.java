package com.brightpessimist.imex.currency;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Static balance holder, analogous to GlobalVariableStorage.GlobalEnergy. */
public final class TradingCurrencyStorage {

    public static final Map<UUID, BigInteger> BALANCES = new HashMap<>();

    private TradingCurrencyStorage() {}
}
