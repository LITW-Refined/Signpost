package gollorum.signpost.blocks.tiles;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import gollorum.signpost.BlockHandler;
import gollorum.signpost.SPEventHandler;
import gollorum.signpost.blocks.SuperPostPost;
import gollorum.signpost.blocks.WaystoneContainer;
import gollorum.signpost.event.UpdateWaystoneEvent;
import gollorum.signpost.management.PostHandler;
import gollorum.signpost.network.NetworkHandler;
import gollorum.signpost.network.messages.BaseUpdateClientMessage;
import gollorum.signpost.network.messages.BaseUpdateServerMessage;
import gollorum.signpost.util.BaseInfo;
import gollorum.signpost.util.MyBlockPos;
import gollorum.signpost.util.Paintable;
import gollorum.signpost.util.Sign;

public abstract class SuperPostPostTile extends TileEntity implements WaystoneContainer {

    public boolean isItem = false;
    public boolean isCanceled = false;
    public UUID owner;

    @Deprecated
    public boolean isWaystone = false;

    public SuperPostPostTile() {
        SPEventHandler.scheduleTask(() -> {
            if (getWorldObj() == null) {
                return false;
            } else {
                isWaystone();
                return true;
            }
        });
    }

    public final MyBlockPos toPos() {
        return new MyBlockPos(xCoord, yCoord, zCoord, dim());
    }

    public final int dim() {
        if (getWorldObj() == null || getWorldObj().provider == null) {
            return Integer.MIN_VALUE;
        } else return getWorldObj().provider.dimensionId;
    }

    public static final ResourceLocation stringToLoc(String str) {
        return str == null || str.equals("null") || str.equals("") ? null : new ResourceLocation(str);
    }

    public static final String locToString(ResourceLocation loc) {
        return loc == null ? "null" : loc.getResourceDomain() + ":" + loc.getResourcePath();
    }

    public void onBlockDestroy(MyBlockPos pos) {
        if (isWaystone()) {
            destroyWaystone();
        }
    }

    public boolean isWaystone() {
        return isWaystone = (getBaseInfo() != null);
    }

    public void destroyWaystone() {
        MyBlockPos pos = toPos();
        isWaystone = false;
        EntityItem item = new EntityItem(getWorldObj(), pos.x, pos.y, pos.z, new ItemStack(BlockHandler.base, 1));
        getWorldObj().spawnEntityInWorld(item);
        BaseInfo base = PostHandler.getNativeWaystones()
            .getByPos(pos);
        SPEventHandler.INSTANCE.updateWaystoneCount(this);
        if (PostHandler.getNativeWaystones()
            .removeByPos(pos)) {
            MinecraftForge.EVENT_BUS.post(
                new UpdateWaystoneEvent(
                    UpdateWaystoneEvent.WaystoneEventType.DESTROYED,
                    getWorldObj(),
                    base.teleportPosition.x,
                    base.teleportPosition.y,
                    base.teleportPosition.z,
                    base == null ? "" : base.getName()));
            NetworkHandler.netWrap.sendToAll(new BaseUpdateClientMessage());
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("signpostNBTVersion", 1);
        NBTTagCompound tagComp = new NBTTagCompound();
        if (!(owner == null)) {
            tagComp.setString("PostOwner", owner.toString());
        }
        save(tagComp);
        tagCompound.setTag("signpostDataTag", tagComp);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        if (tagCompound.getInteger("signpostNBTVersion") == 1) {
            NBTTagCompound tagComp = (NBTTagCompound) tagCompound.getTag("signpostDataTag");
            String owner = tagComp.getString("PostOwner");
            try {
                this.owner = owner == null ? null : UUID.fromString(owner);
            } catch (Exception e) {
                this.owner = null;
            }
            load(tagComp);
        } else {
            load(tagCompound);
        }
    }

    public abstract void save(NBTTagCompound tagCompound);

    public abstract void load(NBTTagCompound tagCompound);

    public abstract Sign getSign(EntityPlayer player);

    public abstract Paintable getPaintable(EntityPlayer player);

    public abstract ResourceLocation getPostPaint();

    public abstract void setPostPaint(ResourceLocation loc);

    public abstract boolean isAwaitingPaint();

    public abstract Paintable getPaintObject();

    public abstract void setAwaitingPaint(boolean awaitingPaint);

    public abstract void setPaintObject(Paintable paintObject);

    public abstract boolean isLoading();

    public abstract List<Sign> getEmptySigns();

    public SuperPostPost getSuperBlock() {
        return (SuperPostPost) this.getBlockType();
    }

    public BaseInfo getBaseInfo() {
        return PostHandler.getNativeWaystones()
            .getByPos(toPos());
    }

    @Override
    public void setName(String name) {
        BaseInfo ws = getBaseInfo();
        ws.setName(name);
        NetworkHandler.netWrap.sendToServer(new BaseUpdateServerMessage(ws, false));
    }

    @Override
    public String getName() {
        BaseInfo ws = getBaseInfo();
        return ws == null ? "null" : getBaseInfo().toString();
    }

    @Override
    public int getBlockMetadata() {
        try {
            return super.getBlockMetadata();
        } catch (NullPointerException e) {
            System.out.println(getClass());
            return 0;
        }
    }
}
