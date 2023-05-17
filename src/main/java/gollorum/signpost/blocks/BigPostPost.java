package gollorum.signpost.blocks;

import java.util.function.Function;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import gollorum.signpost.Signpost;
import gollorum.signpost.Tags;
import gollorum.signpost.blocks.tiles.BigPostPostTile;
import gollorum.signpost.blocks.tiles.SuperPostPostTile;
import gollorum.signpost.management.ClientConfigStorage;
import gollorum.signpost.management.PostHandler;
import gollorum.signpost.network.NetworkHandler;
import gollorum.signpost.network.messages.ChatMessage;
import gollorum.signpost.network.messages.OpenGuiMessage;
import gollorum.signpost.network.messages.SendAllWaystoneNamesMessage;
import gollorum.signpost.network.messages.SendBigPostBasesMessage;
import gollorum.signpost.util.*;
import gollorum.signpost.util.Sign.OverlayType;
import gollorum.signpost.util.math.tracking.Cuboid;
import gollorum.signpost.util.math.tracking.DDDVector;
import gollorum.signpost.util.math.tracking.Intersect;

public class BigPostPost extends SuperPostPost {

    public BigPostType type;

    public enum BigPostType {

        OAK(Material.wood, "bigsign_oak", "log_oak", Item.getItemFromBlock(Blocks.log), 0),
        SPRUCE(Material.wood, "bigsign_spruce", "log_spruce", Item.getItemFromBlock(Blocks.log), 1),
        BIRCH(Material.wood, "bigsign_birch", "log_birch", Item.getItemFromBlock(Blocks.log), 2),
        JUNGLE(Material.wood, "bigsign_jungle", "log_jungle", Item.getItemFromBlock(Blocks.log), 3),
        ACACIA(Material.wood, "bigsign_acacia", "log_acacia", Item.getItemFromBlock(Blocks.log2), 0),
        BIGOAK(Material.wood, "bigsign_big_oak", "log_big_oak", Item.getItemFromBlock(Blocks.log2), 1),
        IRON(Material.iron, "bigsign_iron", "iron_block", Items.iron_ingot, 0),
        STONE(Material.rock, "bigsign_stone", "stone", Item.getItemFromBlock(Blocks.stone), 0);

        public Material material;
        public ResourceLocation texture;
        public String blockTexture;
        public String textureMain;
        public ResourceLocation resLocMain;
        public Item baseItem;
        public int metadata;

        BigPostType(Material material, String texture, String textureMain, Item baseItem, int metadata) {
            this.material = material;
            this.texture = new ResourceLocation(Tags.MODID + ":textures/blocks/" + texture + ".png");
            this.blockTexture = texture;
            this.textureMain = textureMain;
            this.resLocMain = new ResourceLocation("minecraft:textures/blocks/" + textureMain + ".png");
            this.baseItem = baseItem;
            this.metadata = metadata;
        }
    }

    public enum BigHitTarget {
        BASE,
        POST,
        STONE
    }

    public static class BigHit {

        public BigHitTarget target;
        public DDDVector pos;

        public BigHit(BigHitTarget target, DDDVector pos) {
            this.target = target;
            this.pos = pos;
        }
    }

    @Deprecated
    public BigPostPost() {
        super(Material.wood);
        setBlockName("SignpostBigPost");
        setCreativeTab(CreativeTabs.tabTransport);
        setBlockTextureName("minecraft:planks_oak");
        this.setHardness(2);
        this.setResistance(100000);
        float f = 15F / 32;
        this.setBlockBounds(0.5f - f, 0.0F, 0.5F - f, 0.5F + f, 1, 0.5F + f);
    }

    public BigPostPost(BigPostType type) {
        super(type.material);
        this.type = type;
        setBlockName("SignpostBigPost" + type.toString());
        setCreativeTab(CreativeTabs.tabTransport);
        setBlockTextureName(Tags.MODID + ":" + type.blockTexture);
        this.setHardness(2);
        this.setResistance(100000);
        float f = 15F / 32;
        this.setBlockBounds(0.5f - f, 0.0F, 0.5F - f, 0.5F + f, 1, 0.5F + f);
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        BigPostPostTile tile = new BigPostPostTile(type);
        return tile;
    }

    public static BigPostPostTile getWaystonePostTile(World world, int x, int y, int z) {
        TileEntity ret = world.getTileEntity(x, y, z);
        if (ret instanceof BigPostPostTile) {
            return (BigPostPostTile) ret;
        } else {
            return null;
        }
    }

    @Override
    public void clickWrench(Object hitObj, SuperPostPostTile superTile, EntityPlayerMP player, int x, int y, int z) {
        BigHit hit = (BigHit) hitObj;
        BigBaseInfo tilebases = ((BigPostPostTile) superTile).getBases();
        if (hit.target == BigHitTarget.BASE) {
            tilebases.sign.rot(-15, x, z);
        }
    }

    @Override
    public void rightClickWrench(Object hitObj, SuperPostPostTile superTile, EntityPlayerMP player, int x, int y,
        int z) {
        BigHit hit = (BigHit) hitObj;
        BigBaseInfo tilebases = ((BigPostPostTile) superTile).getBases();
        if (hit.target == BigHitTarget.BASE) {
            tilebases.sign.rot(15, x, z);
        }
    }

    @Override
    public void shiftClickWrench(Object hitObj, SuperPostPostTile superTile, EntityPlayerMP player, int x, int y,
        int z) {
        BigHit hit = (BigHit) hitObj;
        BigBaseInfo tilebases = ((BigPostPostTile) superTile).getBases();
        if (hit.target == BigHitTarget.BASE) {
            tilebases.sign.flip = !tilebases.sign.flip;
        }
    }

    @Override
    public void clickBrush(Object hitObj, SuperPostPostTile superTile, EntityPlayerMP player, int x, int y, int z) {
        NetworkHandler.netWrap.sendTo(new OpenGuiMessage(Signpost.GuiPostBrushID, x, y, z), player);
    }

    @Override
    public void rightClickBrush(Object hitObj, SuperPostPostTile superTile, EntityPlayerMP player, int x, int y,
        int z) {
        BigBaseInfo tilebases = ((BigPostPostTile) superTile).getBases();
        if (tilebases.awaitingPaint && tilebases.paintObject != null) {
            tilebases.paintObject = null;
            tilebases.awaitingPaint = false;
        } else {
            BigHit hit = (BigHit) hitObj;
            tilebases.awaitingPaint = true;
            if (hit.target == BigHitTarget.POST) {
                tilebases.paintObject = tilebases;
            } else if (hit.target == BigHitTarget.BASE) {
                tilebases.paintObject = tilebases.sign;
            } else {
                tilebases.paintObject = null;
                tilebases.awaitingPaint = false;
            }
        }
    }

    public void clickCalibratedWrench(Object hitObj, SuperPostPostTile superTile, EntityPlayerMP player, int x, int y,
        int z) {
        if (((BigHit) hitObj).target.equals(BigHitTarget.BASE)) {
            Sign sign = ((BigPostPostTile) superTile).getBases().sign;
            if (sign != null) {
                sign.rotation = (sign.flip ? 90 : 270) - (int) (player.rotationYawHead);
            }
        }
    }

    public void rightClickCalibratedWrench(Object hitObj, SuperPostPostTile superTile, EntityPlayerMP player, int x,
        int y, int z) {
        BigHit hit = (BigHit) hitObj;
        if (hit.target.equals(BigHitTarget.BASE)) {
            NetworkHandler.netWrap.sendTo(new OpenGuiMessage(Signpost.GuiPostRotationID, x, y, z), player);
        }
    }

    public void shiftClickCalibratedWrench(Object hitObj, SuperPostPostTile superTile, EntityPlayerMP player, int x,
        int y, int z) {
        if (((BigHit) hitObj).target.equals(BigHitTarget.BASE)) {
            Sign sign = ((BigPostPostTile) superTile).getBases().sign;
            if (sign != null) {
                sign.rotation = (sign.flip ? 270 : 90) - (int) (player.rotationYawHead);
            }
        }
    }

    @Override
    public void click(Object hitObj, SuperPostPostTile superTile, EntityPlayerMP player, int x, int y, int z) {
        BigHit hit = (BigHit) hitObj;
        BigBaseInfo tilebases = ((BigPostPostTile) superTile).getBases();
        if (hit.target == BigHitTarget.BASE) {
            if (tilebases.sign.overlay != null) {
                player.inventory.addItemStackToInventory(new ItemStack(tilebases.sign.overlay.item, 1));
            }
        }
        for (OverlayType now : OverlayType.values()) {
            if (player.getHeldItem()
                .getItem()
                .getClass() == now.item.getClass()) {
                if (hit.target == BigHitTarget.BASE) {
                    tilebases.sign.overlay = now;
                }
                player.inventory.consumeInventoryItem(now.item);
                return;
            }
        }
        if (hit.target == BigHitTarget.BASE) {
            tilebases.sign.overlay = null;
        }
    }

    @Override
    public void rightClick(Object hitObj, SuperPostPostTile superTile, EntityPlayerMP player, int x, int y, int z) {
        BigHit hit = (BigHit) hitObj;
        if (hit.target != BigHitTarget.POST) {
            if (ClientConfigStorage.INSTANCE.deactivateTeleportation()) {
                return;
            }
            BigPostPostTile tile = (BigPostPostTile) superTile;
            BaseInfo destination = tile.getBases().sign.base;
            if (destination != null) {
                if (destination.teleportPosition == null) {
                    NetworkHandler.netWrap.sendTo(new ChatMessage("signpost.noTeleport"), player);
                } else {
                    int stackSize = PostHandler.getStackSize(tile.toPos(), destination.teleportPosition);
                    if (PostHandler.canPay(player, tile.toPos(), destination.teleportPosition)) {
                        PostHandler.teleportMe(destination, player, stackSize);
                    } else {
                        String[] keyword = { "<itemName>", "<amount>" };
                        String[] replacement = { ClientConfigStorage.INSTANCE.getCost()
                            .getUnlocalizedName() + ".name", "" + stackSize };
                        NetworkHandler.netWrap
                            .sendTo(new ChatMessage("signpost.payment", keyword, replacement), player);
                    }
                }
            }
        } else {
            if (!canUse(player, superTile)) return;
            NetworkHandler.netWrap.sendTo(new OpenGuiMessage(Signpost.GuiBigPostID, x, y, z), player);
            NetworkHandler.netWrap.sendTo(
                new SendAllWaystoneNamesMessage(
                    PostHandler.getAllWaystones()
                        .select(new Function<BaseInfo, String>() {

                            @Override
                            public String apply(BaseInfo b) {
                                return b.getName();
                            }
                        })),
                player);
        }
    }

    @Override
    public void shiftClick(Object hitObj, SuperPostPostTile superTile, EntityPlayerMP player, int x, int y, int z) {
        BigHit hit = (BigHit) hitObj;
        BigBaseInfo tilebases = ((BigPostPostTile) superTile).getBases();
        if (hit.target == BigHitTarget.BASE) {
            tilebases.sign.point = !tilebases.sign.point;
        }
    }

    @Override
    public void clickBare(Object hitObj, SuperPostPostTile superTile, EntityPlayerMP player, int x, int y, int z) {
        BigHit hit = (BigHit) hitObj;
        BigBaseInfo tilebases = ((BigPostPostTile) superTile).getBases();
        if (hit.target == BigHitTarget.BASE) {
            tilebases.sign.point = !tilebases.sign.point;
        }
    }

    @Override
    public void shiftClickBare(Object hitObj, SuperPostPostTile superTile, EntityPlayerMP player, int x, int y, int z) {
        BigHit hit = (BigHit) hitObj;
        shiftClick(hitObj, superTile, player, x, y, z);
    }

    @Override
    public void sendPostBasesToAll(SuperPostPostTile superTile) {
        BigPostPostTile tile = (BigPostPostTile) superTile;
        BigBaseInfo tilebases = tile.getBases();
        NetworkHandler.netWrap.sendToAll(new SendBigPostBasesMessage(tile, tilebases));
    }

    @Override
    public void sendPostBasesToServer(SuperPostPostTile superTile) {
        BigPostPostTile tile = (BigPostPostTile) superTile;
        BigBaseInfo tilebases = tile.getBases();
        NetworkHandler.netWrap.sendToServer(new SendBigPostBasesMessage(tile, tilebases));
    }

    @Override
    public Object getHitTarget(World world, int x, int y, int z, EntityPlayer player) {
        Vec3 head = Vec3.createVectorHelper(player.posX, player.posY, player.posZ);
        head.yCoord += player.getEyeHeight();
        if (player.isSneaking()) head.yCoord -= 0.08;
        Vec3 look = player.getLookVec();
        BigPostPostTile tile = getWaystonePostTile(world, x, y, z);
        BigBaseInfo bases = tile.getBases();
        DDDVector rotPos = new DDDVector(x + 0.5, y + 0.5, z + 0.5);
        DDDVector signPos;
        DDDVector edges = new DDDVector(1.4375, 0.75, 0.0625);

        if (bases.sign.flip) {
            signPos = new DDDVector(x - 0.375, y + 0.1875, z + 0.625);
        } else {
            signPos = new DDDVector(x - 0.0625, y + 0.1875, z + 0.625);
        }
        Cuboid sign = new Cuboid(signPos, edges, bases.sign.calcRot(x, z), rotPos);
        Cuboid post = new Cuboid(new DDDVector(x + 0.375, y, z + 0.375), new DDDVector(0.25, 1, 0.25), 0);
        Cuboid waystone = new Cuboid(new DDDVector(x + 0.25, y, z + 0.25), new DDDVector(0.5, 0.5, 0.5), 0);

        DDDVector start = new DDDVector(head.xCoord, head.yCoord, head.zCoord);
        DDDVector end = start.add(new DDDVector(look.xCoord, look.yCoord, look.zCoord));
        Intersect signHit = sign.traceLine(start, end, true);
        Intersect postHit = post.traceLine(start, end, true);
        Intersect waystoneHit = waystone.traceLine(start, end, true);
        double signDist = signHit.exists && bases.hasSignBoard() ? signHit.pos.distance(start) : Double.MAX_VALUE;
        double postDist = postHit.exists ? postHit.pos.distance(start) : Double.MAX_VALUE / 2;
        double waystoneDist = waystoneHit.exists && tile.isWaystone() ? waystoneHit.pos.distance(start)
            : Double.MAX_VALUE;
        double dist;
        BigHitTarget target;
        DDDVector pos;
        dist = signDist;
        pos = signHit.pos;
        target = BigHitTarget.BASE;
        if (waystoneDist < dist) {
            dist = waystoneDist;
            pos = waystoneHit.pos;
            target = BigHitTarget.STONE;
        }
        if (postDist < dist) {
            dist = postDist;
            pos = postHit.pos;
            target = BigHitTarget.POST;
        }
        return new BigHit(target, pos);
    }

    public int getRenderType() {
        return -1;
    }

    public boolean renderAsNormalBlock() {
        return false;
    }

    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public Paintable getPaintableByHit(SuperPostPostTile tile, Object hit) {
        switch ((BigHitTarget) hit) {
            case BASE:
                return ((BigPostPostTile) tile).getBases().sign;
            case POST:
                return ((BigPostPostTile) tile).getBases();
            default:
                return null;
        }
    }

    public static BigPostPostTile getTile(World world, int x, int y, int z) {
        TileEntity ret = world.getTileEntity(x, y, z);
        if (ret instanceof BigPostPostTile) {
            return (BigPostPostTile) ret;
        } else {
            return null;
        }
    }

    @Override
    protected boolean isHitWaystone(Object hitObj) {
        BigHit hit = (BigHit) hitObj;
        return hit.target == BigHitTarget.STONE;
    }

}
