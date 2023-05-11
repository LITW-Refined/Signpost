package gollorum.signpost;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;

import cpw.mods.fml.common.registry.GameRegistry;
import gollorum.signpost.blocks.BaseModelPost;
import gollorum.signpost.blocks.BasePost;
import gollorum.signpost.blocks.BigPostPost;
import gollorum.signpost.blocks.BigPostPost.BigPostType;
import gollorum.signpost.blocks.ItemBlockWithMetaFacing;
import gollorum.signpost.blocks.PostPost;
import gollorum.signpost.blocks.PostPost.PostType;
import gollorum.signpost.management.ClientConfigStorage;
import gollorum.signpost.util.collections.CollectionUtils;

public class BlockHandler {

    public static BasePost base = new BasePost();
    public static BaseModelPost[] basemodels = BaseModelPost.createAll();

    public PostPost post_oak = new PostPost(PostType.OAK);
    public PostPost post_spruce = new PostPost(PostType.SPRUCE);
    public PostPost post_birch = new PostPost(PostType.BIRCH);
    public PostPost post_jungle = new PostPost(PostType.JUNGLE);
    public PostPost post_acacia = new PostPost(PostType.ACACIA);
    public PostPost post_big_oak = new PostPost(PostType.BIGOAK);
    public PostPost post_iron = new PostPost(PostType.IRON);
    public PostPost post_stone = new PostPost(PostType.STONE);
    public PostPost[] posts = { post_oak, post_spruce, post_birch, post_jungle, post_acacia, post_big_oak, post_iron,
        post_stone };

    public BigPostPost bigpost_oak = new BigPostPost(BigPostType.OAK);
    public BigPostPost bigpost_spruce = new BigPostPost(BigPostType.SPRUCE);
    public BigPostPost bigpost_birch = new BigPostPost(BigPostType.BIRCH);
    public BigPostPost bigpost_jungle = new BigPostPost(BigPostType.JUNGLE);
    public BigPostPost bigpost_acacia = new BigPostPost(BigPostType.ACACIA);
    public BigPostPost bigpost_big_oak = new BigPostPost(BigPostType.BIGOAK);
    public BigPostPost bigpost_iron = new BigPostPost(BigPostType.IRON);
    public BigPostPost bigpost_stone = new BigPostPost(BigPostType.STONE);
    public BigPostPost[] bigposts = { bigpost_oak, bigpost_spruce, bigpost_birch, bigpost_jungle, bigpost_acacia,
        bigpost_big_oak, bigpost_iron, bigpost_stone };

    public List<BaseModelPost> baseModelsForCrafting() {
        List<BaseModelPost> allModels = Arrays.asList(basemodels);
        ArrayList<BaseModelPost> allowedModels = new ArrayList<>();
        for (final String model : ClientConfigStorage.INSTANCE.getAllowedCraftingModels()) {
            BaseModelPost block = CollectionUtils.find(allModels, m -> m.type.name.equals(model));
            if (block != null) allowedModels.add(block);
        }
        return allowedModels;
    }

    public List<BaseModelPost> baseModelsForVillages() {
        List<BaseModelPost> allModels = Arrays.asList(basemodels);
        ArrayList<BaseModelPost> allowedModels = new ArrayList<>();
        for (final String model : ClientConfigStorage.INSTANCE.getAllowedVillageModels()) {
            BaseModelPost block = CollectionUtils.find(allModels, m -> m.type.name.equals(model));
            if (block != null) allowedModels.add(block);
        }
        return allowedModels;
    }

    public void registerBlocks() {
        GameRegistry.registerBlock(base, "SignpostBase");
        for (BaseModelPost basemodel : basemodels) {
            GameRegistry
                .registerBlock(basemodel, ItemBlockWithMetaFacing.class, "blockbasemodel" + basemodel.type.getID());
        }
        for (PostPost now : posts) {
            GameRegistry.registerBlock(now, "SignpostPost" + now.type.name());
        }
        for (BigPostPost now : bigposts) {
            GameRegistry.registerBlock(now, "BigSignpostPost" + now.type.name());
        }
    }

    public void registerRecipes() {
        waystoneRecipe();
        for (PostPost now : posts) {
            postRecipe(now);
        }
        for (BigPostPost now : bigposts) {
            bigPostRecipe(now);
        }
    }

    private void waystoneRecipe() {
        HashSet<Object> toDelete = new HashSet<>();
        Item[] outputsToRemove = new Item[basemodels.length + 1];
        for (int i = 0; i < basemodels.length; i++) outputsToRemove[i] = Item.getItemFromBlock(basemodels[i]);
        outputsToRemove[outputsToRemove.length - 1] = Item.getItemFromBlock(base);
        for (Object now : CraftingManager.getInstance()
            .getRecipeList()) {
            if (!(now instanceof IRecipe)) continue;
            ItemStack output = ((IRecipe) now).getRecipeOutput();
            if (output == null) continue;
            Item item = output.getItem();
            if (item == null) continue;
            for (Item item2 : outputsToRemove) if (item.equals(item2)) {
                toDelete.add(now);
                break;
            }
        }
        CraftingManager.getInstance()
            .getRecipeList()
            .removeAll(toDelete);

        if (ClientConfigStorage.INSTANCE.getSecurityLevelWaystone().canCraft
            && !ClientConfigStorage.INSTANCE.deactivateTeleportation()) {
            // spotless:off
            
            switch (ClientConfigStorage.INSTANCE.getWaysRec()) {
                case EXPENSIVE:
                    GameRegistry.addRecipe(new ItemStack(base, 1),
                            "SSS",
                            "PPP",
                            "SSS",
                            'S', Blocks.stone,
                            'P', Items.ender_pearl);
                    break;
                case VERY_EXPENSIVE:
                    GameRegistry.addRecipe(new ItemStack(base, 1),
                            "SSS",
                            " P ",
                            "SSS",
                            'S', Blocks.stone,
                            'P', Items.nether_star);
                    break;
                case DEACTIVATED:
                    break;
                default:
                    GameRegistry.addRecipe(new ItemStack(base, 1),
                            "SSS",
                            " P ",
                            "SSS",
                            'S', Blocks.cobblestone,
                            'P', Items.ender_pearl);
                    break;
            }
            
            // spotless:on

            List<BaseModelPost> allowedModels = baseModelsForCrafting();
            if (allowedModels.size() > 0) {
                GameRegistry.addShapelessRecipe(new ItemStack(allowedModels.get(0), 1), base);
                for (int i = 1; i < allowedModels.size(); i++) {
                    GameRegistry.addShapelessRecipe(new ItemStack(allowedModels.get(i), 1), allowedModels.get(i - 1));
                }
                GameRegistry.addShapelessRecipe(new ItemStack(base, 1), allowedModels.get(allowedModels.size() - 1));
            }
        }
    }

    private void postRecipe(PostPost post) {
        Object toDelete = null;
        for (Object now : CraftingManager.getInstance()
            .getRecipeList()) {
            if (now == null || !(now instanceof IRecipe)
                || ((IRecipe) now).getRecipeOutput() == null
                || ((IRecipe) now).getRecipeOutput()
                    .getItem() == null) {
                continue;
            }
            if (((IRecipe) now).getRecipeOutput()
                .getItem()
                .equals(Item.getItemFromBlock(post))) {
                toDelete = now;
                break;
            }
        }
        CraftingManager.getInstance()
            .getRecipeList()
            .remove(toDelete);

        // spotless:off

        if (ClientConfigStorage.INSTANCE.getSecurityLevelSignpost().canCraft) {
            switch (ClientConfigStorage.INSTANCE.getSignRec()) {
                case EXPENSIVE:
                    GameRegistry.addRecipe(new ItemStack(post, 1),
                            "A",
                            "P",
                            "B",
                            'A', Items.sign,
                            'B', new ItemStack(post.type.baseItem, 1, post.type.metadata),
                            'P', Items.ender_pearl);
                    break;
                case VERY_EXPENSIVE:
                    GameRegistry.addRecipe(new ItemStack(post, 1),
                            "A",
                            "P",
                            "B",
                            'A', Items.sign,
                            'B', new ItemStack(post.type.baseItem, 1, post.type.metadata),
                            'P', Items.nether_star);
                    break;
                case DEACTIVATED:
                    break;
                default:
                    GameRegistry.addRecipe(new ItemStack(post, 4),
                            "A",
                            "A",
                            "B",
                            'A', Items.sign,
                            'B', new ItemStack(post.type.baseItem, 1, post.type.metadata));
                    break;
            }
        }

        // spotless:on
    }

    private void bigPostRecipe(BigPostPost post) {
        Object toDelete = null;
        for (Object now : CraftingManager.getInstance()
            .getRecipeList()) {
            if (now == null || !(now instanceof IRecipe)
                || ((IRecipe) now).getRecipeOutput() == null
                || ((IRecipe) now).getRecipeOutput()
                    .getItem() == null) {
                continue;
            }
            if (((IRecipe) now).getRecipeOutput()
                .getItem()
                .equals(Item.getItemFromBlock(post))) {
                toDelete = now;
                break;
            }
        }
        CraftingManager.getInstance()
            .getRecipeList()
            .remove(toDelete);

        // spotless:off

        if (ClientConfigStorage.INSTANCE.getSecurityLevelSignpost().canCraft) {
            switch (ClientConfigStorage.INSTANCE.getSignRec()) {
                case EXPENSIVE:
                    GameRegistry.addRecipe(new ItemStack(post, 1),
                            "AAA",
                            "APA",
                            " B ",
                            'A', Items.sign,
                            'B', new ItemStack(post.type.baseItem, 1, post.type.metadata),
                            'P', Items.ender_pearl);
                    break;
                case VERY_EXPENSIVE:
                    GameRegistry.addRecipe(new ItemStack(post, 1),
                            "AAA",
                            "APA",
                            " B ",
                            'A', Items.sign,
                            'B', new ItemStack(post.type.baseItem, 1, post.type.metadata),
                            'P', Items.nether_star);
                    break;
                case DEACTIVATED:
                    break;
                default:
                    GameRegistry.addRecipe(new ItemStack(post, 4),
                            "AAA",
                            "AAA",
                            " B ",
                            'A', Items.sign,
                            'B', new ItemStack(post.type.baseItem, 1, post.type.metadata));
                    break;
            }
        }

        // spotless:on
    }
}
