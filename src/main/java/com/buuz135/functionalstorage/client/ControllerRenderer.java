package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.block.tile.StorageControllerTile;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.hrznstudio.titanium.util.RayTraceUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class ControllerRenderer implements BlockEntityRenderer<StorageControllerTile<?>, ControllerRenderer.State> {
    public static final class State extends BlockEntityRenderState {
        private VoxelShape links = Shapes.empty();
        private AABB area;
        private BlockPos origin = BlockPos.ZERO;
        private boolean visible;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(StorageControllerTile<?> tile, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(tile, state, breakProgress);
        state.visible = false;
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;

        var stack = minecraft.player.getMainHandItem();
        if (!(stack.getItem() instanceof LinkingToolItem)) return;
        BlockPos controller = stack.get(FSAttachments.CONTROLLER);
        if (controller == null || !controller.equals(tile.getBlockPos())) return;

        state.origin = controller;
        if (stack.has(FSAttachments.FIRST_POSITION)) {
            BlockPos first = stack.get(FSAttachments.FIRST_POSITION);
            HitResult result = RayTraceUtils.rayTraceSimple(minecraft.level, minecraft.player, 8, partialTicks);
            if (first != null && result instanceof BlockHitResult blockHit) {
                BlockPos hit = blockHit.getBlockPos();
                state.links = Shapes.create(new AABB(
                        Math.min(first.getX(), hit.getX()), Math.min(first.getY(), hit.getY()), Math.min(first.getZ(), hit.getZ()),
                        Math.max(first.getX(), hit.getX()) + 1, Math.max(first.getY(), hit.getY()) + 1, Math.max(first.getZ(), hit.getZ()) + 1));
                state.area = null;
                state.visible = true;
                return;
            }
        }

        VoxelShape links = tile.getConnectedDrawers().getCachedVoxelShape();
        if (links == null || tile.getLevel().getGameTime() % 400 == 0) {
            tile.getConnectedDrawers().rebuildShapes();
            links = tile.getConnectedDrawers().getCachedVoxelShape();
        }
        state.links = links == null ? Shapes.empty() : links;
        state.area = new AABB(controller).inflate(tile.getStorageMultiplier() + 0.001);
        state.visible = true;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.visible) return;
        double x = -state.origin.getX();
        double y = -state.origin.getY();
        double z = -state.origin.getZ();
        collector.submitCustomGeometry(poseStack, RenderTypes.linesTranslucent(),
                (pose, consumer) -> renderShape(pose, consumer, state.links, x, y, z, 1, 1, 1, 1));
        if (state.area != null) {
            collector.submitCustomGeometry(poseStack, RenderTypes.linesTranslucent(),
                    (pose, consumer) -> renderShape(pose, consumer, Shapes.create(state.area), x, y, z, 0.5F, 1, 0.5F, 1));
            collector.submitCustomGeometry(poseStack, RenderTypes.debugFilledBox(),
                    (pose, consumer) -> renderFaces(pose, consumer, state.area, x, y, z, 0.5F, 1, 0.5F, 0.25F));
        }
    }

    private static void renderShape(PoseStack.Pose pose, VertexConsumer consumer, VoxelShape shape,
                                    double x, double y, double z, float red, float green, float blue, float alpha) {
        shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            float nx = (float) (x2 - x1);
            float ny = (float) (y2 - y1);
            float nz = (float) (z2 - z1);
            float length = Mth.sqrt(nx * nx + ny * ny + nz * nz);
            nx /= length;
            ny /= length;
            nz /= length;
            consumer.addVertex(pose.pose(), (float) (x1 + x), (float) (y1 + y), (float) (z1 + z))
                    .setColor(red, green, blue, alpha)
                    .setNormal(pose, nx, ny, nz)
                    .setLineWidth(Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth);
            consumer.addVertex(pose.pose(), (float) (x2 + x), (float) (y2 + y), (float) (z2 + z))
                    .setColor(red, green, blue, alpha)
                    .setNormal(pose, nx, ny, nz)
                    .setLineWidth(Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth);
        });
    }

    private static void renderFaces(PoseStack.Pose pose, VertexConsumer consumer, AABB box,
                                    double x, double y, double z, float red, float green, float blue, float alpha) {
        float x1 = (float) (box.minX + x), x2 = (float) (box.maxX + x);
        float y1 = (float) (box.minY + y), y2 = (float) (box.maxY + y);
        float z1 = (float) (box.minZ + z), z2 = (float) (box.maxZ + z);
        quad(pose, consumer, red, green, blue, alpha, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1);
        quad(pose, consumer, red, green, blue, alpha, x2, y1, z2, x2, y2, z2, x1, y2, z2, x1, y1, z2);
        quad(pose, consumer, red, green, blue, alpha, x1, y1, z2, x1, y2, z2, x1, y2, z1, x1, y1, z1);
        quad(pose, consumer, red, green, blue, alpha, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2);
        quad(pose, consumer, red, green, blue, alpha, x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1);
        quad(pose, consumer, red, green, blue, alpha, x1, y1, z2, x1, y1, z1, x2, y1, z1, x2, y1, z2);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer consumer, float r, float g, float b, float a, float... xyz) {
        for (int i = 0; i < xyz.length; i += 3) {
            consumer.addVertex(pose.pose(), xyz[i], xyz[i + 1], xyz[i + 2]).setColor(r, g, b, a);
        }
    }

    @Override
    public boolean shouldRender(StorageControllerTile<?> tile, Vec3 cameraPosition) {
        return true;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}
