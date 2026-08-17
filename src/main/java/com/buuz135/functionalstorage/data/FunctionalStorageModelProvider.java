package com.buuz135.functionalstorage.data;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.CompactingDrawerBlock;
import com.buuz135.functionalstorage.block.DrawerBlock;
import com.buuz135.functionalstorage.block.FluidDrawerBlock;
import com.buuz135.functionalstorage.block.FramedBlock;
import com.buuz135.functionalstorage.block.SimpleCompactingDrawerBlock;
import com.buuz135.functionalstorage.client.item.DrawerSpecialRenderer;
import com.buuz135.functionalstorage.client.item.FramedSpecialRenderer;
import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.DelegatedModel;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.util.Lazy;
import com.buuz135.functionalstorage.client.ToolTintSource;

import java.util.List;
import java.util.stream.Stream;

public class FunctionalStorageModelProvider extends ModelProvider {
    private final Lazy<List<Block>> blocks;

    public FunctionalStorageModelProvider(PackOutput output, Lazy<List<Block>> blocks) {
        super(output, FunctionalStorage.MOD_ID);
        this.blocks = blocks;
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        for (StorageUpgradeItem.StorageTier tier : FunctionalStorage.STORAGE_UPGRADES.keySet()) {
            itemModels.generateFlatItem(FunctionalStorage.STORAGE_UPGRADES.get(tier).get(), ModelTemplates.FLAT_ITEM);
        }
        generateFlatItem(itemModels, FunctionalStorage.COLLECTOR_UPGRADE.get());
        generateFlatItem(itemModels, FunctionalStorage.PULLING_UPGRADE.get());
        generateFlatItem(itemModels, FunctionalStorage.PUSHING_UPGRADE.get());
        generateFlatItem(itemModels, FunctionalStorage.VOID_UPGRADE.get());
        generateFlatItem(itemModels, FunctionalStorage.REDSTONE_UPGRADE.get());
        generateFlatItem(itemModels, FunctionalStorage.CREATIVE_UPGRADE.get());
        generateFlatItem(itemModels, FunctionalStorage.DRIPPING_UPGRADE.get());
        generateFlatItem(itemModels, FunctionalStorage.WATER_GENERATOR_UPGRADE.get());
        generateFlatItem(itemModels, FunctionalStorage.OBSIDIAN_UPGRADE.get());

        itemModels.itemModelOutput.accept(FunctionalStorage.LINKING_TOOL.get(), ItemModelUtils.tintedModel(
                ModelLocationUtils.getModelLocation(FunctionalStorage.LINKING_TOOL.get()),
                new ToolTintSource(0), new ToolTintSource(1), new ToolTintSource(2), new ToolTintSource(3)));
        itemModels.itemModelOutput.accept(FunctionalStorage.CONFIGURATION_TOOL.get(), ItemModelUtils.tintedModel(
                ModelLocationUtils.getModelLocation(FunctionalStorage.CONFIGURATION_TOOL.get()),
                new ToolTintSource(0), new ToolTintSource(1)));

        for (Block block : blocks.get()) {
            DrawerSpecialRenderer.Kind kind = null;
            if (block instanceof FluidDrawerBlock) kind = DrawerSpecialRenderer.Kind.FLUID;
            else if (block instanceof SimpleCompactingDrawerBlock) kind = DrawerSpecialRenderer.Kind.SIMPLE_COMPACTING;
            else if (block instanceof CompactingDrawerBlock) kind = DrawerSpecialRenderer.Kind.COMPACTING;
            else if (block instanceof DrawerBlock) kind = DrawerSpecialRenderer.Kind.DRAWER;

            Identifier baseModel = ModelLocationUtils.getModelLocation(block);
            if (block instanceof FramedBlock) {
                var framed = ItemModelUtils.specialModel(baseModel, FramedSpecialRenderer.Unbaked.INSTANCE);
                if (kind == null) {
                    itemModels.itemModelOutput.accept(block.asItem(), framed);
                } else {
                    itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.composite(
                            framed, ItemModelUtils.specialModel(baseModel, new DrawerSpecialRenderer.Unbaked(kind))));
                }
            } else if (kind != null) {
                itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.composite(
                        ItemModelUtils.plainModel(baseModel),
                        ItemModelUtils.specialModel(baseModel, new DrawerSpecialRenderer.Unbaked(kind))));
            }
        }
    }

    private static void generateFlatItem(ItemModelGenerators itemModels, Item item) {
        itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
    }




    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.empty();
    }
}
