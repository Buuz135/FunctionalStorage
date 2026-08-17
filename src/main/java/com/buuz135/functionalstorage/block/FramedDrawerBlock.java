package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.DrawerTile;
import com.buuz135.functionalstorage.block.tile.FramedDrawerTile;
import com.buuz135.functionalstorage.util.CustomFramedDrawerModelData;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.recipe.CopyComponentsRecipe;
import com.buuz135.functionalstorage.util.DrawerWoodType;
import com.hrznstudio.titanium.module.BlockWithTile;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.crafting.DifferenceIngredient;

import java.util.HashMap;
import java.util.stream.StreamSupport;

public class FramedDrawerBlock extends DrawerBlock implements FramedBlock {

    public FramedDrawerBlock(FunctionalStorage.DrawerType type) {
        super(DrawerWoodType.FRAMED, type, Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion().isViewBlocking((p_61036_, p_61037_, p_61038_) -> false));
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<DrawerTile> getTileEntityFactory() {
        return (blockPos, state) -> new FramedDrawerTile(this, (BlockEntityType<DrawerTile>) FunctionalStorage.DRAWER_TYPES.get(this.getType()).stream().filter(registryObjectRegistryObjectPair -> registryObjectRegistryObjectPair.getBlock() == this).map(BlockWithTile::type).findFirst().get().get(), blockPos, state, this.getType());
    }

    public static CustomFramedDrawerModelData getDrawerModelData(ItemStack stack){
        if (stack.has(FSAttachments.STYLE)) {
            CompoundTag tag = stack.getOrDefault(FSAttachments.STYLE, new CompoundTag());
            if (tag.isEmpty()) return null;
            HashMap<String, Item> data = new HashMap<>();
            data.put("particle", BuiltInRegistries.ITEM.getValue(com.buuz135.functionalstorage.util.Utils.resourceLocation(tag.getStringOr("particle", "minecraft:air"))));
            data.put("front", BuiltInRegistries.ITEM.getValue(com.buuz135.functionalstorage.util.Utils.resourceLocation(tag.getStringOr("front", "minecraft:air"))));
            data.put("side", BuiltInRegistries.ITEM.getValue(com.buuz135.functionalstorage.util.Utils.resourceLocation(tag.getStringOr("side", "minecraft:air"))));
            data.put("front_divider", BuiltInRegistries.ITEM.getValue(com.buuz135.functionalstorage.util.Utils.resourceLocation(tag.getStringOr("front_divider", "minecraft:air"))));
            return new CustomFramedDrawerModelData(data);
        }
        return null;
    }

    public static ItemStack fill(ItemStack first, ItemStack second, ItemStack drawer, ItemStack divider){
        drawer = drawer.copyWithCount(1);
        CompoundTag style = drawer.getOrDefault(FSAttachments.STYLE, new CompoundTag());
        style.putString("particle", BuiltInRegistries.ITEM.getKey(first.getItem()).toString());
        style.putString("side", BuiltInRegistries.ITEM.getKey(first.getItem()).toString());
        style.putString("front", BuiltInRegistries.ITEM.getKey(second.getItem()).toString());
        if (divider.isEmpty()){
            style.putString("front_divider", BuiltInRegistries.ITEM.getKey(first.getItem()).toString());
        } else {
            style.putString("front_divider", BuiltInRegistries.ITEM.getKey(divider.getItem()).toString());
        }
        drawer.set(FSAttachments.STYLE, style);
        return drawer;
    }

    @Override
    public void registerRecipe(RecipeOutput consumer) {
        if (this.getType() == FunctionalStorage.DrawerType.X_1) {
            TitaniumShapedRecipeBuilder.shapedRecipe(this)
                    .pattern("PPP").pattern("PCP").pattern("PPP")
                    .define('P', Tags.Items.NUGGETS_IRON)
                    .define('C', Tags.Items.CHESTS_WOODEN)
                    .save(consumer);

            simpleToFramed(consumer, TitaniumShapedRecipeBuilder.shapedRecipe(this)
                    .pattern("PPP").pattern("PCP").pattern("PPP")
                    .define('P', Tags.Items.NUGGETS_IRON));
        } else if (this.getType() == FunctionalStorage.DrawerType.X_2){
            TitaniumShapedRecipeBuilder.shapedRecipe(this, 2)
                    .pattern("PCP").pattern("PPP").pattern("PCP")
                    .define('P', Tags.Items.NUGGETS_IRON)
                    .define('C', Tags.Items.CHESTS_WOODEN)
                    .save(consumer);

            simpleToFramed(consumer, TitaniumShapedRecipeBuilder.shapedRecipe(this)
                    .pattern(" P ").pattern("PCP").pattern(" P ")
                    .define('P', Tags.Items.NUGGETS_IRON));
        } else if (this.getType() == FunctionalStorage.DrawerType.X_4){
            TitaniumShapedRecipeBuilder.shapedRecipe(this, 4)
                    .pattern("CPC").pattern("PPP").pattern("CPC")
                    .define('P', Tags.Items.NUGGETS_IRON)
                    .define('C', Tags.Items.CHESTS_WOODEN)
                    .save(consumer);

            simpleToFramed(consumer, TitaniumShapedRecipeBuilder.shapedRecipe(this)
                    .pattern("PCP")
                    .define('P', Tags.Items.NUGGETS_IRON),1);
        }
    }

    private void simpleToFramed(RecipeOutput output, TitaniumShapedRecipeBuilder recipe,int copyIndex) {
        recipe.define('C', DifferenceIngredient.of(
                        Ingredient.of(StreamSupport.stream(BuiltInRegistries.ITEM.getTagOrEmpty(getType().getTag()).spliterator(), false).map(holder -> holder.value())),
                        Ingredient.of(this)
                ))
                .setName(builtInRegistryHolder().unwrapKey().get().identifier().withSuffix("_from_simple"))
                .save(CopyComponentsRecipe.output(output, copyIndex, FSAttachments.TILE.get()));
    }
    private void simpleToFramed(RecipeOutput output, TitaniumShapedRecipeBuilder recipe) {
        simpleToFramed(output, recipe, 4);
    }

}
