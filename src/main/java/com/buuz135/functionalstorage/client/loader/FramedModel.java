package com.buuz135.functionalstorage.client.loader;

import com.buuz135.functionalstorage.block.FramedDrawerBlock;
import com.buuz135.functionalstorage.client.model.FramedDrawerModelData;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.*;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.client.model.geometry.IModelGeometryPart;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.List;
import java.util.function.Function;

import static com.buuz135.functionalstorage.client.loader.FramedModel.Baked.getQuadsUsingShape;

/**
 * A Custom Model for Framed Drawers. <br>
 * Based on {@link net.minecraftforge.client.model.CompositeModel} from Forge. <br>
 * Using parts of <a href="https://github.com/SleepyTrousers/EnderIO-Rewrite/blob/dev/1.19.x/src/decor/java/com/enderio/decoration/client/model/painted/PaintedBlockModel.java"> Painted Block Model</a> from Ender IO.
 */
public class FramedModel implements IModelGeometry<FramedModel> {
    private static final Logger LOGGER = LogManager.getLogger();

    private final ImmutableMap<String, Submodel> children;
    private final ImmutableList<String> itemPasses;
    private final boolean logWarning;

    public FramedModel(ImmutableMap<String, Submodel> children, ImmutableList<String> itemPasses)
    {
        this(children, itemPasses, false);
    }

    private FramedModel(ImmutableMap<String, Submodel> children, ImmutableList<String> itemPasses, boolean logWarning)
    {
        this.children = children;
        this.itemPasses = itemPasses;
        this.logWarning = logWarning;
    }

    @Override
    public BakedModel bake(IModelConfiguration context, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation)
    {
        if (logWarning)
            LOGGER.warn("Model \"" + modelLocation + "\" is using the deprecated \"parts\" field in its composite model instead of \"children\". This field will be removed in 1.20.");

        Material particleLocation = context.resolveTexture("particle");
        TextureAtlasSprite particle = spriteGetter.apply(particleLocation);

        //var rootTransform = modelState.getRotation();
        //if (!rootTransform.isIdentity())
        //    modelState = new SimpleModelState(modelState.getRotation().compose(rootTransform), modelState.isUvLocked());

        var bakedPartsBuilder = ImmutableMap.<String, BakedModel>builder();
        for (var entry : children.entrySet())
        {
            var name = entry.getKey();
            if (!context.getPartVisibility(entry.getValue(), true))
                continue;
            var model = entry.getValue();
            bakedPartsBuilder.put(name, model.bakeModel(bakery,  spriteGetter, modelState, modelLocation));
        }
        var bakedParts = bakedPartsBuilder.build();

        var itemPassesBuilder = ImmutableList.<BakedModel>builder();
        for (String name : this.itemPasses)
        {
            var model = bakedParts.get(name);
            if (model == null)
                throw new IllegalStateException("Specified \"" + name + "\" in \"item_render_order\", but that is not a child of this model.");
            itemPassesBuilder.add(model);
        }

        return new FramedModel.Baked(context.isShadedInGui(), context.isSideLit(), context.useSmoothLighting(), particle, context.getCameraTransforms(), overrides, bakedParts, itemPassesBuilder.build());
    }

    @Override
    public Collection<Material> getTextures(IModelConfiguration context, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors)
    {
        Set<Material> textures = new HashSet<>();
        if (context.isTexturePresent("particle"))
            textures.add(context.resolveTexture("particle"));
        for (Submodel part : children.values())
            textures.addAll(part.getTextures(context, modelGetter, missingTextureErrors));
        return textures;
    }
    @Override
    public Collection<? extends IModelGeometryPart> getParts() {
        return this.children.values();
    }

    public class Baked implements IDynamicBakedModel
    {
        private final boolean isAmbientOcclusion;
        private final boolean isGui3d;
        private final boolean isSideLit;
        private final TextureAtlasSprite particle;
        private final ItemOverrides overrides;
        private final ItemTransforms transforms;
        private final ImmutableMap<String, BakedModel> children;
        private final ImmutableList<BakedModel> itemPasses;

        public Baked(boolean isGui3d, boolean isSideLit, boolean isAmbientOcclusion, TextureAtlasSprite particle, ItemTransforms transforms, ItemOverrides overrides, ImmutableMap<String, BakedModel> children, ImmutableList<BakedModel> itemPasses)
        {
            this.children = children;
            this.isAmbientOcclusion = isAmbientOcclusion;
            this.isGui3d = isGui3d;
            this.isSideLit = isSideLit;
            this.particle = particle;
            this.overrides = new RetexturedOverride(this);
            this.transforms = transforms;
            this.itemPasses = itemPasses;
        }

        @NotNull
        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull Random rand, @NotNull IModelData data)
        {
            List<List<BakedQuad>> quadLists = new ArrayList<>();
            for (Map.Entry<String, BakedModel> entry : children.entrySet())
            {
   
                    FramedDrawerModelData framedDrawerModelData = data.getData(FramedDrawerModelData.FRAMED_PROPERTY);
                    List<BakedQuad> quads = entry.getValue().getQuads(state, side, rand, Data.resolve(data, entry.getKey()));
                    if (framedDrawerModelData != null && framedDrawerModelData.getDesign().containsKey(entry.getKey())) {
                        Item item = framedDrawerModelData.getDesign().get(entry.getKey());
                        quadLists.add(getQuadsUsingShape(item, quads, side, rand));
                    } else {
                        quadLists.add(quads);
                    }
                
            }
            return quadLists.stream().flatMap(Collection::stream).toList();
        }

        protected static List<BakedQuad> getQuadsUsingShape(@Nullable Item frameItem, List<BakedQuad> shape, @Nullable Direction side, Random rand) {
            if (frameItem instanceof BlockItem blockItem) {
                BlockState state1 = blockItem.getBlock().defaultBlockState();
                BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state1);
                Optional<List<Triple<TextureAtlasSprite, Integer, int[]>>> spriteOptional = getSpriteData(model, state1, side, rand, null);
                List<BakedQuad> returnQuads = new ArrayList<>();
                for (BakedQuad shapeQuad : shape) {
                    List<Triple<TextureAtlasSprite, Integer, int[]>> spriteData = spriteOptional.orElse(getSpriteFromModel(shapeQuad, model, state1,null));
                    returnQuads.addAll(framedQuad(shapeQuad, spriteData, state1.getLightEmission(Minecraft.getInstance().level, BlockPos.ZERO)));
                }
                return returnQuads;
            }
            return List.of();
        }

        private static Optional<List<Triple<TextureAtlasSprite, Integer, int[]>>> getSpriteData(BakedModel model, BlockState state, @Nullable Direction side, Random rand, @Nullable Direction rotation) {
            List<BakedQuad> quads = model.getQuads(state, side, rand, EmptyModelData.INSTANCE);
            List<Float> positions = new ArrayList<>();
            List<Triple<TextureAtlasSprite, Integer, int[]>> modelData = new ArrayList<>();
            if (!quads.isEmpty()) {
                for (BakedQuad bakedQuad: quads) {
                    float[] position = unpackVertices(bakedQuad.getVertices(), 0, IQuadTransformer.POSITION, 3);
                    positions.add(getPositionFromDirection(position, side));
                }
                List<Integer> index = getMinMaxPosition(positions, side);
                for (int i = 0; i < index.size(); i++) {
                    int[] lights = new int[4];
                    for (int j=0; j<4 ; j++) {
                        lights[j] = quads.get(i).getVertices()[IQuadTransformer.UV2 + j * IQuadTransformer.STRIDE];
                    }
                    int tint = quads.get(i).isTinted() ? Minecraft.getInstance().getBlockColors().getColor(state, Minecraft.getInstance().level, null, quads.get(i).getTintIndex()) : -1;
                    Triple<TextureAtlasSprite, Integer, int[]> triple = new ImmutableTriple<>(quads.get(i).getSprite(), tint, lights);
                    modelData.add(triple);
                }
            }
            return quads.isEmpty() ? Optional.empty() : Optional.of(modelData);
        }

        private static float getPositionFromDirection(float[] position, Direction side) {
            Vec3i normal = new Vec3i(0,0,0);
            if (side != null) {
                normal = side.getNormal();
            }
            Vector3f vector3f = new Vector3f(position[0] * normal.getX(), position[1] * normal.getY(), position[2] * normal.getZ()); // making a vector with only 1 element at the normal
            return (float) Math.sqrt(vector3f.dot(vector3f));
        }

        private static List<Integer> getMinMaxPosition(List<Float> positions, Direction side) {
            List<Integer> index = new ArrayList<>();
            float minMax = side.getAxisDirection() == Direction.AxisDirection.POSITIVE ? Collections.max(positions) : Collections.min(positions);
            for (int i = 0; i < positions.size(); i++) {
                if (Math.abs(positions.get(i) - minMax) < 0.1) {
                    index.add(i);
                }
            }
            return index;
        }

        protected static List<Triple<TextureAtlasSprite, Integer, int[]>> getSpriteFromModel(BakedQuad shape, BakedModel model, BlockState state, Direction rotation) {
            List<BakedQuad> quads = model.getQuads(state, shape.getDirection(), new Random());
            List<Float> positions = new ArrayList<>();
            List<Triple<TextureAtlasSprite, Integer, int[]>> modelData = new ArrayList<>();
            if (!quads.isEmpty()) {
                for (BakedQuad bakedQuad: quads) {
                    float[] position = unpackVertices(bakedQuad.getVertices(), 0, IQuadTransformer.POSITION, 3);
                    positions.add(getPositionFromDirection(position, shape.getDirection()));

                }
                List<Integer> index = getMinMaxPosition(positions, shape.getDirection());
                for (int i = 0; i < index.size(); i++) {
                    int[] lights = new int[4];
                    for (int j=0; j<4; j++) {
                        lights[j] = quads.get(i).getVertices()[IQuadTransformer.UV2 + j * IQuadTransformer.STRIDE];
                    }
                    int tint = quads.get(i).isTinted() ? Minecraft.getInstance().getBlockColors().getColor(state, Minecraft.getInstance().level, null, quads.get(i).getTintIndex()) : -1;
                    Triple<TextureAtlasSprite, Integer, int[]> triple = new ImmutableTriple<>(quads.get(i).getSprite(), tint, lights);
                    modelData.add(triple);
                }
            }
            return quads.isEmpty() ? List.of(Triple.of(Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(MissingTextureAtlasSprite.getLocation()), -1, new int[] {0,0,0,0})) : modelData;
        }

        protected static List<BakedQuad> framedQuad(BakedQuad toCopy, List<Triple<TextureAtlasSprite, Integer, int[]>> modelData, int lightEmission) {
            lightEmission = LightTexture.pack(lightEmission, lightEmission);
            List<BakedQuad> quads = new ArrayList<>();
            for (int j = 0; j < modelData.size(); j++) {
                BakedQuad copied = new BakedQuad(Arrays.copyOf(toCopy.getVertices(), 32), -1, toCopy.getDirection(), modelData.get(j).getLeft(), toCopy.isShade());

                for (int i = 0; i < 4; i++) {
                    float[] uv0 = unpackVertices(copied.getVertices(), i, IQuadTransformer.UV0, 2);
                    uv0[0] = (uv0[0] - toCopy.getSprite().getU0()) * modelData.get(j).getLeft().getWidth() / toCopy.getSprite().getWidth() + modelData.get(j).getLeft().getU0();
                    uv0[1] = (uv0[1] - toCopy.getSprite().getV0()) * modelData.get(j).getLeft().getHeight() / toCopy.getSprite().getHeight() + modelData.get(j).getLeft().getV0();
                    int[] packedTextureData = packUV(uv0[0], uv0[1]);
                    copied.getVertices()[IQuadTransformer.UV0 + i * IQuadTransformer.STRIDE] = packedTextureData[0];
                    copied.getVertices()[IQuadTransformer.UV0 + 1 + i * IQuadTransformer.STRIDE] = packedTextureData[1];

                    if (modelData.get(j).getMiddle() != -1) {
                        int[] colors = getColorARGB(copied.getVertices(), i);
                        int[] color1 = getColorARGB(modelData.get(j).getMiddle());
                        colors[0] = (colors[0] * color1[0]) / 255;
                        colors[1] = (colors[1] * color1[1]) / 255;
                        colors[2] = (colors[2] * color1[2]) / 255;
                        colors[3] = (colors[3] * color1[3]) / 255;
                        int packedColor = packColor( colors[3], colors[2], colors[1], colors[0]);
                        copied.getVertices()[IQuadTransformer.COLOR + i * IQuadTransformer.STRIDE] = packedColor;
                    }

                    copied.getVertices()[IQuadTransformer.UV2 + i * IQuadTransformer.STRIDE] = Math.max(modelData.get(j).getRight()[i], lightEmission);

                }
                quads.add(copied);
            }
            return quads;
        }

        private static float[] unpackVertices(int[] vertices, int vertexIndex, int position, int count) {
            float[] floats = new float[count];
            int startIndex = vertexIndex * IQuadTransformer.STRIDE + position;
            for (int i = 0; i < count; i++) {
                floats[i] = Float.intBitsToFloat(vertices[startIndex + i]);
            }
            return floats;
        }

        private static int[] getColorARGB(int[] vertices, int vertexIndex) {
            int color = vertices[IQuadTransformer.STRIDE * vertexIndex + IQuadTransformer.COLOR];
            return getColorARGB(color);
        }

        private static int[] getColorARGB(int color) {
            int[] argb = new int[4];
            argb[0] = color >> 24 & 0xFF;
            argb[1] = color >> 16 & 0xFF;
            argb[2] = color >> 8 & 0xFF;
            argb[3] = color & 0xFF;
            return argb;
        }

        private static int[] getUV2(int[] vertices, int vertexIndex) {
            int[] light = new int[2];
            int uv2 = vertices[IQuadTransformer.STRIDE * vertexIndex + IQuadTransformer.UV2];
            light[0] = (uv2 & 0xFFFF) >> 4;
            light[1] = uv2 >> 20 & '\uffff';
            return light;
        }


        public static int[] packUV(float u, float v) {
            int[] quadData = new int[2];
            quadData[0] = Float.floatToRawIntBits(u);
            quadData[1] = Float.floatToRawIntBits(v);
            return quadData;
        }

        public static int packColor(int r, int g, int b, int a) {
            return ((a & 0xFF) << 24) |
                    ((r & 0xFF) << 16) |
                    ((g & 0xFF) << 8)  |
                    ((b & 0xFF));
        }

        public static int packUV2(int u, int v) {
            return u << 4 | v << 20;
        }

        @Override
        public boolean useAmbientOcclusion()
        {
            return isAmbientOcclusion;
        }

        @Override
        public boolean isGui3d()
        {
            return isGui3d;
        }

        @Override
        public boolean usesBlockLight()
        {
            return isSideLit;
        }

        @Override
        public boolean isCustomRenderer()
        {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleIcon()
        {
            return particle;
        }

        @Override
        public TextureAtlasSprite getParticleIcon(@NotNull IModelData data) {
            FramedDrawerModelData framedDrawerModelData = data.getData(FramedDrawerModelData.FRAMED_PROPERTY);
            if (framedDrawerModelData != null && framedDrawerModelData.getDesign().containsKey("particle")) {
                if (framedDrawerModelData.getDesign().get("particle") instanceof BlockItem blockItem) {
                    return Minecraft.getInstance().getBlockRenderer().getBlockModel(blockItem.getBlock().defaultBlockState()).getParticleIcon(data);
                }
            }
            return particle;
        }

        @Override
        public ItemOverrides getOverrides()
        {
            return overrides;
        }

        @Override
        public ItemTransforms getTransforms()
        {
            return transforms;
        }

        @Override
        public List<Pair<BakedModel, RenderType>> getLayerModels(ItemStack itemStack, boolean fabulous)
        {
            return Collections.singletonList(Pair.of(new ItemModel(this, itemStack), ItemBlockRenderTypes.getRenderType(itemStack, fabulous)));
        }

        @Override
        public boolean isLayered() {
            return true;
        }

        @Nullable
        public BakedModel getPart(String name)
        {
            return children.get(name);
        }
    }

    /**
     * A model data container which stores data for child components.
     */
    public static class Data
    {
        public static final ModelProperty<FramedModel.Data> PROPERTY = new ModelProperty<>();

        private final Map<String, IModelData> partData;

        private Data(Map<String, IModelData> partData)
        {
            this.partData = partData;
        }

        @Nullable
        public IModelData get(String name)
        {
            return partData.get(name);
        }

        /**
         * Helper to get the data from a {@link IModelData} instance.
         *
         * @param modelData The object to get data from
         * @param name      The name of the part to get data for
         * @return The data for the part, or the one passed in if not found
         */
        public static IModelData resolve(IModelData modelData, String name)
        {
            var compositeData = modelData.getData(PROPERTY);
            if (compositeData == null)
                return modelData;
            var partData = compositeData.get(name);
            return partData != null ? partData : modelData;
        }

        public static FramedModel.Data.Builder builder()
        {
            return new FramedModel.Data.Builder();
        }

        public static final class Builder
        {
            private final Map<String, IModelData> partData = new IdentityHashMap<>();

            public FramedModel.Data.Builder with(String name, IModelData data)
            {
                partData.put(name, data);
                return this;
            }

            public FramedModel.Data build()
            {
                return new FramedModel.Data(partData);
            }
        }
    }

    public static final class Loader implements IModelLoader<FramedModel>
    {
        public static final FramedModel.Loader INSTANCE = new FramedModel.Loader();

        private Loader()
        {
        }

        @Override
        public FramedModel read(JsonDeserializationContext deserializationContext, JsonObject jsonObject)
        {
            List<String> itemPasses = new ArrayList<>();
            ImmutableMap.Builder<String, Submodel> childrenBuilder = ImmutableMap.builder();
            readChildren(jsonObject, "children", deserializationContext, childrenBuilder, itemPasses, false);
            boolean logWarning = readChildren(jsonObject, "parts", deserializationContext, childrenBuilder, itemPasses, true);

            var children = childrenBuilder.build();
            if (children.isEmpty())
                throw new JsonParseException("Composite model requires a \"children\" element with at least one element.");

            if (jsonObject.has("item_render_order"))
            {
                itemPasses.clear();
                for (var element : jsonObject.getAsJsonArray("item_render_order"))
                {
                    var name = element.getAsString();
                    if (!children.containsKey(name))
                        throw new JsonParseException("Specified \"" + name + "\" in \"item_render_order\", but that is not a child of this model.");
                    itemPasses.add(name);
                }
            }

            return new FramedModel(children, ImmutableList.copyOf(itemPasses), logWarning);
        }

        private boolean readChildren(JsonObject jsonObject, String name, JsonDeserializationContext deserializationContext, ImmutableMap.Builder<String, Submodel> children, List<String> itemPasses, boolean logWarning)
        {
            if (!jsonObject.has(name))
                return false;
            var childrenJsonObject = jsonObject.getAsJsonObject(name);
            for (Map.Entry<String, JsonElement> entry : childrenJsonObject.entrySet())
            {
                children.put(entry.getKey(), new Submodel(name, deserializationContext.deserialize(entry.getValue(), BlockModel.class) ));
                itemPasses.add(entry.getKey()); // We can do this because GSON preserves ordering during deserialization
            }
            return logWarning;
        }

        @Override
        public void onResourceManagerReload(ResourceManager p_10758_) {

        }

    }

    private static class Submodel implements IModelGeometryPart
    {
        private final String name;
        private final BlockModel model;

        private Submodel(String name, BlockModel model)
        {
            this.name = name;
            this.model = model;
        }

        @Override
        public String name()
        {
            return name;
        }

        @Override
        public void addQuads(IModelConfiguration owner, IModelBuilder<?> modelBuilder, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ResourceLocation modelLocation)
        {
            throw new UnsupportedOperationException("Attempted to call adQuads on a Submodel instance. Please don't.");
        }

        public BakedModel bakeModel(ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ResourceLocation modelLocation)
        {
            return model.bake(bakery, spriteGetter, modelTransform, modelLocation);
        }

        @Override
        public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors)
        {
            return model.getMaterials(modelGetter, missingTextureErrors);
        }
    }

    private class ItemModel implements IDynamicBakedModel {

        private final Baked baked;
        private final ItemStack itemStack;

        public ItemModel(Baked baked, ItemStack itemStack) {
            this.baked = baked;
            this.itemStack = itemStack;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData extraData) {
            List<List<BakedQuad>> quadLists = new ArrayList<>();
            for (Map.Entry<String, BakedModel> entry : baked.children.entrySet())
            {
                    List<BakedQuad> quads = entry.getValue().getQuads(state, side, rand, Data.resolve(extraData, entry.getKey()));
                    FramedDrawerModelData framedDrawerModelData = FramedDrawerBlock.getDrawerModelData(itemStack);
                    if (framedDrawerModelData != null && framedDrawerModelData.getDesign().containsKey(entry.getKey())) {
                        Item item = framedDrawerModelData.getDesign().get(entry.getKey());
                        quadLists.add(getQuadsUsingShape(item, quads, side, rand));
                    } else {
                        quadLists.add(quads);
                    }
                }

            return quadLists.stream().flatMap(Collection::stream).toList();
        }

        @Override
        public boolean useAmbientOcclusion() {
            return false;
        }

        @Override
        public boolean isGui3d() {
            return true;
        }

        @Override
        public boolean usesBlockLight() {
            return true;
        }

        @Override
        public boolean isCustomRenderer() {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return baked.getParticleIcon();
        }

        @Override
        public ItemOverrides getOverrides() {
            return ItemOverrides.EMPTY;
        }

        @Override
        public ItemTransforms getTransforms() {
            return baked.getTransforms();
        }
    }

    private class RetexturedOverride extends ItemOverrides {

        private final BakedModel bakedModel;

        private RetexturedOverride(BakedModel bakedModel) {
            this.bakedModel = bakedModel;
        }

        @javax.annotation.Nullable
        @Override
        public BakedModel resolve(BakedModel originalModel, ItemStack stack, @javax.annotation.Nullable ClientLevel world, @javax.annotation.Nullable LivingEntity entity, int pSeed) {

            // if valid, use the block
            return bakedModel;
        }
    }
}
