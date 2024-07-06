package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.block.tile.CompactingDrawerTile;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

import static com.buuz135.functionalstorage.util.MathUtils.createTransformMatrix;

public abstract class BaseDrawerRenderer<T extends ControllableDrawerTile<T>> implements BlockEntityRenderer<T> {
    @Override
    public final void render(T tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (Minecraft.getInstance().player != null && !tile.getBlockPos().closerThan(Minecraft.getInstance().player.getOnPos(), FunctionalStorageClientConfig.DRAWER_RENDER_RANGE)){
            return;
        }
        matrixStack.pushPose();

        Direction facing = tile.getFacingDirection();
        matrixStack.mulPose(createTransformMatrix(
                new Vector3f(0), new Vector3f(0, 180, 0), 1));

        if (facing == Direction.NORTH) {
            matrixStack.mulPose(createTransformMatrix(
                    new Vector3f(-1, 0, 0), new Vector3f(0), 1));
        }
        else if (facing == Direction.EAST) {
            matrixStack.mulPose(createTransformMatrix(
                    new Vector3f(-1, 0, -1), new Vector3f(0, -90, 0), 1));
        }
        else if (facing == Direction.SOUTH) {
            matrixStack.mulPose(createTransformMatrix(
                    new Vector3f(0, 0, -1), new Vector3f(0, 180, 0), 1));
        }
        else if (facing == Direction.WEST) {
            matrixStack.mulPose(createTransformMatrix(
                    new Vector3f(0, 0, 0), new Vector3f(0, 90, 0), 1));
        }

        matrixStack.translate(0,0,-0.5/16D);
        combinedLightIn = LevelRenderer.getLightColor(tile.getLevel(), tile.getBlockPos().relative(facing));
        DrawerRenderer.renderUpgrades(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);

        renderItems(tile, partialTicks, matrixStack, bufferIn, combinedLightIn, combinedOverlayIn);
    }

    public abstract void renderItems(T tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn);
}
