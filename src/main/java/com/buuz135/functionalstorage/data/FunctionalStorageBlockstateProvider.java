package com.buuz135.functionalstorage.data;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.Drawer;
import com.buuz135.functionalstorage.block.DrawerBlock;
import com.hrznstudio.titanium.block.RotatableBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.List;

public class FunctionalStorageBlockstateProvider extends BlockStateProvider {
    private final Lazy<List<Block>> blocks;

    public FunctionalStorageBlockstateProvider(DataGenerator gen, ExistingFileHelper exFileHelper, Lazy<List<Block>> blocks) {
        super(gen.getPackOutput(), FunctionalStorage.MOD_ID, exFileHelper);
        this.blocks = blocks;
    }

    public static ResourceLocation getModel(Block block) {
        return com.buuz135.functionalstorage.util.Utils.resourceLocation(BuiltInRegistries.BLOCK.getKey(block).getNamespace(), "block/" + BuiltInRegistries.BLOCK.getKey(block).getPath());
    }

    @Override
    protected void registerStatesAndModels() {
        blocks.get().stream()
        .filter(b -> b instanceof RotatableBlock<?>)
        .forEach(b -> registerRotatable((RotatableBlock<?>) b));
    }

    private void registerRotatable(RotatableBlock<?> block) {
        var baseModel = new ModelFile.UncheckedModelFile(getModel(block));
        var lockModel = new ModelFile.UncheckedModelFile(ResourceLocation.fromNamespaceAndPath(FunctionalStorage.MOD_ID, "block/lock"));
        var builder = getMultipartBuilder(block);

        if (block.getRotationType() == RotatableBlock.RotationType.FOUR_WAY) {
            for (Direction direction : Drawer.FACING_HORIZONTAL.getPossibleValues()) {
                builder.part().modelFile(baseModel).uvLock(true).rotationY((int) direction.toYRot()).addModel()
                        .condition(Drawer.FACING_HORIZONTAL, direction.getOpposite()).end();

                if (block instanceof Drawer) {
                    builder.part().modelFile(lockModel).uvLock(true).rotationY((int) direction.toYRot()).addModel()
                            .condition(Drawer.FACING_HORIZONTAL, direction.getOpposite()).condition(DrawerBlock.LOCKED, true).end();
                }
            }
        } else {
            for (Direction direction : Direction.values()) {
                if (direction == Direction.DOWN) {
                    for (Direction possibleValue : Drawer.FACING_HORIZONTAL_CUSTOM.getPossibleValues()) {
                        builder.part().modelFile(baseModel).uvLock(false).rotationX(90).rotationY((int) possibleValue.getOpposite().toYRot()).addModel()
                                .condition(Drawer.FACING_HORIZONTAL_CUSTOM, direction).condition(RotatableBlock.FACING_ALL, possibleValue).end();

                        if (block instanceof Drawer) {
                            builder.part().modelFile(lockModel).uvLock(false).rotationX(90).rotationY((int) possibleValue.getOpposite().toYRot()).addModel()
                                    .condition(Drawer.FACING_HORIZONTAL_CUSTOM, direction).condition(RotatableBlock.FACING_ALL, possibleValue).condition(DrawerBlock.LOCKED, true).end();
                        }
                    }
                } else if (direction == Direction.UP) {
                    for (Direction possibleValue : Drawer.FACING_HORIZONTAL_CUSTOM.getPossibleValues()) {
                        builder.part().modelFile(baseModel).uvLock(false).rotationX(270).rotationY((int) possibleValue.toYRot()).addModel()
                                .condition(Drawer.FACING_HORIZONTAL_CUSTOM, direction).condition(RotatableBlock.FACING_ALL, possibleValue).end();

                        if (block instanceof Drawer) {
                            builder.part().modelFile(lockModel).uvLock(false).rotationX(270).rotationY((int) possibleValue.toYRot()).addModel()
                                    .condition(Drawer.FACING_HORIZONTAL_CUSTOM, direction).condition(RotatableBlock.FACING_ALL, possibleValue).condition(DrawerBlock.LOCKED, true).end();
                        }
                    }
                } else {
                    builder.part().modelFile(baseModel).uvLock(false).rotationY((int) direction.getOpposite().toYRot()).addModel()
                            .condition(Drawer.FACING_HORIZONTAL_CUSTOM, direction).condition(RotatableBlock.FACING_ALL, Direction.DOWN).end();

                    if (block instanceof Drawer) {
                        builder.part().modelFile(lockModel).uvLock(false).rotationY((int) direction.getOpposite().toYRot()).addModel()
                                .condition(Drawer.FACING_HORIZONTAL_CUSTOM, direction).condition(RotatableBlock.FACING_ALL, Direction.DOWN).condition(DrawerBlock.LOCKED, true).end();
                    }
                }
            }
        }
    }
}
