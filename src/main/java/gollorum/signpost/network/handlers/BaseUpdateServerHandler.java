package gollorum.signpost.network.handlers;

import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import gollorum.signpost.event.UpdateWaystoneEvent;
import gollorum.signpost.management.PostHandler;
import gollorum.signpost.network.NetworkHandler;
import gollorum.signpost.network.messages.BaseUpdateClientMessage;
import gollorum.signpost.network.messages.BaseUpdateServerMessage;
import gollorum.signpost.util.BaseInfo;

public class BaseUpdateServerHandler implements IMessageHandler<BaseUpdateServerMessage, IMessage> {

    @Override
    public IMessage onMessage(BaseUpdateServerMessage message, MessageContext ctx) {
        if (message.destroyed) {} else {
            PostHandler.addDiscovered(ctx.getServerHandler().playerEntity.getUniqueID(), message.wayStone);
        }
        BaseInfo waystone = PostHandler.getAllWaystones()
            .getByPos(message.wayStone.blockPosition);
        waystone.setAll(message.wayStone);
        NetworkHandler.netWrap.sendToAll(new BaseUpdateClientMessage());
        MinecraftForge.EVENT_BUS.post(
            new UpdateWaystoneEvent(
                UpdateWaystoneEvent.WaystoneEventType.NAMECHANGED,
                ctx.getServerHandler().playerEntity.worldObj,
                waystone.teleportPosition.x,
                waystone.teleportPosition.y,
                waystone.teleportPosition.z,
                waystone.getName()));
        return null;
    }

}
