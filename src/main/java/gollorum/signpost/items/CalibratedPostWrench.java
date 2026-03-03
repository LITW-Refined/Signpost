package gollorum.signpost.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import gollorum.signpost.Signpost;

public class CalibratedPostWrench extends Item {

    public CalibratedPostWrench() {
        super();

        this.setUnlocalizedName("SignpostCalibratedTool");
        this.setCreativeTab(CreativeTabs.tabTools);
        this.setTextureName(Signpost.modId + ":toolcalibrated");
    }

}
