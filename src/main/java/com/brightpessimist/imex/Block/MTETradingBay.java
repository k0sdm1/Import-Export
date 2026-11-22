package com.brightpessimist.imex.Block;
//
// import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
// import static gregtech.api.enums.HatchElement.*;
// import static gregtech.api.enums.HatchElement.InputHatch;
// import static gregtech.api.enums.HatchElement.Maintenance;
// import static gregtech.api.util.GTStructureUtility.*;
//
// import java.util.List;
//
// import net.minecraft.entity.player.EntityPlayer;
// import net.minecraft.item.ItemStack;
// import net.minecraft.nbt.NBTTagCompound;
// import net.minecraft.util.ChatComponentText;
// import net.minecraftforge.common.util.ForgeDirection;
//
// import org.jetbrains.annotations.NotNull;
//
// import com.brightpessimist.imex.ImportExport;
// import com.brightpessimist.imex.Item.Contract;
// import com.brightpessimist.imex.gui.MTETradingBayGui;
// import com.brightpessimist.imex.util.NBTUtils;
// import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
// import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
// import com.gtnewhorizon.structurelib.structure.StructureDefinition;
//
// import gregtech.api.casing.Casings;
// import gregtech.api.enums.*;
// import gregtech.api.interfaces.ITexture;
// import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
// import gregtech.api.interfaces.tileentity.ICasingTextureProvider;
// import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
// import gregtech.api.metatileentity.implementations.MTEExtendedPowerMultiBlockBase;
// import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
// import gregtech.api.recipe.check.CheckRecipeResult;
// import gregtech.api.recipe.check.CheckRecipeResultRegistry;
// import gregtech.api.structure.error.StructureError;
// import gregtech.api.structure.error.StructureErrors;
// import gregtech.api.util.MultiblockTooltipBuilder;
// import gregtech.common.gui.modularui.multiblock.base.MTEMultiBlockBaseGui;
// import gregtech.common.tileentities.machines.MTEHatchInputBusME;
// import gtPlusPlus.xmod.gregtech.common.blocks.textures.TexturesGtBlock;
//
// public class MTETradingBay extends MTEExtendedPowerMultiBlockBase<MTETradingBay>
// implements ISurvivalConstructable, ICasingTextureProvider {
//
// private static final int OFFSET_X = 1;
// private static final int OFFSET_Y = 3;
// private static final int OFFSET_Z = 0;
// private int glassTier = -1;
// private static final String STRUCTURE_PIECE_MAIN = "main";
// private static final String[][] structure = { { "BBB", "BCB", "BCB", "B~B", "BBB", "B B" },
// { " A ", "AEA", "AEA", "AEA", " A ", " " }, { " A ", "AEA", "ADA", "AEA", " A ", " " },
// { " A ", "AEA", "ADA", "AEA", " A ", " " }, { " A ", "AEA", "ADA", "AEA", " A ", " " },
// { " A ", "AEA", "AEA", "AEA", " A ", " " }, { "BBB", "BCB", "BCB", "BCB", "BBB", "B B" }, };
// private static IStructureDefinition<MTETradingBay> STRUCTURE_DEFINITION = null;
// private int casingAmount;
// private boolean state_importing_items = false;
// private boolean state_exporting_items = false;
// private boolean state_staling = true;
// private boolean can_unload_goods = false;
//
// private int running_tick = 0;
// private int time_to_deliver_order = 0;
//
// public String get_state_text() {
// if (state_staling) return "Staling";
// if (state_importing_items) return "Importing items";
// if (state_exporting_items) return "Exporting items";
// return "Something unknown";
// }
//
// public boolean is_staling() {
// return state_staling;
// }
//
// boolean bay_mode = false;
//
// public MTETradingBay(final int aID, final String aName, final String aNameRegional) {
// super(aID, aName, aNameRegional);
// }
//
// public MTETradingBay(final String aName) {
// super(aName);
// }
//
// @Override
// public void construct(ItemStack stackSize, boolean hintsOnly) {
// buildPiece(STRUCTURE_PIECE_MAIN, stackSize, hintsOnly, OFFSET_X, OFFSET_Y, OFFSET_Z);
// }
//
// @Override
// public IStructureDefinition<MTETradingBay> getStructureDefinition() {
// if (STRUCTURE_DEFINITION == null) {
// STRUCTURE_DEFINITION = StructureDefinition.<MTETradingBay>builder()
// .addShape(STRUCTURE_PIECE_MAIN, structure)
// .addElement('A', chainAllGlasses(-1, (te, t) -> te.glassTier = t, te -> te.glassTier))
// .addElement(
// 'B',
// buildHatchAdder(MTETradingBay.class).atLeast(InputBus, OutputBus, Energy, InputHatch, Maintenance)
// .casingIndex(Casings.SolidSteelMachineCasing.textureId)
// .hint(1)
// .buildAndChain(
// onElementPass(x -> ++x.casingAmount, Casings.SolidSteelMachineCasing.asElement())))
// .addElement('C', Casings.FilterMachineCasing.asElement())
// .addElement('D', ofFrame(Materials.StainlessSteel))
// .addElement('E', Casings.SteelPipeCasing.asElement())
// .build();
// }
// return STRUCTURE_DEFINITION;
// }
//
// @Override
// public IMetaTileEntity newMetaEntity(final IGregTechTileEntity aTileEntity) {
// return new MTETradingBay(this.mName);
// }
//
// @Override
// protected MultiblockTooltipBuilder createTooltip() {
// // FIXED: Must return a valid builder to prevent NullPointerException crashes
// final MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
// tt.addMachineType("Trading Bay")
// .beginStructureBlock(6, 10, 3, false)
// .addController("Front center (bottom layer)")
// .addCasing("Minimum 20", Casings.SteelPipeCasing.getLocalizedName(), false)
// .addEnergyHatch("1", "Any Steel Pipe Casing")
// .addMaintenanceHatch("1", "Any Steel Pipe Casing")
// .addInputBus("1+", "Any Steel Pipe Casing")
// .addOutputBus("1+", "Any Steel Pipe Casing")
// .addInputHatch("1+", "Any Steel Pipe Casing")
// .toolTipFinisher();
// return tt;
// }
//
// @Override
// public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection aFacing,
// int colorIndex, boolean aActive, boolean redstoneLevel) {
// return Textures.BlockIcons.createTextureWithCasing(
// this,
// side,
// aFacing,
// aActive,
// TexturesGtBlock.oMCDAlgaePondBase,
// TexturesGtBlock.oMCDAlgaePondBaseGlow,
// TexturesGtBlock.oMCDAlgaePondBaseActive,
// TexturesGtBlock.oMCDAlgaePondBaseActiveGlow);
// }
//
// @Override
// public void checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack, List<StructureError> errors) {
// casingAmount = 0;
// glassTier = -1;
//
// if (!checkPiece(STRUCTURE_PIECE_MAIN, OFFSET_X, OFFSET_Y, OFFSET_Z, errors)) {
// return;
// }
// checkCasingMin(errors, casingAmount, 20);
// checkOneEnergyHatch(errors);
// // checkHasMaintenanceHatch(errors);
// checkHasOutputBus(errors);
// checkHasInputBus(errors);
//
// if (!mEnergyHatches.isEmpty()) {
// int inputTier = (int) getInputVoltageTier();
// if (glassTier < inputTier) {
// errors.add(StructureErrors.glassTierNotEnough(inputTier));
// return;
// }
// }
// if (!errors.isEmpty()) return;
// }
//
// @Override
// public @NotNull CheckRecipeResult checkProcessing() {
// if (mMaxProgresstime > 0) {
// mEfficiency = 10000;
// return CheckRecipeResultRegistry.SUCCESSFUL;
// }
// mEfficiency = 0;
// mMaxProgresstime = 0;
// return CheckRecipeResultRegistry.NO_RECIPE;
// }
//
// @Override
// public ITexture getCasingTexture() {
// return Casings.SolidSteelMachineCasing.getCasingTexture();
// }
//
// @Override
// public boolean supportsVoidProtection() {
// return false;
// }
//
// @Override
// protected SoundResource getProcessStartSound() {
// return SoundResource.GTCEU_LOOP_MOTOR;
// }
//
// @Override
// public int getPollutionPerSecond(final ItemStack aStack) {
// return 0;
// }
//
// @Override
// public boolean getDefaultHasMaintenanceChecks() {
// return false;
// }
//
// @Override
// public boolean showRecipeTextInGUI() {
// return false;
// }
//
// @Override
// public boolean hasRunningText() {
// return false;
// }
//
// @Override
// public boolean showMachineStatusInGUI() {
// return false;
// }
//
// private long getRecipeEUt() {
// return 0;
// }
//
// @Override
// protected @NotNull MTEMultiBlockBaseGui<?> getGui() {
// return new MTETradingBayGui(this);
// }
//
// public boolean get_bay_mode() {
// return bay_mode;
// }
//
// public void set_bay_mode(boolean val) {
// if (is_staling()) {
// bay_mode = val;
//
// }
// }
//
// private void calculate_import_time() {
// mMaxProgresstime = 200;
// }
//
// @Override
// public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
// NBTTagCompound nbt = NBTUtils.getPersistedData(aPlayer, true);
// if (!aPlayer.worldObj.isRemote) {
// boolean value = nbt.getBoolean("oof");
// aPlayer.addChatMessage(new ChatComponentText("owned by " + getBaseMetaTileEntity().getOwnerName() + value));
// }
// if (aPlayer.getHeldItem() != null && aPlayer.getHeldItem()
// .getItem() instanceof Contract) {
// if (!Contract.getOwnerID(aPlayer.getHeldItem())
// .equals(getBaseMetaTileEntity().getOwnerUuid())) {
// if (!aPlayer.worldObj.isRemote) {
// aPlayer
// .addChatMessage(new ChatComponentText("Contract DOES NOT belong to the owner of the machine!"));
// }
// return true;
// }
// if (!state_staling) {
// if (!aPlayer.worldObj.isRemote) {
// aPlayer.addChatMessage(
// new ChatComponentText("Can't load the order if machine is not in the idle mode."));
// }
// return true;
// }
// if (bay_mode) {
// if (!aPlayer.worldObj.isRemote) {
// aPlayer.addChatMessage(
// new ChatComponentText("Can't load the order if machine is not in the import mode."));
// }
// return true;
// }
// if (!aPlayer.worldObj.isRemote) {
// aPlayer.addChatMessage(new ChatComponentText("Loading contract..."));
// List<Contract.Order> orders = Contract.readOrders(aPlayer.getHeldItem());
// for (Contract.Order order : orders) {
// aPlayer.addChatMessage(new ChatComponentText(order.item.toString() + ", x" + order.amount));
// }
// List<Contract.FluidOrder> forders = Contract.readFluidOrders(aPlayer.getHeldItem());
// for (Contract.FluidOrder forder : forders) {
// aPlayer.addChatMessage(new ChatComponentText(forder.fluid.toString() + ", x" + forder.amount));
// }
// if (!aPlayer.worldObj.isRemote) {
// aPlayer.addChatMessage(new ChatComponentText("Contract data loaded into the bay, import started."));
// }
// enableWorking();
// calculate_import_time();
// state_staling = false;
// state_importing_items = true;
// return true;
// }
// }
// return super.onRightclick(aBaseMetaTileEntity, aPlayer);
// }
//
// public int get_running_second() {
// return mProgresstime / 20;
// }
//
// public int get_total_import_time() {
// return mMaxProgresstime / 20;
// }
//
// @Override
// public boolean onRunningTick(ItemStack aStack) {
// if (state_importing_items) {
// if (mMaxProgresstime > mProgresstime + 2 && mProgresstime % 20 != 0) return true;
//
// for (MTEHatchInputBus bus : mInputBusses) {
// if (bus instanceof MTEHatchInputBusME meBus) {
// ItemStack[] ItemStackArray = new ItemStack[1];
// ImportExport.proxy.add_chat_message("mebus size inventory " + meBus.getSizeInventory());
// for (int i = 0; i < 16; i++) {
// ItemStack itemStack = meBus.getSlotConfig(i);
// if (itemStack != null) {
// ImportExport.proxy.add_chat_message(itemStack.toString());
// }
// }
//
// } else {
// for (ItemStack itemStack : bus.mInventory) {
// if (itemStack != null) {
// ImportExport.proxy.add_chat_message(itemStack.toString());
// }
// }
// }
//
// }
//
// if (mMaxProgresstime <= mProgresstime + 1) {
// ImportExport.proxy.add_chat_message("Imported!!!");
// mProgresstime = 0;
// mMaxProgresstime = 0;
// state_staling = true;
// state_importing_items = false;
// can_unload_goods = true;
// }
// }
// ImportExport.proxy.add_chat_message("running tick = " + mProgresstime);
// return super.onRunningTick(aStack);
// }
//
// @Override
// public void onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ,
// ItemStack aTool) {
// bay_mode = !bay_mode;
// if (!aPlayer.worldObj.isRemote) {
// aPlayer.addChatMessage(new ChatComponentText("Bay mode switched to " + bay_mode_representation()));
// }
// }
//
// public String bay_mode_representation() {
// return bay_mode ? "Export" : "Import";
// }
//
// @Override
// public void saveNBTData(NBTTagCompound aNBT) {
// super.saveNBTData(aNBT);
// aNBT.setBoolean("bay_mode", this.bay_mode);
// aNBT.setBoolean("state_exporting_items", this.state_exporting_items);
// aNBT.setBoolean("state_importing_items", this.state_importing_items);
// aNBT.setBoolean("state_staling", this.state_staling);
// aNBT.setBoolean("can_unload_goods", this.can_unload_goods);
// }
//
// @Override
// public void loadNBTData(NBTTagCompound aNBT) {
// super.loadNBTData(aNBT);
// this.state_exporting_items = aNBT.getBoolean("state_exporting_items");
// this.state_importing_items = aNBT.getBoolean("state_importing_items");
// this.state_staling = aNBT.getBoolean("state_staling");
// this.can_unload_goods = aNBT.getBoolean("can_unload_goods");
// this.bay_mode = aNBT.getBoolean("bay_mode");
//
// }
// }

// import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
// import static gregtech.api.enums.HatchElement.*;
// import static gregtech.api.util.GTStructureUtility.*;
//
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
//
// import net.minecraft.entity.player.EntityPlayer;
// import net.minecraft.item.ItemStack;
// import net.minecraft.nbt.NBTTagCompound;
// import net.minecraft.util.ChatComponentText;
// import net.minecraftforge.common.util.ForgeDirection;
// import net.minecraftforge.fluids.FluidStack;
//
// import org.jetbrains.annotations.NotNull;
//
// import com.brightpessimist.imex.Item.Contract;
// import com.brightpessimist.imex.config.TradingConfig;
// import com.brightpessimist.imex.gui.MTETradingBayGui;
// import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
// import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
// import com.gtnewhorizon.structurelib.structure.StructureDefinition;
//
// import appeng.api.config.Actionable;
// import appeng.api.networking.security.BaseActionSource;
// import appeng.api.networking.security.IActionHost;
// import appeng.api.networking.security.MachineSource;
// import appeng.api.storage.IMEMonitor;
// import appeng.api.storage.data.IAEFluidStack;
// import appeng.api.storage.data.IAEItemStack;
// import appeng.me.GridAccessException;
// import appeng.me.helpers.AENetworkProxy;
// import appeng.util.Platform;
// import appeng.util.item.AEFluidStack;
// import appeng.util.item.AEItemStack;
// import gregtech.api.casing.Casings;
// import gregtech.api.enums.Materials;
// import gregtech.api.enums.SoundResource;
// import gregtech.api.enums.Textures;
// import gregtech.api.interfaces.ITexture;
// import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
// import gregtech.api.interfaces.tileentity.ICasingTextureProvider;
// import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
// import gregtech.api.metatileentity.implementations.MTEExtendedPowerMultiBlockBase;
// import gregtech.api.metatileentity.implementations.MTEHatchInput;
// import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
// import gregtech.api.metatileentity.implementations.MTEHatchOutputBus;
// import gregtech.api.recipe.check.CheckRecipeResult;
// import gregtech.api.recipe.check.CheckRecipeResultRegistry;
// import gregtech.api.structure.error.StructureError;
// import gregtech.api.structure.error.StructureErrors;
// import gregtech.api.util.GTUtility;
// import gregtech.api.util.MultiblockTooltipBuilder;
// import gregtech.api.util.VoidProtectionHelper;
// import gregtech.api.util.shutdown.ShutDownReason;
// import gregtech.common.gui.modularui.multiblock.base.MTEMultiBlockBaseGui;
// import gregtech.common.tileentities.machines.MTEHatchInputBusME;
// import gregtech.common.tileentities.machines.MTEHatchInputME;
// import gtPlusPlus.xmod.gregtech.common.blocks.textures.TexturesGtBlock;
//
// public class MTETradingBay extends MTEExtendedPowerMultiBlockBase<MTETradingBay>
// implements ISurvivalConstructable, ICasingTextureProvider {
//
// public enum BayState {
//
// IDLE("Staling"),
// IMPORTING("Importing items"),
// EXPORTING("Exporting items");
//
// public final String label;
//
// BayState(String label) {
// this.label = label;
// }
// }
//
// private static final int OFFSET_X = 1;
// private static final int OFFSET_Y = 3;
// private static final int OFFSET_Z = 0;
// private static final String STRUCTURE_PIECE_MAIN = "main";
// private static final String[][] structure = { { "BBB", "BCB", "BCB", "B~B", "BBB", "B B" },
// { " A ", "AEA", "AEA", "AEA", " A ", " " }, { " A ", "AEA", "ADA", "AEA", " A ", " " },
// { " A ", "AEA", "ADA", "AEA", " A ", " " }, { " A ", "AEA", "ADA", "AEA", " A ", " " },
// { " A ", "AEA", "AEA", "AEA", " A ", " " }, { "BBB", "BCB", "BCB", "BCB", "BBB", "B B" }, };
// private static IStructureDefinition<MTETradingBay> STRUCTURE_DEFINITION = null;
//
// private int glassTier = -1;
// private int casingAmount;
//
// /** false = import (buy against contract), true = export (sell). */
// private boolean bay_mode = false;
// private BayState state = BayState.IDLE;
// /** Contract waiting to be fulfilled in import mode. Persisted. */
// private ItemStack loadedContract = null;
// /** Credits we already earned but could not yet eject. Persisted - never lost, never voided. */
// private long pendingPayout = 0;
// private long totalEarned = 0;
// private long lastCycleRevenue = 0;
//
// public MTETradingBay(final int aID, final String aName, final String aNameRegional) {
// super(aID, aName, aNameRegional);
// }
//
// public MTETradingBay(final String aName) {
// super(aName);
// }
//
// // region structure (unchanged except OutputHatch added to the hatch element)
//
// @Override
// public void construct(ItemStack stackSize, boolean hintsOnly) {
// buildPiece(STRUCTURE_PIECE_MAIN, stackSize, hintsOnly, OFFSET_X, OFFSET_Y, OFFSET_Z);
// }
//
// @Override
// public IStructureDefinition<MTETradingBay> getStructureDefinition() {
// if (STRUCTURE_DEFINITION == null) {
// STRUCTURE_DEFINITION = StructureDefinition.<MTETradingBay>builder()
// .addShape(STRUCTURE_PIECE_MAIN, structure)
// .addElement('A', chainAllGlasses(-1, (te, t) -> te.glassTier = t, te -> te.glassTier))
// .addElement(
// 'B',
// buildHatchAdder(MTETradingBay.class)
// .atLeast(InputBus, OutputBus, Energy, InputHatch, OutputHatch, Maintenance)
// .casingIndex(Casings.SolidSteelMachineCasing.textureId)
// .hint(1)
// .buildAndChain(
// onElementPass(x -> ++x.casingAmount, Casings.SolidSteelMachineCasing.asElement())))
// .addElement('C', Casings.FilterMachineCasing.asElement())
// .addElement('D', ofFrame(Materials.StainlessSteel))
// .addElement('E', Casings.SteelPipeCasing.asElement())
// .build();
// }
// return STRUCTURE_DEFINITION;
// }
//
// @Override
// public void checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack, List<StructureError> errors) {
// casingAmount = 0;
// glassTier = -1;
// if (!checkPiece(STRUCTURE_PIECE_MAIN, OFFSET_X, OFFSET_Y, OFFSET_Z, errors)) return;
// checkCasingMin(errors, casingAmount, 20);
// checkOneEnergyHatch(errors);
// checkHasOutputBus(errors);
// checkHasInputBus(errors);
// if (!mEnergyHatches.isEmpty()) {
// int inputTier = (int) getInputVoltageTier();
// if (glassTier < inputTier) {
// errors.add(StructureErrors.glassTierNotEnough(inputTier));
// }
// }
// }
//
// @Override
// public IMetaTileEntity newMetaEntity(final IGregTechTileEntity aTileEntity) {
// return new MTETradingBay(this.mName);
// }
//
// @Override
// protected MultiblockTooltipBuilder createTooltip() {
// final MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
// tt.addMachineType("Trading Bay")
// .beginStructureBlock(6, 10, 3, false)
// .addController("Front center (bottom layer)")
// .addCasing("Minimum 20", Casings.SteelPipeCasing.getLocalizedName(), false)
// .addEnergyHatch("1", "Any Steel Pipe Casing")
// .addMaintenanceHatch("1", "Any Steel Pipe Casing")
// .addInputBus("1+", "Any Steel Pipe Casing")
// .addOutputBus("1+", "Any Steel Pipe Casing")
// .addInputHatch("1+", "Any Steel Pipe Casing")
// .addOutputHatch("1+", "Any Steel Pipe Casing")
// .toolTipFinisher();
// return tt;
// }
//
// @Override
// public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection aFacing,
// int colorIndex, boolean aActive, boolean redstoneLevel) {
// return Textures.BlockIcons.createTextureWithCasing(
// this,
// side,
// aFacing,
// aActive,
// TexturesGtBlock.oMCDAlgaePondBase,
// TexturesGtBlock.oMCDAlgaePondBaseGlow,
// TexturesGtBlock.oMCDAlgaePondBaseActive,
// TexturesGtBlock.oMCDAlgaePondBaseActiveGlow);
// }
//
// @Override
// public ITexture getCasingTexture() {
// return Casings.SolidSteelMachineCasing.getCasingTexture();
// }
//
// // endregion
//
// // region trading logic
//
// @Override
// public @NotNull CheckRecipeResult checkProcessing() {
// // The base only calls this while idle, and again immediately after a cycle finishes.
// if (mMaxProgresstime > 0) return CheckRecipeResultRegistry.SUCCESSFUL;
// state = BayState.IDLE;
// mEfficiency = 0;
//
// // Pay out money owed from the previous cycle (also recovers after shutdowns/reloads).
// if (pendingPayout > 0) tryPayout();
//
// return bay_mode ? startExportCycle() : startImportCycle();
// }
//
// private void beginCycle(int ticks, BayState newState) {
// mProgresstime = 0;
// mMaxProgresstime = Math.max(1, ticks);
// mEUt = 0;
// mEfficiency = 10000;
// mEfficiencyIncrease = 0;
// state = newState;
// markDirty();
// }
//
// // ---- export ----
//
// private CheckRecipeResult startExportCycle() {
// if (pendingPayout > 0) return CheckRecipeResultRegistry.ITEM_OUTPUT_FULL; // can't pay yet -> don't take goods
//
// List<SellOffer> offers = new ArrayList<>();
// collectItemOffers(offers);
// collectFluidOffers(offers);
// if (offers.isEmpty()) return CheckRecipeResultRegistry.NO_RECIPE;
//
// long plannedRevenue = 0;
// for (SellOffer offer : offers) plannedRevenue += offer.revenue();
// if (plannedRevenue <= 0) return CheckRecipeResultRegistry.NO_RECIPE;
//
// ItemStack[] plannedCurrency = buildCurrencyStacks(plannedRevenue);
// if (plannedCurrency == null) return CheckRecipeResultRegistry.INTERNAL_ERROR; // currency misconfigured
//
// // Never sell goods we cannot pay for (handles normal + ME output buses).
// boolean fits = new VoidProtectionHelper().setMachine(this, true, false)
// .setItemOutputs(plannedCurrency)
// .setMaxParallel(1)
// .build()
// .getMaxParallel() > 0;
// if (!fits) return CheckRecipeResultRegistry.ITEM_OUTPUT_FULL;
//
// // Commit: actually remove the goods from buses / hatches / ME networks.
// long earned = 0;
// for (SellOffer offer : offers) {
// long removed = offer.sink.remove(offer.amount);
// if (removed > 0) earned += offer.revenueFor(removed);
// }
// if (earned <= 0) return CheckRecipeResultRegistry.NO_RECIPE;
//
// pendingPayout += earned;
// totalEarned += earned;
// lastCycleRevenue = earned;
// beginCycle(TradingConfig.exportCycleTicks, BayState.EXPORTING);
// return CheckRecipeResultRegistry.SUCCESSFUL;
// }
//
// private void collectItemOffers(List<SellOffer> offers) {
// // Per-item-type batch budget, shared across all ME slots/buses this cycle.
// Map<GTUtility.ItemId, Long> meBudget = new HashMap<>();
// for (MTEHatchInputBus bus : GTUtility.validMTEList(mInputBusses)) {
// if (bus instanceof MTEHatchInputBusME meBus) {
// collectMEItemOffers(meBus, offers, meBudget);
// } else {
// collectPlainBusOffers(bus, offers);
// }
// }
// }
//
// /** Plain input bus: sell everything that has a price, emptying the bus this cycle. */
// private void collectPlainBusOffers(MTEHatchInputBus bus, List<SellOffer> offers) {
// for (int i = 0; i < bus.getSizeInventory(); i++) {
// ItemStack stack = bus.getStackInSlot(i);
// if (stack == null || stack.stackSize <= 0) continue;
// if (GTUtility.isAnyIntegratedCircuit(stack)) continue; // never sell programmed circuits
// long price = TradingConfig.getItemPrice(stack);
// if (price <= 0) continue; // whitelist filter
// final int slot = i;
// offers.add(new SellOffer(stack.stackSize, price, false, amount -> {
// ItemStack taken = bus.decrStackSize(slot, (int) amount);
// bus.updateSlots();
// return taken == null ? 0 : taken.stackSize;
// }));
// }
// }
//
// /**
// * ME stocking input bus: the 16 config slots are the FILTER. For each configured type we query the
// * network (SIMULATE) and later extract at most the batch amount via poweredExtraction (MODULATE).
// */
// private void collectMEItemOffers(MTEHatchInputBusME bus, List<SellOffer> offers,
// Map<GTUtility.ItemId, Long> budget) {
// AENetworkProxy proxy = bus.getProxy();
// if (proxy == null || !proxy.isActive()) return;
//
// for (int i = 0; i < MTEHatchInputBusME.SLOT_COUNT; i++) {
// ItemStack config = bus.getSlotConfig(i); // ghost stack = which item to pull
// if (config == null) continue;
// long price = TradingConfig.getItemPrice(config);
// if (price <= 0) continue;
//
// GTUtility.ItemId id = GTUtility.ItemId.create(config);
// long remainingBudget = budget.getOrDefault(id, (long) TradingConfig.getItemBatch(config));
// if (remainingBudget <= 0) continue;
//
// IAEItemStack available = extractItems(bus, config, remainingBudget, true);
// long amount = available == null ? 0 : available.getStackSize();
// if (amount <= 0) continue;
//
// budget.put(id, remainingBudget - amount);
// offers.add(new SellOffer(amount, price, false, requested -> {
// IAEItemStack got = extractItems(bus, config, requested, false);
// return got == null ? 0 : got.getStackSize();
// }));
// }
// }
//
// private void collectFluidOffers(List<SellOffer> offers) {
// Map<String, Long> meBudget = new HashMap<>();
// for (MTEHatchInput hatch : GTUtility.validMTEList(mInputHatches)) {
// if (hatch instanceof MTEHatchInputME meHatch) {
// collectMEFluidOffers(meHatch, offers, meBudget);
// } else {
// collectPlainHatchOffers(hatch, offers);
// }
// }
// }
//
// /** Plain input hatch: drain whole buckets of everything that has a price. */
// private void collectPlainHatchOffers(MTEHatchInput hatch, List<SellOffer> offers) {
// FluidStack fluid = hatch.getFluid();
// if (fluid == null || fluid.amount < 1000) return;
// long price = TradingConfig.getFluidPricePerBucket(fluid);
// if (price <= 0) return;
// int amount = (fluid.amount / 1000) * 1000; // only whole buckets, so nothing is sold for 0
// offers.add(new SellOffer(amount, price, true, requested -> {
// FluidStack drained = hatch.drain(ForgeDirection.UNKNOWN, (int) requested, true);
// return drained == null ? 0 : drained.amount;
// }));
// }
//
// /** ME stocking input hatch: config slots = filter, batch-limited extraction from the fluid network. */
// private void collectMEFluidOffers(MTEHatchInputME hatch, List<SellOffer> offers, Map<String, Long> budget) {
// AENetworkProxy proxy = hatch.getProxy();
// if (proxy == null || !proxy.isActive()) return;
//
// for (int i = 0; i < MTEHatchInputME.SLOT_COUNT; i++) {
// FluidStack config = hatch.getSlotConfig(i);
// if (config == null) continue;
// long price = TradingConfig.getFluidPricePerBucket(config);
// if (price <= 0) continue;
//
// String key = config.getFluid()
// .getName();
// long remainingBudget = (budget.getOrDefault(key, (long) TradingConfig.getFluidBatchMb(config)) / 1000)
// * 1000;
// if (remainingBudget < 1000) continue;
//
// IAEFluidStack available = extractFluids(hatch, config, remainingBudget, true);
// long amount = available == null ? 0 : (available.getStackSize() / 1000) * 1000;
// if (amount < 1000) continue;
//
// budget.put(key, remainingBudget - amount);
// offers.add(new SellOffer(amount, price, true, requested -> {
// IAEFluidStack got = extractFluids(hatch, config, requested, false);
// return got == null ? 0 : got.getStackSize();
// }));
// }
// }
//
// /**
// * The real ME extraction. SIMULATE asks the network how much exists; MODULATE goes through
// * Platform.poweredExtraction so AE energy + security permissions are respected, exactly like
// * the stocking hatch's own endRecipeProcessing() does.
// */
// private static IAEItemStack extractItems(MTEHatchInputBusME bus, ItemStack prototype, long amount,
// boolean simulate) {
// if (amount <= 0) return null;
// AENetworkProxy proxy = bus.getProxy();
// if (proxy == null || !proxy.isActive()) return null;
// IAEItemStack request = AEItemStack.create(prototype);
// if (request == null) return null;
// request.setStackSize(amount);
// try {
// IMEMonitor<IAEItemStack> monitor = proxy.getStorage()
// .getItemInventory();
// BaseActionSource source = new MachineSource((IActionHost) bus.getBaseMetaTileEntity());
// if (simulate) return monitor.extractItems(request, Actionable.SIMULATE, source);
// return Platform.poweredExtraction(proxy.getEnergy(), monitor, request, source);
// } catch (GridAccessException e) {
// return null;
// }
// }
//
// private static IAEFluidStack extractFluids(MTEHatchInputME hatch, FluidStack prototype, long amount,
// boolean simulate) {
// if (amount <= 0) return null;
// AENetworkProxy proxy = hatch.getProxy();
// if (proxy == null || !proxy.isActive()) return null;
// IAEFluidStack request = AEFluidStack.create(prototype);
// if (request == null) return null;
// request.setStackSize(amount);
// try {
// IMEMonitor<IAEFluidStack> monitor = proxy.getStorage()
// .getFluidInventory();
// BaseActionSource source = new MachineSource((IActionHost) hatch.getBaseMetaTileEntity());
// if (simulate) return monitor.extractItems(request, Actionable.SIMULATE, source);
// return Platform.poweredExtraction(proxy.getEnergy(), monitor, request, source);
// } catch (GridAccessException e) {
// return null;
// }
// }
//
// // ---- payout ----
//
// /**
// * Ejects owed credits into output buses. storePartial() exists on both normal and ME output buses
// * and never voids: whatever does not fit stays in the stack, and the remainder stays owed.
// */
// private void tryPayout() {
// ItemStack proto = TradingConfig.createCurrencyStack(1);
// if (proto == null) return;
// int max = Math.max(1, proto.getMaxStackSize());
// // List<MTEHatchOutputBus> buses = GTUtility.validMTEList(mOutputBusses);
//
// while (pendingPayout > 0) {
// int n = (int) Math.min(pendingPayout, max);
// ItemStack stack = GTUtility.copyAmount(n, proto);
// for (MTEHatchOutputBus bus : GTUtility.validMTEList(mOutputBusses)) {
// if (bus.storePartial(stack, false)) break;
// }
// int paid = n - stack.stackSize;
// if (paid <= 0) break; // outputs full, keep the debt, retry on next check
// pendingPayout -= paid;
// }
// markDirty();
// }
//
// private static ItemStack[] buildCurrencyStacks(long credits) {
// ItemStack proto = TradingConfig.createCurrencyStack(1);
// if (proto == null || credits <= 0) return null;
// int max = Math.max(1, proto.getMaxStackSize());
// List<ItemStack> stacks = new ArrayList<>();
// while (credits > 0) {
// int n = (int) Math.min(credits, max);
// stacks.add(GTUtility.copyAmount(n, proto));
// credits -= n;
// }
// return stacks.toArray(new ItemStack[0]);
// }
//
// // ---- import ----
//
// private CheckRecipeResult startImportCycle() {
// if (loadedContract == null) return CheckRecipeResultRegistry.NO_RECIPE;
//
// List<ItemStack> items = new ArrayList<>();
// for (Contract.Order order : Contract.readOrders(loadedContract)) {
// if (order == null || order.item == null || order.amount <= 0) continue;
// addSplit(items, order.item, order.amount);
// }
// List<FluidStack> fluids = new ArrayList<>();
// for (Contract.FluidOrder order : Contract.readFluidOrders(loadedContract)) {
// if (order == null || order.fluid == null || order.amount <= 0) continue;
// int remaining = order.amount;
// while (remaining > 0) {
// int chunk = Math.min(remaining, TradingConfig.importFluidChunkMb);
// fluids.add(new FluidStack(order.fluid, chunk));
// remaining -= chunk;
// }
// }
// if (items.isEmpty() && fluids.isEmpty()) {
// loadedContract = null;
// markDirty();
// return CheckRecipeResultRegistry.NO_RECIPE;
// }
//
// ItemStack[] itemOut = items.isEmpty() ? null : items.toArray(new ItemStack[0]);
// FluidStack[] fluidOut = fluids.isEmpty() ? null : fluids.toArray(new FluidStack[0]);
//
// VoidProtectionHelper vp = new VoidProtectionHelper().setMachine(this, true, true)
// .setItemOutputs(itemOut)
// .setFluidOutputs(fluidOut)
// .setMaxParallel(1)
// .build();
// if (vp.isItemFull()) return CheckRecipeResultRegistry.ITEM_OUTPUT_FULL;
// if (vp.isFluidFull()) return CheckRecipeResultRegistry.FLUID_OUTPUT_FULL;
//
// // TODO if imports should cost money: consume currency from the input buses here,
// // and return NO_RECIPE / a custom failure result if the buyer can't pay.
//
// mOutputItems = itemOut; // the base ejects these at cycle end and persists them in NBT
// mOutputFluids = fluidOut;
// loadedContract = null; // one contract = one shipment
// beginCycle(TradingConfig.importCycleTicks, BayState.IMPORTING);
// return CheckRecipeResultRegistry.SUCCESSFUL;
// }
//
// private static void addSplit(List<ItemStack> out, ItemStack proto, long amount) {
// int max = Math.max(1, proto.getMaxStackSize());
// while (amount > 0) {
// int n = (int) Math.min(amount, max);
// out.add(GTUtility.copyAmount(n, proto));
// amount -= n;
// }
// }
//
// @FunctionalInterface
// private interface GoodsSink {
//
// /** Removes up to {@code amount} from the real source, returns how much was actually removed. */
// long remove(long amount);
// }
//
// private static final class SellOffer {
//
// final long amount;
// final long pricePerUnit;
// final boolean perBucket;
// final GoodsSink sink;
//
// SellOffer(long amount, long pricePerUnit, boolean perBucket, GoodsSink sink) {
// this.amount = amount;
// this.pricePerUnit = pricePerUnit;
// this.perBucket = perBucket;
// this.sink = sink;
// }
//
// long revenue() {
// return revenueFor(amount);
// }
//
// long revenueFor(long n) {
// return perBucket ? n * pricePerUnit / 1000 : n * pricePerUnit;
// }
// }
//
// // endregion
//
// // region interaction / state
//
// @Override
// public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
// ItemStack held = aPlayer.getHeldItem();
// if (held != null && held.getItem() instanceof Contract) {
// if (aPlayer.worldObj.isRemote) return true;
// if (!Contract.getOwnerID(held)
// .equals(getBaseMetaTileEntity().getOwnerUuid())) {
// msg(aPlayer, "Contract DOES NOT belong to the owner of the machine!");
// return true;
// }
// if (bay_mode) {
// msg(aPlayer, "Switch the bay to Import mode first (screwdriver).");
// return true;
// }
// if (state != BayState.IDLE || mMaxProgresstime > 0) {
// msg(aPlayer, "Can't load a contract while the bay is busy.");
// return true;
// }
// if (loadedContract != null) {
// msg(aPlayer, "A contract is already loaded.");
// return true;
// }
// loadedContract = held.copy();
// loadedContract.stackSize = 1;
// markDirty();
// msg(aPlayer, "Contract loaded, import started.");
// scheduleRecipeCheckImmediate(); // wake the event-driven controller
// return true;
// }
// return super.onRightclick(aBaseMetaTileEntity, aPlayer);
// }
//
// @Override
// public void onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ,
// ItemStack aTool) {
// if (state != BayState.IDLE || mMaxProgresstime > 0) {
// msg(aPlayer, "Mode can only be switched while the bay is idle.");
// return;
// }
// set_bay_mode(!bay_mode);
// msg(aPlayer, "Bay mode switched to " + bay_mode_representation());
// }
//
// @Override
// public void stopMachine(@NotNull ShutDownReason reason) {
// state = BayState.IDLE; // pendingPayout intentionally survives
// super.stopMachine(reason);
// }
//
// private static void msg(EntityPlayer player, String text) {
// if (!player.worldObj.isRemote) player.addChatMessage(new ChatComponentText(text));
// }
//
// public String get_state_text() {
// return state.label;
// }
//
// public boolean is_staling() {
// return state == BayState.IDLE && mMaxProgresstime <= 0;
// }
//
// public boolean get_bay_mode() {
// return bay_mode;
// }
//
// public void set_bay_mode(boolean val) {
// if (state == BayState.IDLE && mMaxProgresstime <= 0) {
// bay_mode = val;
// markDirty();
// scheduleRecipeCheckImmediate();
// }
// }
//
// public String bay_mode_representation() {
// return bay_mode ? "Export" : "Import";
// }
//
// public int get_running_second() {
// return mProgresstime / 20;
// }
//
// public int get_total_import_time() {
// return mMaxProgresstime / 20;
// }
//
// public long getPendingPayout() {
// return pendingPayout;
// }
//
// public long getTotalEarned() {
// return totalEarned;
// }
//
// public long getLastCycleRevenue() {
// return lastCycleRevenue;
// }
//
// // endregion
//
// // region misc overrides
//
// @Override
// public boolean supportsVoidProtection() {
// return false; // we always protect; hide the vanilla toggle
// }
//
// @Override
// protected SoundResource getProcessStartSound() {
// return SoundResource.GTCEU_LOOP_MOTOR;
// }
//
// @Override
// public int getPollutionPerSecond(final ItemStack aStack) {
// return 0;
// }
//
// @Override
// public boolean getDefaultHasMaintenanceChecks() {
// return false;
// }
//
// @Override
// public boolean showRecipeTextInGUI() {
// return false;
// }
//
// @Override
// public boolean hasRunningText() {
// return false;
// }
//
// @Override
// public boolean showMachineStatusInGUI() {
// return false;
// }
//
// @Override
// protected @NotNull MTEMultiBlockBaseGui<?> getGui() {
// return new MTETradingBayGui(this);
// }
//
// // endregion
//
// @Override
// public void saveNBTData(NBTTagCompound aNBT) {
// super.saveNBTData(aNBT);
// aNBT.setBoolean("bayMode", bay_mode);
// aNBT.setInteger("bayState", state.ordinal());
// aNBT.setLong("pendingPayout", pendingPayout);
// aNBT.setLong("totalEarned", totalEarned);
// aNBT.setLong("lastCycleRevenue", lastCycleRevenue);
// if (loadedContract != null) GTUtility.saveItem(aNBT, "loadedContract", loadedContract);
// }
//
// @Override
// public void loadNBTData(NBTTagCompound aNBT) {
// super.loadNBTData(aNBT);
// bay_mode = aNBT.hasKey("bayMode") ? aNBT.getBoolean("bayMode") : aNBT.getBoolean("bay_mode");
// int s = aNBT.getInteger("bayState");
// state = (s >= 0 && s < BayState.values().length) ? BayState.values()[s] : BayState.IDLE;
// if (!aNBT.hasKey("bayState")) { // migrate old saves
// if (aNBT.getBoolean("state_importing_items")) state = BayState.IMPORTING;
// else if (aNBT.getBoolean("state_exporting_items")) state = BayState.EXPORTING;
// }
// pendingPayout = aNBT.getLong("pendingPayout");
// totalEarned = aNBT.getLong("totalEarned");
// lastCycleRevenue = aNBT.getLong("lastCycleRevenue");
// loadedContract = aNBT.hasKey("loadedContract") ? GTUtility.loadItem(aNBT, "loadedContract") : null;
// }
// }

import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
import static gregtech.api.enums.HatchElement.*;
import static gregtech.api.util.GTStructureUtility.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import com.brightpessimist.imex.Item.Contract;
import com.brightpessimist.imex.Item.CreditCard;
import com.brightpessimist.imex.config.TradingConfig;
import com.brightpessimist.imex.currency.TradingCurrencyManager;
import com.brightpessimist.imex.gui.MTETradingBayGui;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;

import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import gregtech.api.casing.Casings;
import gregtech.api.enums.Materials;
import gregtech.api.enums.SoundResource;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.ICasingTextureProvider;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEExtendedPowerMultiBlockBase;
import gregtech.api.metatileentity.implementations.MTEHatchInput;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.metatileentity.implementations.MTEHatchOutput;
import gregtech.api.metatileentity.implementations.MTEHatchOutputBus;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.recipe.check.SimpleCheckRecipeResult;
import gregtech.api.structure.error.StructureError;
import gregtech.api.structure.error.StructureErrors;
import gregtech.api.util.GTUtility;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.shutdown.ShutDownReason;
import gregtech.common.gui.modularui.multiblock.base.MTEMultiBlockBaseGui;
import gregtech.common.tileentities.machines.MTEHatchInputBusME;
import gregtech.common.tileentities.machines.MTEHatchInputME;
import gtPlusPlus.xmod.gregtech.common.blocks.textures.TexturesGtBlock;

public class MTETradingBay extends MTEExtendedPowerMultiBlockBase<MTETradingBay>
    implements ISurvivalConstructable, ICasingTextureProvider {

    public enum BayState {

        IDLE("Staling"),
        IMPORTING("Importing items"),
        EXPORTING("Exporting items");

        public final String label;

        BayState(String label) {
            this.label = label;
        }
    }

    private static final CheckRecipeResult INSUFFICIENT_FUNDS = SimpleCheckRecipeResult
        .ofFailure("imex_insufficient_funds");
    private static final CheckRecipeResult NO_CREDIT_CARD = SimpleCheckRecipeResult.ofFailure("imex_no_credit_card");
    private static final CheckRecipeResult WRONG_CREDIT_CARD = SimpleCheckRecipeResult
        .ofFailure("imex_credit_card_owner_dismatch");
    private static final CheckRecipeResult CARD_NOT_INITIALIZED = SimpleCheckRecipeResult
        .ofFailure("imex_credit_card_not_initialized");

    // per-tick ejection caps so a giant buffer can't lag the server
    private static final long EJECT_ITEMS_PER_TICK = 16384;
    private static final long EJECT_FLUID_PER_TICK = 256_000;

    private static final int OFFSET_X = 1, OFFSET_Y = 3, OFFSET_Z = 0;
    private static final String STRUCTURE_PIECE_MAIN = "main";
    private static final String[][] structure = { { "BBB", "BCB", "BCB", "B~B", "BBB", "B B" },
        { " A ", "AEA", "AEA", "AEA", " A ", "   " }, { " A ", "AEA", "ADA", "AEA", " A ", "   " },
        { " A ", "AEA", "ADA", "AEA", " A ", "   " }, { " A ", "AEA", "ADA", "AEA", " A ", "   " },
        { " A ", "AEA", "AEA", "AEA", " A ", "   " }, { "BBB", "BCB", "BCB", "BCB", "BBB", "B B" }, };
    private static IStructureDefinition<MTETradingBay> STRUCTURE_DEFINITION = null;

    private int glassTier = -1;
    private int casingAmount;

    private long transitReadyTick = 0;

    private boolean bay_mode = false; // false = import, true = export
    private BayState state = BayState.IDLE;

    private ItemStack loadedContract = null;
    private int[] itemRemaining = null;
    private int[] fluidRemaining = null;

    // goods already paid for, waiting to be "delivered" at cycle end (persisted)
    private final Map<GTUtility.ItemId, Long> transitItems = new LinkedHashMap<>();
    private final Map<GTUtility.FluidId, Long> transitFluids = new LinkedHashMap<>();
    // goods stored in the bay, being ejected to outputs over time (persisted)
    private final Map<GTUtility.ItemId, Long> bufferItems = new LinkedHashMap<>();
    private final Map<GTUtility.FluidId, Long> bufferFluids = new LinkedHashMap<>();

    private long totalEarned = 0;
    private long lastCycleRevenue = 0;

    public MTETradingBay(final int aID, final String aName, final String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public MTETradingBay(final String aName) {
        super(aName);
    }

    // region structure (unchanged except OutputHatch added)

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece(STRUCTURE_PIECE_MAIN, stackSize, hintsOnly, OFFSET_X, OFFSET_Y, OFFSET_Z);
    }

    @Override
    public IStructureDefinition<MTETradingBay> getStructureDefinition() {
        if (STRUCTURE_DEFINITION == null) {
            STRUCTURE_DEFINITION = StructureDefinition.<MTETradingBay>builder()
                .addShape(STRUCTURE_PIECE_MAIN, structure)
                .addElement('A', chainAllGlasses(-1, (te, t) -> te.glassTier = t, te -> te.glassTier))
                .addElement(
                    'B',
                    buildHatchAdder(MTETradingBay.class)
                        .atLeast(InputBus, OutputBus, Energy, InputHatch, OutputHatch, Maintenance)
                        .casingIndex(Casings.SolidSteelMachineCasing.textureId)
                        .hint(1)
                        .buildAndChain(
                            onElementPass(x -> ++x.casingAmount, Casings.SolidSteelMachineCasing.asElement())))
                .addElement('C', Casings.FilterMachineCasing.asElement())
                .addElement('D', ofFrame(Materials.StainlessSteel))
                .addElement('E', Casings.SteelPipeCasing.asElement())
                .build();
        }
        return STRUCTURE_DEFINITION;
    }

    @Override
    public void checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack, List<StructureError> errors) {
        casingAmount = 0;
        glassTier = -1;
        if (!checkPiece(STRUCTURE_PIECE_MAIN, OFFSET_X, OFFSET_Y, OFFSET_Z, errors)) return;
        checkCasingMin(errors, casingAmount, 20);
        checkOneEnergyHatch(errors);
        checkHasOutputBus(errors);
        checkHasInputBus(errors);
        if (!mEnergyHatches.isEmpty()) {
            int inputTier = (int) getInputVoltageTier();
            if (glassTier < inputTier) errors.add(StructureErrors.glassTierNotEnough(inputTier));
        }
    }

    @Override
    public IMetaTileEntity newMetaEntity(final IGregTechTileEntity aTileEntity) {
        return new MTETradingBay(this.mName);
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        final MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Trading Bay")
            .beginStructureBlock(6, 10, 3, false)
            .addController("Front center (bottom layer)")
            .addCasing("Minimum 20", Casings.SteelPipeCasing.getLocalizedName(), false)
            .addEnergyHatch("1", "Any Steel Pipe Casing")
            .addMaintenanceHatch("1", "Any Steel Pipe Casing")
            .addInputBus("1+", "Any Steel Pipe Casing")
            .addOutputBus("1+", "Any Steel Pipe Casing")
            .addInputHatch("1+", "Any Steel Pipe Casing")
            .addOutputHatch("1+", "Any Steel Pipe Casing")
            .toolTipFinisher();
        return tt;
    }

    @Override
    public void addAdditionalTooltipInformation(ItemStack stack, List<String> tooltip) {
        tooltip.add("Your best friend - the Trading Bay!");
        tooltip.add("Lets you to sell your items for currency and buy ohter things you need.");
        tooltip.add("Has two modes: Import and Export.");
        tooltip.add("In Import mode can buy items for you for your currency, anything you order with the Contract.");
        tooltip.add(
            "In Export mode allows you to sell anything (what possible) you place in it's input buses and hatches.");
        tooltip.add("Does not void anything.");
        tooltip.add("Be careful if used with ME input bus/hatch.");
        super.addAdditionalTooltipInformation(stack, tooltip);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection aFacing,
        int colorIndex, boolean aActive, boolean redstoneLevel) {
        return Textures.BlockIcons.createTextureWithCasing(
            this,
            side,
            aFacing,
            aActive,
            TexturesGtBlock.oMCDAlgaePondBase,
            TexturesGtBlock.oMCDAlgaePondBaseGlow,
            TexturesGtBlock.oMCDAlgaePondBaseActive,
            TexturesGtBlock.oMCDAlgaePondBaseActiveGlow);
    }

    @Override
    public ITexture getCasingTexture() {
        return Casings.SolidSteelMachineCasing.getCasingTexture();
    }

    // endregion

    // region tick / state

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onPostTick(aBaseMetaTileEntity, aTick);
        if (!aBaseMetaTileEntity.isServerSide()) return;

        // Deliver paid in-transit goods if the import cycle ended OR was interrupted (shutdown/disable).
        // if ((!transitItems.isEmpty() || !transitFluids.isEmpty()) && (mMaxProgresstime <= 0 || !isAllowedToWork())) {
        // mergeTransitIntoBuffer();
        // }
        // This still completes delivery if the machine was shut down mid-cycle, but never early.
        if ((!transitItems.isEmpty() || !transitFluids.isEmpty()) && mTotalRunTime >= transitReadyTick) {
            mergeTransitIntoBuffer();
        }

        if (mMachine) {
            // Always eject buffered goods: while staling, importing AND exporting.
            ejectBufferedGoods();
            // Nudge the event-driven controller while a contract waits for funds.
            if (!bay_mode && loadedContract != null && mMaxProgresstime <= 0 && aTick % 100 == 0) {
                scheduleRecipeCheckImmediate();
            }
        }
    }

    // @Override
    // public @NotNull CheckRecipeResult checkProcessing() {
    // if (mMaxProgresstime > 0) return CheckRecipeResultRegistry.SUCCESSFUL;
    // // "at the end" of the previous cycle: deliver its paid goods before starting a new one
    // mergeTransitIntoBuffer();
    // state = BayState.IDLE;
    // mEfficiency = 0;
    // return bay_mode ? startExportCycle() : startImportCycle();
    // }

    // @Override
    // public @NotNull CheckRecipeResult checkProcessing() {
    // if (mMaxProgresstime > 0) return CheckRecipeResultRegistry.SUCCESSFUL;
    // // Safety net: normally outputAfterRecipe() already delivered the previous cycle's goods.
    // mergeTransitIntoBuffer();
    // state = BayState.IDLE;
    // mEfficiency = 0;
    // return bay_mode ? startExportCycle() : startImportCycle();
    // }

    @Override
    public @NotNull CheckRecipeResult checkProcessing() {
        if (mMaxProgresstime > 0) return CheckRecipeResultRegistry.SUCCESSFUL;
        state = BayState.IDLE;
        mEfficiency = 0;
        return bay_mode ? startExportCycle() : startImportCycle();
    }

    // private void beginCycle(int ticks, BayState newState) {
    // mProgresstime = 0;
    // mMaxProgresstime = Math.max(1, ticks);
    // mEUt = 0;
    // mEfficiency = 10000;
    // mEfficiencyIncrease = 0;
    // state = newState;
    // markDirty();
    // }
    private void beginCycle(int ticks, BayState newState) {
        mProgresstime = 0;
        mMaxProgresstime = Math.max(1, ticks);
        mEUt = 0;
        mEfficiency = 10000;
        mEfficiencyIncrease = 0;
        state = newState;
        if (newState == BayState.IMPORTING) {
            transitReadyTick = mTotalRunTime + mMaxProgresstime;
        }
        markDirty();
    }

    @Override
    public void stopMachine(@NotNull ShutDownReason reason) {
        state = BayState.IDLE;
        super.stopMachine(reason);
    }

    @Override
    public void onDisableWorking() {
        super.onDisableWorking();
        if (mMaxProgresstime <= 0) {
            state = BayState.IDLE;
        }
    }

    /** Fix for the GUI button: stale whenever not actively running, including after any shutdown. */
    // public boolean is_staling() {
    // return mMaxProgresstime <= 0 || !isAllowedToWork();
    // }

    public boolean is_staling() {
        return mMaxProgresstime <= 0;
    }

    public String get_state_text() {
        return is_staling() ? BayState.IDLE.label : state.label;
    }

    // endregion

    // region export (sell -> virtual credits)

    // private CheckRecipeResult startExportCycle() {
    // if (getControllerSlot() == null) {
    // return NO_CREDIT_CARD;
    // }
    // if (!(getControllerSlot().getItem() instanceof CreditCard)) return NO_CREDIT_CARD;
    // if (!getControllerSlot().hasTagCompound()) {
    // return CARD_NOT_INITIALIZED;
    // }
    // if (!CreditCard.getOwnerID(getControllerSlot()).equals(getBaseMetaTileEntity().getOwnerUuid())) {
    // return WRONG_CREDIT_CARD;
    // }
    // if (!CreditCard.getOwnerID(getControllerSlot()).equals(getBaseMetaTileEntity().getOwnerUuid())) {
    // return WRONG_CREDIT_CARD;
    // }
    //
    // List<SellOffer> offers = new ArrayList<>();
    // collectItemOffers(offers);
    // collectFluidOffers(offers);
    // if (offers.isEmpty()) return CheckRecipeResultRegistry.NO_RECIPE;
    //
    // long earned = 0;
    // for (SellOffer offer : offers) {
    // long removed = offer.sink.remove(offer.amount);
    // if (removed > 0) earned += offer.revenueFor(removed);
    // }
    // if (earned <= 0) return CheckRecipeResultRegistry.NO_RECIPE;
    //
    // // Virtual currency: no output-space check needed, credits can't be "full".
    // TradingCurrencyManager.addBalance(getBaseMetaTileEntity().getOwnerUuid(), earned);
    // totalEarned += earned;
    // lastCycleRevenue = earned;
    // beginCycle(TradingConfig.exportCycleTicks, BayState.EXPORTING);
    // return CheckRecipeResultRegistry.SUCCESSFUL;
    // }

    private CheckRecipeResult startExportCycle() {
        CheckRecipeResult card = validateCreditCard();
        if (!card.wasSuccessful()) return card;

        // Dry run only: nothing is removed and no money is paid until the cycle finishes.
        List<SellOffer> offers = new ArrayList<>();
        collectItemOffers(offers);
        collectFluidOffers(offers);
        double potential = 0;
        for (SellOffer offer : offers) potential += offer.revenueFor(offer.amount);
        if (Math.round(potential) <= 0) return CheckRecipeResultRegistry.NO_RECIPE;

        beginCycle(TradingConfig.exportCycleTicks, BayState.EXPORTING);
        return CheckRecipeResultRegistry.SUCCESSFUL;
    }

    /** Runs at the exact end of an export cycle: removes the goods and pays the owner. */
    private void finishExportCycle() {
        // The card may have been swapped/removed while the cycle ran; re-validate.
        if (!validateCreditCard().wasSuccessful()) return;

        // Re-scan inputs now (contents may have changed during the cycle) and sell what is actually there.
        List<SellOffer> offers = new ArrayList<>();
        collectItemOffers(offers);
        collectFluidOffers(offers);

        double potential = 0;
        for (SellOffer offer : offers) potential += offer.revenueFor(offer.amount);
        if (Math.round(potential) <= 0) return; // don't consume goods for ~0 credits

        double revenue = 0;
        for (SellOffer offer : offers) {
            long removed = offer.sink.remove(offer.amount);
            if (removed > 0) revenue += offer.revenueFor(removed);
        }
        long earned = Math.round(revenue);
        if (earned <= 0) return;

        TradingCurrencyManager.addBalance(getBaseMetaTileEntity().getOwnerUuid(), earned);
        totalEarned += earned;
        lastCycleRevenue = earned;
        markDirty();
    }

    private void collectItemOffers(List<SellOffer> offers) {
        Map<GTUtility.ItemId, Long> meBudget = new HashMap<>();
        for (MTEHatchInputBus bus : GTUtility.validMTEList(mInputBusses)) {
            if (bus instanceof MTEHatchInputBusME meBus) collectMEItemOffers(meBus, offers, meBudget);
            else collectPlainBusOffers(bus, offers);
        }
    }

    private void collectPlainBusOffers(MTEHatchInputBus bus, List<SellOffer> offers) {
        for (int i = 0; i < bus.getSizeInventory(); i++) {
            ItemStack stack = bus.getStackInSlot(i);
            if (stack == null || stack.stackSize <= 0) continue;
            if (GTUtility.isAnyIntegratedCircuit(stack)) continue;
            // long price = TradingConfig.getItemPrice(stack);
            double price = TradingConfig.getItemSellPrice(stack);
            if (price <= 0) continue;
            final int slot = i;
            offers.add(new SellOffer(stack.stackSize, price, false, amount -> {
                ItemStack taken = bus.decrStackSize(slot, (int) amount);
                bus.updateSlots();
                return taken == null ? 0 : taken.stackSize;
            }));
        }
    }

    private void collectMEItemOffers(MTEHatchInputBusME bus, List<SellOffer> offers,
        Map<GTUtility.ItemId, Long> budget) {
        AENetworkProxy proxy = bus.getProxy();
        if (proxy == null || !proxy.isActive()) return;
        for (int i = 0; i < MTEHatchInputBusME.SLOT_COUNT; i++) {
            ItemStack config = bus.getSlotConfig(i);
            if (config == null) continue;
            double price = TradingConfig.getItemSellPrice(config);
            if (price <= 0) continue;
            GTUtility.ItemId id = GTUtility.ItemId.create(config);
            long remainingBudget = budget.getOrDefault(id, (long) TradingConfig.getItemBatch(config));
            if (remainingBudget <= 0) continue;
            IAEItemStack available = extractItems(bus, config, remainingBudget, true);
            long amount = available == null ? 0 : available.getStackSize();
            if (amount <= 0) continue;
            budget.put(id, remainingBudget - amount);
            offers.add(new SellOffer(amount, price, false, requested -> {
                IAEItemStack got = extractItems(bus, config, requested, false);
                return got == null ? 0 : got.getStackSize();
            }));
        }
    }

    private void collectFluidOffers(List<SellOffer> offers) {
        Map<String, Long> meBudget = new HashMap<>();
        for (MTEHatchInput hatch : GTUtility.validMTEList(mInputHatches)) {
            if (hatch instanceof MTEHatchInputME meHatch) collectMEFluidOffers(meHatch, offers, meBudget);
            else collectPlainHatchOffers(hatch, offers);
        }
    }

    // private void collectPlainHatchOffers(MTEHatchInput hatch, List<SellOffer> offers) {
    // FluidStack fluid = hatch.getFluid();
    // if (fluid == null || fluid.amount < 1000) return;
    // long price = TradingConfig.getFluidPricePerBucket(fluid);
    // if (price <= 0) return;
    // int amount = (fluid.amount / 1000) * 1000;
    // offers.add(new SellOffer(amount, price, true, requested -> {
    // FluidStack drained = hatch.drain(ForgeDirection.UNKNOWN, (int) requested, true);
    // return drained == null ? 0 : drained.amount;
    // }));
    // }

    private void collectPlainHatchOffers(MTEHatchInput hatch, List<SellOffer> offers) {
        FluidStack fluid = hatch.getFluid();
        if (fluid == null || fluid.amount < 1000) return;
        double price = TradingConfig.getFluidSellPrice(fluid);
        if (price <= 0) return;
        int amount = (fluid.amount / 1000) * 1000;
        offers.add(new SellOffer(amount, price, true, requested -> {
            FluidStack drained = hatch.drain(ForgeDirection.UNKNOWN, (int) requested, true);
            return drained == null ? 0 : drained.amount;
        }));
    }

    private void collectMEFluidOffers(MTEHatchInputME hatch, List<SellOffer> offers, Map<String, Long> budget) {
        AENetworkProxy proxy = hatch.getProxy();
        if (proxy == null || !proxy.isActive()) return;
        for (int i = 0; i < MTEHatchInputME.SLOT_COUNT; i++) {
            FluidStack config = hatch.getSlotConfig(i);
            if (config == null) continue;
            // long price = TradingConfig.getFluidPricePerBucket(config);
            double price = TradingConfig.getFluidSellPrice(config);
            if (price <= 0) continue;
            String key = config.getFluid()
                .getName();
            long remainingBudget = (budget.getOrDefault(key, (long) TradingConfig.getFluidBatchMb(config)) / 1000)
                * 1000;
            if (remainingBudget < 1000) continue;
            IAEFluidStack available = extractFluids(hatch, config, remainingBudget, true);
            long amount = available == null ? 0 : (available.getStackSize() / 1000) * 1000;
            if (amount < 1000) continue;
            budget.put(key, remainingBudget - amount);
            offers.add(new SellOffer(amount, price, true, requested -> {
                IAEFluidStack got = extractFluids(hatch, config, requested, false);
                return got == null ? 0 : got.getStackSize();
            }));
        }
    }

    private static IAEItemStack extractItems(MTEHatchInputBusME bus, ItemStack prototype, long amount,
        boolean simulate) {
        if (amount <= 0) return null;
        AENetworkProxy proxy = bus.getProxy();
        if (proxy == null || !proxy.isActive()) return null;
        IAEItemStack request = AEItemStack.create(prototype);
        if (request == null) return null;
        request.setStackSize(amount);
        try {
            IMEMonitor<IAEItemStack> monitor = proxy.getStorage()
                .getItemInventory();
            BaseActionSource source = new MachineSource((IActionHost) bus.getBaseMetaTileEntity());
            if (simulate) return monitor.extractItems(request, Actionable.SIMULATE, source);
            return Platform.poweredExtraction(proxy.getEnergy(), monitor, request, source);
        } catch (GridAccessException e) {
            return null;
        }
    }

    private static IAEFluidStack extractFluids(MTEHatchInputME hatch, FluidStack prototype, long amount,
        boolean simulate) {
        if (amount <= 0) return null;
        AENetworkProxy proxy = hatch.getProxy();
        if (proxy == null || !proxy.isActive()) return null;
        IAEFluidStack request = AEFluidStack.create(prototype);
        if (request == null) return null;
        request.setStackSize(amount);
        try {
            IMEMonitor<IAEFluidStack> monitor = proxy.getStorage()
                .getFluidInventory();
            BaseActionSource source = new MachineSource((IActionHost) hatch.getBaseMetaTileEntity());
            if (simulate) return monitor.extractItems(request, Actionable.SIMULATE, source);
            return Platform.poweredExtraction(proxy.getEnergy(), monitor, request, source);
        } catch (GridAccessException e) {
            return null;
        }
    }

    @FunctionalInterface
    private interface GoodsSink {

        long remove(long amount);
    }

    private static final class SellOffer {

        final long amount;
        final double pricePerUnit;
        final boolean perBucket;
        final GoodsSink sink;

        SellOffer(long amount, double pricePerUnit, boolean perBucket, GoodsSink sink) {
            this.amount = amount;
            this.pricePerUnit = pricePerUnit;
            this.perBucket = perBucket;
            this.sink = sink;
        }

        double revenueFor(long n) {
            return perBucket ? n * pricePerUnit / 1000 : n * pricePerUnit;
        }
    }

    // endregion

    // region import (buy with credits -> internal buffer)

    // private CheckRecipeResult startImportCycle() {
    // if (getControllerSlot() == null) {
    // return NO_CREDIT_CARD;
    // }
    // if (!(getControllerSlot().getItem() instanceof CreditCard)) return NO_CREDIT_CARD;
    // if (!getControllerSlot().hasTagCompound()) {
    // return CARD_NOT_INITIALIZED;
    // }
    // if (!CreditCard.getOwnerID(getControllerSlot()).equals(getBaseMetaTileEntity().getOwnerUuid())) {
    // return WRONG_CREDIT_CARD;
    // }
    //
    // List<Contract.Order> orders = Contract.readOrders(loadedContract);
    // List<Contract.FluidOrder> forders = Contract.readFluidOrders(loadedContract);
    // if (itemRemaining == null || itemRemaining.length != orders.size()) {
    // itemRemaining = new int[orders.size()];
    // for (int i = 0; i < orders.size(); i++) itemRemaining[i] = orders.get(i).amount;
    // }
    // if (fluidRemaining == null || fluidRemaining.length != forders.size()) {
    // fluidRemaining = new int[forders.size()];
    // for (int i = 0; i < forders.size(); i++) fluidRemaining[i] = forders.get(i).amount;
    // }
    //
    // UUID owner = getBaseMetaTileEntity().getOwnerUuid();
    // BigInteger budget = TradingCurrencyManager.getBalance(owner);
    // BigInteger totalCost = BigInteger.ZERO;
    // int[] itemTake = new int[orders.size()];
    // int[] fluidTake = new int[forders.size()];
    // Map<GTUtility.ItemId, Long> plannedItems = new LinkedHashMap<>();
    // Map<GTUtility.FluidId, Long> plannedFluids = new LinkedHashMap<>();
    // boolean anythingLeft = false;
    //
    // for (int i = 0; i < orders.size(); i++) {
    // int remaining = itemRemaining[i];
    // if (remaining <= 0) continue;
    // Contract.Order order = orders.get(i);
    //// long price = TradingConfig.getItemPrice(order.item);
    // double price = TradingConfig.getItemBuyPrice(order.item);
    // if (price <= 0) continue; // untradeable
    // anythingLeft = true;
    // long affordable = budget.divide(BigInteger.valueOf((long)Math.ceil(price)))
    // .min(BigInteger.valueOf(Integer.MAX_VALUE))
    // .longValue();
    // int take = remaining;
    // if (TradingConfig.importItemBatch > 0) take = Math.min(take, TradingConfig.importItemBatch);
    // take = (int) Math.min(take, affordable);
    // if (take <= 0) continue;
    // BigInteger cost = BigInteger.valueOf(Math.round(price))
    // .multiply(BigInteger.valueOf(take));
    // budget = budget.subtract(cost);
    // totalCost = totalCost.add(cost);
    // itemTake[i] = take;
    // plannedItems.merge(GTUtility.ItemId.create(order.item), (long) take, Long::sum);
    // }
    // for (int i = 0; i < forders.size(); i++) {
    // int remaining = fluidRemaining[i];
    // if (remaining < 1000) continue;
    // Contract.FluidOrder order = forders.get(i);
    // // long price = TradingConfig.getFluidPricePerBucket(order.fluid);
    // double price = TradingConfig.getFluidBuyPrice(order.fluid);
    // if (price <= 0) continue;
    // anythingLeft = true;
    // long affordableBuckets = budget.divide(BigInteger.valueOf((long)Math.ceil(price)))
    // .min(BigInteger.valueOf(Integer.MAX_VALUE / 1000))
    // .longValue();
    // int take = (remaining / 1000) * 1000;
    // if (TradingConfig.importFluidBatchMb > 0) {
    // take = Math.min(take, (TradingConfig.importFluidBatchMb / 1000) * 1000);
    // }
    // take = (int) Math.min(take, affordableBuckets * 1000);
    // if (take < 1000) continue;
    // BigInteger cost = BigInteger.valueOf(Math.round(price))
    // .multiply(BigInteger.valueOf(take / 1000));
    // budget = budget.subtract(cost);
    // totalCost = totalCost.add(cost);
    // fluidTake[i] = take;
    // plannedFluids.merge(GTUtility.FluidId.create(order.fluid), (long) take, Long::sum);
    // }
    //
    // if (totalCost.signum() == 0) {
    // if (!anythingLeft) {
    // clearContract();
    // return CheckRecipeResultRegistry.NO_RECIPE;
    // }
    // return INSUFFICIENT_FUNDS;
    // }
    // if (!TradingCurrencyManager.tryCharge(owner, totalCost)) return INSUFFICIENT_FUNDS;
    //
    // for (int i = 0; i < itemTake.length; i++) itemRemaining[i] -= itemTake[i];
    // for (int i = 0; i < fluidTake.length; i++) fluidRemaining[i] -= fluidTake[i];
    // plannedItems.forEach((k, v) -> transitItems.merge(k, v, Long::sum));
    // plannedFluids.forEach((k, v) -> transitFluids.merge(k, v, Long::sum));
    // if (contractDone()) clearContract();
    //
    // beginCycle(TradingConfig.importCycleTicks, BayState.IMPORTING);
    // return CheckRecipeResultRegistry.SUCCESSFUL;
    // }

    private CheckRecipeResult startImportCycle() {
        CheckRecipeResult card = validateCreditCard();
        if (!card.wasSuccessful()) return card;

        List<Contract.Order> orders = Contract.readOrders(loadedContract);
        List<Contract.FluidOrder> forders = Contract.readFluidOrders(loadedContract);
        if (itemRemaining == null || itemRemaining.length != orders.size()) {
            itemRemaining = new int[orders.size()];
            for (int i = 0; i < orders.size(); i++) itemRemaining[i] = orders.get(i).amount;
        }
        if (fluidRemaining == null || fluidRemaining.length != forders.size()) {
            fluidRemaining = new int[forders.size()];
            for (int i = 0; i < forders.size(); i++) fluidRemaining[i] = forders.get(i).amount;
        }

        UUID owner = getBaseMetaTileEntity().getOwnerUuid();
        BigInteger budget = TradingCurrencyManager.getBalance(owner);
        BigInteger totalCost = BigInteger.ZERO;
        int[] itemTake = new int[orders.size()];
        int[] fluidTake = new int[forders.size()];
        Map<GTUtility.ItemId, Long> plannedItems = new LinkedHashMap<>();
        Map<GTUtility.FluidId, Long> plannedFluids = new LinkedHashMap<>();
        boolean anythingLeft = false;

        for (int i = 0; i < orders.size(); i++) {
            int remaining = itemRemaining[i];
            if (remaining <= 0) continue;
            Contract.Order order = orders.get(i);
            double price = TradingConfig.getItemBuyPrice(order.item);
            if (price <= 0) continue; // untradeable
            anythingLeft = true;
            BigDecimal unitPrice = BigDecimal.valueOf(price);
            long affordable = new BigDecimal(budget).divideToIntegralValue(unitPrice)
                .min(BigDecimal.valueOf(Integer.MAX_VALUE))
                .longValue();
            int take = remaining;
            if (TradingConfig.importItemBatch > 0) take = Math.min(take, TradingConfig.importItemBatch);
            take = (int) Math.min(take, affordable);
            if (take <= 0) continue;
            BigInteger cost = unitPrice.multiply(BigDecimal.valueOf(take))
                .setScale(0, RoundingMode.CEILING)
                .toBigInteger();
            budget = budget.subtract(cost);
            totalCost = totalCost.add(cost);
            itemTake[i] = take;
            plannedItems.merge(GTUtility.ItemId.create(order.item), (long) take, Long::sum);
        }
        for (int i = 0; i < forders.size(); i++) {
            int remaining = fluidRemaining[i];
            if (remaining < 1000) continue;
            Contract.FluidOrder order = forders.get(i);
            double price = TradingConfig.getFluidBuyPrice(order.fluid);
            if (price <= 0) continue;
            anythingLeft = true;
            BigDecimal unitPrice = BigDecimal.valueOf(price); // credits per bucket
            long affordableBuckets = new BigDecimal(budget).divideToIntegralValue(unitPrice)
                .min(BigDecimal.valueOf(Integer.MAX_VALUE / 1000))
                .longValue();
            int take = (remaining / 1000) * 1000;
            if (TradingConfig.importFluidBatchMb > 0) {
                take = Math.min(take, (TradingConfig.importFluidBatchMb / 1000) * 1000);
            }
            take = (int) Math.min(take, affordableBuckets * 1000);
            if (take < 1000) continue;
            BigInteger cost = unitPrice.multiply(BigDecimal.valueOf(take / 1000))
                .setScale(0, RoundingMode.CEILING)
                .toBigInteger();
            budget = budget.subtract(cost);
            totalCost = totalCost.add(cost);
            fluidTake[i] = take;
            plannedFluids.merge(GTUtility.FluidId.create(order.fluid), (long) take, Long::sum);
        }

        if (totalCost.signum() == 0) {
            if (!anythingLeft) {
                clearContract();
                return CheckRecipeResultRegistry.NO_RECIPE;
            }
            return INSUFFICIENT_FUNDS;
        }
        // Money is charged the moment the import cycle starts; goods arrive in the buffer at cycle end.
        if (!TradingCurrencyManager.tryCharge(owner, totalCost)) return INSUFFICIENT_FUNDS;

        for (int i = 0; i < itemTake.length; i++) itemRemaining[i] -= itemTake[i];
        for (int i = 0; i < fluidTake.length; i++) fluidRemaining[i] -= fluidTake[i];
        plannedItems.forEach((k, v) -> transitItems.merge(k, v, Long::sum));
        plannedFluids.forEach((k, v) -> transitFluids.merge(k, v, Long::sum));
        if (contractDone()) clearContract();

        beginCycle(TradingConfig.importCycleTicks, BayState.IMPORTING);
        return CheckRecipeResultRegistry.SUCCESSFUL;
    }

    private boolean contractDone() {
        if (itemRemaining != null) for (int r : itemRemaining) if (r > 0) return false;
        if (fluidRemaining != null) for (int r : fluidRemaining) if (r >= 1000) return false;
        return true;
    }

    private void clearContract() {
        loadedContract = null;
        itemRemaining = null;
        fluidRemaining = null;
        markDirty();
    }

    private void mergeTransitIntoBuffer() {
        if (transitItems.isEmpty() && transitFluids.isEmpty()) return;
        transitItems.forEach((k, v) -> bufferItems.merge(k, v, Long::sum));
        transitFluids.forEach((k, v) -> bufferFluids.merge(k, v, Long::sum));
        transitItems.clear();
        transitFluids.clear();
        markDirty();
    }

    // endregion

    // region background ejection (runs while staling / importing / exporting)

    private void ejectBufferedGoods() {
        if (bufferItems.isEmpty() && bufferFluids.isEmpty()) return;
        boolean changed = false;

        long itemBudget = EJECT_ITEMS_PER_TICK;
        for (Iterator<Map.Entry<GTUtility.ItemId, Long>> it = bufferItems.entrySet()
            .iterator(); it.hasNext() && itemBudget > 0;) {
            Map.Entry<GTUtility.ItemId, Long> entry = it.next();
            long remaining = entry.getValue();
            ItemStack proto = entry.getKey()
                .getItemStack();
            int max = Math.max(1, proto.getMaxStackSize());
            while (remaining > 0 && itemBudget > 0) {
                int n = (int) Math.min(Math.min(remaining, max), itemBudget);
                ItemStack stack = GTUtility.copyAmount(n, proto);
                for (MTEHatchOutputBus bus : GTUtility.validMTEList(mOutputBusses)) {
                    if (bus.storePartial(stack, false)) break;
                }
                int moved = n - stack.stackSize;
                if (moved <= 0) break; // outputs full for this item
                remaining -= moved;
                itemBudget -= moved;
                changed = true;
            }
            if (remaining <= 0) it.remove();
            else entry.setValue(remaining);
        }

        long fluidBudget = EJECT_FLUID_PER_TICK;
        for (Iterator<Map.Entry<GTUtility.FluidId, Long>> it = bufferFluids.entrySet()
            .iterator(); it.hasNext() && fluidBudget > 0;) {
            Map.Entry<GTUtility.FluidId, Long> entry = it.next();
            long remaining = entry.getValue();
            FluidStack proto = entry.getKey()
                .getFluidStack();
            while (remaining > 0 && fluidBudget > 0) {
                int n = (int) Math.min(remaining, fluidBudget);
                FluidStack stack = new FluidStack(proto, n);
                for (MTEHatchOutput hatch : GTUtility.validMTEList(mOutputHatches)) {
                    if (hatch.storePartial(stack, false)) break;
                }
                int moved = n - stack.amount;
                if (moved <= 0) break;
                remaining -= moved;
                fluidBudget -= moved;
                changed = true;
            }
            if (remaining <= 0) it.remove();
            else entry.setValue(remaining);
        }

        if (changed) markDirty();
    }

    // endregion

    // region interaction / GUI getters

    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        ItemStack held = aPlayer.getHeldItem();
        if (held != null && held.getItem() instanceof Contract) {
            if (aPlayer.worldObj.isRemote) return true;
            if (!Contract.getOwnerID(held)
                .equals(getBaseMetaTileEntity().getOwnerUuid())) {
                msg(aPlayer, "Contract DOES NOT belong to the owner of the machine!");
                return true;
            }
            if (bay_mode) {
                msg(aPlayer, "Switch the bay to Import mode first (screwdriver).");
                return true;
            }
            if (!is_staling()) {
                msg(aPlayer, "Can't load a contract while the bay is busy.");
                return true;
            }
            if (loadedContract != null) {
                msg(aPlayer, "A contract is already loaded.");
                return true;
            }
            loadedContract = held.copy();
            loadedContract.stackSize = 1;
            itemRemaining = null;
            fluidRemaining = null; // initialized on first import cycle
            markDirty();
            msg(aPlayer, "Contract loaded, import started.");
            scheduleRecipeCheckImmediate();
            return true;
        }
        return super.onRightclick(aBaseMetaTileEntity, aPlayer);
    }

    @Override
    public void onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ,
        ItemStack aTool) {
        if (!is_staling()) {
            msg(aPlayer, "Mode can only be switched while the bay is idle.");
            return;
        }
        set_bay_mode(!bay_mode);
        msg(aPlayer, "Bay mode switched to " + bay_mode_representation());
    }

    private static void msg(EntityPlayer player, String text) {
        if (!player.worldObj.isRemote) player.addChatMessage(new ChatComponentText(text));
    }

    public boolean get_bay_mode() {
        return bay_mode;
    }

    public void set_bay_mode(boolean val) {
        if (is_staling()) {
            bay_mode = val;
            markDirty();
            scheduleRecipeCheckImmediate();
        }
    }

    public String bay_mode_representation() {
        return bay_mode ? "Export" : "Import";
    }

    public int get_running_second() {
        return mProgresstime / 20;
    }

    public int get_total_import_time() {
        return mMaxProgresstime / 20;
    }

    public BigInteger getOwnerBalance() {
        return TradingCurrencyManager.getBalance(getBaseMetaTileEntity().getOwnerUuid());
    }

    public long getTotalEarned() {
        return totalEarned;
    }

    public long getLastCycleRevenue() {
        return lastCycleRevenue;
    }

    public boolean hasBufferedGoods() {
        return !bufferItems.isEmpty() || !bufferFluids.isEmpty();
    }

    public long getBufferedItemCount() {
        long sum = 0;
        for (long v : bufferItems.values()) sum += v;
        return sum;
    }

    public long getBufferedFluidCount() {
        long sum = 0;
        for (long v : bufferFluids.values()) sum += v;
        return sum;
    }

    // endregion

    // region misc overrides

    @Override
    public boolean supportsVoidProtection() {
        return false;
    }

    @Override
    protected SoundResource getProcessStartSound() {
        return SoundResource.GTCEU_LOOP_MOTOR;
    }

    @Override
    public int getPollutionPerSecond(final ItemStack aStack) {
        return 0;
    }

    @Override
    public boolean getDefaultHasMaintenanceChecks() {
        return false;
    }

    @Override
    public boolean showRecipeTextInGUI() {
        return false;
    }

    @Override
    public boolean hasRunningText() {
        return false;
    }

    @Override
    public boolean showMachineStatusInGUI() {
        return false;
    }

    @Override
    protected @NotNull MTEMultiBlockBaseGui<?> getGui() {
        return new MTETradingBayGui(this);
    }

    // endregion

    // region NBT

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setBoolean("bayMode", bay_mode);
        aNBT.setInteger("bayState", state.ordinal());
        aNBT.setLong("totalEarned", totalEarned);
        aNBT.setLong("lastCycleRevenue", lastCycleRevenue);
        if (loadedContract != null) {
            NBTTagCompound c = new NBTTagCompound();
            loadedContract.writeToNBT(c);
            aNBT.setTag("loadedContract", c);
        }
        if (itemRemaining != null) aNBT.setIntArray("itemRemaining", itemRemaining);
        if (fluidRemaining != null) aNBT.setIntArray("fluidRemaining", fluidRemaining);
        saveItems(aNBT, "bufferItems", bufferItems);
        saveFluids(aNBT, "bufferFluids", bufferFluids);
        saveItems(aNBT, "transitItems", transitItems);
        saveFluids(aNBT, "transitFluids", transitFluids);
        aNBT.setLong("transitReadyTick", transitReadyTick);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        bay_mode = aNBT.hasKey("bayMode") ? aNBT.getBoolean("bayMode") : aNBT.getBoolean("bay_mode");
        int s = aNBT.getInteger("bayState");
        state = (s >= 0 && s < BayState.values().length) ? BayState.values()[s] : BayState.IDLE;
        totalEarned = aNBT.getLong("totalEarned");
        lastCycleRevenue = aNBT.getLong("lastCycleRevenue");
        loadedContract = aNBT.hasKey("loadedContract")
            ? ItemStack.loadItemStackFromNBT(aNBT.getCompoundTag("loadedContract"))
            : null;
        itemRemaining = aNBT.hasKey("itemRemaining") ? aNBT.getIntArray("itemRemaining") : null;
        fluidRemaining = aNBT.hasKey("fluidRemaining") ? aNBT.getIntArray("fluidRemaining") : null;
        loadItems(aNBT, "bufferItems", bufferItems);
        loadFluids(aNBT, "bufferFluids", bufferFluids);
        loadItems(aNBT, "transitItems", transitItems);
        loadFluids(aNBT, "transitFluids", transitFluids);
        transitReadyTick = aNBT.getLong("transitReadyTick");
    }

    private static void saveItems(NBTTagCompound nbt, String key, Map<GTUtility.ItemId, Long> map) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<GTUtility.ItemId, Long> e : map.entrySet()) {
            if (e.getValue() == null || e.getValue() <= 0) continue;
            ItemStack proto = e.getKey()
                .getItemStack();
            proto.stackSize = 1;
            NBTTagCompound t = new NBTTagCompound();
            t.setTag("stack", proto.writeToNBT(new NBTTagCompound()));
            t.setLong("amount", e.getValue());
            list.appendTag(t);
        }
        nbt.setTag(key, list);
    }

    private static void loadItems(NBTTagCompound nbt, String key, Map<GTUtility.ItemId, Long> map) {
        map.clear();
        NBTTagList list = nbt.getTagList(key, 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound t = list.getCompoundTagAt(i);
            ItemStack stack = ItemStack.loadItemStackFromNBT(t.getCompoundTag("stack"));
            long amount = t.getLong("amount");
            if (stack != null && amount > 0) map.merge(GTUtility.ItemId.create(stack), amount, Long::sum);
        }
    }

    private static void saveFluids(NBTTagCompound nbt, String key, Map<GTUtility.FluidId, Long> map) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<GTUtility.FluidId, Long> e : map.entrySet()) {
            if (e.getValue() == null || e.getValue() <= 0) continue;
            FluidStack proto = e.getKey()
                .getFluidStack();
            proto.amount = 1;
            NBTTagCompound t = new NBTTagCompound();
            t.setTag("fluid", proto.writeToNBT(new NBTTagCompound()));
            t.setLong("amount", e.getValue());
            list.appendTag(t);
        }
        nbt.setTag(key, list);
    }

    private static void loadFluids(NBTTagCompound nbt, String key, Map<GTUtility.FluidId, Long> map) {
        map.clear();
        NBTTagList list = nbt.getTagList(key, 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound t = list.getCompoundTagAt(i);
            FluidStack fluid = FluidStack.loadFluidStackFromNBT(t.getCompoundTag("fluid"));
            long amount = t.getLong("amount");
            if (fluid != null && amount > 0) map.merge(GTUtility.FluidId.create(fluid), amount, Long::sum);
        }
    }

    private CheckRecipeResult validateCreditCard() {
        ItemStack card = getControllerSlot();
        if (card == null || !(card.getItem() instanceof CreditCard)) return NO_CREDIT_CARD;
        if (!card.hasTagCompound()) return CARD_NOT_INITIALIZED;
        if (!CreditCard.getOwnerID(card)
            .equals(getBaseMetaTileEntity().getOwnerUuid())) return WRONG_CREDIT_CARD;
        return CheckRecipeResultRegistry.SUCCESSFUL;
    }

    @Override
    protected void outputAfterRecipe() {
        super.outputAfterRecipe();
        // Called by MTEMultiBlockBase.runMachine() on the exact tick the cycle finishes,
        // before progress is reset and before the next checkProcessing() runs.
        if (state == BayState.EXPORTING) {
            finishExportCycle();
        }
        // Import goods were paid for at cycle start; they become ejectable now the cycle is done.
        mergeTransitIntoBuffer();
        state = BayState.IDLE;
    }
    // endregion
}

// items should be imported and exported only in the end of the cycle of the machine progress, first we wait, and only
// then we have the result
// prices for the items are obtained with TradingConfig.getFluidBuyPrice(fluidstack);
// TradingConfig.getFluidSellPrice(fluidstack); TradingConfig.getItemBuyPrice(itemstack);
// TradingConfig.getItemSellPrice(itemstack);
