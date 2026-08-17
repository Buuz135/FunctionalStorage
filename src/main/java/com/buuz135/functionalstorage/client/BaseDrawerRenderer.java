package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.block.Drawer;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import static com.buuz135.functionalstorage.util.MathUtils.createTransformMatrix;

public abstract class BaseDrawerRenderer<T extends ControllableDrawerTile<T>>
        implements BlockEntityRenderer<T, BaseDrawerRenderer.DrawerRenderState<T>> {

    public static class DrawerRenderState<T extends ControllableDrawerTile<T>> extends BlockEntityRenderState {
        public T tile;
    }

    @Override
    public DrawerRenderState<T> createRenderState() {
        return new DrawerRenderState<>();
    }

    @Override
    public void extractRenderState(T tile, DrawerRenderState<T> state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(tile, state, breakProgress);
        state.tile = tile;
    }

    @Override
    public int getViewDistance() {
        return FunctionalStorageClientConfig.DRAWER_RENDER_RANGE;
    }

    @Override
    public final void submit(DrawerRenderState<T> state, PoseStack matrixStack, SubmitNodeCollector collector, CameraRenderState camera) {
        T tile = state.tile;
        if (tile == null) return;
        matrixStack.pushPose();

        Direction subfacing = tile.getFacingDirection();

        if (tile.getBlockState().hasProperty(Drawer.FACING_ALL)) {
            Direction facing = tile.getBlockState().getValue(Drawer.FACING_ALL);
            if (subfacing == Direction.UP) {
                matrixStack.mulPose(createTransformMatrix(new Vector3f(1, 1, 0), new Vector3f(90, 0, 0), 1));
                if (facing == Direction.EAST) {
                    matrixStack.mulPose(createTransformMatrix(new Vector3f(-1, 0, 0), new Vector3f(0, 0, -90), 1));
                } else if (facing == Direction.WEST) {
                    matrixStack.mulPose(createTransformMatrix(new Vector3f(0, 1, 0), new Vector3f(0, 0, 90), 1));
                }
            }
            if (subfacing == Direction.DOWN) {
                matrixStack.mulPose(createTransformMatrix(new Vector3f(0, 0, 0), new Vector3f(-90, 0, -180), 1));
                if (facing == Direction.WEST) {
                    matrixStack.mulPose(createTransformMatrix(new Vector3f(-1, 0, 0), new Vector3f(0, 0, -90), 1));
                } else if (facing == Direction.EAST) {
                    matrixStack.mulPose(createTransformMatrix(new Vector3f(0, 1, 0), new Vector3f(0, 0, 90), 1));
                }
            }
            if (facing == Direction.NORTH) {
                matrixStack.mulPose(createTransformMatrix(new Vector3f(-1, 1, 0), new Vector3f(0, 0, 180), 1));
            }
        }
        matrixStack.mulPose(createTransformMatrix(new Vector3f(0), new Vector3f(0, 180, 0), 1));
        if (subfacing == Direction.NORTH) {
            matrixStack.mulPose(createTransformMatrix(new Vector3f(-1, 0, 0), new Vector3f(0), 1));
        } else if (subfacing == Direction.EAST) {
            matrixStack.mulPose(createTransformMatrix(new Vector3f(-1, 0, -1), new Vector3f(0, -90, 0), 1));
        } else if (subfacing == Direction.SOUTH) {
            matrixStack.mulPose(createTransformMatrix(new Vector3f(0, 0, -1), new Vector3f(0, 180, 0), 1));
        } else if (subfacing == Direction.WEST) {
            matrixStack.mulPose(createTransformMatrix(new Vector3f(0, 0, 0), new Vector3f(0, 90, 0), 1));
        }

        matrixStack.translate(0, 0, -0.5 / 16D);
        int lightCoords = tile.getLevel() != null
                ? LevelRenderer.getLightCoords(tile.getLevel(), tile.getBlockPos().relative(subfacing))
                : state.lightCoords;
        DrawerRenderer.renderUpgrades(matrixStack, collector, lightCoords, tile);

        renderItems(tile, matrixStack, collector, lightCoords);
    }

    public abstract void renderItems(T tile, PoseStack matrixStack, SubmitNodeCollector collector, int lightCoords);
}
