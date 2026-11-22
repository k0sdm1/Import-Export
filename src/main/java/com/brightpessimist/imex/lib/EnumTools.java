package com.brightpessimist.imex.lib;

public enum EnumTools {

    CREDITCARD(0, "Credit Card"),
    PRICELIST(1, "Price List"),
    CONTRACT(2, "Contract");

    private final int meta;
    private final String name;

    private EnumTools(int meta, String name) {
        this.meta = meta;
        this.name = name;
    }

    public int getMeta() {
        return this.meta;
    }

    public String getName() {
        return this.name;
    }

    public static int count() {
        return values().length;
    }
}
