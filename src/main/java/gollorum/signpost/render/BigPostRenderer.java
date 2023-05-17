package gollorum.signpost.render;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import gollorum.signpost.Tags;
import gollorum.signpost.blocks.tiles.BigPostPostTile;
import gollorum.signpost.util.BigBaseInfo;
import gollorum.signpost.util.Sign;

public class BigPostRenderer extends TileEntitySpecialRenderer {

    private static final ModelBigSign model16 = new ModelBigSign(true);
    private static final ModelBigSign model32 = new ModelBigSign(false);
    private static final ModelBigPost post = new ModelBigPost();

    public BigPostRenderer() {}

    void setTexture(ResourceLocation loc) {
        try {
            bindTexture(loc);
        } catch (Exception e) {
            if (loc != null && loc.equals(new ResourceLocation("signpost:textures/blocks/bigsign.png"))) {
                bindTexture(new ResourceLocation("signpost:textures/blocks/bigsign_oak.png"));
            }
        }
    }

    @Override
    public void renderTileEntityAt(TileEntity ti, double x, double y, double z, float scale) {
        BigPostPostTile tile = (BigPostPostTile) ti;
        BigBaseInfo tilebases = tile.bases;
        double rotation = 0;
        if (tilebases == null && !tile.isItem) {
            tilebases = tile.getBases();
        }
        if (!tile.isItem) {
            rotation = tilebases.sign.calcRot(tile.xCoord, tile.zCoord);
        }

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y, z + 0.5);
        post.render(this, 0.1f, 0.0625f, tilebases, tile, rotation);
        ResourceLocation resLoc;
        if (!tile.isItem && tile.isAwaitingPaint() && tile.getPaintObject() instanceof Sign) {
            resLoc = tilebases.sign.BIGSIGN_PAINT;
        } else {
            resLoc = tile.isItem ? tile.type.texture : tilebases.sign.paint;
        }
        try {
            this.bindTexture(resLoc);
        } catch (Exception e) {
            this.setTexture(resLoc = tile.type.texture);
            tilebases.sign.paint = resLoc;
        }

        // Check if we should draw the sign (even without a waystone)
        boolean drawSign = !tile.isItem && tilebases.hasSignBoard();

        if (resLoc.getResourceDomain()
            .equals("signpost")) {
            model32.render(this, 0.1f, 0.0625f, tilebases, tile, rotation, drawSign);
        } else {
            model16.render(this, 0.1f, 0.0625f, tilebases, tile, rotation, drawSign);
        }

        // Overlays
        if (!tile.isItem) {
            if (drawSign && tilebases.sign.overlay != null) {
                setTexture(
                    new ResourceLocation(
                        Tags.MODID + ":textures/blocks/bigsign_overlay_" + tilebases.sign.overlay.texture + ".png"));
                model32.renderOverlay(tilebases, 0.0625f, rotation);
            }
        }

        FontRenderer fontrenderer = this.func_147498_b();
        GL11.glPushMatrix();
        GL11.glTranslated(0, 1.12, 0);
        GL11.glRotated(180, 0, 0, 1);
        GL11.glRotated(180, 0, 1, 0);
        double sc = 0.013d;

        int color = 0;

        if (!tile.isItem) {
            if (drawSign) {
                GL11.glTranslated(0, 0.1, 0);
                for (String s : tilebases.description) {
                    int stringWidth = fontrenderer.getStringWidth(s);
                    GL11.glTranslated(0, 0.165, 0);
                    if (s == null) {
                        continue;
                    }
                    double sc2 = 90d / stringWidth;
                    if (sc2 >= 1) {
                        sc2 = 1;
                    }
                    double lurch = (tilebases.sign.flip ? -0.1 : 0.1) - stringWidth * sc * sc2 / 2;
                    double alpha = Math.atan(lurch * 16 / 3.001);
                    double d = Math.sqrt(Math.pow(3.001 / 16, 2) + Math.pow(lurch, 2));
                    double beta = alpha + rotation;
                    double dx = Math.sin(beta) * d;
                    double dz = -Math.cos(beta) * d;
                    GL11.glPushMatrix();
                    GL11.glTranslated(dx, 0, dz);
                    GL11.glScaled(sc, sc, sc);
                    GL11.glRotated(-Math.toDegrees(rotation), 0, 1, 0);
                    GL11.glScaled(sc2, sc2, sc2);
                    fontrenderer.drawString(s, 0, 0, color);
                    GL11.glPopMatrix();
                }
            }
        }

        GL11.glPopMatrix();
        GL11.glPopMatrix();

    }

}
