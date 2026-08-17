package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.FluidDrawerTile;
import com.buuz135.functionalstorage.inventory.item.FluidDrawerStackItemHandler;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.util.NumberUtils;
import com.buuz135.functionalstorage.util.Utils;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import com.hrznstudio.titanium.tab.TitaniumTab;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class FluidDrawerBlock extends Drawer<FluidDrawerTile>{

    /**
     * Gas rendering
     */

    private final FunctionalStorage.DrawerType type;

    public FluidDrawerBlock(FunctionalStorage.DrawerType type, Properties properties) {
        super("fluid_" + type.getSlots(), properties, FluidDrawerTile.class);
        this.type = type;
        setItemGroup(FunctionalStorage.TAB);
        registerDefaultState(defaultBlockState().setValue(Drawer.FACING_HORIZONTAL_CUSTOM, Direction.NORTH).setValue(DrawerBlock.LOCKED, false));
    }

    private static List<VoxelShape> getShapes(BlockState state, BlockGetter source, BlockPos pos, FunctionalStorage.DrawerType type) {
        List<VoxelShape> boxes = new ArrayList<>();
        DrawerBlock.CACHED_SHAPES.get(type).get(state.getValue(Drawer.FACING_HORIZONTAL_CUSTOM)).forEach(boxes::add);
        VoxelShape total = Shapes.block();
        boxes.add(total);
        return boxes;
    }
    @Override
    public BlockEntityType.BlockEntitySupplier<FluidDrawerTile> getTileEntityFactory() {
        return (blockPos, state) -> {
            BlockEntityType<FluidDrawerTile> entityType = (BlockEntityType<FluidDrawerTile>) FunctionalStorage.FLUID_DRAWER_1.type().get();
            if (type == FunctionalStorage.DrawerType.X_2) {
                entityType = (BlockEntityType<FluidDrawerTile>) FunctionalStorage.FLUID_DRAWER_2.type().get();
            }
            if (type == FunctionalStorage.DrawerType.X_4) {
                entityType = (BlockEntityType<FluidDrawerTile>) FunctionalStorage.FLUID_DRAWER_4.type().get();
            }
            return new FluidDrawerTile(this, entityType, blockPos, state, type);
        };
    }

    @Override
    public List<VoxelShape> getBoundingBoxes(BlockState state, BlockGetter source, BlockPos pos) {
        return getShapes(state, source, pos, this.type);
    }

    @Override
    public Collection<VoxelShape> getHitShapes(BlockState state) {
        return DrawerBlock.getDefaultHitShapes(this.type, state);
    }

    @Override
    public void registerRecipe(RecipeOutput consumer) {
        if (type == FunctionalStorage.DrawerType.X_1) {
            TitaniumShapedRecipeBuilder.shapedRecipe(this)
                    .pattern("PPP").pattern("PCP").pattern("PPP")
                    .define('P', ItemTags.PLANKS)
                    .define('C', Tags.Items.BUCKETS_EMPTY)
                    .save(consumer);
        }
        if (type == FunctionalStorage.DrawerType.X_2) {
            TitaniumShapedRecipeBuilder.shapedRecipe(this, 2)
                    .pattern("PCP").pattern("PPP").pattern("PCP")
                    .define('P', ItemTags.PLANKS)
                    .define('C', Tags.Items.BUCKETS_EMPTY)
                    .save(consumer);
        }
        if (type == FunctionalStorage.DrawerType.X_4) {
            TitaniumShapedRecipeBuilder.shapedRecipe(this, 4)
                    .pattern("CPC").pattern("PPP").pattern("CPC")
                    .define('P', ItemTags.PLANKS)
                    .define('C', Tags.Items.BUCKETS_EMPTY)
                    .save(consumer);
        }
    }

    public FunctionalStorage.DrawerType getType() {
        return type;
    }

    public void appendFluidHoverText(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
        if (itemStack.has(FSAttachments.TILE)) {
            var tileTag = itemStack.get(FSAttachments.TILE).getCompoundOrEmpty("TitaniumData").getCompoundOrEmpty("fluidHandler");
            tooltip.accept(Component.translatable("drawer.block.contents").withStyle(ChatFormatting.GRAY));
            for (int i = 0; i < type.getSlots(); i++) {
                FluidStack stack = FluidStack.OPTIONAL_CODEC.decode(RegistryOps.create(NbtOps.INSTANCE, Utils.registryAccess()), tileTag.getCompoundOrEmpty(i + "")).getOrThrow().getFirst();
                if (!stack.isEmpty())
                    tooltip.accept(Component.literal(" - " + ChatFormatting.YELLOW + NumberUtils.getFormatedFluidBigNumber(stack.getAmount()) + ChatFormatting.WHITE + " of ").append(stack.getHoverName().copy().withStyle(ChatFormatting.GOLD)));
            }
            var tile = itemStack.get(FSAttachments.TILE).getCompoundOrEmpty("TitaniumData");
            tooltip.accept(Component.translatable("drawer.block.upgrades").withStyle(ChatFormatting.GRAY));
            var anyupgrade = false;
            if (tile.getBooleanOr("isCreative", false)) {
                tooltip.accept(Component.literal("- ").withStyle(ChatFormatting.GRAY).append(Component.translatable("drawer.block.upgrades.is_creative").withStyle(ChatFormatting.LIGHT_PURPLE)));
                anyupgrade = true;
            }
            if (tile.getBooleanOr("isVoid", false)) {
                tooltip.accept(Component.literal("- ").withStyle(ChatFormatting.GRAY).append(Component.translatable("drawer.block.upgrades.is_void").withStyle(ChatFormatting.BLUE)));
                anyupgrade = true;
            }
            if (!anyupgrade) {
                tooltip.accept(Component.literal("- ").withStyle(ChatFormatting.GRAY).append(Component.translatable("drawer.block.upgrades.none").withStyle(ChatFormatting.GRAY)));
            }
        }
        if (this instanceof FramedBlock) {
            tooltip.accept(Component.translatable("frameddrawer.use").withStyle(ChatFormatting.GRAY));
        }
    }

    public static class FluidDrawerItem extends BlockItem {

        private final FluidDrawerBlock drawerBlock;

        public FluidDrawerItem(FluidDrawerBlock block, Properties props,  TitaniumTab tab) {
            super(block, com.hrznstudio.titanium.module.DeferredRegistryHelper.applyItemRegistrationId(props.useBlockDescriptionPrefix()));
            this.drawerBlock = block;
        }

        public ResourceHandler<FluidResource> initCapabilities(ItemStack stack) {
            return new FluidDrawerStackItemHandler(stack, this.drawerBlock.getType());
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, context, display, tooltip, flag);
            drawerBlock.appendFluidHoverText(stack, context, display, tooltip, flag);
        }

    }
}
