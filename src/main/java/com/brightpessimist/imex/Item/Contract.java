package com.brightpessimist.imex.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.brightpessimist.imex.ImportExport;
import com.cleanroommc.modularui.drawable.UITexture;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

import com.brightpessimist.imex.market.MarketCache;
import com.brightpessimist.imex.market.MarketSnapshot;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.utils.fluid.FluidInteractions;
import com.cleanroommc.modularui.utils.item.ItemStackHandler;
import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.FluidSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import gregtech.api.util.GTDataUtils;
import gregtech.api.util.GTUtility;
import gregtech.common.gui.modularui.util.FilterSlot;

public class Contract extends Item implements IGuiHolder<PlayerInventoryGuiData> {

    public static final int MAX_ROWS = 8;
    public static final int MAX_FLUID_ROWS = 4;

    private static final String TAG_ENTRIES = "Entries";
    private static final String TAG_ITEM = "Item";
    private static final String TAG_FLUID_ENTRIES = "FluidEntries";
    private static final String TAG_FLUID = "Fluid";
    private static final String TAG_AMOUNT = "Amount";

    public Contract() {
        super();
        this.setUnlocalizedName("contract");
        this.setTextureName("imex:contract");
        this.setCreativeTab(CreativeTabs.tabMisc);
        this.setMaxStackSize(1);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            if (!stack.hasTagCompound()) create(stack, player);
            GuiFactories.playerInventory()
                .openFromMainHand(player);
        }
        return stack;
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> tooltip,
        boolean advancedItemTooltips) {
        tooltip.add("Your best friend - Contract.");
        tooltip.add("Allows to make an orader in your Trading Bay");
        tooltip.add("Up to 8 item types and 4 fluids.");
        super.addInformation(itemStack, player, tooltip, advancedItemTooltips);

    }

    @Override
    public ModularPanel buildUI(PlayerInventoryGuiData data, PanelSyncManager syncManager, UISettings settings) {
        // MarketSnapshot snapshot = MarketCache.get();
        final ItemStackHandler rows = new ItemStackHandler(MAX_ROWS);
        final int[] amounts = new int[MAX_ROWS];
        final int[] rowCount = { 1 };
        // final String[] itemPrices = new String[MAX_ROWS];
        // for (int i = 0; i < MAX_ROWS; i++) {
        // itemPrices[i] = "price is " + i;
        // }
        // fluid "orders": pure data, never actual fluid storage
        final FluidStack[] fluids = new FluidStack[MAX_FLUID_ROWS];
        final int[] fluidAmounts = new int[MAX_FLUID_ROWS];
        final int[] fluidRowCount = { 1 };
        // final String[] fluidPrices = new String[MAX_FLUID_ROWS];
        // for (int i = 0; i < MAX_FLUID_ROWS; i++) {
        // fluidPrices[i] = "fluid price is " + i;
        // }

        // ---------- load ----------
        ItemStack start = data.getUsedItemStack();
        NBTTagCompound startTag = start != null ? start.getTagCompound() : null;
        if (startTag != null) {
            NBTTagList list = startTag.getTagList(TAG_ENTRIES, Constants.NBT.TAG_COMPOUND);
            rowCount[0] = Math.max(1, Math.min(MAX_ROWS, list.tagCount()));
            for (int i = 0; i < rowCount[0]; i++) {
                NBTTagCompound e = list.getCompoundTagAt(i);
                ItemStack s = e.hasKey(TAG_ITEM, Constants.NBT.TAG_COMPOUND)
                    ? ItemStack.loadItemStackFromNBT(e.getCompoundTag(TAG_ITEM))
                    : null;
                if (s != null) s.stackSize = 1;
                rows.setStackInSlot(i, s);
                amounts[i] = e.getInteger(TAG_AMOUNT);
            }

            NBTTagList fluidList = startTag.getTagList(TAG_FLUID_ENTRIES, Constants.NBT.TAG_COMPOUND);
            fluidRowCount[0] = Math.max(1, Math.min(MAX_FLUID_ROWS, fluidList.tagCount()));
            for (int i = 0; i < fluidRowCount[0]; i++) {
                NBTTagCompound e = fluidList.getCompoundTagAt(i);
                FluidStack f = e.hasKey(TAG_FLUID, Constants.NBT.TAG_COMPOUND)
                    ? FluidStack.loadFluidStackFromNBT(e.getCompoundTag(TAG_FLUID))
                    : null;
                if (f != null) f.amount = 1; // amount to order lives in the text field, not the stack
                fluids[i] = f;
                fluidAmounts[i] = e.getInteger(TAG_AMOUNT);
            }
        }
        for (int i = 0; i < MAX_ROWS; i++) if (amounts[i] < 1) amounts[i] = 1;
        for (int i = 0; i < MAX_FLUID_ROWS; i++) if (fluidAmounts[i] < 1) fluidAmounts[i] = 1;

        final Runnable save = () -> saveToItem(
            data,
            rows,
            amounts,
            rowCount[0],
            fluids,
            fluidAmounts,
            fluidRowCount[0]);

        // row counts are authoritative on the server; buttons use InteractionSyncHandler for C2S
        syncManager.syncValue("row_count", new IntSyncValue(() -> rowCount[0], v -> rowCount[0] = v));
        syncManager.syncValue("fluid_row_count", new IntSyncValue(() -> fluidRowCount[0], v -> fluidRowCount[0] = v));

        // ---------- widgets: ALWAYS MAX_ROWS, extras are just disabled ----------
        Flow column = Flow.column()
            .collapseDisabledChild() // <-- opt-in on Flow!
            .childPadding(2)
            .padding(4);

        column.child(
            IKey.str("Items")
                .asWidget());

        for (int i = 0; i < MAX_ROWS; i++) {
            final int row = i;

            TextFieldWidget amountField = new TextFieldWidget();
            amountField.numbersInt(1, Integer.MAX_VALUE);
            amountField.formatAsInteger(true);
            amountField.value(new IntSyncValue(() -> amounts[row], v -> {
                amounts[row] = v;
                save.run();
            }).allowC2S());
            amountField.width(60);

            Flow rowFlow = Flow.row()
                .coverChildren()
                .collapseDisabledChild() // so the minus button can collapse
                .childPadding(3);

            rowFlow.child(
                new FilterSlot().slot(
                    new ModularSlot(rows, row).accessibility(true, false)
                        .changeListener((newItem, onlyAmount, client, init) -> {
                            if (client || init) return;
                            if (newItem != null) newItem.stackSize = 1;
                            save.run();
                        })));
            rowFlow.child(amountField);
            // here's dynamic update works good
            rowFlow.child(IKey.dynamic(() -> {
                MarketSnapshot snapshot = MarketCache.get();
                if (rows.getStackInSlot(row) == null) return "";
                double buyPrice = 0;
                for (var item : snapshot.goods.items) {
                    if (rows.getStackInSlot(row)
                        .getItem() == item.itemStack.getItem()
                        && rows.getStackInSlot(row)
                            .getItemDamage() == item.itemStack.getItemDamage()) {
                        buyPrice = item.buyPrice;
                    }
                }
                if (buyPrice > 0) {
                    return "x " + buyPrice;
                } else {
                    return "Not found!";
                }
            })
                .asWidget()
                .tooltip(t -> t.addLine("Price for the one item")));
            rowFlow.child(
                IKey.str(" to order")
                    .asWidget());
            rowFlow.child(
                new ButtonWidget<>().overlay(GuiTextures.REMOVE)
                    .tooltip(
                        t -> t.textColor(Color.RED.main)
                            .addLine("Are you sure about that?"))
                    .setEnabledIf(w -> rowCount[0] > 1) // hidden when only one row left
                    .syncHandler(new InteractionSyncHandler().setOnMousePressed(mouse -> {
                        if (rowCount[0] <= 1) return;
                        for (int j = row; j < rowCount[0] - 1; j++) { // shift everything up
                            rows.setStackInSlot(j, rows.getStackInSlot(j + 1));
                            amounts[j] = amounts[j + 1];
                        }
                        int last = rowCount[0] - 1;
                        rows.setStackInSlot(last, null);
                        amounts[last] = 1;
                        rowCount[0]--;
                        save.run();
                    })))
                .left(5);

            rowFlow.setEnabledIf(w -> row < rowCount[0]); // hidden beyond the current count
            column.child(rowFlow);
        }

        // plus button is the last child -> always sits under the last visible row
        column.child(
            new ButtonWidget<>().overlay(GuiTextures.ADD)
                .playClickSound(true)
                .setEnabledIf(w -> rowCount[0] < MAX_ROWS) // gone at 8 rows
                .syncHandler(new InteractionSyncHandler().setOnMousePressed(mouse -> {
                    if (rowCount[0] >= MAX_ROWS) return;
                    rowCount[0]++;
                    save.run();
                }))
                .left(5));

        // ---------- fluid rows: same system as items ----------
        column.child(
            IKey.str("Fluids")
                .asWidget());

        for (int i = 0; i < MAX_FLUID_ROWS; i++) {
            final int row = i;

            TextFieldWidget fluidAmountField = new TextFieldWidget();
            fluidAmountField.numbersInt(1000, Integer.MAX_VALUE);
            fluidAmountField.formatAsInteger(true);
            fluidAmountField.value(new IntSyncValue(() -> fluidAmounts[row], v -> {
                fluidAmounts[row] = v;
                save.run();
            }).allowC2S());
            fluidAmountField.width(60);

            FluidSlot fluidSlot = new FluidSlot() {

                @Override
                protected void addToolTip(RichTooltip tooltip) {
                    FluidStack fluid = getFluidStack();
                    if (fluid != null) {
                        tooltip.addFromFluid(fluid);
                        tooltip.addLine(IKey.lang("modularui2.fluid.phantom.clear"));
                    } else {
                        tooltip.addLine(IKey.lang("modularui2.fluid.empty"));
                    }
                }
            }.syncHandler(new FluidSlotSyncHandler(new ConfigFluidTank(fluids, row)) {

                @Override
                public void tryScrollPhantom(MouseData mouseData) {} // amount is set via the text field

                @Override
                protected void tryClickPhantom(MouseData mouseData, ItemStack cursorStack) {
                    // runs on the server only; MUI syncs the resulting fluid back to the client
                    if (mouseData.mouseButton != 0) return;

                    FluidStack heldFluid = FluidInteractions.getFluidForItem(cursorStack);

                    // no duplicates across rows (null = click with empty hand -> clear this row)
                    if (heldFluid != null && containsSuchFluid(fluids, heldFluid, row)) return;

                    fluids[row] = GTUtility.copyAmount(1, heldFluid);
                    save.run();
                }
            }.phantom(true)
                .controlsAmount(false));

            Flow rowFlow = Flow.row()
                .coverChildren()
                .collapseDisabledChild()
                .childPadding(3);

            rowFlow.child(fluidSlot)
                .left(5);
            rowFlow.child(fluidAmountField);
            // no dynamic update of the price value here which needs to be fixed
            rowFlow.child(IKey.dynamic(() -> {
                MarketSnapshot snapshot = MarketCache.get();
                if (fluids[row] == null) return "";
                double buyPrice = 0;
                for (var fluid : snapshot.goods.fluids) {
                    if (fluids[row].getFluid() == fluid.fluidStack.getFluid()) {
                        buyPrice = fluid.buyPrice;
                        break;
                    }
                }
                if (buyPrice > 0) {
                    return "x " + buyPrice;
                } else {
                    return "Not found!";
                }
            })
                .asWidget()
                .tooltip(t -> t.addLine("Price for the one bucket of fluid")));
            rowFlow.child(
                IKey.str(" L to order")
                    .asWidget());
            rowFlow.child(
                new ButtonWidget<>().overlay(GuiTextures.REMOVE)
                    .tooltip(t -> t.addLine("Remove this line"))
                    .setEnabledIf(w -> fluidRowCount[0] > 1)
                    .syncHandler(new InteractionSyncHandler().setOnMousePressed(mouse -> {
                        if (fluidRowCount[0] <= 1) return;
                        for (int j = row; j < fluidRowCount[0] - 1; j++) {
                            fluids[j] = fluids[j + 1];
                            fluidAmounts[j] = fluidAmounts[j + 1];
                        }
                        int last = fluidRowCount[0] - 1;
                        fluids[last] = null;
                        fluidAmounts[last] = 1;
                        fluidRowCount[0]--;
                        save.run();
                    })));

            rowFlow.setEnabledIf(w -> row < fluidRowCount[0]);
            column.child(rowFlow);
        }

        column.child(
            new ButtonWidget<>().overlay(GuiTextures.ADD)
                .playClickSound(true)
                .setEnabledIf(w -> fluidRowCount[0] < MAX_FLUID_ROWS)
                .syncHandler(new InteractionSyncHandler().setOnMousePressed(mouse -> {
                    if (fluidRowCount[0] >= MAX_FLUID_ROWS) return;
                    fluidRowCount[0]++;
                    save.run();
                }))
                .left(5));

        Flow totalFlow = Flow.column()
            .coverChildren()
            .childPadding(3);

        totalFlow.child(
            IKey.str("Total:")
                .asWidget());
        totalFlow.child(IKey.dynamic(() -> {
            try {
                return get_total_prices(rows, amounts, fluids, fluidAmounts);
            } catch (Exception e) {
                return e.toString();
            }
        })
            .asWidget());

        // column.child(totalFlow);

        ModularPanel panel = new ModularPanel("contract").size(250, 320).background(
            UITexture.builder().location(ImportExport.MODID, "gui/contract.png").imageSize(142, 90).adaptable(1).canApplyTheme().build()
        );
        panel.child(column).child(totalFlow.bottom(5).left(5));
        syncManager.addCloseListener(player -> save.run());
        return panel;
    }

    /** True if any row other than {@code excludeIndex} already orders this fluid. */
    private static boolean containsSuchFluid(FluidStack[] fluids, FluidStack target, int excludeIndex) {
        for (int i = 0; i < fluids.length; i++) {
            if (i == excludeIndex) continue;
            if (fluids[i] != null && GTUtility.areFluidsEqual(fluids[i], target)) return true;
        }
        return false;
    }

    // ---------- persistence: both lists rewritten completely ----------
    private void saveToItem(PlayerInventoryGuiData data, ItemStackHandler rows, int[] amounts, int count,
        FluidStack[] fluids, int[] fluidAmounts, int fluidCount) {
        EntityPlayer player = data.getPlayer();
        if (player.worldObj.isRemote) return;

        ItemStack live = data.getUsedItemStack();
        if (live == null || live.getItem() != this) return;

        NBTTagCompound tag = live.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            live.setTagCompound(tag);
        }

        NBTTagList list = new NBTTagList();
        for (int i = 0; i < count; i++) {
            NBTTagCompound entry = new NBTTagCompound();
            ItemStack s = rows.getStackInSlot(i);
            if (s != null) {
                s.stackSize = 1;
                NBTTagCompound itemTag = new NBTTagCompound();
                s.writeToNBT(itemTag);
                entry.setTag(TAG_ITEM, itemTag);
            }
            entry.setInteger(TAG_AMOUNT, Math.max(1, amounts[i]));
            list.appendTag(entry);
        }
        tag.setTag(TAG_ENTRIES, list);

        NBTTagList fluidList = new NBTTagList();
        for (int i = 0; i < fluidCount; i++) {
            NBTTagCompound entry = new NBTTagCompound();
            FluidStack f = fluids[i];
            if (f != null) {
                entry.setTag(
                    TAG_FLUID,
                    GTUtility.copyAmount(1, f)
                        .writeToNBT(new NBTTagCompound()));
            }
            entry.setInteger(TAG_AMOUNT, Math.max(1, fluidAmounts[i]));
            fluidList.appendTag(entry);
        }
        tag.setTag(TAG_FLUID_ENTRIES, fluidList); // replaces the old list -> no stale rows survive

        data.setUsedItemStack(live);
        player.inventory.markDirty();
    }

    // ---------- API for the trading bay ----------
    public static final class Order {

        public final ItemStack item; // reference only, stackSize == 1
        public final int amount;

        Order(ItemStack item, int amount) {
            this.item = item;
            this.amount = amount;
        }
    }

    public static final class FluidOrder {

        public final FluidStack fluid; // reference only, amount == 1
        public final int amount; // liters to order

        FluidOrder(FluidStack fluid, int amount) {
            this.fluid = fluid;
            this.amount = amount;
        }
    }

    /** Server side: what this contract asks for. Empty lines are skipped. */
    public static List<Order> readOrders(ItemStack contract) {
        List<Order> out = new ArrayList<>();
        if (contract == null || !(contract.getItem() instanceof Contract) || !contract.hasTagCompound()) return out;
        NBTTagList list = contract.getTagCompound()
            .getTagList(TAG_ENTRIES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound e = list.getCompoundTagAt(i);
            if (!e.hasKey(TAG_ITEM, Constants.NBT.TAG_COMPOUND)) continue;
            ItemStack s = ItemStack.loadItemStackFromNBT(e.getCompoundTag(TAG_ITEM));
            if (s == null) continue;
            s.stackSize = 1;
            out.add(new Order(s, Math.max(1, e.getInteger(TAG_AMOUNT))));
        }
        return out;
    }

    /** Server side: fluids this contract asks for. Empty lines are skipped. */
    public static List<FluidOrder> readFluidOrders(ItemStack contract) {
        List<FluidOrder> out = new ArrayList<>();
        if (contract == null || !(contract.getItem() instanceof Contract) || !contract.hasTagCompound()) return out;
        NBTTagList list = contract.getTagCompound()
            .getTagList(TAG_FLUID_ENTRIES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound e = list.getCompoundTagAt(i);
            if (!e.hasKey(TAG_FLUID, Constants.NBT.TAG_COMPOUND)) continue;
            FluidStack f = FluidStack.loadFluidStackFromNBT(e.getCompoundTag(TAG_FLUID));
            if (f == null) continue;
            f.amount = 1;
            out.add(new FluidOrder(f, Math.max(1, e.getInteger(TAG_AMOUNT))));
        }
        return out;
    }

    private void create(ItemStack stack, EntityPlayer player) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString(
            "UniqueId",
            UUID.randomUUID()
                .toString());
        tag.setString(
            "OwnerUniqueId",
            player.getUniqueID()
                .toString());
        tag.setInteger("UsageCount", 0);
        stack.setTagCompound(tag);
    }

    public static UUID getOwnerID(ItemStack item) {
        NBTTagCompound tag = item.stackTagCompound;
        return UUID.fromString(tag.getString("OwnerUniqueId"));
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return 1;
    }

    public void writeToNBT(FluidStack fluid_stack, NBTTagCompound tag) {
        tag.setTag("fluid", fluid_stack.writeToNBT(new NBTTagCompound()));
    }

    public static FluidStack readFromNBT(NBTTagCompound tag) {
        return FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("fluid"));
    }

    protected class ConfigFluidTank implements IFluidTank {

        private final FluidStack[] slots;
        private final int slotIndex;

        public ConfigFluidTank(FluidStack[] slots, int slotIndex) {
            this.slots = slots;
            this.slotIndex = slotIndex;
        }

        @Override
        public FluidStack getFluid() {
            FluidStack slot = GTDataUtils.getIndexSafe(slots, slotIndex);

            return slot;
        }

        @Override
        public int getFluidAmount() {
            FluidStack slot = GTDataUtils.getIndexSafe(slots, slotIndex);

            return slot == null ? 0 : 1;
        }

        @Override
        public int getCapacity() {
            return 1;
        }

        @Override
        public FluidTankInfo getInfo() {
            return new FluidTankInfo(this);
        }

        // @Override
        // public int fill(FluidStack resource, boolean doFill) {
        // return 0;
        // }
        //
        // @Override
        // public FluidStack drain(int maxDrain, boolean doDrain) {
        // return null;
        // }
        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (resource == null || resource.amount <= 0) return 0;
            FluidStack current = GTDataUtils.getIndexSafe(slots, slotIndex);
            if (current != null && !current.isFluidEqual(resource)) return 0;
            if (doFill) {
                slots[slotIndex] = GTUtility.copyAmount(1, resource); // config slot: always amount 1
            }
            return 1; // capacity is 1
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            FluidStack current = GTDataUtils.getIndexSafe(slots, slotIndex);
            if (current == null || maxDrain <= 0) return null;
            FluidStack drained = GTUtility.copyAmount(1, current);
            if (doDrain) {
                slots[slotIndex] = null; // any drain empties this 1 L config slot
            }
            return drained;
        }

        // @Override
        // public FluidStack drain(FluidStack resource, boolean doDrain) {
        // FluidStack current = GTDataUtils.getIndexSafe(slots, slotIndex);
        // if (resource == null || current == null || !current.isFluidEqual(resource)) return null;
        // return drain(resource.amount, doDrain);
        // }

    }

    private String get_total_prices(ItemStackHandler rows, int[] amounts, FluidStack[] fluids, int[] fluid_amounts) {
        MarketSnapshot snapshot = MarketCache.get();

        double[] item_prices = new double[MAX_ROWS];
        double[] fluid_prices = new double[MAX_FLUID_ROWS];

        for (int i = 0; i < MAX_ROWS; i++) {
            if (rows.getStackInSlot(i) == null) {
                item_prices[i] = 0;
                continue;
            }
            for (var item : snapshot.goods.items) {
                double buyPrice = 0;
                if (GTUtility.areStacksEqual(item.itemStack, rows.getStackInSlot(i), true)) {
                    item_prices[i] = item.buyPrice * amounts[i];
                    break;
                }
            }
        }
        for (int i = 0; i < MAX_FLUID_ROWS; i++) {
            if (fluids[i] == null) {
                fluid_prices[i] = 0;
                continue;
            }
            for (var fluid : snapshot.goods.fluids) {
                double buyPrice = 0;
                if (GTUtility.areFluidsEqual(fluid.fluidStack, fluids[i], true)) {
                    fluid_prices[i] = fluid.buyPrice * fluid_amounts[i];
                    break;
                }
            }
        }
        double totalItems = Arrays.stream(item_prices)
            .sum();
        double totalFluids = Arrays.stream(fluid_prices)
            .sum();
        double vat = (totalItems + totalFluids) * snapshot.goods.vat / 100;
        return "Item total price: " + totalItems
            + "\nFluid total amount: "
            + totalFluids
            + "\nVAT "
            + snapshot.goods.vat
            + "%: "
            + vat
            + "\nTotal including VAT: "
            + (totalItems + totalFluids + vat);
    }
}
