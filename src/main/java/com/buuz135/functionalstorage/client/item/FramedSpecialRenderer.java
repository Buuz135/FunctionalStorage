package com.buuz135.functionalstorage.client.item;

import com.buuz135.functionalstorage.block.FramedBlock;
import com.buuz135.functionalstorage.block.FramedDrawerBlock;
import com.buuz135.functionalstorage.client.loader.FramedModel;
import com.buuz135.functionalstorage.util.CustomFramedDrawerModelData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Arrays;
import java.util.function.Consumer;


public final class FramedSpecialRenderer implements SpecialModelRenderer<FramedSpecialRenderer.RenderData> {
    @Override
    public void submit(@Nullable RenderData data, PoseStack pose, SubmitNodeCollector collector, int light,
                       int overlay, boolean foil, int outlineColor) {
        if (data == null) return;

        var state = data.block().defaultBlockState();
        var model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
        var baseParts = FramedModel.collectBaseParts(model, state);
        var styledParts = FramedModel.styleParts(baseParts, data.design(), BlockAndTintGetter.EMPTY, BlockPos.ZERO, true);
        var quads = FramedModel.flattenParts(styledParts);

        int[] tints = new int[16];
        Arrays.fill(tints, 0xFFFFFFFF);
        collector.submitItem(pose, ItemDisplayContext.NONE, light, overlay, outlineColor, tints, quads,
                foil ? ItemStackRenderState.FoilType.STANDARD : ItemStackRenderState.FoilType.NONE);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        for (int x = 0; x <= 1; x++) {
            for (int y = 0; y <= 1; y++) {
                for (int z = 0; z <= 1; z++) output.accept(new Vector3f(x, y, z));
            }
        }
    }

    @Override
    public @Nullable RenderData extractArgument(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem) || !(blockItem.getBlock() instanceof FramedBlock)) {
            return null;
        }
        CustomFramedDrawerModelData design = FramedDrawerBlock.getDrawerModelData(stack);
        return new RenderData(blockItem.getBlock(), design);
    }

    public record RenderData(Block block, @Nullable CustomFramedDrawerModelData design) {}

    public enum Unbaked implements SpecialModelRenderer.Unbaked<RenderData> {
        INSTANCE;

        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public FramedSpecialRenderer bake(SpecialModelRenderer.BakingContext context) {
            return new FramedSpecialRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
