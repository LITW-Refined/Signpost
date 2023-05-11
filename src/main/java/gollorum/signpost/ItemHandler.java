package gollorum.signpost;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.registry.GameRegistry;
import gollorum.signpost.items.CalibratedPostWrench;
import gollorum.signpost.items.PostBrush;
import gollorum.signpost.items.PostWrench;
import gollorum.signpost.management.ClientConfigStorage;
import gollorum.signpost.management.ConfigHandler;

public class ItemHandler {

    public static PostWrench tool = new PostWrench();
    public static CalibratedPostWrench calibratedTool = new CalibratedPostWrench();
    public static PostBrush brush = new PostBrush();

    public static void init() {
        tool = new PostWrench();
    }

    public static void registerItems() {
        GameRegistry.registerItem(tool, "SignpostTool");
        GameRegistry.registerItem(calibratedTool, "SignpostCalibratedTool");
        GameRegistry.registerItem(brush, "SignpostBrush");
        registerRecipes();
    }

    protected static void registerRecipes() {
        if (ClientConfigStorage.INSTANCE.getSecurityLevelSignpost()
            .equals(ConfigHandler.SecurityLevel.ALL)
            || ClientConfigStorage.INSTANCE.getSecurityLevelSignpost()
                .equals(ConfigHandler.SecurityLevel.OWNERS)) {
            GameRegistry.addRecipe(new ItemStack(tool), "II", "IS", "S ", 'I', Items.iron_ingot, 'S', Items.stick);
            GameRegistry.addShapelessRecipe(new ItemStack(calibratedTool), tool, Items.compass);
            GameRegistry.addRecipe(
                new ItemStack(brush),
                "W",
                "I",
                "S",
                'W',
                Item.getItemFromBlock(Blocks.wool),
                'I',
                Items.iron_ingot,
                'S',
                Items.stick);
        }
    }

}
