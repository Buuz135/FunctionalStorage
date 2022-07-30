package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.block.tile.DrawerControllerTile;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.hrznstudio.titanium.util.RayTraceUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.OptionalDouble;

import static com.buuz135.functionalstorage.item.LinkingToolItem.NBT_CONTROLLER;
import static com.buuz135.functionalstorage.item.LinkingToolItem.NBT_FIRST;

public class ControllerRenderer implements BlockEntityRenderer<DrawerControllerTile> {

    public static RenderType TYPE = RenderType.create("custom_lines", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 256, false, false,RenderType.CompositeState.builder()
            .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeLinesShader))
            .setDepthTestState(new RenderStateShard.DepthTestStateShard("always", 519))
            .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
            .setLayeringState(new RenderStateShard.LayeringStateShard("view_offset_z_layering", () -> {
                PoseStack posestack = RenderSystem.getModelViewStack();
                posestack.pushPose();
                posestack.scale(0.99975586F, 0.99975586F, 0.99975586F);
                RenderSystem.applyModelViewMatrix();
            }, () -> {
                PoseStack posestack = RenderSystem.getModelViewStack();
                posestack.popPose();
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
            p_109784_.vertex(posestack$pose.pose(), (float) (p_194324_ + p_109786_), (float) (p_194325_ + p_109787_), (float) (p_194326_ + p_109788_)).color(p_109789_, p_109790_, p_109791_, p_109792_).normal(posestack$pose.normal(), f, f1, f2).endVertex();
            p_109784_.vertex(posestack$pose.pose(), (float) (p_194327_ + p_109786_), (float) (p_194328_ + p_109787_), (float) (p_194329_ + p_109788_)).color(p_109789_, p_109790_, p_109791_, p_109792_).normal(posestack$pose.normal(), f, f1, f2).endVertex();
        });
    }

    @Override
    public void render(DrawerControllerTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        ItemStack stack = Minecraft.getInstance().player.getMainHandItem();
        if (stack.isEmpty()) return;
        if (stack.getItem() instanceof LinkingToolItem) {
            CompoundTag controllerNBT = stack.getOrCreateTag().getCompound(NBT_CONTROLLER);
            BlockPos controller = new BlockPos(controllerNBT.getInt("X"), controllerNBT.getInt("Y"), controllerNBT.getInt("Z"));
            if (!controller.equals(tile.getBlockPos())) return;
            if (stack.getOrCreateTag().contains(NBT_FIRST)) {
                CompoundTag firstpos = stack.getOrCreateTag().getCompound(NBT_FIRST);
                BlockPos firstPos = new BlockPos(firstpos.getInt("X"), firstpos.getInt("Y"), firstpos.getInt("Z"));
                HitResult result = RayTraceUtils.rayTraceSimple(Minecraft.getInstance().level, Minecraft.getInstance().player, 8, partialTicks);
                if (result.getType() == HitResult.Type.BLOCK){
                    BlockPos hit = ((BlockHitResult)result).getBlockPos();
                    AABB aabb = new AABB(Math.min(firstPos.getX(), hit.getX()), Math.min(firstPos.getY(), hit.getY()), Math.min(firstPos.getZ(), hit.getZ()), Math.max(firstPos.getX(), hit.getX()) + 1, Math.max(firstPos.getY(), hit.getY()) + 1, Math.max(firstPos.getZ(), hit.getZ()) + 1);
                    VoxelShape shape = Shapes.create(aabb);
                    LevelRenderer.renderVoxelShape(matrixStack, bufferIn.getBuffer(TYPE), shape, -controller.getX(), -controller.getY(), -controller.getZ(), 1f, 1f, 1f, 1f);
                    return;
                }
            }
            VoxelShape shape = Shapes.create(new AABB(tile.getBlockPos()));
            for (Long connectedDrawer : tile.getConnectedDrawers().getConnectedDrawers()) {
                shape = Shapes.join(shape, Shapes.create(new AABB(BlockPos.of(connectedDrawer))), BooleanOp.OR);
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
        }

    }

    @Override
    public boolean shouldRender(DrawerControllerTile p_173568_, Vec3 p_173569_) {
        return true;
    }

    @Override
    public boolean shouldRenderOffScreen(DrawerControllerTile p_112306_) {
        return true;
    }
}
