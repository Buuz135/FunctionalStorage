package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.CompactingDrawerTile;
import com.buuz135.functionalstorage.block.tile.CompactingFramedDrawerTile;
import com.buuz135.functionalstorage.block.tile.FramedDrawerTile;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.recipe.CopyComponentsRecipe;
import com.buuz135.functionalstorage.recipe.TagWithoutComponentIngredient;
import com.buuz135.functionalstorage.util.StorageTags;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CompactingFramedDrawerBlock extends CompactingDrawerBlock implements FramedBlock {
    public CompactingFramedDrawerBlock(String name) {
        super(name, Properties.ofFullCopy(Blocks.STONE).noOcclusion().isViewBlocking((p_61036_, p_61037_, p_61038_) -> false));
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<CompactingDrawerTile> getTileEntityFactory() {
        return (blockPos, state) -> new CompactingFramedDrawerTile(this,  (BlockEntityType<CompactingDrawerTile>) FunctionalStorage.FRAMED_COMPACTING_DRAWER.type().get(), blockPos, state);
    }

    @Override
    public void registerRecipe(RecipeOutput consumer) {
        TitaniumShapedRecipeBuilder.shapedRecipe(this)
                .pattern("SSS").pattern("PDP").pattern("SIS")
                .define('S', Items.IRON_NUGGET)
                .define('P', Blocks.PISTON)
                .define('D', new TagWithoutComponentIngredient(StorageTags.DRAWER).toVanilla())
                .define('I', Tags.Items.INGOTS_IRON)
                .save(consumer);

        TitaniumShapedRecipeBuilder.shapedRecipe(this)
                .pattern("S S").pattern("SDS").pattern(" S ")
                .define('S', Tags.Items.NUGGETS_IRON)
                .define('D', FunctionalStorage.COMPACTING_DRAWER)
                .save(CopyComponentsRecipe.output(
                        consumer, 4, FSAttachments.TILE.get()
                ), builtInRegistryHolder().unwrapKey().get().location().withSuffix("_from_simple"));
    }
    
}
