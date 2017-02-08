package net.dragon.computery.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityTrackNFC extends TileEntity {

    private int nfcVariable = 0;

    public int getNfcVariable() {
        return nfcVariable;
    }

    public void setNfcVariable(int nfcVariable) {
        this.nfcVariable = nfcVariable;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);
        this.nfcVariable = nbtTagCompound.getInteger("nfcVariable");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);
        nbtTagCompound.setInteger("nfcVariable", this.nfcVariable);
    }
}
