package gollorum.signpost.blocks;

import gollorum.signpost.management.ConfigHandler;
import gollorum.signpost.util.BlockPos;
import gollorum.signpost.util.DoubleBaseInfo;
import gollorum.signpost.util.math.tracking.DDDVector;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public abstract class SuperPostPostTile extends TileEntity{
	
	public boolean isItem = false;
	public boolean isCanceled = false;

	public final BlockPos toPos(){
		if(worldObj==null||worldObj.isRemote){
			return new BlockPos("", xCoord, yCoord, zCoord, dim());
		}else{
			return new BlockPos(worldObj.getWorldInfo().getWorldName(), xCoord, yCoord, zCoord, dim());
		}
	}

	public final int dim(){
		if(worldObj==null||worldObj.provider==null){
			return Integer.MIN_VALUE;
		}else
			return worldObj.provider.dimensionId;
	}

	public static final ResourceLocation stringToLoc(String str){
		return str==null||str.equals("null")||str.equals("")?null:new ResourceLocation(str);
	}
	
	public static final String LocToString(ResourceLocation loc){
		return loc==null?"null":loc.getResourcePath();
	}

	public abstract void onBlockDestroy(BlockPos pos);
	
}