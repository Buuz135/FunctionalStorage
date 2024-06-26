package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.block.config.FunctionalStorageConfig;
import com.buuz135.functionalstorage.block.tile.StorageControllerTile;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.hrznstudio.titanium.util.RayTraceUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;

import java.util.List;

public class ControllerRenderer implements BlockEntityRenderer<StorageControllerTile<?>> {

    public static RenderType TYPE = RenderType.create("custom_lines", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 256, false, false,RenderType.CompositeState.builder()
            .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeLinesShader))
            .setDepthTestState(new RenderStateShard.DepthTestStateShard("always", 519))
//            .set(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
            .setLayeringState(new RenderStateShard.LayeringStateShard("view_offset_z_layering", () -> {
                var posestack = RenderSystem.getModelViewStack();
                posestack.pushMatrix();
                posestack.scale(0.99975586F, 0.99975586F, 0.99975586F);
                RenderSystem.applyModelViewMatrix();
            }, () -> {
                var posestack = RenderSystem.getModelViewStack();
                posestack.popMatrix();
                RenderSystem.applyModelViewMatrix();
            }))
            .setCullState(new RenderStateShard.CullStateShard(false))
            .createCompositeState(false));


    private static void renderShape(PoseStack p_109783_, VertexConsumer p_109784_, VoxelShape p_109785_, double p_109786_, double p_109787_, double p_109788_, float p_109789_, float p_109790_, float p_109791_, float p_109792_) {
        PoseStack.Pose posestack$pose = p_109783_.last();
        p_109785_.forAllEdges((p_194324_, p_194325_, p_194326_, p_194327_, p_194328_, p_194329_) -> {
            float f = (float) (p_194327_ - p_194324_);
            float f1 = (float) (p_194328_ - p_194325_);
            float f2 = (float) (p_194329_ - p_194326_);
            float f3 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
            f /= f3;
            f1 /= f3;
            f2 /= f3;
            p_109784_.addVertex(posestack$pose.pose(), (float) (p_194324_ + p_109786_), (float) (p_194325_ + p_109787_), (float) (p_194326_ + p_109788_)).setColor(p_109789_, p_109790_, p_109791_, p_109792_).setNormal(posestack$pose, f, f1, f2);
            p_109784_.addVertex(posestack$pose.pose(), (float) (p_194327_ + p_109786_), (float) (p_194328_ + p_109787_), (float) (p_194329_ + p_109788_)).setColor(p_109789_, p_109790_, p_109791_, p_109792_).setNormal(posestack$pose, f, f1, f2);
        });
    }

    @Override
    public void render(StorageControllerTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        ItemStack stack = Minecraft.getInstance().player.getMainHandItem();
        if (stack.isEmpty()) return;
        if (stack.getItem() instanceof LinkingToolItem) {
            BlockPos controller = FSAttachments.CONTROLLER.get(stack);
            if (!controller.equals(tile.getBlockPos())) return;
            if (stack.has(FSAttachments.FIRST_POSITION)) {
                BlockPos firstPos = stack.get(FSAttachments.FIRST_POSITION);
                HitResult result = RayTraceUtils.rayTraceSimple(Minecraft.getInstance().level, Minecraft.getInstance().player, 8, partialTicks);
                if (result.getType() == HitResult.Type.BLOCK){
                    BlockPos hit = ((BlockHitResult)result).getBlockPos();
                    AABB aabb = new AABB(Math.min(firstPos.getX(), hit.getX()), Math.min(firstPos.getY(), hit.getY()), Math.min(firstPos.getZ(), hit.getZ()), Math.max(firstPos.getX(), hit.getX()) + 1, Math.max(firstPos.getY(), hit.getY()) + 1, Math.max(firstPos.getZ(), hit.getZ()) + 1);
                    VoxelShape shape = Shapes.create(aabb);
                    renderShape(matrixStack, bufferIn.getBuffer(TYPE), shape, -controller.getX(), -controller.getY(), -controller.getZ(), 1f, 1f, 1f, 1f);
                    return;
                }
            }


            VoxelShape shape = tile.getConnectedDrawers().getCachedVoxelShape();
            if (shape == null || tile.getLevel().getGameTime() % 400 == 0) {
                tile.getConnectedDrawers().rebuildShapes();
                shape = tile.getConnectedDrawers().getCachedVoxelShape();
            }
            //LevelRenderer.renderVoxelShape(matrixStack, bufferIn.getBuffer(TYPE), shape, -tile.getBlockPos().getX(), -tile.getBlockPos().getY(), -tile.getBlockPos().getZ(), 1f, 1f, 1f, 1f);
            List<AABB> list = shape.toAabbs();
            int i = Mth.ceil((double) list.size() / 3.0D);

            for (int j = 0; j < list.size(); ++j) {
                AABB aabb = list.get(j);
                float f = ((float) j % (float) i + 1.0F) / (float) i;
                float f1 = (float) (j / i);
                float f2 = 1;
                float f3 = 1;
                float f4 = 1;
                renderShape(matrixStack, bufferIn.getBuffer(TYPE), Shapes.create(aabb.move(0.0D, 0.0D, 0.0D)), -tile.getBlockPos().getX(), -tile.getBlockPos().getY(), -tile.getBlockPos().getZ(), f2, f3, f4, 1.0F);
            }
            var extraRange = tile.getStorageMultiplier();
            if (extraRange == 1){
                extraRange = 0;
            }
            var area = new AABB(tile.getBlockPos())
                    .inflate(FunctionalStorageConfig.DRAWER_CONTROLLER_LINKING_RANGE + extraRange);
            renderShape(matrixStack, bufferIn.getBuffer(TYPE), Shapes.create(area), -tile.getBlockPos().getX(), -tile.getBlockPos().getY(), -tile.getBlockPos().getZ(), 0.5f, 1, 0.5f, 1.0F);
            renderFaces(matrixStack, bufferIn, area , -tile.getBlockPos().getX(), -tile.getBlockPos().getY(), -tile.getBlockPos().getZ(), 0.5f, 1, 0.5f , 0.25f);
        }

    }

    @Override
    public boolean shouldRender(StorageControllerTile p_173568_, Vec3 p_173569_) {
        return true;
    }

    @Override
    public boolean shouldRenderOffScreen(StorageControllerTile p_112306_) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(StorageControllerTile<?> blockEntity) {
        return AABB.INFINITE;
    }

    private static RenderType AREA_TYPE = createRenderType();

    public static RenderType createRenderType() {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
                .setTransparencyState(new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
                    RenderSystem.enableBlend();
                    RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                }, () -> {
                    RenderSystem.disableBlend();
                    RenderSystem.defaultBlendFunc();
                }))
                .setDepthTestState(new RenderStateShard.DepthTestStateShard("<=", 515)).createCompositeState(true);
        return RenderType.create("controller_area", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, true, state);
    }

    private void renderFaces(PoseStack stack, MultiBufferSource renderTypeBuffer, AABB pos, double x, double y, double z, float red, float green, float blue, float alpha) {

        float x1 = (float) (pos.minX + x);
        float x2 = (float) (pos.maxX + x);
        float y1 = (float) (pos.minY + y);
        float y2 = (float) (pos.maxY + y);
        float z1 = (float) (pos.minZ + z);
        float z2 = (float) (pos.maxZ + z);

        Matrix4f matrix = stack.last().pose();
        VertexConsumer buffer;

        buffer = renderTypeBuffer.getBuffer(AREA_TYPE);

        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y2, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y1, z1).setColor(red, green, blue, alpha);

        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y2, z1).setColor(red, green, blue, alpha);


        buffer.addVertex(matrix, x1, y1, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y1, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y2, z2).setColor(red, green, blue, alpha);

        buffer.addVertex(matrix, x1, y1, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y1, z2).setColor(red, green, blue, alpha);


        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y1, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y1, z2).setColor(red, green, blue, alpha);

        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y1, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y1, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y1, z1).setColor(red, green, blue, alpha);


        buffer.addVertex(matrix, x1, y2, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z1).setColor(red, green, blue, alpha);

        buffer.addVertex(matrix, x1, y2, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y2, z2).setColor(red, green, blue, alpha);


        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y1, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y2, z1).setColor(red, green, blue, alpha);

        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y2, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y1, z2).setColor(red, green, blue, alpha);


        buffer.addVertex(matrix, x2, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y1, z2).setColor(red, green, blue, alpha);

        buffer.addVertex(matrix, x2, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y1, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z1).setColor(red, green, blue, alpha);

    }
}
