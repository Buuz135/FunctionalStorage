package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.DrawerTile;
import com.buuz135.functionalstorage.block.tile.FramedDrawerTile;
import com.buuz135.functionalstorage.client.model.FramedDrawerModelData;
import com.buuz135.functionalstorage.util.DrawerWoodType;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.FinishedRecipe;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.Tags;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class FramedDrawerBlock extends DrawerBlock{

    public FramedDrawerBlock(FunctionalStorage.DrawerType type) {
        super(DrawerWoodType.FRAMED, type, Properties.copy(Blocks.OAK_PLANKS).noOcclusion().isViewBlocking((p_61036_, p_61037_, p_61038_) -> false));
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<DrawerTile> getTileEntityFactory() {
        return (blockPos, state) -> new FramedDrawerTile(this, (BlockEntityType<DrawerTile>) FunctionalStorage.DRAWER_TYPES.get(this.getType()).stream().filter(registryObjectRegistryObjectPair -> registryObjectRegistryObjectPair.getLeft().get().equals(this)).map(Pair::getRight).findFirst().get().get(), blockPos, state, this.getType());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState p_49849_, @Nullable LivingEntity p_49850_, ItemStack stack) {
        super.setPlacedBy(level, pos, p_49849_, p_49850_, stack);
        TileUtil.getTileEntity(level, pos, FramedDrawerTile.class).ifPresent(framedDrawerTile -> {
            framedDrawerTile.setFramedDrawerModelData(getDrawerModelData(stack));
        });
    }

    public static FramedDrawerModelData getDrawerModelData(ItemStack stack){
        if (stack.hasTag() && stack.getTag().contains("Style")){
            CompoundTag tag = stack.getTag().getCompound("Style");
            if (tag.isEmpty()) return null;
            HashMap<String, Item> data = new HashMap<>();
            data.put("particle", ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString("particle"))));
            data.put("front", ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString("front"))));
            data.put("side", ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString("side"))));
            data.put("front_divider", ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString("front_divider"))));
            return new FramedDrawerModelData(data);
        }
        return null;
    }

    public static ItemStack fill(ItemStack first, ItemStack second, ItemStack drawer){
        drawer = ItemHandlerHelper.copyStackWithSize(drawer, 1);
        CompoundTag style = drawer.getOrCreateTagElement("Style");
        style.putString("particle", ForgeRegistries.ITEMS.getKey(first.getItem()).toString());
        style.putString("side", ForgeRegistries.ITEMS.getKey(first.getItem()).toString());
        style.putString("front", ForgeRegistries.ITEMS.getKey(second.getItem()).toString());
        style.putString("front_divider", ForgeRegistries.ITEMS.getKey(first.getItem()).toString());
        drawer.getOrCreateTag().put("Style", style);
        return drawer;
    }

    @Override
    public List<ItemStack> getDrops(BlockState p_60537_, LootContext.Builder builder) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        ItemStack stack = new ItemStack(this);
        BlockEntity drawerTile = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (drawerTile instanceof FramedDrawerTile framedDrawerTile) {
            if (!framedDrawerTile.isEverythingEmpty()) {
                stack.getOrCreateTag().put("Tile", drawerTile.saveWithoutMetadata());
            }
            if (framedDrawerTile.getFramedDrawerModelData() != null) {
                stack.getOrCreateTag().put("Style", framedDrawerTile.getFramedDrawerModelData().serializeNBT());
            }
        }
        stacks.add(stack);
        return stacks;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof FramedDrawerTile framedDrawerTile && framedDrawerTile.getFramedDrawerModelData() != null && !framedDrawerTile.getFramedDrawerModelData().getDesign().isEmpty()){
            ItemStack stack = new ItemStack(this);
            stack.getOrCreateTag().put("Style", framedDrawerTile.getFramedDrawerModelData().serializeNBT());
            return stack;
        }
        return super.getCloneItemStack(state, target, level, pos, player);
    }

    @Override
    public void registerRecipe(Consumer<FinishedRecipe> consumer) {
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
