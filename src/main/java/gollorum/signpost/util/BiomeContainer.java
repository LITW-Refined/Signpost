package gollorum.signpost.util;

import net.minecraft.world.biome.BiomeGenBase;

import gollorum.signpost.util.code.MinecraftDependent;

@MinecraftDependent
public class BiomeContainer {

    private BiomeGenBase biome;

    public BiomeContainer(BiomeGenBase biome) {
        this.biome = biome;
    }

    public BiomeGenBase getBiome() {
        return biome;
    }

}
