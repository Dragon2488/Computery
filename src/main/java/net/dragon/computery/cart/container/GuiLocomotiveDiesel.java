package net.dragon.computery.cart.container;

import net.dragon.computery.cart.entity.EntityLocomotiveDiesel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiLocomotiveDiesel extends GuiLocomotive {

    private final EntityLocomotiveDiesel loco;
    private final EntityPlayer player;

    public GuiLocomotiveDiesel(InventoryPlayer inv, EntityLocomotiveDiesel loco) {
        super(inv, loco, new ContainerLocomotiveDiesel(inv, loco), "diesel", "gui_locomotive_diesel.png", 205, false);
        this.loco = loco;
        this.player = inv.player;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
        super.drawGuiContainerBackgroundLayer(par1, par3, par3);
    }

}
