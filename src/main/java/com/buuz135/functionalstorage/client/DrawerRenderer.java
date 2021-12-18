package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.block.tile.DrawerTile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class DrawerRenderer implements BlockEntityRenderer<DrawerTile> {

    private static final Matrix3f FAKE_NORMALS;

    static {
        Vector3f NORMAL = new Vector3f(1, 1, 1);
        NORMAL.normalize();
        FAKE_NORMALS = new Matrix3f(new Quaternion(NORMAL, 0, true));
    }

    @Override
    public void render(DrawerTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        matrixStack.pushPose();

        Direction facing = tile.getFacingDirection();
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(-180));
        matrixStack.last().normal().load(FAKE_NORMALS);
        if (facing == Direction.NORTH) {
            //matrixStack.translate(0, 0, 1.016 / 16D);
            matrixStack.translate(-1, 0, 0);
        }
        if (facing == Direction.EAST) {
            matrixStack.translate(-1, 0, -1);
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(-90));
        }
        if (facing == Direction.SOUTH) {
            matrixStack.translate(0, 0,-1);
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(-180));
        }
        if (facing == Direction.WEST) {
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        }
        //matrixStack.last().normal().add(FAKE_NORMALS);
        combinedLightIn = LevelRenderer.getLightColor(tile.getLevel(), tile.getBlockPos().relative(facing));
        render1Slot(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
    }

    private void render1Slot(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, DrawerTile tile){
        //System.out.println(tile.getHandler().getStackInSlot(0));
        if (!tile.getHandler().getStackInSlot(0).isEmpty()){
            matrixStack.translate(0.5, 0.5, 0.0005f);
            ItemStack stack = tile.getHandler().getStackInSlot(0);
            renderStack(matrixStack,  bufferIn, combinedLightIn, combinedOverlayIn, stack, stack.getCount());
            matrixStack.popPose();
            renderText(matrixStack, bufferIn, combinedOverlayIn, new TextComponent(ChatFormatting.WHITE + "" + stack.getCount()), tile.getFacingDirection(), 0.007f);
        }else {
            matrixStack.popPose();
        }

    }

    private void renderStack(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, ItemStack stack, int amount){
        if (stack.getItem() instanceof BlockItem){
            matrixStack.scale(0.5f, 0.5f, 0.0001f);
        } else {
            matrixStack.scale(0.4f, 0.4f, 0.0001f);
        }

        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.GUI, combinedLightIn, combinedOverlayIn, matrixStack, bufferIn, 0);

    }


    /* Thanks Mekanism */
    private void renderText(PoseStack matrix, MultiBufferSource renderer, int overlayLight, Component text, Direction side, float maxScale) {
        matrix.pushPose();
        matrix.translate(0, -0.3725, 0);
        switch (side) {
            case SOUTH:
                matrix.translate(0, 1, 0.0001);
                matrix.mulPose(Vector3f.XP.rotationDegrees(90));
                break;
            case NORTH:
                matrix.translate(1, 1, 0.9999);
                matrix.mulPose(Vector3f.YP.rotationDegrees(180));
                matrix.mulPose(Vector3f.XP.rotationDegrees(90));
                break;
            case EAST:
                matrix.translate(0.0001, 1, 1);
                matrix.mulPose(Vector3f.YP.rotationDegrees(90));
                matrix.mulPose(Vector3f.XP.rotationDegrees(90));
                break;
            case WEST:
                matrix.translate(0.9999, 1, 0);
                matrix.mulPose(Vector3f.YP.rotationDegrees(-90));
                matrix.mulPose(Vector3f.XP.rotationDegrees(90));
                break;
        }

        float displayWidth = 1;
        float displayHeight = 1;
        matrix.translate(displayWidth / 2, 1, displayHeight / 2);
        matrix.mulPose(Vector3f.XP.rotationDegrees(-90));

        Font font = Minecraft.getInstance().font;

        int requiredWidth = Math.max(font.width(text), 1);
        int requiredHeight = font.lineHeight + 2;
        float scaler = 0.4F;
        float scaleX = displayWidth / requiredWidth;
        float scale = scaleX * scaler;
        if (maxScale > 0) {
            scale = Math.min(scale, maxScale);
        }

        matrix.scale(scale, -scale, scale);
        int realHeight = (int) Math.floor(displayHeight / scale);
        int realWidth = (int) Math.floor(displayWidth / scale);
        int offsetX = (realWidth - requiredWidth) / 2;
        int offsetY = (realHeight - requiredHeight) / 2;
        font.drawInBatch(text, offsetX - realWidth / 2, 3 + offsetY - realHeight / 2, overlayLight,
                false, matrix.last().pose(), renderer, false, 0, 0xF000F0);
        matrix.popPose();
    }
}
