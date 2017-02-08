package net.dragon.computery.cart;

import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import mods.railcraft.common.carts.*;
import mods.railcraft.common.util.misc.EnumColor;
import mods.railcraft.common.util.misc.Game;
import mods.railcraft.common.util.misc.MiscTools;
import net.dragon.computery.ComputeryMod;
import net.dragon.computery.cart.entity.EntityLocomotiveDiesel;
import net.dragon.computery.cart.render.ModRenderType;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.lang.reflect.Constructor;

public enum ModEnumCart implements ICartType {

    LOCO_DIESEL(100, EntityLocomotiveDiesel.class);

    private ItemStack cartItem;
    private final byte entityId;
    private final Class<? extends EntityMinecart> cartClass;

    ModEnumCart(int entityId, Class<? extends EntityMinecart> cartClass) {
        this.entityId = (byte) entityId;
        this.cartClass = cartClass;
    }

    @Override
    public byte getId() {
        return entityId;
    }

    @Override
    public String getTag() {
        return "railcraft.cart." + name().toLowerCase();
    }

    @Override
    public Class<? extends EntityMinecart> getCartClass() {
        return cartClass;
    }

    private Item defineItem() {
        switch (this) {
            case LOCO_DIESEL:
                return new ItemLocomotive(this, ModRenderType.DIESEL, EnumColor.WHITE, EnumColor.BLACK);
            default:
                return new ItemCart(this);
        }
    }

    public void setup() {
        Item cart = defineItem();
        cart.setUnlocalizedName(getTag());
        GameRegistry.registerItem(cart, getTag());
        cartItem = new ItemStack(cart);
        EntityRegistry.registerModEntity(getCartClass(), MiscTools.cleanTag(getTag()), getId(), ComputeryMod.mod, 256, 3, true);
    }

    @Override
    public ItemStack getContents() {
        return null;
    }

    @Override
    public EntityMinecart makeCart(ItemStack stack, World world, double i, double j, double k) {
        try {
            Constructor<? extends EntityMinecart> con = getCartClass().getConstructor(World.class, double.class, double.class, double.class);
            EntityMinecart entity = con.newInstance(world, i, j, k);
            if (entity instanceof IRailcraftCart)
                ((IRailcraftCart) entity).initEntityFromItem(stack);
            return entity;
        } catch (Throwable ex) {
            Game.logThrowable("Failed to create cart entity!", ex);
        }
        return new EntityCartBasic(world, i, j, k);
    }

    @Override
    public ItemStack getCartItem() {
        return cartItem;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
