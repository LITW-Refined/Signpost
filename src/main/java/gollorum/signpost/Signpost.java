package gollorum.signpost;

import java.io.File;

import net.minecraft.command.ServerCommandManager;
import net.minecraft.nbt.NBTTagCompound;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.NetworkRegistry;
import gollorum.signpost.commands.*;
import gollorum.signpost.gui.SignGuiHandler;
import gollorum.signpost.management.ConfigHandler;
import gollorum.signpost.management.PostHandler;
import gollorum.signpost.worldGen.villages.NameLibrary;
import gollorum.signpost.worldGen.villages.VillageLibrary;

@Mod(modid = Signpost.modId, version = Tags.VERSION, name = Signpost.modName)
public class Signpost {

    public static final String modId = "signpost";
    public static final String modGroup = "gollorum.signpost";
    public static final String modName = "SignPost";

    @Instance
    public static Signpost instance;
    public static final int GuiBaseID = 0;
    public static final int GuiPostID = 1;
    public static final int GuiBigPostID = 2;
    public static final int GuiPostBrushID = 3;
    public static final int GuiPostRotationID = 4;

    public static NBTTagCompound saveFile;
    public static final Logger LOG = LogManager.getLogger(Signpost.modId);

    public static File configFile;
    public static File configFolder;

    @SidedProxy(clientSide = "gollorum.signpost.ClientProxy", serverSide = "gollorum.signpost.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        configFolder = new File(event.getModConfigurationDirectory() + "/" + Signpost.modId);
        configFolder.mkdirs();
        configFile = new File(configFolder.getPath(), Signpost.modId + ".cfg");
        ConfigHandler.init(configFile);
        NameLibrary.init(configFolder.getPath());
        proxy.preInit();
        proxy.init();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new SignGuiHandler());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        ConfigHandler.postInit();
    }

    @EventHandler
    public void preServerStart(FMLServerAboutToStartEvent event) {
        PostHandler.init();
        VillageLibrary.init();
    }

    @EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent e) {
        PostHandler.init();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent e) {
        registerCommands(
            (ServerCommandManager) e.getServer()
                .getCommandManager());
        ConfigHandler.init(configFile);
    }

    private void registerCommands(ServerCommandManager manager) {
        manager.registerCommand(new ConfirmTeleportCommand());
        manager.registerCommand(new GetWaystoneCount());
        manager.registerCommand(new GetSignpostCount());
        manager.registerCommand(new SetWaystoneCount());
        manager.registerCommand(new SetSignpostCount());
        manager.registerCommand(new DiscoverWaystone());
        manager.registerCommand(new ListAllWaystones());
    }
}
