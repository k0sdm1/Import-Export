package com.brightpessimist.imex.gui;

import java.util.function.Predicate;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import com.brightpessimist.imex.Block.MTETradingTerminal;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.gtnewhorizon.gtnhlib.util.numberformatting.NumberFormatUtil;

import gregtech.api.modularui2.GTWidgetThemes;
import gregtech.api.modularui2.common.CommonWidgets;
import gregtech.common.gui.modularui.singleblock.base.MTEMachineWithFluidScreenBaseGui;

public class MTETradingTerminalGui<T extends MTETradingTerminal> extends MTEMachineWithFluidScreenBaseGui<T> {

    private StringSyncValue creditsSync;

    public MTETradingTerminalGui(T machine) {
        super(machine);
    }

    // --- this is the fix for "gets off the ui" ---
    @Override
    protected int getBasePanelWidth() {
        return 232;
    }

    @Override
    protected int getBasePanelHeight() {
        return 196;
    }

    @Override
    protected void registerSyncValues(PanelSyncManager syncManager) {
        super.registerSyncValues(syncManager);

        // credits display: server -> client
        creditsSync = new StringSyncValue(() -> String.valueOf(machine.getCredits()), s -> {});
        syncManager.syncValue("credits", creditsSync);

        // buy buttons: client -> server, one sync value per product
        registerBuyButton(syncManager, "buy_iron", new ItemStack(Items.iron_ingot), 50);
        registerBuyButton(syncManager, "buy_gold", new ItemStack(Items.gold_ingot), 150);
        registerBuyButton(syncManager, "buy_diamond", new ItemStack(Items.diamond), 500);
    }

    private void registerBuyButton(PanelSyncManager syncManager, String key, ItemStack product, long price) {
        BooleanSyncValue sync = new BooleanSyncValue(() -> false, pressed -> {
            // This setter runs ON THE SERVER when the player clicks the button.
            if (pressed) machine.buyItem(product.copy(), price);
        }).allowC2S();
        syncManager.syncValue(key, sync);
    }

    @Override
    protected ParentWidget<?> createScreen(ModularPanel panel, PanelSyncManager syncManager, IFluidTank fluidTank) {
        ParentWidget<?> screen = CommonWidgets.createFluidScreen(71, 54);
        Flow textColumn = Flow.column()
            .childPadding(1)
            .crossAxisAlignment(Alignment.CrossAxis.START);
        textColumn.child(
            IKey.lang("GT5U.machines.basic_tank.liquid_amount")
                .asWidget()
                .widgetTheme(GTWidgetThemes.DISPLAY_TEXT_WHITE));
        textColumn.child(
            IKey.dynamic(() -> NumberFormatUtil.formatNumber(fluidTank.getFluidAmount()))
                .asWidget()
                .widgetTheme(GTWidgetThemes.DISPLAY_TEXT_WHITE));
        screen.child(textColumn);
        screen.child(this.createFluidSlot(panel, syncManager, fluidTank));
        return screen;
    }

    @Override
    protected ParentWidget<?> createContentSection(ModularPanel panel, PanelSyncManager syncManager) {
        return Flow.row()
            .fullWidth()
            .coverChildrenHeight()
            .mainAxisAlignment(Alignment.MainAxis.CENTER)
            .crossAxisAlignment(Alignment.CrossAxis.START)
            .childPadding(4)
            // .child(createScreen(panel, syncManager, machine.getFluidTank())) // base class: 71x54 screen + fluid slot
            .child(createShopColumn())
            .child(
                createIO(panel, syncManager, machine.getInputSlot(), machine.getOutputSlot(), machine.getFluidTank()));
    }

    private Flow createShopColumn() {
        return Flow.column()
            .coverChildren()
            .childPadding(2)
            .child(
                IKey.str("Shop")
                    .asWidget()
                    .widgetTheme(GTWidgetThemes.TEXT_TITLE))
            .child(
                IKey.dynamic(creditsSync::getValue)
                    .asWidget()
                    .widgetTheme(GTWidgetThemes.DISPLAY_TEXT_WHITE))
            .child(buyRow("buy_iron", "Iron Ingot (50)"))
            .child(buyRow("buy_gold", "Gold Ingot (150)"))
            .child(buyRow("buy_diamond", "Diamond (500)"));
    }

    private Flow buyRow(String syncKey, String label) {
        return Flow.row()
            .coverChildren()
            .childPadding(2)
            .crossAxisAlignment(Alignment.CrossAxis.CENTER)
            .child(
                new ToggleButton().syncHandler(syncKey)
                    .size(16))
            .child(
                IKey.str(label)
                    .asWidget()
                    .widgetTheme(GTWidgetThemes.DISPLAY_TEXT_WHITE));
    }

    @Override
    protected Predicate<FluidStack> getFluidSlotFilter() {
        return machine::isFluidInputAllowed;
    }
}
