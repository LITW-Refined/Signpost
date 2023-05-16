package gollorum.signpost.util;

import net.minecraft.util.ResourceLocation;

public class DoubleBaseInfo extends SignBaseInfo {

    public Sign sign1;
    public Sign sign2;
    public String[] description;

    public DoubleBaseInfo(ResourceLocation signTexture, ResourceLocation postTexture) {
        this(new Sign(signTexture), new Sign(signTexture), postTexture);
    }

    public DoubleBaseInfo(Sign sign1, Sign sign2, ResourceLocation texture) {
        this.sign1 = sign1;
        this.sign2 = sign2;
        postPaint = texture;
        String[] description = { "", "" };
        this.description = description;
    }

    public DoubleBaseInfo(Sign sign1, Sign sign2, String[] description, ResourceLocation texture) {
        this.sign1 = sign1;
        this.sign2 = sign2;
        postPaint = texture;
        this.description = description;
    }

    @Override
    public String toString() {
        return sign1 + " and " + sign2;
    }
}
