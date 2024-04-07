package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.block.tile.SimpleCompactingDrawerTile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

import static com.buuz135.functionalstorage.util.MathUtils.createTransformMatrix;

public class SimpleCompactingDrawerRenderer extends BaseDrawerRenderer<SimpleCompactingDrawerTile> {

    @Override
    public void renderItems(SimpleCompactingDrawerTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        ItemStack stack = tile.getHandler().getResultList().get(0).getResult();
        if (!stack.isEmpty()) {
            matrixStack.pushPose();
            matrixStack.mulPoseMatrix(createTransformMatrix(
                    new Vector3f(0.5f, 0.27f, 0.0005f), new Vector3f(0), new Vector3f(.5f, .5f, 1.0f)));
            DrawerRenderer.renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack, tile.getHandler().getStackInSlot(0).getCount(),tile.getHandler().getSlotLimit(0), 0.02f, tile.getDrawerOptions(), tile.getLevel());
            matrixStack.popPose();
        }
        stack = tile.getHandler().getResultList().get(1).getResult();
        if (!stack.isEmpty()) {
            matrixStack.pushPose();
            matrixStack.mulPoseMatrix(createTransformMatrix(
                    new Vector3f(0.5f, 0.77f, 0.0005f), new Vector3f(0), new Vector3f(.5f, .5f, 1.0f)));
            DrawerRenderer.renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack, tile.getHandler().getStackInSlot(1).getCount(), tile.getHandler().getSlotLimit(1),0.02f, tile.getDrawerOptions(), tile.getLevel());
            matrixStack.popPose();
        }
        matrixStack.popPose();
    }

}
