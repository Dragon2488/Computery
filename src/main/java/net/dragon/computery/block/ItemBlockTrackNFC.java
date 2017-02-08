package net.dragon.computery.block;

import net.dragon.computery.item.RecipeConfigurationCircuit;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemBlockTrackNFC extends ItemBlock {

    public ItemBlockTrackNFC(Block block) {
        super(block);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean showAdvancedInfo) {
        list.add(EnumChatFormatting.GRAY + "This track can trigger NFC-trackable targets which passed it.");
        list.add(EnumChatFormatting.GRAY + "NFC Variable == " + RecipeConfigurationCircuit.getConfiguration(stack));
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata) {
        if (!world.setBlock(x, y, z, field_150939_a, metadata, 3)) {
            return false;
        }

        if (world.getBlock(x, y, z) == field_150939_a) {
            field_150939_a.onBlockPlacedBy(world, x, y, z, player, stack);
            field_150939_a.onPostBlockPlaced(world, x, y, z, metadata);
            TileEntityTrackNFC tileEntity = new TileEntityTrackNFC();
            tileEntity.setNfcVariable(RecipeConfigurationCircuit.getConfiguration(stack));
            world.setTileEntity(x, y, z, tileEntity);
        }

        return true;
    }
}
