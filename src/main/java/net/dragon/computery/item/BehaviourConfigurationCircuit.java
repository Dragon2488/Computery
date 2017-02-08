package net.dragon.computery.item;

import gregtech.api.items.GT_MetaBase_Item;
import gregtech.common.items.behaviors.Behaviour_None;
import net.minecraft.item.ItemStack;

import java.util.List;

public class BehaviourConfigurationCircuit extends Behaviour_None {

    @Override
    public List<String> getAdditionalToolTips(GT_MetaBase_Item aItem, List<String> aList, ItemStack aStack) {
        int configuration = RecipeConfigurationCircuit.getConfiguration(aStack);
        aList.add("Configuration == " + configuration);
        return aList;
    }

}
