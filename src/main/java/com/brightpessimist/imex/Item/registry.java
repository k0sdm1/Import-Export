package com.brightpessimist.imex.Item;

import net.minecraft.item.Item;

import cpw.mods.fml.common.registry.GameRegistry;

public class registry {

    public static Item itemcontract;
    public static Item priceList;
    public static Item creditCard;

    public static void register() {
        itemcontract = new Contract();
        GameRegistry.registerItem(itemcontract, "contract");
        priceList = new PriceList();
        GameRegistry.registerItem(priceList, "priceList");
        creditCard = new CreditCard();
        GameRegistry.registerItem(creditCard, "creditCard");
    }
}
