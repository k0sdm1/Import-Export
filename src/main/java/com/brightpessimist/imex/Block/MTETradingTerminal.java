package com.brightpessimist.imex.Block;

import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import com.brightpessimist.imex.gui.MTETradingTerminalGui;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEBasicTank;

public class MTETradingTerminal extends MTEBasicTank {

    private static final long CREDITS_PER_COAL = 10;
    private static final long CREDITS_PER_LOG = 5;
    private static final long PRICE_IRON_INGOT = 50;

    // accepted fluids, by Fluid.getName(): "oil", "gas", "hydrogen", ...
    private static final List<String> ACCEPTED_FLUIDS = Arrays.asList("oil", "gas", "hydrogen");

    private long mCredits = 0;

    public MTETradingTerminal(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier, 3, "Exports materials for Credits and imports others");
    }

    public MTETradingTerminal(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, 3, aDescription, aTextures);
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTETradingTerminal(mName, mTier, mDescriptionArray, mTextures);
    }

    // ---------- tank ----------
    @Override
    public boolean doesFillContainers() {
        return false;
    }

    @Override
    public boolean doesEmptyContainers() {
        return false;
    }

    @Override
    public boolean canTankBeFilled() {
        return true;
    }

    @Override
    public boolean canTankBeEmptied() {
        return true;
    }

    @Override
    public int getCapacity() {
        return 2000000000;
    }

    @Override
    public boolean isFluidInputAllowed(FluidStack aFluid) {
        return aFluid != null && aFluid.getFluid() != null
            && ACCEPTED_FLUIDS.contains(
                aFluid.getFluid()
                    .getName());
    }

    // ---------- item I/O ----------
    @Override
    public boolean allowPutStack(IGregTechTileEntity base, int index, ForgeDirection side, ItemStack stack) {
        return index == getInputSlot() && isExportableItem(stack);
    }

    @Override
    public boolean allowPullStack(IGregTechTileEntity base, int index, ForgeDirection side, ItemStack stack) {
        return index == getOutputSlot();
    }

    private static boolean isExportableItem(ItemStack stack) {
        if (stack == null) return false;
        if (stack.getItem() == Items.coal) return true;
        for (int oreId : OreDictionary.getOreIDs(stack)) {
            if ("logWood".equals(OreDictionary.getOreName(oreId))) return true;
        }
        return false;
    }

    // ---------- logic ----------
    @Override
    public void onPostTick(IGregTechTileEntity base, long tick) {
        super.onPostTick(base, tick);
        if (!base.isServerSide()) return;

        // EXPORT: coal/logs -> credits
        ItemStack input = mInventory[getInputSlot()];
        if (input != null && isExportableItem(input)) {
            mCredits += (input.getItem() == Items.coal ? CREDITS_PER_COAL : CREDITS_PER_LOG) * input.stackSize;
            base.decrStackSize(getInputSlot(), input.stackSize);
            base.markDirty();
        }
        // NEXT STEP: convert getFillableStack() fluid into credits here (drain + credit per bucket).
    }

    // ---------- NBT ----------
    @Override
    public void saveNBTData(NBTTagCompound nbt) {
        super.saveNBTData(nbt);
        nbt.setLong("mCredits", mCredits);
    }

    @Override
    public void loadNBTData(NBTTagCompound nbt) {
        super.loadNBTData(nbt);
        mCredits = nbt.getLong("mCredits");
    }

    // ---------- GUI ----------
    // @Override
    // public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext ctx) {
    // return;
    // super.addUIWidgets(builder, ctx); // slots + fluid widget from MTEBasicTank
    // builder.widget(
    // new TextWidget().setStringSupplier(() -> "Credits: " + mCredits)
    // .setDefaultColor(COLOR_TEXT_WHITE.get())
    // .setPos(10, 42))
    // .widget(
    // new ButtonWidget()
    // .setOnClick((clickData, widget) -> buyItem(new ItemStack(Items.iron_ingot), PRICE_IRON_INGOT))
    // .setPlayClickSound(true)
    // .setBackground(GTUITextures.BUTTON_STANDARD)
    // .addTooltip("Buy Iron Ingot (" + PRICE_IRON_INGOT + " Credits)")
    // .setPos(100, 40)
    // .setSize(18, 18));
    // }

    // @Override
    // public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
    // super.addUIWidgets(builder, buildContext);
    // builder.widget(
    // new TextWidget().setStringSupplier(() -> "Credits: " + mCredits)
    // .setDefaultColor(COLOR_TEXT_WHITE.get())
    // .setPos(10, 42))
    // .widget(
    // new ButtonWidget()
    // .setOnClick((clickData, widget) -> buyItem(new ItemStack(Items.iron_ingot), PRICE_IRON_INGOT))
    // .setPlayClickSound(true)
    // .setBackground(GTUITextures.BUTTON_STANDARD)
    // .addTooltip("Buy Iron Ingot (" + PRICE_IRON_INGOT + " Credits)")
    // .setPos(100, 40)
    // .setSize(18, 18));
    // // System.exit(0);
    // }

    // ---------- texture (machine casing, hull-style) ----------
    @Override
    public ITexture[] getTexture(IGregTechTileEntity base, ForgeDirection side, ForgeDirection facing, int colorIndex,
        boolean connected, boolean redstoneLevel) {
        return mTextures[Math.min(2, side.ordinal()) + (side == facing ? 3 : 0)][colorIndex + 1];
    }

    @Override
    public ITexture[][][] getTextureSet(ITexture[] textures) {
        ITexture[][][] r = new ITexture[6][17][];
        for (byte i = -1; i < 16; i++) {
            for (int j = 0; j < 6; j++) {
                r[j][i + 1] = new ITexture[] { Textures.BlockIcons.MACHINE_CASINGS[mTier][i + 1] };
            }
        }
        return r;
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        if (!aPlayer.worldObj.isRemote) {
            aPlayer.addChatMessage(new ChatComponentText("clicked"));
        }
        openGui(aPlayer);
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings uiSettings) {
        return new MTETradingTerminalGui<>(this).build(guiData, syncManager, uiSettings);
    }

    public long getCredits() {
        return mCredits;
    }

    public void buyItem(ItemStack product, long price) {
        IGregTechTileEntity base = getBaseMetaTileEntity();
        if (base == null || base.isClientSide()) return; // safety: logic only on server
        if (mCredits < price) return;
        if (base.addStackToSlot(getOutputSlot(), product, 1)) {
            mCredits -= price;
            base.markDirty();
        }
    }
}
