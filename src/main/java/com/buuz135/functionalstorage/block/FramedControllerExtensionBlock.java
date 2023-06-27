package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.FramedControllerExtensionTile;
import com.buuz135.functionalstorage.block.tile.FramedDrawerControllerTile;
import com.buuz135.functionalstorage.util.StorageTags;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class FramedControllerExtensionBlock extends StorageControllerExtensionBlock<FramedControllerExtensionTile>{

    public FramedControllerExtensionBlock() {
        super("framed_controller_extension", Properties.copy(Blocks.IRON_BLOCK).noOcclusion().isViewBlocking(((p_61036_, p_61037_, p_61038_) -> false)), FramedControllerExtensionTile.class);
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<?> getTileEntityFactory() {
        return ((p_155268_, p_155269_) -> new FramedControllerExtensionTile(this, (BlockEntityType<FramedControllerExtensionTile>) FunctionalStorage.FRAMED_CONTROLLER_EXTENSION.getRight().get(), p_155268_, p_155269_));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState p_49849_, @Nullable LivingEntity p_49850_, ItemStack stack) {
        super.setPlacedBy(level, pos, p_49849_, p_49850_, stack);
        TileUtil.getTileEntity(level, pos, FramedControllerExtensionTile.class).ifPresent(framedControllerExtensionTile -> {
            framedControllerExtensionTile.setFramedDrawerModelData(FramedDrawerBlock.getDrawerModelData(stack));
        });
    }

    @Override
    public List<ItemStack> getDrops(BlockState p_60537_, LootParams.Builder builder) {
        NonNullList stacks = NonNullList.create();
        ItemStack stack = new ItemStack(this);
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);

        if(blockEntity instanceof FramedControllerExtensionTile framedControllerExtensionTile)
        {
            if(framedControllerExtensionTile.getFramedDrawerModelData() != null)
            {
                stack.getOrCreateTag().put("Style", framedControllerExtensionTile.getFramedDrawerModelData().serializeNBT());
            }
        }

        stacks.add(stack);
        return stacks;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if(blockEntity instanceof FramedControllerExtensionTile framedControllerExtensionTile)
        {
            if(framedControllerExtensionTile.getFramedDrawerModelData() != null)
            {
                if(!framedControllerExtensionTile.getFramedDrawerModelData().getDesign().isEmpty())
                {
                    ItemStack stack = new ItemStack(this);
                    stack.getOrCreateTag().put("Style", framedControllerExtensionTile.getFramedDrawerModelData().serializeNBT());
                    return stack;
                }
            }
        }
        return super.getCloneItemStack(state, target, level, pos, player);
    }

    @Override
    public void registerRecipe(Consumer<FinishedRecipe> consumer) {
        TitaniumShapedRecipeBuilder.shapedRecipe(FunctionalStorage.FRAMED_CONTROLLER_EXTENSION.getLeft().get())
                .pattern("IBI").pattern("CDC").pattern("IBI")
                .define('I', Items.IRON_NUGGET)
                .define('B', Tags.Items.STORAGE_BLOCKS_QUARTZ)
                .define('C', StorageTags.DRAWER)
                .define('D', Items.REPEATER)
                .save(consumer);
    }
}
