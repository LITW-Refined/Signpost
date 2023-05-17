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

    public String getSign1Text() {
        return getSignTextInternal(this, sign1, 0);
    }

    public String getSign2Text() {
        return getSignTextInternal(this, sign2, 1);
    }

    public Boolean hasSignBoard1() {
        return getSign1Text() != null;
    }

    public Boolean hasSignBoard2() {
        return getSign2Text() != null;
    }

    private static String getSignTextInternal(DoubleBaseInfo tilebases, Sign sign, int descriptionIndex) {
        String text = null;

        if (tilebases.description.length > descriptionIndex && tilebases.description[descriptionIndex] != null
            && !tilebases.description[descriptionIndex].equals("")) {
            text = tilebases.description[descriptionIndex];
        } else if (sign.base != null) {
            text = sign.base.getName();
            if (sign.base.getName()
                .equals("")
                || sign.base.getName()
                    .equals("null")) {
                text = null;
            }
        }

        return text;
    }
}
