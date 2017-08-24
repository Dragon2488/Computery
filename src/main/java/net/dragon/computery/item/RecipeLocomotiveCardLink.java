package net.dragon.computery.item;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public class RecipeLocomotiveCardLink implements IRecipe {

    @Override
    public boolean matches(InventoryCrafting crafting, World world) {
        boolean foundAnything = false;
        for(int i = 0; i < getRecipeSize(); i++) {
            ItemStack stack = crafting.getStackInSlot(i);
            if(stack != null && (stack.getItem() != Objects.ocComponents || stack.getItemDamage() != 0)) {
                return false;
            } else if(stack != null) {
                foundAnything = true;
            }
        }
        return foundAnything;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting crafting) {
        ItemStack firstCard = null;
        for(int i = 0; i < getRecipeSize(); i++) {
            ItemStack stack = crafting.getStackInSlot(i);
            if(stack != null) {
                if(firstCard == null) {
                    firstCard = stack.copy();
                    firstCard.stackSize = 1;
                } else firstCard.stackSize++;
            }
        }
        System.out.println(firstCard);
        return firstCard;
    }

    @Override
    public int getRecipeSize() {
        return 9;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(Objects.ocComponents, 0, 0);
    }

}
