package net.dragon.computery;

import cpw.mods.fml.common.network.IGuiHandler;
import net.dragon.computery.cart.container.ContainerLocomotiveDiesel;
import net.dragon.computery.cart.entity.EntityLocomotiveDiesel;
import net.dragon.computery.cart.container.GuiLocomotiveDiesel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class ModGuiHandler implements IGuiHandler {

    public static final int GUI_LOCOMOTIVE_DIESEL = 1;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID) {
            case GUI_LOCOMOTIVE_DIESEL:
                Entity locomotive = world.getEntityByID(x);
                if(locomotive == null) {
                    System.err.println("WTF???");
                    return null;
                }
                return new ContainerLocomotiveDiesel(player.inventory, (EntityLocomotiveDiesel) locomotive);
            default:
                return null;
        }
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID) {
            case GUI_LOCOMOTIVE_DIESEL:
                Entity locomotive = world.getEntityByID(x);
                if(locomotive == null) {
                    System.err.println("WTF???");
                    return null;
                }
                EntityLocomotiveDiesel loco = (EntityLocomotiveDiesel) locomotive;
                return new GuiLocomotiveDiesel(player.inventory, loco);
            default:
                return null;
        }
    }
}
