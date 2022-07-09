/*
 * This file is part of Industrial Foregoing.
 *
 * Copyright 2021, Buuz135
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.buuz135.functionalstorage.client.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class FramedDrawerBlockModel implements IDynamicBakedModel {

    public static Cache<Pair<Pair<String, Pair<Direction, Direction>>, Direction>, List<BakedQuad>> CACHE = CacheBuilder.newBuilder().build();
    private VertexFormat format;
    private BakedModel previousModel;
    private Map<Direction, List<BakedQuad>> prevQuads = new HashMap<>();

    public FramedDrawerBlockModel(BakedModel previousConveyor) {
        this.previousModel = previousConveyor;
        this.format = DefaultVertexFormat.BLOCK;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        List<BakedQuad> bakedQuads = new ArrayList<>();
        BakedModel otherModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(Blocks.STONE.defaultBlockState());
        for (BakedQuad quad : previousModel.getQuads(state, side, rand)) {
            if (otherModel.getQuads(state, side, rand).size() > 0) {
                BakedQuadBuilder bakedQuadBuilder = new BakedQuadBuilder(otherModel.getQuads(state, side, rand).get(0).getSprite());
                bakedQuadBuilder.setQuadOrientation(quad.getDirection());
                bakedQuadBuilder.setQuadTint(quad.getTintIndex());
                bakedQuadBuilder.setApplyDiffuseLighting(quad.isShade());
                quad.pipe(bakedQuadBuilder);

                bakedQuadBuilder.setTexture(otherModel.getQuads(state, side, rand).get(0).getSprite());
                System.out.println(otherModel.getQuads(state, side, rand).get(0).getVertices().length);
                BakedQuad extra = bakedQuadBuilder.build();
                System.out.println(Arrays.toString(extra.getVertices()));
                bakedQuads.add(extra);
                //bakedQuadBuilder.getVertexFormat()
                //bakedQuads.add(bakedQuadBuilder.build());
            }
        }
        return bakedQuads;/*
        if (state == null) {
            if (!prevQuads.containsKey(side))
                prevQuads.put(side, previousModel.getQuads(state, side, rand));
            return prevQuads.get(side);
        }
        if (!prevQuads.containsKey(side))
            prevQuads.put(side, previousModel.getQuads(state, side, rand));
        List<BakedQuad> quads = new ArrayList<>(prevQuads.get(side));
        return quads;*/
    }

    @Override
    public boolean useAmbientOcclusion() {
        return previousModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return previousModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return previousModel.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return previousModel.isCustomRenderer();
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleIcon() {
        return previousModel.getParticleIcon();
    }

    @Nonnull
    @Override
    public ItemOverrides getOverrides() {
        return previousModel.getOverrides();
    }

    @Override
    public BakedModel handlePerspective(ItemTransforms.TransformType cameraTransformType, PoseStack mat) {
        return previousModel.handlePerspective(cameraTransformType, mat);
    }

    @Override
    public ItemTransforms getTransforms() {
        return previousModel.getTransforms();
    }
}