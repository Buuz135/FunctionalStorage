package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.DrawerTile;
import com.buuz135.functionalstorage.block.tile.FramedDrawerTile;
import com.buuz135.functionalstorage.client.model.FramedDrawerModelData;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.util.DrawerWoodType;
import com.hrznstudio.titanium.module.BlockWithTile;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public class FramedDrawerBlock extends DrawerBlock{

    public FramedDrawerBlock(FunctionalStorage.DrawerType type) {
        super(DrawerWoodType.FRAMED, type, Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion().isViewBlocking((p_61036_, p_61037_, p_61038_) -> false));
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<DrawerTile> getTileEntityFactory() {
        return (blockPos, state) -> new FramedDrawerTile(this, (BlockEntityType<DrawerTile>) FunctionalStorage.DRAWER_TYPES.get(this.getType()).stream().filter(registryObjectRegistryObjectPair -> registryObjectRegistryObjectPair.getBlock() == this).map(BlockWithTile::type).findFirst().get().get(), blockPos, state, this.getType());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState p_49849_, @Nullable LivingEntity p_49850_, ItemStack stack) {
        super.setPlacedBy(level, pos, p_49849_, p_49850_, stack);
        TileUtil.getTileEntity(level, pos, FramedDrawerTile.class).ifPresent(framedDrawerTile -> {
            framedDrawerTile.setFramedDrawerModelData(getDrawerModelData(stack));
        });
    }

    public static FramedDrawerModelData getDrawerModelData(ItemStack stack){
        if (stack.hasData(FSAttachments.STYLE)){
            CompoundTag tag = stack.getData(FSAttachments.STYLE);
            if (tag.isEmpty()) return null;
            HashMap<String, Item> data = new HashMap<>();
            data.put("particle", BuiltInRegistries.ITEM.get(new ResourceLocation(tag.getString("particle"))));
            data.put("front", BuiltInRegistries.ITEM.get(new ResourceLocation(tag.getString("front"))));
            data.put("side", BuiltInRegistries.ITEM.get(new ResourceLocation(tag.getString("side"))));
            data.put("front_divider", BuiltInRegistries.ITEM.get(new ResourceLocation(tag.getString("front_divider"))));
            return new FramedDrawerModelData(data);
        }
        return null;
    }

    public static ItemStack fill(ItemStack first, ItemStack second, ItemStack drawer, ItemStack divider){
        drawer = ItemHandlerHelper.copyStackWithSize(drawer, 1);
        CompoundTag style = drawer.getData(FSAttachments.STYLE);
        style.putString("particle", BuiltInRegistries.ITEM.getKey(first.getItem()).toString());
        style.putString("side", BuiltInRegistries.ITEM.getKey(first.getItem()).toString());
        style.putString("front", BuiltInRegistries.ITEM.getKey(second.getItem()).toString());
        if (divider.isEmpty()){
            style.putString("front_divider", BuiltInRegistries.ITEM.getKey(first.getItem()).toString());
        } else {
            style.putString("front_divider", BuiltInRegistries.ITEM.getKey(divider.getItem()).toString());
        }
        drawer.setData(FSAttachments.STYLE, style);
        return drawer;
    }

    @Override
    public List<ItemStack> getDrops(BlockState p_60537_, LootParams.Builder builder) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        ItemStack stack = new ItemStack(this);
        BlockEntity drawerTile = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (drawerTile instanceof FramedDrawerTile framedDrawerTile) {
            if (framedDrawerTile.getFramedDrawerModelData() != null) {
                stack.setData(FSAttachments.STYLE, framedDrawerTile.getFramedDrawerModelData().serializeNBT());
            }
            if (!framedDrawerTile.isEverythingEmpty()) {
                stack.setData(FSAttachments.TILE, framedDrawerTile.saveWithoutMetadata());
            }
            if (framedDrawerTile.isLocked()) {
                stack.setData(FSAttachments.LOCKED, framedDrawerTile.isLocked());
            }
        }
        stacks.add(stack);
        return stacks;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof FramedDrawerTile framedDrawerTile && framedDrawerTile.getFramedDrawerModelData() != null && !framedDrawerTile.getFramedDrawerModelData().getDesign().isEmpty()){
            ItemStack stack = new ItemStack(this);
            stack.setData(FSAttachments.STYLE, framedDrawerTile.getFramedDrawerModelData().serializeNBT());
            return stack;
        }
        return super.getCloneItemStack(state, target, level, pos, player);
    }

    @Override
    public void registerRecipe(RecipeOutput consumer) {
        if (this.getType() == FunctionalStorage.DrawerType.X_1) {
            TitaniumShapedRecipeBuilder.shapedRecipe(this)
                    .pattern("PPP").pattern("PCP").pattern("PPP")
                    .define('P', Items.IRON_NUGGET)
                    .define('C', Tags.Items.CHESTS_WOODEN)
                    .save(consumer);
        }
        if (this.getType() == FunctionalStorage.DrawerType.X_2){
            TitaniumShapedRecipeBuilder.shapedRecipe(this, 2)
                    .pattern("PCP").pattern("PPP").pattern("PCP")
                    .define('P', Items.IRON_NUGGET)
                    .define('C', Tags.Items.CHESTS_WOODEN)
                    .save(consumer);

        }
        if (this.getType() == FunctionalStorage.DrawerType.X_4){
            TitaniumShapedRecipeBuilder.shapedRecipe(this, 4)
                    .pattern("CPC").pattern("PPP").pattern("CPC")
                    .define('P', Items.IRON_NUGGET)
                    .define('C', Tags.Items.CHESTS_WOODEN)
                    .save(consumer);

        }
    }

    @Override
    public void appendHoverText(ItemStack p_49816_, @Nullable BlockGetter p_49817_, List<Component> components, TooltipFlag p_49819_) {
        components.add(Component.translatable("frameddrawer.use").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(p_49816_, p_49817_, components, p_49819_);
    }
}
