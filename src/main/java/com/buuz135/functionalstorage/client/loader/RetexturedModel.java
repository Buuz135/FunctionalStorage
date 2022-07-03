package com.buuz135.functionalstorage.client.loader;

import com.buuz135.functionalstorage.block.FramedDrawerBlock;
import com.buuz135.functionalstorage.client.model.FramedDrawerModelData;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.geometry.IModelGeometry;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Class from Mantle {@url https://github.com/SlimeKnights/Mantle/blob/1.18.2/src/main/java/slimeknights/mantle/client/model/RetexturedModel.java}
 *
 * Model that dynamically retextures a list of textures based on data from {@link RetexturedHelper}.
 */
@SuppressWarnings("WeakerAccess")

public class RetexturedModel implements IModelGeometry<RetexturedModel> {
    private final SimpleBlockModel model;
    private final Set<String> retextured;

    public RetexturedModel(SimpleBlockModel model, Set<String> retextured) {
        this.model = model;
        this.retextured = retextured;
    }

    @Override
    public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation,UnbakedModel> modelGetter, Set<Pair<String,String>> missingTextureErrors) {
        return model.getTextures(owner, modelGetter, missingTextureErrors);
    }

    @Override
    public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material,TextureAtlasSprite> spriteGetter, ModelState transform, ItemOverrides overrides, ResourceLocation location) {
        // bake the model and return
        BakedModel baked = model.bakeModel(owner, transform, overrides, spriteGetter, location);
        return new Baked(baked, owner, model, transform, getAllRetextured(owner, this.model, retextured));
    }

    /**
     * Gets a list of all names to retexture based on the block model texture references
     * @param owner        Model config instance
     * @param model        Model fallback
     * @param originalSet  Original list of names to retexture
     * @return  Set of textures including parent textures
     */
    public static Set<String> getAllRetextured(IModelConfiguration owner, SimpleBlockModel model, Set<String> originalSet) {
        Set<String> retextured = Sets.newHashSet(originalSet);
        for (Map<String,Either<Material, String>> textures : ModelTextureIteratable.of(owner, model)) {
            textures.forEach((name, either) ->
                    either.ifRight(parent -> {
                        if (retextured.contains(parent)) {
                            retextured.add(name);
                        }
                    })
            );
        }
        return ImmutableSet.copyOf(retextured);
    }


    /** Registered model loader instance registered */
    public static class Loader implements IModelLoader<RetexturedModel> {
        public static final Loader INSTANCE = new Loader();
        private Loader() {}

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {}

        @Override
        public RetexturedModel read(JsonDeserializationContext context, JsonObject json) {
            // get base model
            SimpleBlockModel model = SimpleBlockModel.deserialize(context, json);

            // get list of textures to retexture
            Set<String> retextured = getRetextured(json);

            // return retextured model
            return new RetexturedModel(model, retextured);
        }

        /**
         * Gets the list of retextured textures from the model
         * @param json  Model json
         * @return  List of textures
         */
        public static Set<String> getRetextured(JsonObject json) {
            if (json.has("retextured")) {
                // if an array, set from each texture in array
                JsonElement retextured = json.get("retextured");
                if (retextured.isJsonArray()) {
                    JsonArray array = retextured.getAsJsonArray();
                    if (array.size() == 0) {
                        throw new JsonSyntaxException("Must have at least one texture in retextured");
                    }
                    ImmutableSet.Builder<String> builder = ImmutableSet.builder();
                    for (int i = 0; i < array.size(); i++) {
                        builder.add(GsonHelper.convertToString(array.get(i), "retextured[" + i + "]"));
                    }
                    return builder.build();
                }
                // if string, single texture
                if (retextured.isJsonPrimitive()) {
                    return ImmutableSet.of(retextured.getAsString());
                }
            }
            // if neither or missing, error
            throw new JsonSyntaxException("Missing retextured, expected to find a String or a JsonArray");
        }
    }

    /** Baked variant of the model, used to swap out quads based on the texture */
    public static class Baked extends DynamicBakedWrapper<BakedModel> {
        /** Cache of texture name to baked model */
        private final Map<String, BakedModel> cache = new ConcurrentHashMap<>();
        /* Properties for rebaking */
        private final IModelConfiguration owner;
        private final SimpleBlockModel model;
        private final ModelState transform;
        /** List of texture names that are retextured */
        private final Set<String> retextured;

        public Baked(BakedModel baked, IModelConfiguration owner, SimpleBlockModel model, ModelState transform, Set<String> retextured) {
            super(baked);
            this.model = model;
            this.owner = owner;
            this.transform = transform;
            this.retextured = retextured;
        }

        /**
         * Gets the model with the given texture applied
         * @param framedDrawerModelData  Texture location
         * @return  Retextured model
         */
        private BakedModel getRetexturedModel(FramedDrawerModelData framedDrawerModelData) {
            return model.bakeDynamic(new RetexturedConfiguration(owner, retextured, framedDrawerModelData), transform);
        }

        /**
         * Gets a cached retextured model, computing it if missing from the cache
         * @param framedDrawerModelData  Block determining the texture
         * @return  Retextured model
         */
        private BakedModel getCachedModel(FramedDrawerModelData framedDrawerModelData) {
            return cache.computeIfAbsent(framedDrawerModelData.getCode(), (s) -> this.getRetexturedModel(framedDrawerModelData));
        }

        @Override
        public TextureAtlasSprite getParticleIcon(IModelData data) {
            // if particle is retextured, fetch particle from the cached model
            if (retextured.contains("particle")) {
                FramedDrawerModelData framedDrawerModelData = data.getData(FramedDrawerModelData.FRAMED_PROPERTY);
                if (framedDrawerModelData != null) {
                    return getCachedModel(framedDrawerModelData).getParticleIcon(data);
                }
            }
            return originalModel.getParticleIcon(data);
        }

        @Nonnull
        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, Random random, IModelData data) {
            FramedDrawerModelData framedDrawerModelData = data.getData(FramedDrawerModelData.FRAMED_PROPERTY);
            if (framedDrawerModelData == null) {
                return originalModel.getQuads(state, direction, random, data);
            }
            return getCachedModel(framedDrawerModelData).getQuads(state, direction, random, data);
        }

        @Override
        public ItemOverrides getOverrides() {
            return RetexturedOverride.INSTANCE;
        }
    }

    /**
     * Model configuration wrapper to retexture the block
     */
    public static class RetexturedConfiguration extends ModelConfigurationWrapper {
        /** List of textures to retexture */
        private final Set<String> retextured;
        /** Replacement texture */
        private final HashMap<String, Material> texture;

        /**
         * Creates a new configuration wrapper
         * @param base        Original model configuration
         * @param retextured  Set of textures that should be retextured
         * @param texture     New texture to replace those in the set
         */
        public RetexturedConfiguration(IModelConfiguration base, Set<String> retextured, FramedDrawerModelData texture) {
            super(base);
            this.retextured = retextured;
            this.texture = new HashMap<>();
            texture.getDesign().forEach((s, item) -> {
                this.texture.put(s, ModelLoaderRegistry.blockMaterial(ModelHelper.getParticleTexture(item)));
            });
        }

        @Override
        public boolean isTexturePresent(String name) {
            if (retextured.contains(name) && texture.containsKey(name)) {
                return !MissingTextureAtlasSprite.getLocation().equals(texture.get(name).texture());
            }
            return super.isTexturePresent(name);
        }

        @Override
        public Material resolveTexture(String name) {
            if (retextured.contains(name) && texture.containsKey(name)) {
                return texture.get(name);
            }
            return super.resolveTexture(name);
        }
    }

    /** Override list to swap the texture in from NBT */
    private static class RetexturedOverride extends ItemOverrides {
        private static final RetexturedOverride INSTANCE = new RetexturedOverride();

        @Nullable
        @Override
        public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int pSeed) {
            if (stack.isEmpty() || !stack.hasTag()) {
                return originalModel;
            }

            // get the block first, ensuring its valid
            FramedDrawerModelData data = FramedDrawerBlock.getDrawerModelData(stack);
            if (data == null) {
                return originalModel;
            }

            // if valid, use the block
            return ((Baked)originalModel).getCachedModel(data);
        }
    }
}
