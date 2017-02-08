package net.dragon.computery.item;

import gregtech.api.items.GT_MetaGenerated_Item;

public class CP_MetaGenerated_Item_01 extends GT_MetaGenerated_Item {

    public CP_MetaGenerated_Item_01() {
        super("cp.metaitem.01", (short) 0, (short) 4);

        addItem(0, "Tungstensteel Minecart Wheels", null);
        addItem(1, "Reinforced Minecart Casing", null);
        addItem(2, "Locomotive Gearbox", null);
        addItem(3, "Configuration Circuit", "Combine with Screwdriver to set configuration.", new BehaviourConfigurationCircuit());

    }
}
