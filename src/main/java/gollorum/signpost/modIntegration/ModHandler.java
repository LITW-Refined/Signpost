package gollorum.signpost.modIntegration;

import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;

import gollorum.signpost.util.BaseInfo;

public interface ModHandler {

    /**
     * use BaseInfo.fromExternal()
     */
    Set<BaseInfo> getAllBaseInfos();

    Set<BaseInfo> getAllBaseInfosByPlayer(EntityPlayer player);

}
