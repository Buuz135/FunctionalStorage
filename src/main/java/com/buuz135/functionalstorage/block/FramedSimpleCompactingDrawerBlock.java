package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.FramedSimpleCompactingDrawerTile;
import com.buuz135.functionalstorage.block.tile.SimpleCompactingDrawerTile;
import com.buuz135.functionalstorage.util.StorageTags;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
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
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class FramedSimpleCompactingDrawerBlock extends SimpleCompactingDrawerBlock {
    public FramedSimpleCompactingDrawerBlock(String name) {
        super(name, Properties.copy(Blocks.STONE).noOcclusion().isViewBlocking((p_61036_, p_61037_, p_61038_) -> false));
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<SimpleCompactingDrawerTile> getTileEntityFactory() {
        return (blockPos, state) -> new FramedSimpleCompactingDrawerTile(this, (BlockEntityType<SimpleCompactingDrawerTile>) FunctionalStorage.FRAMED_SIMPLE_COMPACTING_DRAWER.getValue().get(), blockPos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState p_49849_, @Nullable LivingEntity p_49850_, ItemStack stack) {
        super.setPlacedBy(level, pos, p_49849_, p_49850_, stack);
        TileUtil.getTileEntity(level, pos, FramedSimpleCompactingDrawerTile.class).ifPresent(framedDrawerTile -> {
            framedDrawerTile.setFramedDrawerModelData(FramedDrawerBlock.getDrawerModelData(stack));
        });
    }


    @Override
    public List<ItemStack> getDrops(BlockState p_60537_, LootParams.Builder builder) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        ItemStack stack = new ItemStack(this);
        BlockEntity drawerTile = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (drawerTile instanceof FramedSimpleCompactingDrawerTile framedDrawerTile) {
            if (!framedDrawerTile.isEverythingEmpty()) {
                stack.getOrCreateTag().put("Tile", drawerTile.saveWithoutMetadata());
            }
            if (framedDrawerTile.getFramedDrawerModelData() != null) {
                stack.getOrCreateTag().put("Style", framedDrawerTile.getFramedDrawerModelData().serializeNBT());
            }
            if (framedDrawerTile.isLocked()) {
                stack.getOrCreateTag().putBoolean("Locked", framedDrawerTile.isLocked());
            }
        }
        stacks.add(stack);
        return stacks;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof FramedSimpleCompactingDrawerTile framedDrawerTile && framedDrawerTile.getFramedDrawerModelData() != null && !framedDrawerTile.getFramedDrawerModelData().getDesign().isEmpty()) {
            ItemStack stack = new ItemStack(this);
            stack.getOrCreateTag().put("Style", framedDrawerTile.getFramedDrawerModelData().serializeNBT());
            return stack;
        }
        return super.getCloneItemStack(state, target, level, pos, player);
    }

    @Override
    public void registerRecipe(Consumer<FinishedRecipe> consumer) {
        TitaniumShapedRecipeBuilder.shapedRecipe(this)
                .pattern("SSS").pattern("SDP").pattern("SIS")
                .define('S', Items.IRON_NUGGET)
                .define('P', Blocks.PISTON)
                .define('D', StorageTags.DRAWER)
                .define('I', Tags.Items.INGOTS_IRON)
                .save(consumer);
    }

    @Override
    public void appendHoverText(ItemStack p_49816_, @Nullable BlockGetter p_49817_, List<Component> components, TooltipFlag p_49819_) {
        components.add(Component.translatable("frameddrawer.use").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(p_49816_, p_49817_, components, p_49819_);
    }
}
