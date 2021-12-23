package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.DrawerTile;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.util.NumberUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
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
        if (facing != Direction.SOUTH) matrixStack.last().normal().load(FAKE_NORMALS);
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
        matrixStack.translate(0,0,-0.5/16D);
        combinedLightIn = LevelRenderer.getLightColor(tile.getLevel(), tile.getBlockPos().relative(facing));
        renderUpgrades(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
        if (tile.getDrawerType() == FunctionalStorage.DrawerType.X_1) render1Slot(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
        if (tile.getDrawerType() == FunctionalStorage.DrawerType.X_2) render2Slot(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
        if (tile.getDrawerType() == FunctionalStorage.DrawerType.X_4) render4Slot(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
        matrixStack.popPose();
    }

    public static void renderUpgrades(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, ControllableDrawerTile<?> tile){
        matrixStack.pushPose();
        matrixStack.translate(0.031,0.031f,0.469/16D);
        float scale = 0.0625f;
        for (int i = 0; i < tile.getStorageUpgrades().getSlots(); i++) {
            ItemStack stack = tile.getStorageUpgrades().getStackInSlot(i);
            if (!stack.isEmpty()){
                matrixStack.pushPose();
                matrixStack.scale(scale, scale, scale);
                Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.NONE, combinedLightIn, combinedOverlayIn, matrixStack, bufferIn, 0);
                matrixStack.popPose();
                matrixStack.translate(scale,0,0);
            }
        }
        matrixStack.popPose();
        if (tile.isVoid()){
            matrixStack.pushPose();
            matrixStack.translate(0.969,0.031f,0.469/16D);
            matrixStack.scale(scale, scale, scale);
            Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(FunctionalStorage.VOID_UPGRADE.get()), ItemTransforms.TransformType.NONE, combinedLightIn, combinedOverlayIn, matrixStack, bufferIn, 0);
            matrixStack.popPose();
        }
    }

    private void render1Slot(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, DrawerTile tile){
        BigInventoryHandler inventoryHandler = (BigInventoryHandler) tile.getStorage();
        if (!inventoryHandler.getStoredStacks().get(0).getStack().isEmpty()){
            matrixStack.translate(0.5, 0.5, 0.0005f);
            ItemStack stack = inventoryHandler.getStoredStacks().get(0).getStack();
            renderStack(matrixStack,  bufferIn, combinedLightIn, combinedOverlayIn, stack, inventoryHandler.getStoredStacks().get(0).getAmount(), 0.015f);
        }
    }

    private void render2Slot(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, DrawerTile tile){
        BigInventoryHandler inventoryHandler = (BigInventoryHandler) tile.getStorage();
        if (!inventoryHandler.getStoredStacks().get(0).getStack().isEmpty()){
            matrixStack.pushPose();
            matrixStack.translate(0.5, 0.27f, 0.0005f);
            matrixStack.scale(0.5f, 0.5f, 1);
            ItemStack stack = inventoryHandler.getStoredStacks().get(0).getStack();
            renderStack(matrixStack,  bufferIn, combinedLightIn, combinedOverlayIn, stack, inventoryHandler.getStoredStacks().get(0).getAmount(), 0.02f);
            matrixStack.popPose();
        }
        if (!inventoryHandler.getStoredStacks().get(1).getStack().isEmpty()){
            matrixStack.pushPose();
            matrixStack.translate(0.5, 0.77f, 0.0005f);
            matrixStack.scale(0.5f, 0.5f, 1);
            ItemStack stack = inventoryHandler.getStoredStacks().get(1).getStack();
            renderStack(matrixStack,  bufferIn, combinedLightIn, combinedOverlayIn, stack, inventoryHandler.getStoredStacks().get(1).getAmount(), 0.02f);
            matrixStack.popPose();
        }
    }
    private void render4Slot(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, DrawerTile tile){
        BigInventoryHandler inventoryHandler = (BigInventoryHandler) tile.getStorage();
        if (!inventoryHandler.getStoredStacks().get(0).getStack().isEmpty()){ //BOTTOM RIGHT
            matrixStack.pushPose();
            matrixStack.translate(0.75, 0.27f, 0.0005f);
            matrixStack.scale(0.5f, 0.5f, 1);
            ItemStack stack = inventoryHandler.getStoredStacks().get(0).getStack();
            renderStack(matrixStack,  bufferIn, combinedLightIn, combinedOverlayIn, stack, inventoryHandler.getStoredStacks().get(0).getAmount(), 0.02f);
            matrixStack.popPose();
        }
        if (!inventoryHandler.getStoredStacks().get(1).getStack().isEmpty()){ //BOTTOM LEFT
            matrixStack.pushPose();
            matrixStack.translate(0.25, 0.27f, 0.0005f);
            matrixStack.scale(0.5f, 0.5f, 1);
            ItemStack stack = inventoryHandler.getStoredStacks().get(1).getStack();
            renderStack(matrixStack,  bufferIn, combinedLightIn, combinedOverlayIn, stack, inventoryHandler.getStoredStacks().get(1).getAmount(), 0.02f);
            matrixStack.popPose();
        }
        if (!inventoryHandler.getStoredStacks().get(2).getStack().isEmpty()){ //TOP RIGHT
            matrixStack.pushPose();
            matrixStack.translate(0.75, 0.77f, 0.0005f);
            matrixStack.scale(0.5f, 0.5f, 1);
            ItemStack stack = inventoryHandler.getStoredStacks().get(2).getStack();
            renderStack(matrixStack,  bufferIn, combinedLightIn, combinedOverlayIn, stack, inventoryHandler.getStoredStacks().get(2).getAmount(), 0.02f);
            matrixStack.popPose();
        }
        if (!inventoryHandler.getStoredStacks().get(3).getStack().isEmpty()){ //TOP LEFT
            matrixStack.pushPose();
            matrixStack.translate(0.25, 0.77f, 0.0005f);
            matrixStack.scale(0.5f, 0.5f, 1);
            ItemStack stack = inventoryHandler.getStoredStacks().get(3).getStack();
            renderStack(matrixStack,  bufferIn, combinedLightIn, combinedOverlayIn, stack, inventoryHandler.getStoredStacks().get(3).getAmount(), 0.02f);
            matrixStack.popPose();
        }
    }


    public static void renderStack(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, ItemStack stack, int amount, float scale){
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, Minecraft.getInstance().level, null, 0);
        if (model.isGui3d()){
            matrixStack.translate(0,0, -0.115);
            matrixStack.scale(0.5f, 0.5f, 0.25f);
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(180));
        } else {
            matrixStack.scale(0.4f, 0.4f, 0.4f);
        }

        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.NONE, combinedLightIn, combinedOverlayIn, matrixStack, bufferIn, 0);
        if (!model.isGui3d()){
            matrixStack.scale(1/0.4f, 1/0.4f, 1/0.0001f);
            matrixStack.scale(0.5f, 0.5f, 0.0001f);
        }else {
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(-180));
            matrixStack.translate(0,0, 0.23f*2);
        }
        renderText(matrixStack, bufferIn, combinedOverlayIn, new TextComponent(ChatFormatting.WHITE + "" + NumberUtils.getFormatedBigNumber(amount)), Direction.NORTH, scale);
    }


    /* Thanks Mekanism */
    public static void renderText(PoseStack matrix, MultiBufferSource renderer, int overlayLight, Component text, Direction side, float maxScale) {

        matrix.translate(0, -0.745, 0);


        float displayWidth = 1;
        float displayHeight = 1;
        //matrix.translate(displayWidth / 2, 0, displayHeight / 2);
        //matrix.mulPose(Vector3f.XP.rotationDegrees(-90));

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

    }
}
