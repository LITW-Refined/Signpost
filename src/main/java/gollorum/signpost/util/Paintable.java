package gollorum.signpost.util;

import net.minecraft.util.ResourceLocation;

import gollorum.signpost.Tags;

public interface Paintable {

    public static final ResourceLocation SIGN_PAINT = new ResourceLocation(
        Tags.MODID,
        "textures/blocks/sign_paint.png");
    public static final ResourceLocation BIGSIGN_PAINT = new ResourceLocation(
        Tags.MODID,
        "textures/blocks/bigsign_paint.png");
    public static final ResourceLocation POST_PAINT = new ResourceLocation(Tags.MODID, "textures/blocks/paint.png");

    public ResourceLocation getTexture();

    public void setTexture(ResourceLocation texture);

    public ResourceLocation getDefaultBiomeTexture(BiomeContainer biome);

    public void setTextureToBiomeDefault(BiomeContainer biome);

}
