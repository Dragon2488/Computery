package net.dragon.computery.item;

import gregtech.api.GregTech_API;
import gregtech.api.objects.GT_ItemStack;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import scala.actors.threadpool.Arrays;

import java.util.List;

public class RecipeConfigurationCircuit implements IRecipe {
    @Override
    public boolean matches(InventoryCrafting inventory, World world) {
        ItemStack center = inventory.getStackInSlot(4);
        if(center != null && center.getItem() == Objects.metaItem && center.getItemDamage() == 3) {
            int conf = getConfiguration(center);
            if(isScrewdriver(inventory, 1) && checkFree(inventory, 1, 4) && conf < 32767) {
                return true;
            }
            if(isScrewdriver(inventory, 7) && checkFree(inventory, 7, 4) && conf > 0) {
                return true;
            }
            if(isScrewdriver(inventory, 5) && checkFree(inventory, 5, 4) && conf + 100 <= 32767) {
                return true;
            }
            if(isScrewdriver(inventory, 3) && checkFree(inventory, 3, 4) && conf - 100 >= 0) {
                return true;
            }
            return false;
        }
        return false;
    }

    private boolean isScrewdriver(InventoryCrafting crafting, int slot) {
        ItemStack stack = crafting.getStackInSlot(slot);
        return stack != null && GregTech_API.sScrewdriverList.contains(new GT_ItemStack(stack.getItem(), 1, stack.getItemDamage()));
    }

    private boolean checkFree(InventoryCrafting crafting, Integer... excludes) {
        List<Integer> excludes1 = Arrays.asList(excludes);
        for(int i = 0; i < 9; i++) {
            if(!excludes1.contains(i) && crafting.getStackInSlot(i) != null)
                return false;
        }
        return true;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventory) {
        ItemStack center = inventory.getStackInSlot(4).copy();
        int configuration = getConfiguration(center);
        if (isScrewdriver(inventory, 1) && checkFree(inventory, 1, 4)) {
            setConfiguration(center, (short) (configuration + 1));
            return center;
        }
        if (isScrewdriver(inventory, 7) && checkFree(inventory, 7, 4)) {
            setConfiguration(center, (short) (configuration - 1));
            return center;
        }
        if (isScrewdriver(inventory, 5) && checkFree(inventory, 5, 4)) {
            setConfiguration(center, (short) (configuration + 100));
            return center;
        }
        if (isScrewdriver(inventory, 3) && checkFree(inventory, 3, 4)) {
            setConfiguration(center, (short) (configuration - 100));
            return center;
        }
        return center;
    }

    public static int getConfiguration(ItemStack aStack) {
        return aStack.hasTagCompound() ? aStack.getTagCompound().getShort("configuration") : 0;
    }

    public static void setConfiguration(ItemStack aStack, short configuration) {
        if(!aStack.hasTagCompound())
            aStack.setTagCompound(new NBTTagCompound());
        aStack.getTagCompound().setShort("configuration", configuration);
    }

    @Override
    public int getRecipeSize() {
        return 9;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(Objects.metaItem, 1, 3);
    }
}
