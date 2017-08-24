package net.dragon.computery.component;


import com.mojang.authlib.GameProfile;
import li.cil.oc.Settings;
import li.cil.oc.api.API;
import li.cil.oc.api.component.RackMountable;
import li.cil.oc.api.driver.DeviceInfo;
import li.cil.oc.api.internal.Rack;
import li.cil.oc.api.machine.*;
import li.cil.oc.api.network.ComponentConnector;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.ManagedEnvironment;
import li.cil.oc.common.inventory.ComponentInventory;
import li.cil.repack.org.luaj.vm2.LuaError;
import mods.railcraft.common.carts.EntityLocomotive;
import mods.railcraft.common.plugins.forge.LocalizationPlugin;
import net.dragon.computery.cart.entity.EntityLocomotiveDiesel;
import net.dragon.computery.item.ItemOpenComputers;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import scala.Option;

import java.util.*;

public class LocomotiveCard extends ManagedEnvironment implements DeviceInfo {

    private static HashMap<UUID, WeakHashMap<String, LocomotiveCard>> connectedNodes = new HashMap<>();

    private final ComponentConnector node;
    private final EnvironmentHost environmentHost;
    private MachineHost host;
    private final ItemStack cardStack;

    public LocomotiveCard(ItemStack cardStack, EnvironmentHost host) {
        this.node = API.network.newNode(this, Visibility.Network)
                .withComponent("locomotive", Visibility.Neighbors)
                .withConnector()
                .create();
        this.environmentHost = host;
        this.cardStack = cardStack;
        setNode(node);
    }

    private MachineHost determineTrueHost() {
        try {
            if(environmentHost instanceof MachineHost) {
                return (MachineHost) environmentHost;
            } else if(environmentHost instanceof Rack) {
                Rack rack = (Rack) environmentHost;
                for(int rackIndex = 0; rackIndex < rack.getSizeInventory(); rackIndex++) {
                    RackMountable mountable = rack.getMountable(rackIndex);
                    if(mountable instanceof MachineHost && mountable instanceof ComponentInventory) {
                        ComponentInventory inventory = (ComponentInventory) mountable;
                        Option<li.cil.oc.api.network.ManagedEnvironment>[] components = inventory.components();
                        for(Option<li.cil.oc.api.network.ManagedEnvironment> environment : components) {
                            if(environment.isDefined() && environment.get() == this) {
                                return (MachineHost) mountable; //true host
                            }
                        }
                    }
                }
            }
            throw new IllegalArgumentException("Cannot determine true host of " + environmentHost);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onConnect(Node node) {
        if(this.node == node) {
            this.host = determineTrueHost();
            UUID myCardId = ItemOpenComputers.getLocomotiveCardId(cardStack);
            WeakHashMap<String, LocomotiveCard> connected = connectedNodes.get(myCardId);
            if(connected == null) {
                connected = new WeakHashMap<>();
                connectedNodes.put(myCardId, connected);
            }
            if(connected.containsKey(this.node.address())) {
                connected.remove(this.node.address());
            }
            connected.put(this.node.address(), this);
        }
    }

    @Override
    public void onDisconnect(Node node) {
       if(this.node == node) {
           UUID myCardId = ItemOpenComputers.getLocomotiveCardId(cardStack);
           WeakHashMap<String, LocomotiveCard> connected = connectedNodes.get(myCardId);
           if(connected == null) {
               connected = new WeakHashMap<>();
               connectedNodes.put(myCardId, connected);
           }
           if(connected.containsKey(this.node.address())) {
               connected.remove(this.node.address());
           }
       }
    }

    @Callback(doc = "getSpeed(id): string -- get locomotive move speed (max, slow, slower, slowest, reverse)")
    public Object[] getSpeed(Context context, Arguments arguments) throws LuaError {
        return array(request(arguments.checkString(0)).getSpeed().name().toLowerCase());
    }

    @Callback(doc = "getMode(id): string -- get locomotive mode (running, idle, shutdown)")
    public Object[] getMode(Context context, Arguments arguments) throws LuaError {
        return array(request(arguments.checkString(0)).getMode().name().toLowerCase());
    }

    @Callback(doc = "getSecurity(id): string -- get locomotive lock status (unlocked, locked, private)")
    public Object[] getSecurity(Context context, Arguments arguments) throws LuaError {
        return array(request(arguments.checkString(0)).getSecurityState().name().toLowerCase());
    }

    @Callback(doc = "getId(id): number -- get internal locomotive id")
    public Object[] getId(Context context, Arguments arguments) throws LuaError {
        return array(request(arguments.checkString(0)).getCartType().getId());
    }

    @Callback(doc = "getTag(id): string -- get internal locomotive name")
    public Object[] getTag(Context context, Arguments arguments) throws LuaError {
        return array(request(arguments.checkString(0)).getCartType().getTag());
    }

    @Callback(doc = "getName(id): string -- get locomotive translated name")
    public Object[] getName(Context context, Arguments arguments) throws LuaError {
        return array(request(arguments.checkString(0)).getName());
    }

    @Callback(doc = "getModelTag(id): string -- get locomotive model name")
    public Object[] getModelTag(Context context, Arguments arguments) throws LuaError {
        return array(request(arguments.checkString(0)).getModel());
    }

    @Callback(doc = "getModel(id): string -- get localized locomotive model name")
    public Object[] getModel(Context context, Arguments arguments) throws LuaError {
        return array(LocalizationPlugin.translate("railcraft." + request(arguments.checkString(0)).getCartType().getTag() + ".name"));
    }

    @Callback(doc = "getDestination(id): string -- get locomotive destination or null")
    public Object[] getDestination(Context context, Arguments arguments) throws LuaError {
        return array(request(arguments.checkString(0)).getDestination());
    }

    @Callback(doc = "getOwner(id): string -- get locomotive owner or null")
    public Object[] getOwner(Context context, Arguments arguments) throws LuaError {
        GameProfile owner = request(arguments.checkString(0)).getOwner();
        if(owner == null) return null;
        return array(owner.getId(), owner.getName());
    }


    @Callback(doc = "getOrientation(): string -- get locomotive orientation")
    public Object[] getOrientation(Context context, Arguments arguments) throws LuaError {
        return array(request(arguments.checkString(0)).rotationYaw);
    }

    @Callback(doc = "setSpeed(id, speed): boolean -- set locomotive move speed (max, slow, slower, slowest, reverse)")
    public Object[] setSpeed(Context context, Arguments arguments) throws LuaError, IllegalArgumentException {
        try {
            EntityLocomotive.LocoSpeed speed = EntityLocomotive.LocoSpeed.valueOf(arguments.checkString(1).toUpperCase());
            request(arguments.checkString(0)).setSpeed(speed);
            return array(true);
        } catch (IllegalArgumentException invalidSpeed) {
            throw new LuaError("Invalid speed specified!");
        }
    }

    @Callback(doc = "setMode(id, mode): boolean -- set locomotive mode (running, idle, shutdown)")
    public Object[] setMode(Context context, Arguments arguments) throws LuaError, IllegalArgumentException {
        try {
            EntityLocomotive.LocoMode speed = EntityLocomotive.LocoMode.valueOf(arguments.checkString(1).toUpperCase());
            request(arguments.checkString(0)).setMode(speed);
            return array(true);
        } catch (IllegalArgumentException invalidSpeed) {
            throw new LuaError("Invalid mode specified!");
        }
    }


    @Callback(doc = "setDestination(id, destination): boolean -- set locomotive destination")
    public Object[] setDestination(Context context, Arguments arguments) throws LuaError {
        EntityLocomotive loco = request(arguments.checkString(0));
        ItemStack ticket = loco.getDestItem().copy();
        if(ticket == null || !ticket.hasTagCompound()) return array(false);
        ticket.getTagCompound().setString("dest", arguments.checkString(1));
        return array(loco.setDestination(ticket));
    }

    @Callback(doc = "setOrientation(id, orientation): boolean -- set locomotive orientation (0-360)")
    public Object[] setOrientation(Context context, Arguments arguments) throws LuaError, IllegalArgumentException {
        float yaw = (float) arguments.checkDouble(1);
        if(yaw < 0 || yaw > 360)
            throw new IllegalArgumentException("Invalid orientation!");
        request(arguments.checkString(0)).rotationYaw = yaw;
        return array(true);
    }

    @Callback(doc = "whistle(id): boolean -- choo-choo!")
    public Object[] whistle(Context context, Arguments arguments) throws LuaError, IllegalArgumentException {
        request(arguments.checkString(0)).whistle();
        return array(true);
    }

    @Callback(doc = "getPosition(id): number, number, number -- get locomotive position in the world. Warning: IT'S CHEAT!")
    public Object[] getPosition(Context context, Arguments arguments) throws LuaError {
        EntityLocomotive loco = request(arguments.checkString(0));
        return array(loco.posX, loco.posY, loco.posZ);
    }

    @Callback(doc = "getLightColor(id): integer -- get locomotive light color (0xRRGGBB)")
    public Object[] getLightColor(Context context, Arguments arguments) throws LuaError {
        return array(request(arguments.checkString(0)).getDataWatcher().getWatchableObjectInt(31));
    }

    @Callback(doc = "setLightColor(id, color): boolean -- set locomotive light color (RGBInt)")
    public Object[] setLightColor(Context context, Arguments arguments) throws LuaError {
        int color = arguments.checkInteger(1);
        if(color < 0x000000 || color > 0xFFFFFF) {
            throw new LuaError("Color is out of bounds");
        }
        request(arguments.checkString(0)).getDataWatcher().updateObject(31, new Integer(color));
        return array(true);
    }

    @Callback(doc = "getPrimaryColor(id): string -- get locomotive primary color (dye color)")
    public Object[] getPrimaryColor(Context context, Arguments arguments) throws LuaError {
        return array(request(arguments.checkString(0)).getPrimaryColor());
    }

    @Callback(doc = "getSecondaryColor(id): string -- get locomotive secondary color (dye color)")
    public Object[] getSecondaryColor(Context context, Arguments arguments) throws LuaError {
        return array(request(arguments.checkString(0)).getSecondaryColor());
    }

    @Callback(doc = "getAttachedList(): string[] -- get list of all available locomotives")
    public Object[] getAttachedList(Context context, Arguments arguments) {
        List<String> attached = new ArrayList<>();
        System.out.println(host.getClass());
        UUID myCardId = ItemOpenComputers.getLocomotiveCardId(cardStack);
        for (Entity entity : (List<Entity>) host.world().loadedEntityList) {
            if (entity instanceof EntityLocomotiveDiesel) {
                UUID cardId = ((EntityLocomotiveDiesel) entity).getLocomotiveCardId();
                if (myCardId.equals(cardId)) {
                    attached.add(entity.getUniqueID().toString());
                }
            }
        }
        return attached.toArray();
    }

    private Object[] array(Object... array) {
        return array;
    }

    private EntityLocomotive request(String id) throws LuaError {
        EntityLocomotive locomotive = getLocomotive(id);
        if(locomotive != null) {
            double distance = locomotive.getDistanceSq(host.xPosition(), host.yPosition(), host.zPosition());
            if(node.tryChangeBuffer(-(distance / (Settings.get().maxWirelessRange() * Settings.get().wirelessCostPerRange()) * 1.5))) {
                return locomotive;
            } else throw new LuaError("not enough energy in network");
        } else throw new LuaError("locomotive not found");
    }

    private EntityLocomotiveDiesel getLocomotive(String id) throws LuaError {
        UUID myCardId = ItemOpenComputers.getLocomotiveCardId(cardStack);
        for(Entity entity : (List<Entity>) host.world().getLoadedEntityList()) {
            if(entity instanceof EntityLocomotiveDiesel) {
                UUID cardId = ((EntityLocomotiveDiesel) entity).getLocomotiveCardId();
                if(myCardId.equals(cardId)) {
                    if(entity.getUniqueID().toString().equals(id))
                        return (EntityLocomotiveDiesel) entity;
                }
            }
        }
        throw new LuaError("Entity not found!");
    }

    @Override
    public Map<String, String> getDeviceInfo() {
        Map<String, String> INFO = new HashMap<>();
        INFO.put(DeviceAttribute.Class, DeviceClass.Network);
        INFO.put(DeviceAttribute.Vendor, "Avium Inc.");
        INFO.put(DeviceAttribute.Product, "Locomotive controller");
        return INFO;
    }

    public static void pushSignal(UUID networkId, String signal, Object... arguments) {
        WeakHashMap<String, LocomotiveCard> connected = connectedNodes.get(networkId);
        if(connected != null && !connected.isEmpty()) {
            for(LocomotiveCard locomotiveCard : connected.values()) {
                if(locomotiveCard.host != null) {
                    Machine machine = locomotiveCard.host.machine();
                    if(machine.isRunning()) {
                        machine.signal(signal, arguments);
                    }
                }
            }
        }
    }

}
