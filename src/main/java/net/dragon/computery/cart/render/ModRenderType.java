package net.dragon.computery.cart.render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.railcraft.api.carts.locomotive.LocomotiveRenderType;
import net.minecraftforge.common.util.EnumHelper;

public class ModRenderType {

    public static LocomotiveRenderType DIESEL = makeRenderType("DIESEL", "cart.loco.diesel");


    public static LocomotiveRenderType makeRenderType(String name, String tag) {
        return EnumHelper.addEnum(LocomotiveRenderType.class, name,
                new Class[] {String.class},
                new Object[] {tag});
    }

    @SideOnly(Side.CLIENT)
    public static void attachRenderers() {
        DIESEL.registerRenderer(new LocomotiveRendererDiesel("default", "diesel.default"));
        DIESEL.registerRenderer(new LocomotiveRendererDiesel("heart", "diesel.heart"));
    }

}
