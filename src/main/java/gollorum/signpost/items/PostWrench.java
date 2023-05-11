package gollorum.signpost.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import gollorum.signpost.Tags;

public class PostWrench extends Item {

    public PostWrench() {
        super();

        this.setUnlocalizedName("SignpostTool");
        this.setCreativeTab(CreativeTabs.tabTools);
        this.setTextureName(Tags.MODID + ":tool");
    }

}
