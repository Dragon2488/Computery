package net.dragon.computery.item;

import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ShapedNFCRecipe extends ShapedOreRecipe {

    public ShapedNFCRecipe(Block result, Object... recipe) {
        super(result, recipe);
    }

    public ShapedNFCRecipe(Item result, Object... recipe) {
        super(result, recipe);
    }

    public ShapedNFCRecipe(ItemStack result, Object... recipe) {
        super(result, recipe);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting crafting) {
        ItemStack itemStack = super.getCraftingResult(crafting);
        int configuration = RecipeConfigurationCircuit.getConfiguration(crafting.getStackInSlot(4));
        RecipeConfigurationCircuit.setConfiguration(itemStack, (short) configuration);
        return itemStack;
    }
}
