package gollorum.signpost.blocks.tiles;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import gollorum.signpost.SPEventHandler;
import gollorum.signpost.blocks.BigPostPost;
import gollorum.signpost.blocks.BigPostPost.BigHit;
import gollorum.signpost.blocks.BigPostPost.BigHitTarget;
import gollorum.signpost.blocks.BigPostPost.BigPostType;
import gollorum.signpost.management.PostHandler;
import gollorum.signpost.network.NetworkHandler;
import gollorum.signpost.network.messages.SendAllBigPostBasesMessage;
import gollorum.signpost.network.messages.SendBigPostBasesMessage;
import gollorum.signpost.util.BaseInfo;
import gollorum.signpost.util.BigBaseInfo;
import gollorum.signpost.util.BoolRun;
import gollorum.signpost.util.MyBlockPos;
import gollorum.signpost.util.Paintable;
import gollorum.signpost.util.Sign;
import gollorum.signpost.util.Sign.OverlayType;

public class BigPostPostTile extends SuperPostPostTile {

    public BigPostType type = BigPostType.OAK;
    public static final int DESCRIPTIONLENGTH = 4;
    private boolean isLoading = false;

    @Deprecated
    public BigBaseInfo bases = null;

    public BigPostPostTile() {
        super();
    }

    public BigPostPostTile(BigPostType type) {
        this();
        this.type = type;
    }

    public BigBaseInfo getBases() {
        BigBaseInfo bases = PostHandler.getBigPosts()
            .get(toPos());
        if (bases == null) {
            bases = new BigBaseInfo(type.texture, type.resLocMain);
            PostHandler.getBigPosts()
                .put(toPos(), bases);
        } else {
            if (bases.sign != null && bases.sign.base != null && bases.sign.base.teleportPosition == null) {
                BaseInfo newBase = PostHandler.getWSbyName(bases.sign.base.getName());
                if (newBase != null) {
                    bases.sign.base = newBase;
                }
            }
        }
        this.bases = bases;
        return bases;
    }

    @Override
    public void onBlockDestroy(MyBlockPos pos) {
        super.onBlockDestroy(pos);
        isCanceled = true;
        BigBaseInfo bases = getBases();
        if (bases.sign.overlay != null) {
            EntityItem item = new EntityItem(
                getWorldObj(),
                pos.x,
                pos.y,
                pos.z,
                new ItemStack(bases.sign.overlay.item, 1));
            getWorldObj().spawnEntityInWorld(item);
        }
        if (PostHandler.getBigPosts()
            .remove(pos) != null) {
            NetworkHandler.netWrap.sendToAll(new SendAllBigPostBasesMessage());
        }
    }

    @Override
    public void save(NBTTagCompound tagCompound) {
        BigBaseInfo bases = getBases();
        tagCompound.setString("base", "" + bases.sign.base);
        tagCompound.setInteger("rot", bases.sign.rotation);
        tagCompound.setBoolean("flip", bases.sign.flip);
        tagCompound.setString("overlay", "" + bases.sign.overlay);
        tagCompound.setBoolean("point", bases.sign.point);
        tagCompound.setString("paint", locToString(bases.sign.paint));
        tagCompound.setString("postPaint", locToString(bases.postPaint));
        for (int i = 0; i < bases.description.length; i++) {
            tagCompound.setString("description" + i, bases.description[i]);
        }
        if (bases.equals(getPaintObject())) {
            tagCompound.setByte("paintObjectIndex", (byte) 1);
        } else if (bases.sign.equals(getPaintObject())) {
            tagCompound.setByte("paintObjectIndex", (byte) 2);
        } else {
            tagCompound.setByte("paintObjectIndex", (byte) 0);
        }
    }

    @Override
    public void load(NBTTagCompound tagCompound) {
        isLoading = true;
        final String base = tagCompound.getString("base");
        final int rotation = tagCompound.getInteger("rot");
        final boolean flip = tagCompound.getBoolean("flip");
        final OverlayType overlay = OverlayType.get(tagCompound.getString("overlay"));
        final boolean point = tagCompound.getBoolean("point");
        final String[] description = new String[DESCRIPTIONLENGTH];
        final String paint = tagCompound.getString("paint");
        final String postPaint = tagCompound.getString("postPaint");

        for (int i = 0; i < DESCRIPTIONLENGTH; i++) {
            description[i] = tagCompound.getString("description" + i);
        }
        final byte paintObjectIndex = tagCompound.getByte("paintObjectIndex");

        SPEventHandler.scheduleTask(new BoolRun() {

            @Override
            public boolean run() {
                if (getWorldObj() == null) {
                    return false;
                } else {
                    if (getWorldObj().isRemote) {
                        return true;
                    }
                    BigBaseInfo bases = getBases();
                    bases.sign.base = PostHandler.getForceWSbyName(base);
                    bases.sign.rotation = rotation;
                    bases.sign.flip = flip;
                    bases.sign.overlay = overlay;
                    bases.sign.point = point;
                    bases.description = description;
                    bases.sign.paint = stringToLoc(paint);
                    bases.postPaint = postPaint == null || postPaint.equals("")
                        || postPaint.equals("null")
                        || postPaint.equals("minecraft:") ? type.resLocMain : stringToLoc(postPaint);
                    switch (paintObjectIndex) {
                        case 1:
                            bases.paintObject = bases;
                            bases.awaitingPaint = true;
                            break;
                        case 2:
                            bases.paintObject = bases.sign;
                            bases.awaitingPaint = true;
                            break;
                        default:
                            bases.paintObject = null;
                            bases.awaitingPaint = false;
                            break;
                    }
                    NetworkHandler.netWrap.sendToAll(
                        new SendBigPostBasesMessage(
                            (BigPostPostTile) getWorldObj().getTileEntity(xCoord, yCoord, zCoord),
                            bases));
                    isLoading = false;
                    return true;
                }
            }
        });
    }

    public boolean isLoading() {
        return isLoading;
    }

    @Override
    public Sign getSign(EntityPlayer player) {
        BigBaseInfo bases = getBases();
        BigHit hit = (BigHit) ((BigPostPost) blockType).getHitTarget(getWorldObj(), xCoord, yCoord, zCoord, player);
        if (hit.target.equals(BigHitTarget.BASE)) {
            return bases.sign;
        } else {
            return null;
        }
    }

    @Override
    public Paintable getPaintable(EntityPlayer player) {
        BigBaseInfo bases = getBases();
        BigHit hit = (BigHit) ((BigPostPost) getBlockType())
            .getHitTarget(getWorldObj(), xCoord, yCoord, zCoord, player);
        if (hit.target.equals(BigHitTarget.BASE)) {
            return bases.sign;
        } else {
            return bases;
        }
    }

    @Override
    public ResourceLocation getPostPaint() {
        return getBases().postPaint;
    }

    public void setPostPaint(ResourceLocation loc) {
        getBases().postPaint = loc;
    }

    @Override
    public boolean isAwaitingPaint() {
        return getBases().awaitingPaint;
    }

    @Override
    public Paintable getPaintObject() {
        return getBases().paintObject;
    }

    @Override
    public void setAwaitingPaint(boolean awaitingPaint) {
        getBases().awaitingPaint = awaitingPaint;
    }

    @Override
    public void setPaintObject(Paintable paintObject) {
        getBases().paintObject = paintObject;
    }

    @Override
    public String toString() {
        return getBases() + " at " + toPos();
    }

    @Override
    public List<Sign> getEmptySigns() {
        List<Sign> ret = new LinkedList<Sign>();
        BigBaseInfo bases = getBases();
        if (bases.sign == null && !bases.sign.isValid()) {
            ret.add(bases.sign);
        }
        return ret;
    }
}
