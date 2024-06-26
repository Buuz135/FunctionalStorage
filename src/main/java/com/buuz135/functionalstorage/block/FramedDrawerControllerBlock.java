package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.FramedDrawerControllerTile;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.util.StorageTags;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

public class FramedDrawerControllerBlock extends StorageControllerBlock<FramedDrawerControllerTile> implements FramedBlock {
    public FramedDrawerControllerBlock() {
        super("framed_storage_controller", Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion().isViewBlocking(((p_61036_, p_61037_, p_61038_) -> false)), FramedDrawerControllerTile.class);
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<?> getTileEntityFactory() {
        return (p_155268_, p_155269_) -> new FramedDrawerControllerTile(this, (BlockEntityType<FramedDrawerControllerTile>) FunctionalStorage.FRAMED_DRAWER_CONTROLLER.type().get(), p_155268_, p_155269_);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState p_49849_, @Nullable LivingEntity p_49850_, ItemStack stack) {
        super.setPlacedBy(level, pos, p_49849_, p_49850_, stack);
        TileUtil.getTileEntity(level, pos, FramedDrawerControllerTile.class).ifPresent(framedDrawerControllerTile -> {
            framedDrawerControllerTile.setFramedDrawerModelData(FramedDrawerBlock.getDrawerModelData(stack));
        });
    }

    @Override
    public List<ItemStack> getDrops(BlockState p_60537_, LootParams.Builder builder) {
        NonNullList stacks = NonNullList.create();
        ItemStack stack = new ItemStack(this);
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);

        if (blockEntity instanceof FramedDrawerControllerTile framedControllerTile) {
            if (framedControllerTile.getFramedDrawerModelData() != null) {
                stack.set(FSAttachments.STYLE, framedControllerTile.getFramedDrawerModelData().serializeNBT(blockEntity.getLevel().registryAccess()));
            }
        }

        stacks.add(stack);
        return stacks;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FramedDrawerControllerTile framedDrawerControllerTile) {
            if (framedDrawerControllerTile.getFramedDrawerModelData() != null) {
                if (!framedDrawerControllerTile.getFramedDrawerModelData().getDesign().isEmpty()) {
                    ItemStack stack = new ItemStack(this);
                    stack.set(FSAttachments.STYLE, framedDrawerControllerTile.getFramedDrawerModelData().serializeNBT(blockEntity.getLevel().registryAccess()));
                    return stack;
                }
            }
        }
        return super.getCloneItemStack(state, target, level, pos, player);
    }

    @Override
    public void registerRecipe(RecipeOutput consumer) {
        TitaniumShapedRecipeBuilder.shapedRecipe(FunctionalStorage.FRAMED_DRAWER_CONTROLLER.block().get())
                .pattern("IBI").pattern("CDC").pattern("IBI")
                .define('I', Items.IRON_NUGGET)
                .define('B', Items.QUARTZ_BLOCK)
                .define('C', StorageTags.DRAWER)
                .define('D', Items.COMPARATOR)
                .save(consumer);
    }
}
