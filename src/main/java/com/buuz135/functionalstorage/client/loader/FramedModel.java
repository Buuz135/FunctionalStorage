package com.buuz135.functionalstorage.client.loader;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.FramedBlock;
import com.buuz135.functionalstorage.util.CustomFramedDrawerModelData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.quad.MutableQuad;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FramedModel implements DynamicBlockStateModel {
    private static final String SIDE_MARKER = "framed_side";
    private static final String DIVIDER_MARKER = "machine_divider";
    private static final String FRONT_MARKER_PREFIX = "framed_front_";

    private final BlockStateModel delegate;

    public FramedModel(BlockStateModel delegate) {
        this.delegate = delegate;
    }

    public static void wrapModels(ModelEvent.ModifyBakingResult event) {
        event.getBakingResult().blockStateModels().replaceAll((state, model) ->
                FunctionalStorage.FRAMED_BLOCKS.contains(state.getBlock()) && !(model instanceof FramedModel)
                        ? new FramedModel(model)
                        : model);
    }

    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        Object delegateKey = delegate.createGeometryKey(level, pos, state, random);
        CustomFramedDrawerModelData design = getDesign(level, pos);
        return design == null || delegateKey == null ? delegateKey : new GeometryKey(delegateKey, design.getCode(), this);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> output) {
        List<BlockStateModelPart> baseParts = new ArrayList<>();
        delegate.collectParts(level, pos, state, random, baseParts);
        output.addAll(styleParts(baseParts, getDesign(level, pos), level, pos, false));
    }

    @Override
    public Material.Baked particleMaterial() {
        return delegate.particleMaterial();
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        CustomFramedDrawerModelData design = getDesign(level, pos);
        Item particle = getMaterial(design, "particle");
        if (particle instanceof BlockItem blockItem && !(blockItem.getBlock() instanceof FramedBlock)) {
            BlockState materialState = blockItem.getBlock().defaultBlockState();
            return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(materialState)
                    .particleMaterial(level, pos, materialState);
        }
        return delegate.particleMaterial(level, pos, state);
    }

    @Override
    public int materialFlags() {
        return delegate.materialFlags();
    }

    @Override
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        int flags = delegate.materialFlags(level, pos, state);
        CustomFramedDrawerModelData design = getDesign(level, pos);
        if (design != null) {
            for (Item item : design.getDesign().values()) {
                if (item instanceof BlockItem blockItem && !(blockItem.getBlock() instanceof FramedBlock)) {
                    BlockState materialState = blockItem.getBlock().defaultBlockState();
                    flags |= Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(materialState)
                            .materialFlags(level, pos, materialState);
                }
            }
        }
        return flags;
    }

    private static @Nullable CustomFramedDrawerModelData getDesign(BlockAndTintGetter level, BlockPos pos) {
        return level.getModelData(pos).get(CustomFramedDrawerModelData.MODEL_PROPERTY);
    }


    public static List<BlockStateModelPart> styleParts(List<BlockStateModelPart> baseParts,
                                                       @Nullable CustomFramedDrawerModelData design,
                                                       BlockAndTintGetter level, BlockPos pos,
                                                       boolean itemContext) {
        if (baseParts.isEmpty()) return List.of();

        Map<String, BakedQuad> fallbackMarkers = findFallbackMarkers(baseParts);
        Map<MaterialKey, List<BakedQuad>> materialCache = new HashMap<>();
        List<BlockStateModelPart> styled = new ArrayList<>(baseParts.size());
        for (BlockStateModelPart part : baseParts) {
            styled.add(new StyledPart(part, design, fallbackMarkers, materialCache, level, pos, itemContext));
        }
        return List.copyOf(styled);
    }

    public static List<BlockStateModelPart> collectBaseParts(BlockStateModel model, BlockState state) {
        BlockStateModel baseModel = model instanceof FramedModel framed ? framed.delegate : model;
        List<BlockStateModelPart> parts = new ArrayList<>();
        baseModel.collectParts(BlockAndTintGetter.EMPTY, BlockPos.ZERO, state, RandomSource.create(42), parts);
        return List.copyOf(parts);
    }

    public static List<BakedQuad> flattenParts(List<BlockStateModelPart> parts) {
        List<BakedQuad> quads = new ArrayList<>();
        for (BlockStateModelPart part : parts) quads.addAll(collectAllQuads(part));
        return List.copyOf(quads);
    }

    private static Map<String, BakedQuad> findFallbackMarkers(List<BlockStateModelPart> parts) {
        Map<String, BakedQuad> markers = new HashMap<>();
        for (BlockStateModelPart part : parts) {
            collectAllQuads(part).forEach(quad -> {
                String key = markerKey(quad);
                if (key != null) markers.putIfAbsent(key, quad);
            });
        }
        return markers;
    }

    private static List<BakedQuad> collectAllQuads(BlockStateModelPart part) {
        List<BakedQuad> quads = new ArrayList<>(part.getQuads(null));
        for (Direction direction : Direction.values()) quads.addAll(part.getQuads(direction));
        return quads;
    }

    private static List<BakedQuad> styleQuads(List<BakedQuad> shapeQuads,
                                               @Nullable CustomFramedDrawerModelData design,
                                               Map<String, BakedQuad> fallbackMarkers,
                                               Map<MaterialKey, List<BakedQuad>> materialCache,
                                               BlockAndTintGetter level, BlockPos pos,
                                               boolean itemContext) {
        List<BakedQuad> result = new ArrayList<>(shapeQuads.size());
        for (BakedQuad shape : shapeQuads) {
            String key = markerKey(shape);
            if (key == null) {
                result.add(shape);
                continue;
            }

            Item material = getMaterial(design, key);
            if (material == null && key.equals("front_divider")) material = getMaterial(design, "side");
            List<BakedQuad> samples = material == null ? List.of() : materialCache.computeIfAbsent(
                    new MaterialKey(material, shape.direction()), cacheKey -> collectMaterialQuads(
                            cacheKey.item(), cacheKey.direction(), level, pos));

            if (samples.isEmpty() && key.equals("front_divider")) {
                BakedQuad fallback = fallbackMarkers.get("side");
                if (fallback != null) samples = List.of(fallback);
            }

            if (samples.isEmpty()) {
                result.add(shape);
            } else {
                for (BakedQuad sample : samples) {
                    result.add(applyMaterial(shape, sample, material, level, pos, itemContext));
                }
            }
        }
        return List.copyOf(result);
    }

    private static List<BakedQuad> collectMaterialQuads(Item item, Direction direction,
                                                         BlockAndTintGetter level, BlockPos pos) {
        if (!(item instanceof BlockItem blockItem) || blockItem.getBlock() instanceof FramedBlock) return List.of();
        BlockState materialState = blockItem.getBlock().defaultBlockState();
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(materialState);
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(level, pos, materialState, RandomSource.create(42), parts);

        List<BakedQuad> quads = new ArrayList<>();
        for (BlockStateModelPart part : parts) quads.addAll(part.getQuads(direction));
        if (quads.isEmpty()) {
            for (BlockStateModelPart part : parts) {
                for (BakedQuad quad : part.getQuads(null)) {
                    if (quad.direction() == direction) quads.add(quad);
                }
            }
        }
        return List.copyOf(quads);
    }

    private static BakedQuad applyMaterial(BakedQuad shape, BakedQuad sample, @Nullable Item material,
                                           BlockAndTintGetter level, BlockPos pos, boolean itemContext) {
        MutableQuad quad = new MutableQuad().setFrom(shape).setSpriteAndMoveUv(
                sample.materialInfo().sprite(), sample.materialInfo().layer(), sample.materialInfo().itemRenderType());
        quad.setTintIndex(-1);

        int tint = 0xFFFFFFFF;
        int lightEmission = sample.materialInfo().lightEmission();
        if (material instanceof BlockItem blockItem) {
            BlockState materialState = blockItem.getBlock().defaultBlockState();
            lightEmission = Math.max(lightEmission, materialState.getLightEmission());
            int tintIndex = sample.materialInfo().tintIndex();
            if (tintIndex >= 0) {
                BlockTintSource tintSource = Minecraft.getInstance().getBlockColors().getTintSource(materialState, tintIndex);
                if (tintSource != null) {
                    tint = itemContext ? tintSource.color(materialState) : tintSource.colorInWorld(materialState, level, pos);
                }
            }
        }
        quad.setLightEmission(Math.max(shape.materialInfo().lightEmission(), lightEmission));
        for (int vertex = 0; vertex < 4; vertex++) {
            quad.setColor(vertex, multiplyColors(shape.bakedColors().color(vertex), sample.bakedColors().color(vertex), tint));
        }
        return quad.toBakedQuad();
    }

    private static int multiplyColors(int first, int second, int third) {
        int a = channel(first, 24) * channel(second, 24) * channel(third, 24) / (255 * 255);
        int r = channel(first, 16) * channel(second, 16) * channel(third, 16) / (255 * 255);
        int g = channel(first, 8) * channel(second, 8) * channel(third, 8) / (255 * 255);
        int b = channel(first, 0) * channel(second, 0) * channel(third, 0) / (255 * 255);
        return a << 24 | r << 16 | g << 8 | b;
    }

    private static int channel(int color, int shift) {
        return color >>> shift & 0xFF;
    }

    private static @Nullable Item getMaterial(@Nullable CustomFramedDrawerModelData design, String key) {
        if (design == null) return null;
        Item item = design.getDesign().get(key);
        return item instanceof BlockItem ? item : null;
    }

    private static @Nullable String markerKey(BakedQuad quad) {
        Identifier texture = quad.materialInfo().sprite().contents().name();
        if (!texture.getNamespace().equals(FunctionalStorage.MOD_ID)) return null;
        String path = texture.getPath();
        if (path.equals("block/" + SIDE_MARKER)) return "side";
        if (path.equals("block/" + DIVIDER_MARKER)) return "front_divider";
        if (path.startsWith("block/" + FRONT_MARKER_PREFIX)) return "front";
        return null;
    }

    private record GeometryKey(@Nullable Object delegateKey, String design, FramedModel owner) {}
    private record MaterialKey(Item item, Direction direction) {}

    private static final class StyledPart implements BlockStateModelPart {
        private final BlockStateModelPart delegate;
        private final List<BakedQuad> unculled;
        private final EnumMap<Direction, List<BakedQuad>> culled = new EnumMap<>(Direction.class);
        private final int materialFlags;

        private StyledPart(BlockStateModelPart delegate, @Nullable CustomFramedDrawerModelData design,
                           Map<String, BakedQuad> fallbackMarkers,
                           Map<MaterialKey, List<BakedQuad>> materialCache,
                           BlockAndTintGetter level, BlockPos pos, boolean itemContext) {
            this.delegate = delegate;
            this.unculled = styleQuads(delegate.getQuads(null), design, fallbackMarkers, materialCache, level, pos, itemContext);
            for (Direction direction : Direction.values()) {
                culled.put(direction, styleQuads(delegate.getQuads(direction), design, fallbackMarkers, materialCache, level, pos, itemContext));
            }
            int flags = 0;
            for (BakedQuad quad : unculled) flags |= quad.materialInfo().flags();
            for (List<BakedQuad> quads : culled.values()) {
                for (BakedQuad quad : quads) flags |= quad.materialInfo().flags();
            }
            this.materialFlags = flags;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable Direction direction) {
            return direction == null ? unculled : culled.get(direction);
        }

        @Override
        public boolean useAmbientOcclusion() {
            return delegate.useAmbientOcclusion();
        }

        @Override
        public Material.Baked particleMaterial() {
            return delegate.particleMaterial();
        }

        @Override
        public int materialFlags() {
            return materialFlags;
        }
    }
}
