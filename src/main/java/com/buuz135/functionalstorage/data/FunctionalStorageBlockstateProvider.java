package com.buuz135.functionalstorage.data;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.Drawer;
import com.buuz135.functionalstorage.block.DrawerBlock;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.mojang.math.Quadrant;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.core.Direction;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FunctionalStorageBlockstateProvider implements DataProvider {
    private final PackOutput.PathProvider blockStatePathProvider;
    private final Lazy<List<Block>> blocks;

    public FunctionalStorageBlockstateProvider(PackOutput output, Lazy<List<Block>> blocks) {
        this.blockStatePathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        this.blocks = blocks;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Map<Block, BlockStateModelDispatcher> definitions = new LinkedHashMap<>();
        for (Block block : blocks.get()) {
            if (block instanceof RotatableBlock<?> rotatable) {
                definitions.put(block, createRotatable(rotatable).create());
            }
        }
        return DataProvider.saveAll(
                cache,
                BlockStateModelDispatcher.CODEC,
                block -> blockStatePathProvider.json(block.builtInRegistryHolder().key().identifier()),
                definitions);
    }

    private static MultiPartGenerator createRotatable(RotatableBlock<?> block) {
        Identifier baseModel = ModelLocationUtils.getModelLocation(block);
        Identifier lockModel = Identifier.fromNamespaceAndPath(FunctionalStorage.MOD_ID, "block/lock");
        MultiPartGenerator builder = MultiPartGenerator.multiPart(block);

        if (block.getRotationType() == RotatableBlock.RotationType.FOUR_WAY) {
            for (Direction direction : Drawer.FACING_HORIZONTAL.getPossibleValues()) {
                builder.with(
                        BlockModelGenerators.condition().term(Drawer.FACING_HORIZONTAL, direction.getOpposite()),
                        variant(baseModel, 0, direction.toYRot(), true));
                if (block instanceof Drawer) {
                    builder.with(
                            BlockModelGenerators.condition()
                                    .term(Drawer.FACING_HORIZONTAL, direction.getOpposite())
                                    .term(DrawerBlock.LOCKED, true),
                            variant(lockModel, 0, direction.toYRot(), true));
                }
            }
        } else {
            for (Direction direction : Direction.values()) {
                if (direction == Direction.DOWN || direction == Direction.UP) {
                    float xRotation = direction == Direction.DOWN ? 90 : 270;
                    for (Direction horizontal : Drawer.FACING_HORIZONTAL_CUSTOM.getPossibleValues()) {
                        float yRotation = direction == Direction.DOWN ? horizontal.getOpposite().toYRot() : horizontal.toYRot();
                        builder.with(
                                BlockModelGenerators.condition()
                                        .term(Drawer.FACING_HORIZONTAL_CUSTOM, direction)
                                        .term(RotatableBlock.FACING_ALL, horizontal),
                                variant(baseModel, xRotation, yRotation, false));
                        if (block instanceof Drawer) {
                            builder.with(
                                    BlockModelGenerators.condition()
                                            .term(Drawer.FACING_HORIZONTAL_CUSTOM, direction)
                                            .term(RotatableBlock.FACING_ALL, horizontal)
                                            .term(DrawerBlock.LOCKED, true),
                                    variant(lockModel, xRotation, yRotation, false));
                        }
                    }
                } else {
                    builder.with(
                            BlockModelGenerators.condition()
                                    .term(Drawer.FACING_HORIZONTAL_CUSTOM, direction)
                                    .term(RotatableBlock.FACING_ALL, Direction.DOWN),
                            variant(baseModel, 0, direction.getOpposite().toYRot(), false));
                    if (block instanceof Drawer) {
                        builder.with(
                                BlockModelGenerators.condition()
                                        .term(Drawer.FACING_HORIZONTAL_CUSTOM, direction)
                                        .term(RotatableBlock.FACING_ALL, Direction.DOWN)
                                        .term(DrawerBlock.LOCKED, true),
                                variant(lockModel, 0, direction.getOpposite().toYRot(), false));
                    }
                }
            }
        }

        return builder;
    }

    private static net.minecraft.client.data.models.MultiVariant variant(Identifier model, float x, float y, boolean uvLock) {
        Variant variant = new Variant(model)
                .withXRot(quadrant(x))
                .withYRot(quadrant(y))
                .withUvLock(uvLock);
        return BlockModelGenerators.variant(variant);
    }

    private static Quadrant quadrant(float degrees) {
        return switch (Math.floorMod(Math.round(degrees / 90.0F), 4)) {
            case 1 -> Quadrant.R90;
            case 2 -> Quadrant.R180;
            case 3 -> Quadrant.R270;
            default -> Quadrant.R0;
        };
    }

    @Override
    public String getName() {
        return "Functional Storage Blockstates";
    }
}
