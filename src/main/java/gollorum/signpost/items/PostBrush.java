package gollorum.signpost.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import gollorum.signpost.Tags;

public class PostBrush extends Item {

    public PostBrush() {
        super();

        this.setUnlocalizedName("SignpostBrush");
        this.setCreativeTab(CreativeTabs.tabTools);
        this.setTextureName(Tags.MODID + ":brush");
    }

}
