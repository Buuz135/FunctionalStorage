package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.FluidDrawerTile;
import com.buuz135.functionalstorage.block.tile.FramedFluidDrawerTile;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.util.NumberUtils;
import com.buuz135.functionalstorage.util.Utils;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class FramedFluidDrawerBlock extends FluidDrawerBlock implements FramedBlock{

    public FramedFluidDrawerBlock(FunctionalStorage.DrawerType type, Properties properties) {
        super(type, properties);
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<FluidDrawerTile> getTileEntityFactory() {
        return (blockPos, state) -> {
            BlockEntityType<FluidDrawerTile> entityType = (BlockEntityType<FluidDrawerTile>) FunctionalStorage.FRAMED_FLUID_DRAWER_1.type().get();
            if (this.getType() == FunctionalStorage.DrawerType.X_2) {
                entityType = (BlockEntityType<FluidDrawerTile>) FunctionalStorage.FRAMED_FLUID_DRAWER_2.type().get();
            }
            if (this.getType() == FunctionalStorage.DrawerType.X_4) {
                entityType = (BlockEntityType<FluidDrawerTile>) FunctionalStorage.FRAMED_FLUID_DRAWER_4.type().get();
            }
            return new FramedFluidDrawerTile(this, entityType, blockPos, state, this.getType());
        };
    }

    @Override
    public void registerRecipe(RecipeOutput consumer) {
        if (this.getType() == FunctionalStorage.DrawerType.X_1) {
            TitaniumShapedRecipeBuilder.shapedRecipe(this)
                    .pattern("PPP").pattern("PCP").pattern("PPP")
                    .define('P', Items.IRON_NUGGET)
                    .define('C', Items.BUCKET)
                    .save(consumer);
        }
        if (this.getType() == FunctionalStorage.DrawerType.X_2) {
            TitaniumShapedRecipeBuilder.shapedRecipe(this, 2)
                    .pattern("PCP").pattern("PPP").pattern("PCP")
                    .define('P', Items.IRON_NUGGET)
                    .define('C', Items.BUCKET)
                    .save(consumer);
        }
        if (this.getType() == FunctionalStorage.DrawerType.X_4) {
            TitaniumShapedRecipeBuilder.shapedRecipe(this, 4)
                    .pattern("CPC").pattern("PPP").pattern("CPC")
                    .define('P', Items.IRON_NUGGET)
                    .define('C', Items.BUCKET)
                    .save(consumer);
        }
    }
}
