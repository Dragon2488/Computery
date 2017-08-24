package net.dragon.computery;

import com.mojang.realmsclient.gui.ChatFormatting;
import cpw.mods.fml.common.*;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.GregTech_API;
import gregtech.common.items.GT_MetaGenerated_Item_01;
import mods.railcraft.common.items.RailcraftItem;
import net.dragon.computery.cart.ModEnumCart;
import net.dragon.computery.cart.render.ModRenderType;
import net.dragon.computery.item.Objects;
import net.dragon.computery.item.RecipeConfigurationCircuit;
import net.dragon.computery.item.RecipeLocomotiveCardLink;
import net.dragon.computery.item.ShapedNFCRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

@Mod(name = "Computery", modid = "computery", version = "0.7", dependencies = "required-after:Railcraft; required-after:gregtech; required-after:OpenComputers")
public class ComputeryMod {

    @Mod.Instance("computery")
    public static ComputeryMod mod;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

        Objects.init();

        for(ModEnumCart cart : ModEnumCart.values())
            cart.setup();
        if(event.getSide().isClient())
            ModRenderType.attachRenderers();

        NetworkRegistry.INSTANCE.registerGuiHandler(this, new ModGuiHandler());
        MinecraftForge.EVENT_BUS.register(this);

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Objects.metaItem, 1, 0),
                "GXG", "IRI", "OWO",
                'X', "craftingToolHardHammer",
                'W', "craftingToolWrench",
                'I', "ringTungstenSteel",
                'R', "stickTungstenSteel",
                'G', "gearGtSmallTungstenSteel",
                'O', "screwTungstenSteel"
                ));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Objects.metaItem, 1, 1),
                "OHO", "PWP", "IGI",
                'H', "craftingToolWrench",
                'W', "gearGtTungstenSteel",
                'P', "plateDoubleTungstenSteel",
                'G', "stickTungstenSteel",
                'I', new ItemStack(Objects.metaItem, 1, 0)
                ));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Objects.metaItem, 1, 2),
                "CPC", "WWW", "EEE",
                'C', "circuitAdvanced",
                'P', "oc:cpu2",
                'W', "cableGt01Gold",
                'E', new ItemStack(GT_MetaGenerated_Item_01.INSTANCE, 1, 32602)
                ));

        GameRegistry.addRecipe(new ShapedOreRecipe(ModRenderType.DIESEL.getItemWithRenderer(
                "railcraft:default", ModEnumCart.LOCO_DIESEL.getCartItem()),

                "ABC", "DEF", "GHG",
                'A', "screwTungstenSteel",
                'B', "gearGtSmallTungstenSteel",
                'C', new ItemStack(GT_MetaGenerated_Item_01.INSTANCE, 1, 32406),
                'D', "plateDoubleTungstenSteel",
                'E', new ItemStack(Objects.metaItem, 1, 2),
                'F', new ItemStack(GregTech_API.sBlockMachines, 1, 1112),
                'G', "gearGtTungstenSteel",
                'H', new ItemStack(Objects.metaItem, 1, 1)
        ));

        GameRegistry.addRecipe(new ShapelessOreRecipe(ModRenderType.DIESEL.getItemWithRenderer(
                "railcraft:default", ModEnumCart.LOCO_DIESEL.getCartItem()),
                ModRenderType.DIESEL.getItemWithRenderer("railcraft:heart", ModEnumCart.LOCO_DIESEL.getCartItem()),
                "dyePink"
        ));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Objects.ocComponents, 4, 0),
                "XYX", "WYW", "WCW",
                'X', "oc:wlanCard",
                'Y', "oc:circuitChip3",
                'W', "oc:lanCard",
                'C', "oc:dataCard1"));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Objects.ocComponents, 1, 1),
                "XYX", "MYM", "MCM",
                'X', "oc:graphicsCard3",
                'Y', "oc:circuitChip3",
                'M', "oc:materialALU",
                'C', "oc:dataCard2"
                ));

        GameRegistry.addRecipe(new ShapedNFCRecipe(new ItemStack(Objects.nfcTrack, 1, 0),
                "XRX", "XCX", "XYX",
                'X', RailcraftItem.rail.getStack(),
                'R', RailcraftItem.railbed.getStack(),
                'C', new ItemStack(Objects.metaItem, 1, 3),
                'Y', "plateRedAlloy"
                ));

        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Objects.metaItem, 1, 3), "circuitData"));

        CraftingManager.getInstance().getRecipeList().add(new RecipeConfigurationCircuit());
        CraftingManager.getInstance().getRecipeList().add(new RecipeLocomotiveCardLink());

    }

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        if (event.showAdvancedItemTooltips) {
            for (String oreId : OreDictionary.getOreNames()) {
                for (ItemStack stack : OreDictionary.getOres(oreId)) {
                    if (stack.isItemEqual(event.itemStack)) {
                        event.toolTip.add(ChatFormatting.GRAY + oreId);
                    }
                }
            }
        }
    }

}
