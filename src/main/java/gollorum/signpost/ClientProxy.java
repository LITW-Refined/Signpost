package gollorum.signpost;

import java.util.Collection;
import java.util.LinkedList;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class ClientProxy extends CommonProxy{

	public ClientProxy(){
		blockHandler = new BlockHandlerClient();
	}
	
	@Override
	public void init(){
		super.init();
		registerRenderers();
	}
	
	private void registerRenderers(){
		((BlockHandlerClient)blockHandler).registerRenders();
	}

	@Override
	public World getWorld(MessageContext ctx){
		return FMLClientHandler.instance().getWorldClient();
	}
	
	@Override
	public World getWorld(String worldName, int dim){
		if(FMLCommonHandler.instance()!=null && 
			FMLCommonHandler.instance().getMinecraftServerInstance()!=null && 
			FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer()){
			return super.getWorld(worldName, dim);
		}else{
			return FMLClientHandler.instance().getWorldClient();
		}
	}
	@Override
	public Collection<EntityPlayer> getAllPlayers(){
		LinkedList<EntityPlayer> ret = new LinkedList();
		ret.add(FMLClientHandler.instance().getClientPlayerEntity());
		return ret;
	}
	
}