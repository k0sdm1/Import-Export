package com.brightpessimist.imex.gui;

import com.brightpessimist.imex.Block.MTETradingBay;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;

import gregtech.api.modularui2.GTWidgetThemes;
import gregtech.common.gui.modularui.multiblock.base.MTEMultiBlockBaseGui;

public class MTETradingBayGui extends MTEMultiBlockBaseGui<MTETradingBay> {

    public MTETradingBayGui(MTETradingBay multiblock) {
        super(multiblock);
    }

    private BooleanSyncValue bayModeSyncer;

    @Override
    protected boolean shouldDisplayBatchMode() {
        return false;
    }

    @Override
    protected boolean shouldDisplayInputSeparation() {
        return false;
    }

    @Override
    protected boolean shouldDisplayRecipeLock() {
        return false;
    }

    protected ToggleButton create_toggle_import_export(PanelSyncManager syncManager) {
        BooleanSyncValue toggle_syncer = new BooleanSyncValue(multiblock::is_staling);
        syncManager.syncValue("is_staling", toggle_syncer);
        ToggleButton button = new ToggleButton() {

            @Override
            public WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
                if (toggle_syncer.getBoolValue()) return theme.getWidgetTheme(GTWidgetThemes.TOGGLE_BUTTON_DISABLED);
                return super.getWidgetThemeInternal(theme);
            }
        };
        if (!multiblock.is_staling()) {
            button.widgetTheme(GTWidgetThemes.TOGGLE_BUTTON_DISABLED);
        }
        // BooleanSyncValue conditionSyncer = new BooleanSyncValue(multiblock::is_staling).allowC2S();
        toggle_syncer.changeListener(() -> {
            if (toggle_syncer.getBoolValue()) button.widgetTheme(IThemeApi.TOGGLE_BUTTON);
            else button.widgetTheme(GTWidgetThemes.TOGGLE_BUTTON_DISABLED);
        })
            .allowC2S();
        return button;
    }

    @Override
    protected Flow createRightPanelGapRow(ModularPanel parent, PanelSyncManager syncManager) {
        return super.createRightPanelGapRow(parent, syncManager).child(
            create_toggle_import_export(syncManager).value(bayModeSyncer.allowC2S()) // Binds the button state to the
                                                                                     // sync value
                .playClickSound(multiblock.is_staling())
                .width(50) // Adjust width to fit the text comfortably
                .height(20)
                .tooltip(
                    t -> t.add(
                        multiblock.is_staling() ? "Switch bay mode between Import and Export"
                            : "Can't swich mode if not staling"))
                .widgetTheme(multiblock.is_staling() ? IThemeApi.TOGGLE_BUTTON : GTWidgetThemes.TOGGLE_BUTTON_DISABLED)
                .child(
                    new TextWidget<>(IKey.dynamic(multiblock::bay_mode_representation))
                        // IKey.dynamic ensures the text re-evaluates whenever the sync value changes
                        .center()));
    }

    @Override
    protected int getTerminalRowHeight() {
        return super.getTerminalRowHeight() + 50;
    }

    @Override
    protected int getBasePanelHeight() {
        return super.getBasePanelHeight() + 50;
    }

    @Override
    protected ListWidget<IWidget, ?> createTerminalTextWidget(PanelSyncManager syncManager, ModularPanel parent) {
        StringSyncValue state_sync = new StringSyncValue(multiblock::get_state_text);
        syncManager.syncValue("state_sync", state_sync);
        state_sync.changeListener(() -> multiblock.get_state_text())
            .allowC2S();

        IntSyncValue running_tick_sync = new IntSyncValue(multiblock::get_running_second);
        syncManager.syncValue("running_tick_sync", running_tick_sync);
        running_tick_sync.changeListener(() -> multiblock.get_running_second())
            .allowC2S();

        IntSyncValue total_time_sync = new IntSyncValue(multiblock::get_total_import_time);
        syncManager.syncValue("total_time_sync", total_time_sync);
        total_time_sync.changeListener(() -> multiblock.get_total_import_time())
            .allowC2S();

        ListWidget<IWidget, ?> terminal = super.createTerminalTextWidget(syncManager, parent);
        return terminal.child(
            IKey.dynamic(
                () -> state_sync.getStringValue() + ", progress: "
                    + running_tick_sync.getIntValue()
                    + " / "
                    + total_time_sync.getIntValue())
                .asWidget()
                .widgetTheme(GTWidgetThemes.DISPLAY_TEXT_WHITE));
    }

    @Override
    protected void registerSyncValues(PanelSyncManager syncManager) {
        super.registerSyncValues(syncManager);
        // allowC2S() is crucial: it allows the client to send the toggle action to the server
        bayModeSyncer = new BooleanSyncValue(multiblock::get_bay_mode, multiblock::set_bay_mode).allowC2S();
        syncManager.syncValue("bay_mode", bayModeSyncer);
    }
}
