package gollorum.signpost.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import gollorum.signpost.management.ConfigHandler;
import gollorum.signpost.management.PostHandler;
import gollorum.signpost.network.NetworkHandler;
import gollorum.signpost.network.messages.ChatMessage;

public class GetSignpostCount extends CommandBase {

    @Override
    public String getCommandName() {
        return "getsignpostsleft";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "/getsignpostsleft [Player - op only]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) sender;
            if (args.length == 0 || !ConfigHandler.isOp(player)) {
                String amount = ""
                    + PostHandler.getPlayerKnownWaystonePositions(player.getUniqueID()).remainingSignposts;
                if (amount.equals("-1")) {
                    amount = "unlimited";
                }
                NetworkHandler.netWrap.sendTo(new ChatMessage("signpost.getsignpostsleft", "<amount>", amount), player);
            } else {
                EntityPlayerMP p = (EntityPlayerMP) PostHandler.getPlayerByName(args[0]);
                if (p == null) {
                    NetworkHandler.netWrap
                        .sendTo(new ChatMessage("signpost.playerNotFound", "<player>", args[0]), player);
                    return;
                }
                String amount = "" + PostHandler.getPlayerKnownWaystonePositions(p.getUniqueID()).remainingSignposts;
                if (amount.equals("-1")) {
                    amount = "unlimited";
                }
                String[] keys = { "<amount>", "<player>" };
                String[] replacement = { amount, args[0] };
                NetworkHandler.netWrap
                    .sendTo(new ChatMessage("signpost.getsignpostsleftop", keys, replacement), player);
            }
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return sender instanceof EntityPlayer;
    }

}
