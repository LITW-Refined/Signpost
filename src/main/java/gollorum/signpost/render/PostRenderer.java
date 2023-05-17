package gollorum.signpost.render;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import gollorum.signpost.Tags;
import gollorum.signpost.blocks.PostPost.PostType;
import gollorum.signpost.blocks.tiles.PostPostTile;
import gollorum.signpost.util.DoubleBaseInfo;

public class PostRenderer extends TileEntitySpecialRenderer {

    private static final ModelPost model = new ModelPost();

    public PostRenderer() {}

    void setTexture(ResourceLocation loc) {
        try {
            bindTexture(loc);
        } catch (Exception e) {
            if (loc != null && (loc.equals(new ResourceLocation("signpost:textures/blocks/bigsign.png")))) {
                bindTexture(new ResourceLocation("signpost:textures/blocks/bigsign_oak.png"));
            }
        }
    }

    @Override
    public void renderTileEntityAt(TileEntity ti, double x, double y, double z, float scale) {
        PostPostTile tile = (PostPostTile) ti;
        DoubleBaseInfo tilebases = tile.bases;
        double rotation1 = 0;
        double rotation2 = 0;
        if (tilebases == null && !tile.isItem) {
            tilebases = tile.getBases();
        }
        if (!tile.isItem) {
            rotation1 = tilebases.sign1.calcRot(tile.xCoord, tile.zCoord);
            rotation2 = tilebases.sign2.calcRot(tile.xCoord, tile.zCoord);
        }
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y, z + 0.5);
        if (tile.type == null) this.setTexture(PostType.OAK.texture);
        else this.setTexture(tile.type.texture);

        // Check if we should draw the sign (even without a waystone)
        String sign1Text = !tile.isItem ? tilebases.getSign1Text() : null;
        String sign2Text = !tile.isItem ? tilebases.getSign2Text() : null;
        boolean drawSign1 = sign1Text != null;
        boolean drawSign2 = sign2Text != null;

        model.render(this, 0.1f, 0.0625f, tilebases, tile, rotation1, rotation2, drawSign1, drawSign2);

        // Overlays
        if (drawSign1 && tilebases.sign1.overlay != null) {
            setTexture(
                new ResourceLocation(
                    Tags.MODID + ":textures/blocks/sign_overlay_" + tilebases.sign1.overlay.texture + ".png"));
            model.renderOverlay1(tilebases, 0.0625f, rotation1);
        }
        if (drawSign2 && tilebases.sign2.overlay != null) {
            setTexture(
                new ResourceLocation(
                    Tags.MODID + ":textures/blocks/sign_overlay_" + tilebases.sign2.overlay.texture + ".png"));
            model.renderOverlay2(tilebases, 0.0625f, rotation2);
        }

        FontRenderer fontrenderer = this.func_147498_b();
        GL11.glPushMatrix();
        GL11.glTranslated(0, 0.8d, 0);
        GL11.glRotated(180, 0, 0, 1);
        GL11.glRotated(180, 0, 1, 0);
        double sc = 0.013d;
        double ys = 1.3d * sc;

        int color = 0;
        // int color = (1<<16) + (1<<8);

        if (drawSign1) {
            int stringWidth = fontrenderer.getStringWidth(sign1Text);
            double sc2 = 100d / stringWidth;
            if (sc2 >= 1) {
                sc2 = 1;
            }
            double lurch = (tilebases.sign1.flip ? -0.2 : 0.2) - stringWidth * sc * sc2 / 2;
            double alpha = Math.atan(lurch * 16 / 3.001);
            double d = Math.sqrt(Math.pow(3.001 / 16, 2) + Math.pow(lurch, 2));
            double beta = alpha + rotation1;
            double dx = Math.sin(beta) * d;
            double dz = -Math.cos(beta) * d;
            GL11.glPushMatrix();
            GL11.glTranslated(dx, 0, dz);
            GL11.glScaled(sc, ys, sc);
            GL11.glRotated(-Math.toDegrees(rotation1), 0, 1, 0);
            GL11.glScaled(sc2, sc2, sc2);
            fontrenderer.drawString(sign1Text, 0, 0, color);
            GL11.glPopMatrix();
        }

        if (drawSign2) {
            int stringWidth = fontrenderer.getStringWidth(sign2Text);
            GL11.glTranslated(0, 0.5d, 0);
            double sc2 = 100d / stringWidth;
            if (sc2 >= 1) {
                sc2 = 1;
            }
            double lurch = (tilebases.sign2.flip ? -0.2 : 0.2) - stringWidth * sc * sc2 / 2;
            double alpha = Math.atan(lurch * 16 / 3.001);
            double d = Math.sqrt(Math.pow(3.001 / 16, 2) + Math.pow(lurch, 2));
            double beta = alpha + rotation2;
            double dx = Math.sin(beta) * d;
            double dz = -Math.cos(beta) * d;
            GL11.glPushMatrix();
            GL11.glTranslated(dx, 0, dz);
            GL11.glScaled(sc, ys, sc);
            GL11.glRotated(-Math.toDegrees(rotation2), 0, 1, 0);
            GL11.glScaled(sc2, sc2, sc2);
            fontrenderer.drawString(sign2Text, 0, 0, color);
            GL11.glPopMatrix();
        }

        GL11.glPopMatrix();
        GL11.glPopMatrix();

    }
}
