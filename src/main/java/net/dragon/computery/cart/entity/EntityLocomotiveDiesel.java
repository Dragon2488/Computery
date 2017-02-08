package net.dragon.computery.cart.entity;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import mods.railcraft.api.carts.IFluidCart;
import mods.railcraft.api.carts.IRefuelableCart;
import mods.railcraft.api.carts.locomotive.LocomotiveRenderType;
import mods.railcraft.api.electricity.IElectricMinecart;
import mods.railcraft.common.carts.*;
import mods.railcraft.common.fluids.TankManager;
import mods.railcraft.common.fluids.tanks.StandardTank;
import mods.railcraft.common.items.ItemTicket;
import mods.railcraft.common.util.inventory.wrappers.InventoryMapper;
import mods.railcraft.common.util.sounds.SoundHelper;
import net.dragon.computery.ComputeryMod;
import net.dragon.computery.ModGuiHandler;
import net.dragon.computery.block.INFCTrackable;
import net.dragon.computery.cart.ModEnumCart;
import net.dragon.computery.cart.container.ContainerLocomotiveDiesel;
import net.dragon.computery.cart.render.ModRenderType;
import net.dragon.computery.component.LocomotiveCard;
import net.dragon.computery.item.ItemOpenComputers;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;

import java.util.UUID;

public class EntityLocomotiveDiesel extends EntityLocomotive implements IFluidHandler, IRefuelableCart, IFluidCart, ISidedInventory, INFCTrackable {

    private static final int SLOT_TICKET = 5;
    private static final int[] SLOTS = new int[] {0, 1, 2, 3, SLOT_TICKET};
    private final IInventory invTicket = new InventoryMapper(this, SLOT_TICKET, 2, false);

    private TankManager tankManager;
    private StandardTank fuelTank;
    private StandardTank coolantTank;

    public EntityLocomotiveDiesel(World world) {
        super(world);
    }

    public EntityLocomotiveDiesel(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public UUID getLocomotiveCardId() {
        ItemStack card = getStackInSlot(4);
        if(card != null) {
            return ItemOpenComputers.getLocomotiveCardId(card);
        }
        return null;
    }

    @Override
    public void onNFCTrackPassed(int nfcVariable) {
        UUID networkId = getLocomotiveCardId();
        if(networkId != null) {
            LocomotiveCard.pushSignal(networkId, "nfcTrackPass", nfcVariable);
        }
    }

    @Override
    public void initEntityFromItem(ItemStack item) {
       if(item != null && item.hasTagCompound()) {
           NBTTagCompound tag = item.getTagCompound();
           if (tag.hasKey("UUIDMost", 4)) {
               this.entityUniqueID = new UUID(tag.getLong("UUIDMost"), tag.getLong("UUIDLeast"));
           }
           super.initEntityFromItem(item);
       }
    }

    @Override
    public ItemStack getCartItem() {
        ItemStack stack = super.getCartItem();
        NBTTagCompound tag = stack.getTagCompound();
        tag.setLong("UUIDMost", entityUniqueID.getMostSignificantBits());
        tag.setLong("UUIDLeast", entityUniqueID.getLeastSignificantBits());
        return stack;
    }



    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataWatcher.addObject(31, new Integer(0xFFFFFF));
        this.fuelTank = new StandardTank(32000);
        this.coolantTank = new StandardTank(32000);
        this.tankManager = new TankManager(fuelTank, coolantTank);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if(!worldObj.isRemote) {
            manageTank(fuelTank, 0, 1); //Fuel
            manageTank(coolantTank, 2, 3); //Coolant

            Train train = Train.getTrain(this);
            if(train != null && getFuel() > 1) {
                for(EntityMinecart minecart : train) {
                    if(minecart == this) continue;
                    if(isRunning() && minecart instanceof IElectricMinecart) {
                        double euCanEmit = getEuPerTickMax();
                        IElectricMinecart energyCart = (IElectricMinecart) minecart;
                        IElectricMinecart.ChargeHandler chargeHandler = energyCart.getChargeHandler();
                        double canReceive = chargeHandler.getCapacity() - chargeHandler.getCharge();
                        if(canReceive >= euCanEmit) {
                            int consume = consumeAndRefuel(1);
                            if(consume == 1) {
                                ObfuscationReflectionHelper.setPrivateValue(
                                        IElectricMinecart.ChargeHandler.class, chargeHandler,
                                        chargeHandler.getCharge() + euCanEmit, "charge");
                            }
                        }
                    }
                    if(minecart instanceof EntityLocomotive) {
                        EntityLocomotive locomotive = (EntityLocomotive) minecart;
                        locomotive.setSpeed(getSpeed());
                        locomotive.setMode(getMode());
                    }
                }
            }
        }


    }

    public int consumeAndRefuel(int fuelConsumption) {
        if(fuelConsumption >= getFuel()) {
            setFuel(getFuel() + getMoreGoJuice());
        }
        int fuel = getFuel();
        if(fuel >= fuelConsumption) {
            setFuel(fuel - fuelConsumption);
            return fuelConsumption;
        }
        setFuel(0);
        return fuel;
    }

    public int getFuel() {
        return ObfuscationReflectionHelper.getPrivateValue(EntityLocomotive.class, this, "fuel");
    }

    public void setFuel(int fuel) {
        ObfuscationReflectionHelper.setPrivateValue(EntityLocomotive.class, this, fuel, "fuel");
        setHasFuel(fuel > 0);
    }

    public int getEuPerTickMax() {
        return 128;
    }

    @Override
    public int getMoreGoJuice() {
        if (!fuelTank.isEmpty() && !coolantTank.isEmpty()) {
            for (GT_Recipe recipe : GT_Recipe.GT_Recipe_Map.sDieselFuels.mRecipeList) {
                FluidStack stack = GT_Utility.getFluidForFilledItem(recipe.mInputs[0], true);
                if (stack.getFluid() == fuelTank.getFluidType()) {
                    float fuel = recipe.mSpecialValue * 0.034F;
                    float coolant = getCoolantModifier(coolantTank.getFluidType()) * 10f;

                    fuelTank.drain((int) Math.ceil(fuel > 10f ? 2f : (fuel > 8f ? 1f : 4f)), true);
                    coolantTank.drain((int) Math.ceil(coolant > 20f ? 2 : (coolant > 15f ? 1 : 3f)), true);

                    return (int) Math.ceil(fuel * coolant * 0.087f);
                }
            }
        }
        return 0;
    }

    @Override
    public boolean needsRefuel() {
        return coolantTank.isEmpty() || fuelTank.isEmpty();
    }

    private void manageTank(StandardTank fuelTank, int inSlot, int outSlot) {
        int fuelSpace = fuelTank.getRemainingSpace();
        ItemStack fuelBucket = getStackInSlot(inSlot);

        if(fuelBucket != null && fuelSpace != 0) {
            Item fuelItem = fuelBucket.getItem();

            if(fuelItem instanceof IFluidContainerItem) {

                ItemStack oneContainer = fuelBucket.copy();
                oneContainer.stackSize = 1;
                IFluidContainerItem fuelContainer = (IFluidContainerItem) fuelItem;
                FluidStack fuelFluid = fuelContainer.getFluid(oneContainer);
                Fluid fluid = fuelFluid == null ? null : fuelFluid.getFluid();

                if(fluid != null && (fuelTank.isEmpty() || fuelTank.getFluidType() == fluid)) {
                    FluidStack addedAmount = fuelContainer.drain(oneContainer, fuelSpace, true);

                    if(addedAmount != null && addedAmount.amount != 0) {
                        fuelTank.fill(addedAmount, true);

                        if (--fuelBucket.stackSize == 0)
                            setInventorySlotContents(inSlot, null);

                        ItemStack output = getStackInSlot(outSlot);
                        if (canMergeStacks(oneContainer, output)) {
                            if (output == null)
                                setInventorySlotContents(outSlot, oneContainer);
                            else output.stackSize++;
                        } else worldObj.spawnEntityInWorld(new EntityItem(worldObj, posX, posY, posZ, oneContainer));
                    }

                }

            } else if(FluidContainerRegistry.isFilledContainer(fuelBucket)) {

                FluidStack fuelFluid = FluidContainerRegistry.getFluidForFilledItem(fuelBucket);
                ItemStack empty = FluidContainerRegistry.drainFluidContainer(fuelBucket);
                ItemStack output = getStackInSlot(outSlot);

                if(fuelSpace >= fuelFluid.amount && canMergeStacks(empty, output)) {
                    fuelTank.fill(fuelFluid, true);

                    if(--fuelBucket.stackSize == 0)
                        setInventorySlotContents(inSlot, null);

                    if(empty != null) {
                        if(output == null)
                            setInventorySlotContents(outSlot, empty);
                        else output.stackSize += empty.stackSize;
                    }
                }

            }

        }
    }

    private boolean canMergeStacks(ItemStack stack1, ItemStack stack2) {
        return (stack1 == null || stack2 == null) || (stack1.isItemEqual(stack2) && stack1.stackSize + stack2.stackSize <= stack1.getMaxStackSize());
    }

    public TankManager getTankManager() {
        return tankManager;
    }

    @Override
    protected ItemStack getCartItemBase() {
        return getCartType().getCartItem();
    }


    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        getTankManager().writeTanksToNBT(tag);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        getTankManager().readTanksFromNBT(tag);
    }

    @Override
    protected void openGui(EntityPlayer player) {
        player.openGui(ComputeryMod.mod, ModGuiHandler.GUI_LOCOMOTIVE_DIESEL, worldObj, getEntityId(), 0, 0);
    }


    @Override
    public String getWhistle() {
        return SoundHelper.SOUND_LOCOMOTIVE_ELECTRIC_WHISTLE;
    }

    @Override
    public LocomotiveRenderType getRenderType() {
        return ModRenderType.DIESEL;
    }

    @Override
    public ICartType getCartType() {
        return ModEnumCart.LOCO_DIESEL;
    }

    @Override
    protected IInventory getTicketInventory() {
        return invTicket;
    }

    @Override
    public int getSizeInventory() {
        return 7;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int var1) {
        return SLOTS;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return isItemValidForSlot(slot, stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return slot == SLOT_TICKET || slot == 1 || slot == 3;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        switch (slot) {
            case SLOT_TICKET:
                return ItemTicket.FILTER.matches(stack);
            case 0:
                return isGregtechFuel(stack);
            case 2:
                return isValidCoolant(stack);
            default:
                return false;
        }
    }

    @Override
    public boolean canPassFluidRequests(Fluid fluid) {
        return isValidCoolant(fluid) || isGregtechFuel(fluid);
    }

    @Override
    public boolean canAcceptPushedFluid(EntityMinecart entityMinecart, Fluid fluid) {
        return canPassFluidRequests(fluid);
    }

    @Override
    public boolean canProvidePulledFluid(EntityMinecart entityMinecart, Fluid fluid) {
        return false;
    }

    @Override
    public void setFilling(boolean filling) {}

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        if(isGregtechFuel(resource.getFluid())) {
            return fuelTank.fill(resource, doFill);
        }
        if(isValidCoolant(resource.getFluid())) {
            return coolantTank.fill(resource, doFill);
        }
        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return canPassFluidRequests(fluid);
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[] {coolantTank.getInfo(), fuelTank.getInfo()};
    }

    private static Fluid DISTILLED_WATER = FluidRegistry.getFluid("ic2distilledwater");
    private static Fluid HOLY_WATER = FluidRegistry.getFluid("holywater");
    private static Fluid COOLANT = FluidRegistry.getFluid("ic2coolant");
    private static Fluid NITROGEN = FluidRegistry.getFluid("nitrogen");

    public static boolean isValidCoolant(Fluid fluid) {
        return fluid == FluidRegistry.WATER || fluid == DISTILLED_WATER || fluid == COOLANT || fluid == HOLY_WATER;
    }

    public static float getCoolantModifier(Fluid fluid) {
        if(fluid == FluidRegistry.WATER) {
            return 0.85f;
        } else if(fluid == DISTILLED_WATER) {
            return 1.00f;
        } else if(fluid == COOLANT) {
            return 1.75f;
        } else if(fluid == NITROGEN) {
            return 2.95f;
        } else if(fluid == HOLY_WATER) {
            return 3.95f;
        }
        return 0.0f;
    }

    public static boolean isValidCoolant(ItemStack stack) {
        FluidStack fluid2 = GT_Utility.getFluidForFilledItem(stack, true);
        return fluid2 != null && isValidCoolant(fluid2.getFluid());
    }

    public static boolean isGregtechFuel(ItemStack stack) {
        FluidStack fluid2 = GT_Utility.getFluidForFilledItem(stack, true);
        if(fluid2 == null) {
            return false;
        }
        for (GT_Recipe recipe : GT_Recipe.GT_Recipe_Map.sDieselFuels.mRecipeList) {
            if(recipe.mInputs[0].isItemEqual(stack)) return true;
            FluidStack fluid1 = GT_Utility.getFluidForFilledItem(recipe.mInputs[0], true);
            if(GT_Utility.areFluidsEqual(fluid1, fluid2)) return true;
        }
        return false;
    }

    public static boolean isGregtechFuel(Fluid fluid) {
        for (GT_Recipe recipe : GT_Recipe.GT_Recipe_Map.sDieselFuels.mRecipeList) {
            FluidStack stack = GT_Utility.getFluidForFilledItem(recipe.mInputs[0], true);
            if (stack.getFluid() == fluid) {
                return true;
            }
        }
        return false;
    }

}
