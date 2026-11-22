package com.brightpessimist.imex.Item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import com.brightpessimist.imex.lib.EnumTools;

public class ItemMetaTool extends Item {

    protected IIcon[] icons;

    public ItemMetaTool() {
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        // this.setUnlocalizedName("");
        this.setCreativeTab(CreativeTabs.tabMisc);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int metadata = stack.getItemDamage();
        EnumTools tool = EnumTools.values()[metadata];
        return "imex_itemmetatool" + tool.getName()
            .replaceAll("\\s+", "_")
            .toLowerCase();
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {
        for (EnumTools tool : EnumTools.values()) {
            list.add(new ItemStack(item, 1, tool.getMeta()));
        }
    }

    @Override
    public void registerIcons(IIconRegister register) {
        icons = new IIcon[EnumTools.count()];
        for (EnumTools tool : EnumTools.values()) {
            icons[tool.getMeta()] = register.registerIcon(
                "tool_" + tool.getName()
                    .replaceAll("\\s+", "_")
                    .toLowerCase());
        }
    }

    @Override
    public IIcon getIconFromDamage(int meta) {
        if (meta >= 0 && meta < EnumTools.count()) {
            return icons[meta];
        }
        return null;
    }
}
