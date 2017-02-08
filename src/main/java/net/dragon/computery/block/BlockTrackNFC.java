package net.dragon.computery.block;

import cpw.mods.fml.common.registry.GameRegistry;
import net.dragon.computery.item.RecipeConfigurationCircuit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.ArrayList;

public class BlockTrackNFC extends BlockRailBase implements ITileEntityProvider {

    public BlockTrackNFC() {
        super(false);
        setBlockName("computery.nfc_track");
        setBlockTextureName("computery:nfc_track");

        GameRegistry.registerBlock(this, ItemBlockTrackNFC.class, "nfc_track");
        GameRegistry.registerTileEntity(TileEntityTrackNFC.class, "nfc_track");
    }

    @Override
    public void onMinecartPass(World world, EntityMinecart cart, int x, int y, int z) {
        if(!world.isRemote && cart instanceof INFCTrackable) {
            TileEntity rail = world.getTileEntity(x, y, z);
            if(rail != null && rail instanceof TileEntityTrackNFC) {
                ((INFCTrackable) cart).onNFCTrackPassed(((TileEntityTrackNFC) rail).getNfcVariable());
            }
        }
    }

    @Override
    public int getMobilityFlag() {
        return 2;
    }

    private ThreadLocal<TileEntityTrackNFC> cache = new ThreadLocal<>();

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if(tileEntity instanceof TileEntityTrackNFC) {
            world.removeTileEntity(x, y, z);
            cache.set((TileEntityTrackNFC) tileEntity);
        }
        return super.removedByPlayer(world, player, x, y, z, willHarvest);
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        ArrayList<ItemStack> drops = new ArrayList<>();
        if(cache.get() != null) {
            ItemStack itemStack = new ItemStack(this, 1, 0);
            RecipeConfigurationCircuit.setConfiguration(itemStack, (short) cache.get().getNfcVariable());
            drops.add(itemStack);
        } else {
            TileEntity tileEntity = world.getTileEntity(x, y, z);
            if(tileEntity instanceof TileEntityTrackNFC) {
                ItemStack itemStack = new ItemStack(this, 1, 0);
                RecipeConfigurationCircuit.setConfiguration(itemStack, (short) ((TileEntityTrackNFC) tileEntity).getNfcVariable());
                drops.add(itemStack);
            }
        }
        return drops;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityTrackNFC();
    }

}
