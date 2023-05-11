package gollorum.signpost.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;

import cpw.mods.fml.common.FMLCommonHandler;
import gollorum.signpost.management.ConfigHandler;
import gollorum.signpost.management.PostHandler;
import gollorum.signpost.network.NetworkHandler;
import gollorum.signpost.network.messages.ChatMessage;
import gollorum.signpost.util.BaseInfo;

public class DiscoverWaystone extends CommandBase {

    @Override
    public String getCommandName() {
        return "discoverwaystone";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "/discoverwaystone <name> [player]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws PlayerNotFoundException, CommandException {
        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) sender;
            if (args.length < 1) {
                throw new WrongUsageException(getCommandUsage(null), new Object[0]);
            }
            String waystoneName = "";
            String playerName = null;
            for (String now : args) {
                waystoneName += " " + now;
            }
            waystoneName = waystoneName.substring(1);
            BaseInfo base = PostHandler.getNativeWaystones()
                .getByName(waystoneName);
            if (base == null && args.length > 1) {
                waystoneName = "";
                playerName = args[args.length - 1];
                for (int i = 0; i < args.length - 1; i++) {
                    waystoneName += " " + args[i];
                }
                waystoneName = waystoneName.substring(1);
                base = PostHandler.getNativeWaystones()
                    .getByName(waystoneName);
            }
            EntityPlayerMP target = player;
            if (playerName != null) {
                target = getPlayer(
                    FMLCommonHandler.instance()
                        .getMinecraftServerInstance(),
                    playerName);
            }
            if (base == null) {
                String[] keys = { "<Waystone>" };
                String[] replacement = { waystoneName };
                NetworkHandler.netWrap.sendTo(new ChatMessage("signpost.waystoneNotFound", keys, replacement), player);
            } else {
                PostHandler.addDiscovered(target.getUniqueID(), base);
                String[] keys = { "<Waystone>" };
                String[] replacement = { waystoneName };
                NetworkHandler.netWrap.sendTo(new ChatMessage("signpost.discovered", keys, replacement), target);
            }
        }
    }

    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return sender instanceof EntityPlayerMP && ConfigHandler.isOp((EntityPlayerMP) sender);
    }
}
