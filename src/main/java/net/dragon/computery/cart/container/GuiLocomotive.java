package net.dragon.computery.cart.container;

import mods.railcraft.client.gui.EntityGui;
import mods.railcraft.client.gui.GuiTools;
import mods.railcraft.client.gui.buttons.GuiBetterButton;
import mods.railcraft.client.gui.buttons.GuiMultiButton;
import mods.railcraft.client.gui.buttons.GuiToggleButtonSmall;
import mods.railcraft.common.carts.EntityLocomotive;
import mods.railcraft.common.core.RailcraftConstants;
import mods.railcraft.common.gui.tooltips.ToolTip;
import mods.railcraft.common.plugins.forge.LocalizationPlugin;
import mods.railcraft.common.util.network.PacketBuilder;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;

import java.util.ArrayList;
import java.util.List;

public abstract class GuiLocomotive extends EntityGui {

    private final EntityLocomotive loco;
    private final EntityPlayer player;
    private final boolean hasIdleMode;
    private final String typeTag;
    private GuiToggleButtonSmall running;
    private GuiToggleButtonSmall idle;
    private GuiToggleButtonSmall shutdown;
    private GuiToggleButtonSmall reverse;
    private GuiToggleButtonSmall slowest;
    private GuiToggleButtonSmall slower;
    private GuiToggleButtonSmall slow;
    private GuiToggleButtonSmall max;
    private GuiMultiButton lockButton;
    private ToolTip lockedToolTips;
    private ToolTip unlockedToolTips;
    private ToolTip privateToolTips;
    private String locoOwner;

    public GuiLocomotive(InventoryPlayer inv, EntityLocomotive loco, ContainerLocomotive container, String typeTag, String guiName, int guiHeight, boolean hasIdleMode) {
        super(loco, container, RailcraftConstants.GUI_TEXTURE_FOLDER + guiName);
        ySize = guiHeight;
        xSize = 200;
        this.loco = loco;
        this.player = inv.player;
        this.typeTag = typeTag;
        this.hasIdleMode = hasIdleMode;
        loco.clientMode = loco.getMode();
        loco.clientSpeed = loco.getSpeed();
        lockedToolTips = ToolTip.buildToolTip("railcraft.gui.locomotive.tip.button.locked", "{owner}=[Unknown]");
        unlockedToolTips = ToolTip.buildToolTip("railcraft.gui.locomotive.tip.button.unlocked", "{owner}=[Unknown]");
        privateToolTips = ToolTip.buildToolTip("railcraft.gui.locomotive.tip.button.private", "{owner}=[Unknown]");
    }

    @Override
    public void initGui() {
        super.initGui();
        if (loco == null)
            return;
        buttonList.clear();
        int w = (width - xSize) / 2;
        int h = (height - ySize) / 2;

        List<GuiBetterButton> buttons = new ArrayList<GuiBetterButton>();
        running = new GuiToggleButtonSmall(0, 0, h + ySize - 129, 55, LocalizationPlugin.translate("railcraft.gui.locomotive.mode.running"), loco.clientMode == EntityLocomotive.LocoMode.RUNNING);
        buttons.add(running);
        idle = new GuiToggleButtonSmall(1, 0, h + ySize - 129, 45, LocalizationPlugin.translate("railcraft.gui.locomotive.mode.idle"), loco.clientMode == EntityLocomotive.LocoMode.IDLE);
        if (hasIdleMode)
            buttons.add(idle);
        shutdown = new GuiToggleButtonSmall(2, 0, h + ySize - 129, 55, LocalizationPlugin.translate("railcraft.gui.locomotive.mode.shutdown"), loco.clientMode == EntityLocomotive.LocoMode.SHUTDOWN);
        buttons.add(shutdown);

        running.setToolTip(ToolTip.buildToolTip("railcraft.gui.locomotive." + typeTag + ".tip.button.mode.running"));
        idle.setToolTip(ToolTip.buildToolTip("railcraft.gui.locomotive." + typeTag + ".tip.button.mode.idle"));
        shutdown.setToolTip(ToolTip.buildToolTip("railcraft.gui.locomotive." + typeTag + ".tip.button.mode.shutdown"));

        GuiTools.newButtonRowAuto(buttonList, w + 3, 171, buttons);

        List<GuiBetterButton> speedButtons = new ArrayList<>();
        reverse = new GuiToggleButtonSmall(3, 0, h + ySize - 112, 12, "<", loco.clientSpeed == EntityLocomotive.LocoSpeed.REVERSE);
        speedButtons.add(reverse);
        slowest = new GuiToggleButtonSmall(4, 0, h + ySize - 112, 12, ">", loco.clientSpeed == EntityLocomotive.LocoSpeed.SLOWEST);
        speedButtons.add(slowest);
        slower = new GuiToggleButtonSmall(5, 0, h + ySize - 112, 17, ">>", loco.clientSpeed == EntityLocomotive.LocoSpeed.SLOWER);
        speedButtons.add(slower);
        slow = new GuiToggleButtonSmall(6, 0, h + ySize - 112, 22, ">>>", loco.clientSpeed == EntityLocomotive.LocoSpeed.SLOW);
        speedButtons.add(slow);
        max = new GuiToggleButtonSmall(7, 0, h + ySize - 112, 27, ">>>>", loco.clientSpeed == EntityLocomotive.LocoSpeed.MAX);
        speedButtons.add(max);

        GuiTools.newButtonRow(buttonList, w + 8, 3, speedButtons);

        buttonList.add(lockButton = new GuiMultiButton(8, w + 152, h + ySize - 111, 16, loco.getLockController()));
        lockButton.enabled = loco.clientCanLock;
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (loco == null)
            return;
        switch (guibutton.id) {
            case 0:
                loco.clientMode = EntityLocomotive.LocoMode.RUNNING;
                break;
            case 1:
                loco.clientMode = EntityLocomotive.LocoMode.IDLE;
                break;
            case 2:
                loco.clientMode = EntityLocomotive.LocoMode.SHUTDOWN;
                break;
            case 3:
                loco.clientSpeed = EntityLocomotive.LocoSpeed.REVERSE;
                break;
            case 4:
                loco.clientSpeed = EntityLocomotive.LocoSpeed.SLOWEST;
                break;
            case 5:
                loco.clientSpeed = EntityLocomotive.LocoSpeed.SLOWER;
                break;
            case 6:
                loco.clientSpeed = EntityLocomotive.LocoSpeed.SLOW;
                break;
            case 7:
                loco.clientSpeed = EntityLocomotive.LocoSpeed.MAX;
                break;
        }
        updateButtons();
        sendUpdatePacket();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        updateButtons();
    }

    private void updateButtons() {
        lockButton.enabled = loco.clientCanLock;
        lockButton.setToolTip(loco.isPrivate() ? privateToolTips : loco.isSecure() ? lockedToolTips : lockButton.enabled ? unlockedToolTips : null);
        String ownerName = ((ContainerLocomotive) container).ownerName;
        if (ownerName != null && !ownerName.equals(locoOwner)) {
            locoOwner = ownerName;
            lockedToolTips = ToolTip.buildToolTip("railcraft.gui.locomotive.tip.button.locked", "{owner}=" + ownerName);
            unlockedToolTips = ToolTip.buildToolTip("railcraft.gui.locomotive.tip.button.unlocked", "{owner}=" + ownerName);
            privateToolTips = ToolTip.buildToolTip("railcraft.gui.locomotive.tip.button.private", "{owner}=" + ownerName);
        }
        running.active = loco.clientMode == EntityLocomotive.LocoMode.RUNNING;
        idle.active = loco.clientMode == EntityLocomotive.LocoMode.IDLE;
        shutdown.active = loco.clientMode == EntityLocomotive.LocoMode.SHUTDOWN;
        reverse.active = loco.clientSpeed == EntityLocomotive.LocoSpeed.REVERSE;
        slowest.active = loco.clientSpeed == EntityLocomotive.LocoSpeed.SLOWEST;
        slower.active = loco.clientSpeed == EntityLocomotive.LocoSpeed.SLOWER;
        slow.active = loco.clientSpeed == EntityLocomotive.LocoSpeed.SLOW;
        max.active = loco.clientSpeed == EntityLocomotive.LocoSpeed.MAX;
    }

    @Override
    public void onGuiClosed() {
        sendUpdatePacket();
    }

    private void sendUpdatePacket() {
        PacketBuilder.instance().sendGuiReturnPacket(loco);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String name = loco.getCommandSenderName();
        GuiTools.drawCenteredString(fontRendererObj, name, 6);
        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
    }

}