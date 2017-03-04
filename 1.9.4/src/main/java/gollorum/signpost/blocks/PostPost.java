package gollorum.signpost.blocks;

import javax.annotation.Nullable;

import gollorum.signpost.Signpost;
import gollorum.signpost.items.PostWrench;
import gollorum.signpost.management.PostHandler;
import gollorum.signpost.network.NetworkHandler;
import gollorum.signpost.network.messages.SendAllPostBasesMessage;
import gollorum.signpost.network.messages.SendPostBasesMessage;
import gollorum.signpost.network.messages.TeleportMeMessage;
import gollorum.signpost.util.BaseInfo;
import gollorum.signpost.util.MyBlockPos;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class PostPost extends GolloBlock {
	
	public PostPost() {
		super(Material.WOOD, "post");
		setCreativeTab(CreativeTabs.TRANSPORTATION);
		this.setHarvestLevel("axe", 0);
		this.setHardness(2);
		this.setResistance(100000);
		this.setLightOpacity(0);
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		PostPostTile tile = new PostPostTile();
		tile.isItem = false;
		return tile;
	}

	public static PostPostTile getWaystonePostTile(World world, BlockPos pos) {
		TileEntity ret = world.getTileEntity(pos);
		if (ret instanceof PostPostTile) {
			return (PostPostTile) ret;
		} else {
			return null;
		}
	}

	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		if (!world.isRemote) {
			return;
		}
		PostPostTile tile = getTile(world, pos);
		double lookY  = player.getLookVec().yCoord;
		double playerX = player.posX;
		double playerY = player.posY+player.eyeHeight;
		double playerZ = player.posZ;
		if (player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof PostWrench) {
			double deltaY = MyBlockPos.normalizedY(pos.getX()+0.5-playerX, pos.getY()+0.5-playerY, pos.getZ()+0.5-playerZ);
			if (player.isSneaking()) {
				if (deltaY < lookY) {
					tile.bases.flip1 = !tile.bases.flip1;
				} else {
					tile.bases.flip2 = !tile.bases.flip2;
				}
			} else {
				if (deltaY < lookY) {
					tile.bases.rotation1 = (tile.bases.rotation1 - 15) % 360;
				} else {
					tile.bases.rotation2 = (tile.bases.rotation2 - 15) % 360;
				}
			}
			NetworkHandler.netWrap.sendToServer(new SendPostBasesMessage(tile.toPos(), tile.bases));
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(!world.isRemote){
			return super.onBlockActivated(world, pos, state, player, hand, heldItem, facing, hitX, hitY, hitZ);
		}
		if(player.getHeldItemMainhand()!=null&&player.getHeldItemMainhand().getItem() instanceof PostWrench){
			PostPostTile tile = getTile(world, pos);
			if(player.isSneaking()){
				if(hitY > 0.5){
					tile.bases.flip1 = !tile.bases.flip1;
				}else{
					tile.bases.flip2 = !tile.bases.flip2;
				}
			}else{
				if(hitY > 0.5){
					tile.bases.rotation1 = (tile.bases.rotation1+15)%360;
				}else{
					tile.bases.rotation2 = (tile.bases.rotation2+15)%360;
				}
			}
			NetworkHandler.netWrap.sendToServer(new SendPostBasesMessage(tile.toPos(), tile.bases));
		} else {
			PostPostTile tile = getTile(world, pos);
			if (!player.isSneaking()) {
				BaseInfo destination = hitY > 0.5 ? tile.bases.base1 : tile.bases.base2;
				if (!(destination == null || !world.isRemote)) {
					NetworkHandler.netWrap.sendToServer(new TeleportMeMessage(destination));
				}
			}else{
				player.openGui(Signpost.instance, Signpost.GuiPostID, world, pos.getX(), pos.getY(), pos.getZ());
			}
		}
		return true;
	}

	@Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity){
		if(!(entity instanceof EntityPlayer)){
			return false;
		}
		EntityPlayer player = ((EntityPlayer)entity);
		return !(player.getHeldItemMainhand()!=null&&player.getHeldItemMainhand().getItem() instanceof PostWrench);
	}

	public static void onBlockDestroy(MyBlockPos pos) {
		if(PostHandler.posts.remove(pos)!=null){
			NetworkHandler.netWrap.sendToAll(new SendAllPostBasesMessage());
		}
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.INVISIBLE;
	}

	@Override
    public boolean isOpaqueCube(IBlockState state){
        return false;
    }
	
	@Override
	public boolean isFullCube(IBlockState state){
		return false;
	}
	
	@Override
	public BlockRenderLayer getBlockLayer(){
		return BlockRenderLayer.TRANSLUCENT;
	}

	public static PostPostTile getTile(World world, BlockPos pos) {
		return getWaystonePostTile(world, pos);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		PostPostTile tile = new PostPostTile();
		tile.isItem = false;
		return tile;
	}
}