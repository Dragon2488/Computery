package net.dragon.computery.cart.container;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import mods.railcraft.common.fluids.TankManager;
import mods.railcraft.common.gui.slots.*;
import mods.railcraft.common.gui.tooltips.ToolTip;
import mods.railcraft.common.gui.widgets.FluidGaugeWidget;
import net.dragon.computery.cart.entity.EntityLocomotiveDiesel;
import net.dragon.computery.item.Objects;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class ContainerLocomotiveDiesel extends ContainerLocomotive {

    private final EntityLocomotiveDiesel loco;

    public ContainerLocomotiveDiesel(InventoryPlayer playerInv, EntityLocomotiveDiesel loco) {
        super(playerInv, loco, 205);
        this.loco = loco;
        init();
    }

    @Override
    public void defineSlotsAndWidgets() {
        addWidget(new FluidGaugeWidget(loco.getTankManager().get(1), 116, 23, 200, 0, 16, 47));
        addWidget(new FluidGaugeWidget(loco.getTankManager().get(0), 44, 23, 200, 0, 16, 47));

        //addWidget(new IndicatorWidget(loco.boiler.heatIndicator, 40, 25, 176, 61, 6, 43));

        SlotRailcraft locomotiveCardSlot = new SlotRailcraft(loco, 4, 178, 4) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() == Objects.ocComponents && stack.getItemDamage() == 0;
            }
        };
        locomotiveCardSlot.setToolTips(ToolTip.buildToolTip("railcraft.tooltip.locomotive_card"));

        //Locomotive Card
        addSlot(locomotiveCardSlot);

        //Fuel
        addSlot(new SlotRailcraft(loco, 0, 17, 21) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return isGregtechFuel(stack);
            }
        });
        addSlot(new SlotOutput(loco, 1, 17, 56));

        //Coolant
        addSlot(new SlotRailcraft(loco, 2, 143, 21) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                FluidStack fluid = GT_Utility.getFluidForFilledItem(stack, true);
                return fluid != null && EntityLocomotiveDiesel.isValidCoolant(fluid.getFluid());
            }
        });

        addSlot(new SlotOutput(loco, 3, 143, 56));

    }

    public static boolean isGregtechFuel(ItemStack stack) {
        FluidStack fluid2 = GT_Utility.getFluidForFilledItem(stack, true);
        for (GT_Recipe recipe : GT_Recipe.GT_Recipe_Map.sDieselFuels.mRecipeList) {
            if(recipe.mInputs[0].isItemEqual(stack)) return true;
            FluidStack fluid1 = GT_Utility.getFluidForFilledItem(recipe.mInputs[0], true);
            if(GT_Utility.areFluidsEqual(fluid1, fluid2)) return true;
        }
        return false;
    }

    @Override
    public void addCraftingToCrafters(ICrafting icrafting) {
        super.addCraftingToCrafters(icrafting);
        TankManager tMan = loco.getTankManager();
        if (tMan != null) {
            tMan.initGuiData(this, icrafting, 0);
            tMan.initGuiData(this, icrafting, 1);
        }
    }

    @Override
    public void sendUpdateToClient() {
        super.sendUpdateToClient();
        TankManager tMan = loco.getTankManager();
        if (tMan != null) {
            tMan.updateGuiData(this, crafters, 0);
            tMan.updateGuiData(this, crafters, 1);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int value) {
        super.updateProgressBar(id, value);
        TankManager tMan = loco.getTankManager();
        if (tMan != null)
            tMan.processGuiUpdate(id, value);
    }

}