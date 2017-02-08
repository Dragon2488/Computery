package net.dragon.computery.cart.container;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.railcraft.common.carts.EntityLocomotive;
import mods.railcraft.common.gui.containers.RailcraftContainer;
import mods.railcraft.common.gui.slots.SlotRailcraft;
import mods.railcraft.common.gui.slots.SlotStackFilter;
import mods.railcraft.common.gui.slots.SlotUntouchable;
import mods.railcraft.common.gui.tooltips.ToolTip;
import mods.railcraft.common.items.ItemTicket;
import mods.railcraft.common.plugins.forge.PlayerPlugin;
import mods.railcraft.common.util.network.PacketBuilder;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;

public class ContainerLocomotive extends RailcraftContainer {

    private final EntityLocomotive loco;
    protected final InventoryPlayer playerInv;
    private EntityLocomotive.LocoSpeed lastSpeed;
    private EntityLocomotive.LocoMode lastMode;
    private int lastLockState;
    private final int guiHeight;
    public String ownerName;

    public ContainerLocomotive(InventoryPlayer playerInv, EntityLocomotive loco, int guiHeight) {
        super(loco);
        this.loco = loco;
        this.playerInv = playerInv;
        this.guiHeight = guiHeight;
    }

    public static ContainerLocomotive make(InventoryPlayer playerInv, EntityLocomotive loco) {
        ContainerLocomotive con = new ContainerLocomotive(playerInv, loco, 161);
        con.init();
        return con;
    }

    public final void init() {
        defineSlotsAndWidgets();

        SlotRailcraft slotTicket = new SlotStackFilter(ItemTicket.FILTER, loco, loco.getSizeInventory() - 2, 116, guiHeight - 111) {
            @Override
            public int getSlotStackLimit() {
                return 1;
            }
        };
        slotTicket.setToolTips(ToolTip.buildToolTip("gui.locomotive.tip.slot.ticket"));
        addSlot(slotTicket);
        addSlot(new SlotUntouchable(loco, loco.getSizeInventory() - 1, 134, guiHeight - 111));

        for (int i = 0; i < 3; i++) {
            for (int k = 0; k < 9; k++) {
                addSlot(new Slot(playerInv, k + i * 9 + 9, 8 + k * 18, guiHeight - 82 + i * 18));
            }
        }

        for (int j = 0; j < 9; j++) {
            addSlot(new Slot(playerInv, j, 8 + j * 18, guiHeight - 24));
        }
    }

    public void defineSlotsAndWidgets() {

    }

    @Override
    public void addCraftingToCrafters(ICrafting icrafting) {
        super.addCraftingToCrafters(icrafting);

        icrafting.sendProgressBarUpdate(this, 10, loco.getSpeed().ordinal());
        icrafting.sendProgressBarUpdate(this, 11, loco.getMode().ordinal());
        icrafting.sendProgressBarUpdate(this, 12, loco.getLockController().getCurrentState());
        icrafting.sendProgressBarUpdate(this, 13, PlayerPlugin.isOwnerOrOp(loco.getOwner(), playerInv.player) ? 1 : 0);

        String oName = loco.getOwner().getName();
        if (oName != null)
            PacketBuilder.instance().sendGuiStringPacket((EntityPlayerMP) icrafting, windowId, 0, oName);
    }

    @Override
    public void sendUpdateToClient() {
        super.sendUpdateToClient();

        for (Object crafter : crafters) {
            ICrafting var2 = (ICrafting) crafter;

            EntityLocomotive.LocoSpeed speed = loco.getSpeed();
            if (lastSpeed != speed)
                var2.sendProgressBarUpdate(this, 10, speed.ordinal());

            EntityLocomotive.LocoMode mode = loco.getMode();
            if (lastMode != mode)
                var2.sendProgressBarUpdate(this, 11, mode.ordinal());

            int lock = loco.getLockController().getCurrentState();
            if (lastLockState != lock)
                var2.sendProgressBarUpdate(this, 12, lock);
        }

        this.lastSpeed = loco.getSpeed();
        this.lastMode = loco.getMode();
        this.lastLockState = loco.getLockController().getCurrentState();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int value) {
        switch (id) {
            case 10:
                loco.clientSpeed = EntityLocomotive.LocoSpeed.VALUES[value];
                break;
            case 11:
                loco.clientMode = EntityLocomotive.LocoMode.VALUES[value];
                break;
            case 12:
                loco.getLockController().setCurrentState(value);
                break;
            case 13:
                loco.clientCanLock = value == 1;
                break;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateString(byte id, String data) {
        switch (id) {
            case 0:
                ownerName = data;
                break;
        }
    }

}