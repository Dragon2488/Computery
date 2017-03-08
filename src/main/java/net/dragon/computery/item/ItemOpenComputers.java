package net.dragon.computery.item;

import com.mojang.realmsclient.gui.ChatFormatting;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import li.cil.oc.CreativeTab$;
import li.cil.oc.api.API;
import li.cil.oc.api.driver.EnvironmentProvider;
import li.cil.oc.api.driver.item.HostAware;
import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.api.driver.item.UpgradeRenderer;
import li.cil.oc.api.event.RobotRenderEvent;
import li.cil.oc.api.internal.Robot;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import net.dragon.computery.component.LocomotiveCard;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import scala.Tuple2;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ItemOpenComputers extends Item implements HostAware, EnvironmentProvider, UpgradeRenderer {

    private IIcon[] icons = new IIcon[1];

    public ItemOpenComputers() {
        setCreativeTab(CreativeTab$.MODULE$);
        setMaxStackSize(4);
        setHasSubtypes(true);
        setUnlocalizedName("computery.opencomputers");
        GameRegistry.registerItem(this, "card");
        API.driver.add((li.cil.oc.api.driver.Item) this);
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        for(int i = 0; i < 1; i++) {
            list.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int metadata) {
        if(metadata < icons.length) {
            return icons[metadata];
        }
        return icons[0];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        for(int i = 0; i < icons.length; i++) {
            icons[i] = register.registerIcon("computery:opencomputers_" + i);
        }
    }

    @Override
    public void onCreated(ItemStack itemstack, World world, EntityPlayer player) {
        if(itemstack.getItemDamage() == 0) {
            UUID randomUUID = UUID.randomUUID();
            NBTTagCompound tagCompound = new NBTTagCompound();
            tagCompound.setLong("UUIDMost", randomUUID.getMostSignificantBits());
            tagCompound.setLong("UUIDLeast", randomUUID.getLeastSignificantBits());
            itemstack.setTagCompound(tagCompound);
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return getUnlocalizedName() + "_" + stack.getItemDamage();
    }

    public static UUID getLocomotiveCardId(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if(stack.getItemDamage() != 0 || tagCompound == null || tagCompound.getLong("UUIDLeast") == 0 || tagCompound.getLong("UUIDMost") == 0) {
            return null;
        }
        return new UUID(tagCompound.getLong("UUIDMost"), tagCompound.getLong("UUIDLeast"));
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        switch (stack.getItemDamage()) {
            case 0:
                tooltip.add("Allows communication with diesel-electric locomotives");
                tooltip.add("This technology is heavy based on Linkage Cards and works in similar way");
                tooltip.add("Crafted in 4 cards connected into one network");
                tooltip.add("You put 1 card in the computer and others in locomotives");
                tooltip.add("You can query attached locomotives state and change it");
                tooltip.add("Installed in a card slot");

                UUID uuid = getLocomotiveCardId(stack);
                if(uuid != null)
                    tooltip.add("Card ID: " + uuid.toString());
                break;
            default:
                tooltip.add(ChatFormatting.RED + "Invalid item");
                tooltip.add(ChatFormatting.RED + "If you obtained it legally, please report");
                break;
        }
    }


    @Override
    public ManagedEnvironment createEnvironment(ItemStack stack, EnvironmentHost host) {
        if(stack.getItem() == this && stack.stackSize == 1) {
            switch (stack.getItemDamage()) {
                case 0:
                    return new LocomotiveCard(stack, host);
                default:
                    return null;
            }
        }
        return null;
    }

    @Override
    public Class<?> getEnvironment(ItemStack stack) {
        if(stack.getItem() == this && stack.stackSize == 1) {
            switch (stack.getItemDamage()) {
                case 0:
                    return LocomotiveCard.class;
                default:
                    return null;
            }
        }
        return null;
    }

    @Override
    public boolean worksWith(ItemStack stack, Class<? extends EnvironmentHost> host) {
        return worksWith(stack);
    }

    @Override
    public boolean worksWith(ItemStack stack) {
        if(stack.getItem() == this && stack.stackSize == 1) {
            switch (stack.getItemDamage()) {
                case 0:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    @Override
    public String slot(ItemStack stack) {
        if(stack.getItem() == this && stack.stackSize == 1) {
            switch (stack.getItemDamage()) {
                case 0:
                    return Slot.Card;
                default:
                    return null;
            }
        }
        return null;
    }

    @Override
    public int tier(ItemStack stack) {
        if(stack.getItem() == this && stack.stackSize == 1) {
            switch (stack.getItemDamage()) {
                case 0:
                    return 2;
                default:
                    return -1;
            }
        }
        return -1;
    }

    @Override
    public NBTTagCompound dataTag(ItemStack stack) {
        if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        NBTTagCompound tag = stack.getTagCompound();
        if(!tag.hasKey("oc:data", 10))
            tag.setTag("oc:data", new NBTTagCompound());
        return tag.getCompoundTag("oc:data");
    }

    @Override
    public String computePreferredMountPoint(ItemStack stack, Robot robot, Set<String> availableMountPoints) {
        return availableMountPoints.size() > 0 ? availableMountPoints.iterator().next() : null;
    }

    @Override
    public void render(ItemStack stack, RobotRenderEvent.MountPoint mountPoint, Robot robot, float pt) {
        li.cil.oc.client.renderer.item.UpgradeRenderer.render(stack, mountPoint);
    }

}
