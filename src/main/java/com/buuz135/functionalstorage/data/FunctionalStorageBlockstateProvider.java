package com.buuz135.functionalstorage.data;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.*;
import com.hrznstudio.titanium.block.RotatableBlock;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.util.NonNullLazy;
import net.neoforged.neoforge.registries.ForgeRegistries;
import java.util.List;

public class FunctionalStorageBlockstateProvider extends BlockStateProvider {
    private ExistingFileHelper helper;
    private final NonNullLazy<List<Block>> blocks;

    public FunctionalStorageBlockstateProvider(DataGenerator gen, ExistingFileHelper exFileHelper, NonNullLazy<List<Block>> blocks) {
        super(gen.getPackOutput(), FunctionalStorage.MOD_ID, exFileHelper);
        this.helper = exFileHelper;
        this.blocks = blocks;
    }

    public static ResourceLocation getModel(Block block) {
        return new ResourceLocation(ForgeRegistries.BLOCKS.getKey(block).getNamespace(), "block/" + ForgeRegistries.BLOCKS.getKey(block).getPath());
    }

    public static ResourceLocation getModelLocked(Block block) {
        return new ResourceLocation(ForgeRegistries.BLOCKS.getKey(block).getNamespace(), "block/" + ForgeRegistries.BLOCKS.getKey(block).getPath() + "_locked");
    }

    @Override
    protected void registerStatesAndModels() {
        blocks.get().stream().filter(blockBase -> blockBase instanceof RotatableBlock)
                .map(blockBase -> (RotatableBlock) blockBase)
                .forEach(rotatableBlock -> {
                    VariantBlockStateBuilder builder = getVariantBuilder(rotatableBlock);
                    if (rotatableBlock.getRotationType().getProperties().length > 0) {
                        for (DirectionProperty property : rotatableBlock.getRotationType().getProperties()) {
                            for (Direction allowedValue : property.getPossibleValues()) {
                                if (rotatableBlock instanceof DrawerBlock || rotatableBlock instanceof CompactingDrawerBlock || rotatableBlock instanceof EnderDrawerBlock || rotatableBlock instanceof FluidDrawerBlock || rotatableBlock instanceof SimpleCompactingDrawerBlock) {
                                    builder.partialState().with(property, allowedValue).with(DrawerBlock.LOCKED, false)
                                            .addModels(new ConfiguredModel(new ModelFile.UncheckedModelFile(getModel(rotatableBlock)), allowedValue.get2DDataValue() == -1 ? allowedValue.getOpposite().getAxisDirection().getStep() * 90 : 0, (int) allowedValue.getOpposite().toYRot(), true));
                                    builder.partialState().with(property, allowedValue).with(DrawerBlock.LOCKED, true)
                                            .addModels(new ConfiguredModel(new ModelFile.UncheckedModelFile(getModelLocked(rotatableBlock)), allowedValue.get2DDataValue() == -1 ? allowedValue.getOpposite().getAxisDirection().getStep() * 90 : 0, (int) allowedValue.getOpposite().toYRot(), true));
                                } else {
                                    builder.partialState().with(property, allowedValue)
                                            .addModels(new ConfiguredModel(new ModelFile.UncheckedModelFile(getModel(rotatableBlock)), allowedValue.get2DDataValue() == -1 ? allowedValue.getOpposite().getAxisDirection().getStep() * 90 : 0, (int) allowedValue.getOpposite().toYRot(), true));
                                }
                            }
                        }
                    } else {
                        builder.partialState().addModels(new ConfiguredModel(new ModelFile.UncheckedModelFile(getModel(rotatableBlock))));
                    }
                });
    }
}
