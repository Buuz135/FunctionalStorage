package com.buuz135.functionalstorage.client.loader;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraftforge.client.model.IModelConfiguration;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Class from Mantle {@url https://github.com/SlimeKnights/Mantle/blob/1.18.2/src/main/java/slimeknights/mantle/client/}
 */
public class ModelTextureIteratable implements Iterable<Map<String,Either<Material, String>>> {
    /** Initial map for iteration */
    @Nullable
    private final Map<String,Either<Material, String>> startMap;
    /** Initial model for iteration */
    @Nullable
    private final BlockModel startModel;

    public ModelTextureIteratable(@Nullable Map<String, Either<Material, String>> startMap, @Nullable BlockModel startModel) {
        this.startMap = startMap;
        this.startModel = startModel;
    }

    /**
     * Creates an iterable over the given model
     * @param model  Model
     */
    public ModelTextureIteratable(BlockModel model) {
        this(null, model);
    }

    /**
     *
     * @param owner     Model configuration owner
     * @param fallback  Fallback in case the owner does not contain a block model
     * @return  Iteratable over block model texture maps
     */
    public static ModelTextureIteratable of(IModelConfiguration owner, SimpleBlockModel fallback) {
        UnbakedModel unbaked = owner.getOwnerModel();
        if (unbaked instanceof BlockModel) {
            return new ModelTextureIteratable(null, (BlockModel)unbaked);
        }
        return new ModelTextureIteratable(fallback.getTextures(), fallback.getParent());
    }

    @Override
    public MapIterator iterator() {
        return new MapIterator(startMap, startModel);
    }

    private static class MapIterator implements Iterator<Map<String,Either<Material, String>>> {

        public MapIterator(@Nullable Map<String, Either<Material, String>> initial, @Nullable BlockModel model) {
            this.initial = initial;
            this.model = model;
        }

        /** Initial map for iteration */
        @Nullable
        private Map<String,Either<Material, String>> initial;
        /** current model in the iterator */
        @Nullable
        private BlockModel model;

        @Override
        public boolean hasNext() {
            return initial != null || model != null;
        }

        @Override
        public Map<String,Either<Material,String>> next() {
            Map<String,Either<Material, String>> map;
            if (initial != null) {
                map = initial;
                initial = null;
            } else if (model != null) {
                map = model.textureMap;
                model = model.parent;
            } else {
                throw new NoSuchElementException();
            }
            return map;
        }
    }
}
