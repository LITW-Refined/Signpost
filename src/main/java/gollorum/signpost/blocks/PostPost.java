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
import gollorum.signpost.blocks.tiles.PostPostTile;
import gollorum.signpost.blocks.tiles.SuperPostPostTile;
import gollorum.signpost.management.ClientConfigStorage;
import gollorum.signpost.management.PostHandler;
import gollorum.signpost.network.NetworkHandler;
import gollorum.signpost.network.messages.ChatMessage;
import gollorum.signpost.network.messages.OpenGuiMessage;
import gollorum.signpost.network.messages.SendAllWaystoneNamesMessage;
import gollorum.signpost.network.messages.SendPostBasesMessage;
import gollorum.signpost.util.BaseInfo;
import gollorum.signpost.util.DoubleBaseInfo;
import gollorum.signpost.util.Paintable;
import gollorum.signpost.util.Sign;
import gollorum.signpost.util.Sign.OverlayType;
import gollorum.signpost.util.math.tracking.Cuboid;
import gollorum.signpost.util.math.tracking.DDDVector;
import gollorum.signpost.util.math.tracking.Intersect;

public class PostPost extends SuperPostPost {

    public PostType type;

    public enum PostType {

        OAK(Material.wood, "sign_oak", "log_oak", Item.getItemFromBlock(Blocks.log), 0),
        SPRUCE(Material.wood, "sign_spruce", "log_spruce", Item.getItemFromBlock(Blocks.log), 1),
        BIRCH(Material.wood, "sign_birch", "log_birch", Item.getItemFromBlock(Blocks.log), 2),
        JUNGLE(Material.wood, "sign_jungle", "log_jungle", Item.getItemFromBlock(Blocks.log), 3),
        ACACIA(Material.wood, "sign_acacia", "log_acacia", Item.getItemFromBlock(Blocks.log2), 0),
        BIGOAK(Material.wood, "sign_big_oak", "log_big_oak", Item.getItemFromBlock(Blocks.log2), 1),
        IRON(Material.iron, "sign_iron", "iron_block", Items.iron_ingot, 0),
        STONE(Material.rock, "sign_stone", "stone", Item.getItemFromBlock(Blocks.stone), 0);

        public Material material;
        public ResourceLocation texture;
        public String blockTexture;
        public String textureMain;
        public ResourceLocation resLocMain;
        public Item baseItem;
        public int metadata;

        PostType(Material material, String texture, String textureMain, Item baseItem, int metadata) {
            this.material = material;
            this.texture = new ResourceLocation(Tags.MODID + ":textures/blocks/" + texture + ".png");
            this.blockTexture = texture;
            this.textureMain = textureMain;
            this.resLocMain = new ResourceLocation("minecraft:textures/blocks/" + textureMain + ".png");
            this.baseItem = baseItem;
            this.metadata = metadata;
        }
    }

    public enum HitTarget {
        BASE1,
        BASE2,
        POST,
        STONE
    }

    public static class Hit {

        public HitTarget target;
        public DDDVector pos;

        public Hit(HitTarget target, DDDVector pos) {
            this.target = target;
            this.pos = pos;
        }
    }

    @Deprecated
    public PostPost() {
        super(Material.wood);
        setBlockName("SignpostPost");
        setCreativeTab(CreativeTabs.tabTransport);
        setBlockTextureName("minecraft:planks_oak");
        this.setHardness(2);
        this.setResistance(100000);
        float f = 15F / 32;
        this.setBlockBounds(0.5f - f, 0.0F, 0.5F - f, 0.5F + f, 1, 0.5F + f);
    }

    public PostPost(PostType type) {
        super(type.material);
        this.type = type;
        setBlockName("SignpostPost" + type.toString());
        setCreativeTab(CreativeTabs.tabTransport);
        setBlockTextureName(Tags.MODID + ":" + type.blockTexture);
        this.setHardness(2);
        this.setResistance(100000);
        float f = 15F / 32;
        this.setBlockBounds(0.5f - f, 0.0F, 0.5F - f, 0.5F + f, 1, 0.5F + f);
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        PostPostTile tile = new PostPostTile(type);
        return tile;
    }

    public static PostPostTile getWaystonePostTile(World world, int x, int y, int z) {
        TileEntity ret = world.getTileEntity(x, y, z);
        if (ret instanceof PostPostTile) {
            return (PostPostTile) ret;
        } else {
            return null;
        }
    }

    @Override
    public void clickWrench(Object hitObj, SuperPostPostTile superTile, EntityPlayerMP player, int x, int y, int z) {
        Hit hit = (Hit) hitObj;
        DoubleBaseInfo tilebases = ((PostPostTile) superTile).getBases();
        if (hit.target == HitTarget.BASE1) {
            tilebases.sign1.rot(-15, x, z);
        } else if (hit.target == HitTarget.BASE2) {
            tilebases.sign2.rot(-15, x, z);
        }
    }

    @Override
    public void rightClickWrench(Object hitObj, SuperPostPostTile superTile, EntityPlayerMP player, int x, int y,
        int z) {
        Hit hit = (Hit) hitObj;
        DoubleBaseInfo tilebases = ((PostPostTile) superTile).getBases();
        if (hit.target == HitTarget.BASE1) {
            tilebases.sign1.rot(15, x, z);
        } else if (hit.target == HitTarget.BASE2) {
            tilebases.sign2.rot(15, x, z);
        }
    }

    @Override
    public void shiftClickWrench(Object hitObj, SuperPostPostTile superTile, EntityPlayerMP player, int x, int y,
        int z) {
        Hit hit = (Hit) hitObj;
        DoubleBaseInfo tilebases = ((PostPostTile) superTile).getBases();
        if (hit.target == HitTarget.BASE1) {
            tilebases.sign1.flip = !tilebases.sign1.flip;
        } else if (hit.target == HitTarget.BASE2) {
            tilebases.sign2.flip = !tilebases.sign2.flip;
        }
    }

    @Override
    public void clickBrush(Object hitObj, SuperPostPostTile superTile, EntityPlayerMP player, int x, int y, int z) {
        NetworkHandler.netWrap.sendTo(new OpenGuiMessage(Signpost.GuiPostBrushID, x, y, z), player);
    }

    @Override
    public void rightClickBrush(Object hitObj, SuperPostPostTile superTile, EntityPlayerMP player, int x, int y,
        int z) {
        DoubleBaseInfo tilebases = ((PostPostTile) superTile).getBases();
        if (tilebases.awaitingPaint && tilebases.paintObject != null) {
            tilebases.paintObject = null;
            tilebases.awaitingPaint = false;
        } else {
            Hit hit = (Hit) hitObj;
            tilebases.awaitingPaint = true;
            if (hit.target == HitTarget.POST) {
                tilebases.paintObject = tilebases;
            } else if (hit.target == HitTarget.BASE1) {
                tilebases.paintObject = tilebases.sign1;
            } else if (hit.target == HitTarget.BASE2) {
                tilebases.paintObject = tilebases.sign2;
            } else {
                tilebases.paintObject = null;
                tilebases.awaitingPaint = false;
            }
        }
    }

    public void clickCalibratedWrench(Object hitObj, SuperPostPostTile superTile, EntityPlayerMP player, int x, int y,
        int z) {
        Sign sign = getSignByHit((Hit) hitObj, (PostPostTile) superTile);
        if (sign != null) {
            sign.rotation = (sign.flip ? 90 : 270) - (int) (player.rotationYawHead);
        }
    }

    public void rightClickCalibratedWrench(Object hitObj, SuperPostPostTile superTile, EntityPlayerMP player, int x,
        int y, int z) {
        Hit hit = (Hit) hitObj;
        if (hit.target.equals(HitTarget.BASE1) || hit.target.equals(HitTarget.BASE2)) {
            NetworkHandler.netWrap.sendTo(new OpenGuiMessage(Signpost.GuiPostRotationID, x, y, z), player);
        }
    }

    public void shiftClickCalibratedWrench(Object hitObj, SuperPostPostTile superTile, EntityPlayerMP player, int x,
        int y, int z) {
        Sign sign = getSignByHit((Hit) hitObj, (PostPostTile) superTile);
        if (sign != null) {
            sign.rotation = (sign.flip ? 270 : 90) - (int) (player.rotationYawHead);
        }
    }

    @Override
    public void click(Object hitObj, SuperPostPostTile superTile, EntityPlayerMP player, int x, int y, int z) {
        Hit hit = (Hit) hitObj;
        PostPostTile tile = (PostPostTile) superTile;
        DoubleBaseInfo tilebases = tile.getBases();
        if (hit.target == HitTarget.BASE1) {
            if (tilebases.sign1.overlay != null) {
                player.inventory.addItemStackToInventory(new ItemStack(tilebases.sign1.overlay.item, 1));
            }
        } else if (hit.target == HitTarget.BASE2) {
            if (tilebases.sign2.overlay != null) {
                player.inventory.addItemStackToInventory(new ItemStack(tilebases.sign2.overlay.item, 1));
            }
        }
        for (OverlayType now : OverlayType.values()) {
            if (player.getHeldItem()
                .getItem()
                .getClass() == now.item.getClass()) {
                if (hit.target == HitTarget.BASE1) {
                    tilebases.sign1.overlay = now;
                } else if (hit.target == HitTarget.BASE2) {
                    tilebases.sign2.overlay = now;
                }
                player.inventory.consumeInventoryItem(now.item);
                NetworkHandler.netWrap.sendToAll(new SendPostBasesMessage(tile, tilebases));
                return;
            }
        }
        if (hit.target == HitTarget.BASE1) {
            tilebases.sign1.overlay = null;
        } else if (hit.target == HitTarget.BASE2) {
            tilebases.sign2.overlay = null;
        }
        NetworkHandler.netWrap.sendToAll(new SendPostBasesMessage(tile, tilebases));
    }

    @Override
    public void rightClick(Object hitObj, SuperPostPostTile superTile, EntityPlayerMP player, int x, int y, int z) {
        Hit hit = (Hit) hitObj;
        PostPostTile tile = (PostPostTile) superTile;
        if (hit.target != HitTarget.POST) {
            if (ClientConfigStorage.INSTANCE.deactivateTeleportation()) {
                return;
            }
            BaseInfo destination = hit.target == HitTarget.BASE1 ? tile.getBases().sign1.base
                : tile.getBases().sign2.base;
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
            NetworkHandler.netWrap.sendTo(new OpenGuiMessage(Signpost.GuiPostID, x, y, z), player);
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
        Hit hit = (Hit) hitObj;
        DoubleBaseInfo tilebases = ((PostPostTile) superTile).getBases();
        if (hit.target == HitTarget.BASE1) {
            tilebases.sign1.point = !tilebases.sign1.point;
        } else if (hit.target == HitTarget.BASE2) {
            tilebases.sign2.point = !tilebases.sign2.point;
        }
    }

    @Override
    public void clickBare(Object hitObj, SuperPostPostTile superTile, EntityPlayerMP player, int x, int y, int z) {
        Hit hit = (Hit) hitObj;
        PostPostTile tile = (PostPostTile) superTile;
        DoubleBaseInfo tilebases = tile.getBases();
        if (hit.target == HitTarget.BASE1) {
            tilebases.sign1.point = !tilebases.sign1.point;
            NetworkHandler.netWrap.sendToAll(new SendPostBasesMessage(tile, tilebases));
        } else if (hit.target == HitTarget.BASE2) {
            tilebases.sign2.point = !tilebases.sign2.point;
            NetworkHandler.netWrap.sendToAll(new SendPostBasesMessage(tile, tilebases));
        }
    }

    @Override
    public void shiftClickBare(Object hitObj, SuperPostPostTile superTile, EntityPlayerMP player, int x, int y, int z) {
        shiftClick(hitObj, superTile, player, x, y, z);
    }

    @Override
    public void sendPostBasesToAll(SuperPostPostTile superTile) {
        PostPostTile tile = (PostPostTile) superTile;
        DoubleBaseInfo tilebases = tile.getBases();
        NetworkHandler.netWrap.sendToAll(new SendPostBasesMessage(tile, tilebases));
    }

    @Override
    public void sendPostBasesToServer(SuperPostPostTile superTile) {
        PostPostTile tile = (PostPostTile) superTile;
        DoubleBaseInfo tilebases = tile.getBases();
        NetworkHandler.netWrap.sendToServer(new SendPostBasesMessage(tile, tilebases));
    }

    @Override
    public Object getHitTarget(World world, int x, int y, int z, EntityPlayer player) {
        Vec3 head = Vec3.createVectorHelper(player.posX, player.posY, player.posZ);
        head.yCoord += player.getEyeHeight();
        if (player.isSneaking()) head.yCoord -= 0.08;
        Vec3 look = player.getLookVec();
        PostPostTile tile = getWaystonePostTile(world, x, y, z);
        DoubleBaseInfo bases = tile.getBases();
        DDDVector rotPos = new DDDVector(x + 0.5, y + 0.5, z + 0.5);
        DDDVector signPos;
        DDDVector edges = new DDDVector(1.4375, 0.375, 0.0625);

        if (bases.sign1.flip) {
            signPos = new DDDVector(x - 0.375, y + 0.5625, z + 0.625);
        } else {
            signPos = new DDDVector(x - 0.0625, y + 0.5625, z + 0.625);
        }
        Cuboid sign1 = new Cuboid(signPos, edges, bases.sign1.calcRot(x, z), rotPos);

        if (bases.sign2.flip) {
            signPos = new DDDVector(x - 0.375, y + 0.0625, z + 0.625);
        } else {
            signPos = new DDDVector(x - 0.0625, y + 0.0625, z + 0.625);
        }
        Cuboid sign2 = new Cuboid(signPos, edges, bases.sign2.calcRot(x, z), rotPos);
        Cuboid post = new Cuboid(new DDDVector(x + 0.375, y, z + 0.375), new DDDVector(0.25, 1, 0.25), 0);
        Cuboid waystone = new Cuboid(new DDDVector(x + 0.25, y, z + 0.25), new DDDVector(0.5, 0.5, 0.5), 0);

        DDDVector start = new DDDVector(head.xCoord, head.yCoord, head.zCoord);
        DDDVector end = start.add(new DDDVector(look.xCoord, look.yCoord, look.zCoord));
        Intersect sign1Hit = sign1.traceLine(start, end, true);
        Intersect sign2Hit = sign2.traceLine(start, end, true);
        Intersect postHit = post.traceLine(start, end, true);
        Intersect waystoneHit = waystone.traceLine(start, end, true);
        double sign1Dist = sign1Hit.exists && bases.hasSignBoard1() ? sign1Hit.pos.distance(start) : Double.MAX_VALUE;
        double sign2Dist = sign2Hit.exists && bases.hasSignBoard2() ? sign2Hit.pos.distance(start) : Double.MAX_VALUE;
        double postDist = postHit.exists ? postHit.pos.distance(start) : Double.MAX_VALUE / 2;
        double waystoneDist = waystoneHit.exists && tile.isWaystone() ? waystoneHit.pos.distance(start)
            : Double.MAX_VALUE;
        double dist;
        HitTarget target;
        DDDVector pos;
        if (sign1Dist < sign2Dist) {
            dist = sign1Dist;
            pos = sign1Hit.pos;
            target = HitTarget.BASE1;
        } else {
            dist = sign2Dist;
            pos = sign2Hit.pos;
            target = HitTarget.BASE2;
        }
        if (waystoneDist <= dist) {
            dist = waystoneDist;
            pos = waystoneHit.pos;
            target = HitTarget.STONE;
        }
        if (postDist < dist) {
            dist = postDist;
            pos = postHit.pos;
            target = HitTarget.POST;
        }
        return new Hit(target, pos);
    }

    @Override
    public Paintable getPaintableByHit(SuperPostPostTile tile, Object hit) {
        switch ((HitTarget) hit) {
            case BASE1:
                return ((PostPostTile) tile).getBases().sign1;
            case BASE2:
                return ((PostPostTile) tile).getBases().sign2;
            case POST:
                return ((PostPostTile) tile).getBases();
            default:
                return null;
        }
    }

    public static PostPostTile getTile(World world, int x, int y, int z) {
        TileEntity ret = world.getTileEntity(x, y, z);
        if (ret instanceof PostPostTile) {
            return (PostPostTile) ret;
        } else {
            return null;
        }
    }

    public Sign getSignByHit(Hit hit, PostPostTile tile) {
        if (hit.target.equals(HitTarget.BASE1)) {
            return tile.getBases().sign1;
        } else if (hit.target.equals(HitTarget.BASE2)) {
            return tile.getBases().sign2;
        } else {
            return null;
        }
    }

    @Override
    protected boolean isHitWaystone(Object hitObj) {
        return ((Hit) hitObj).target.equals(HitTarget.STONE);
    }
}
