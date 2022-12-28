package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.DrawerTile;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.util.NumberUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import static com.buuz135.functionalstorage.util.MathUtils.*;

public class DrawerRenderer implements BlockEntityRenderer<DrawerTile> {
	
    @Override
    public void render(DrawerTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (Minecraft.getInstance().player != null && !tile.getBlockPos().closerThan(Minecraft.getInstance().player.getOnPos(), FunctionalStorageClientConfig.DRAWER_RENDER_RANGE)){
            return;
        }
        matrixStack.pushPose();

        Direction facing = tile.getFacingDirection();
        matrixStack.mulPoseMatrix(createTransformMatrix(
        		Vector3f.ZERO, new Vector3f(0, 180, 0), 1));
        
        if (facing == Direction.NORTH) {
        	matrixStack.mulPoseMatrix(createTransformMatrix(
        			new Vector3f(-1, 0, 0), Vector3f.ZERO, 1));
        }
        else if (facing == Direction.EAST) {
        	matrixStack.mulPoseMatrix(createTransformMatrix(
        			new Vector3f(-1, 0, -1), new Vector3f(0, -90, 0), 1));
        }
        else if (facing == Direction.SOUTH) {
        	matrixStack.mulPoseMatrix(createTransformMatrix(
        			new Vector3f(0, 0, -1), new Vector3f(0, 180, 0), 1));
        }
        else if (facing == Direction.WEST) {
        	matrixStack.mulPoseMatrix(createTransformMatrix(
        			new Vector3f(0, 0, 0), new Vector3f(0, 90, 0), 1));
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
        float scale = 0.0625f;
        if (tile.getDrawerOptions().isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_UPGRADES)){
            matrixStack.pushPose();
            matrixStack.translate(0.031,0.031f,0.472/16D);
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
        }
        if (tile.isVoid()){
            matrixStack.pushPose();
            matrixStack.mulPoseMatrix(createTransformMatrix(
            		new Vector3f(0.969f,0.031f,0.469f/16.0f), Vector3f.ZERO, scale));
            Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(FunctionalStorage.VOID_UPGRADE.get()), ItemTransforms.TransformType.NONE, combinedLightIn, combinedOverlayIn, matrixStack, bufferIn, 0);
            matrixStack.popPose();
        }
    }

    private void render1Slot(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, DrawerTile tile){
        BigInventoryHandler inventoryHandler = (BigInventoryHandler) tile.getStorage();
        if (!inventoryHandler.getStoredStacks().get(0).getStack().isEmpty()){
            matrixStack.translate(0.5, 0.5, 0.0005f);
            ItemStack stack = inventoryHandler.getStoredStacks().get(0).getStack();
            renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack, inventoryHandler.getStackInSlot(0).getCount(), 0.015f, tile.getDrawerOptions());
        }
    }

    private void render2Slot(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, DrawerTile tile){
        BigInventoryHandler inventoryHandler = (BigInventoryHandler) tile.getStorage();
        if (!inventoryHandler.getStoredStacks().get(0).getStack().isEmpty()){
            matrixStack.pushPose();
            matrixStack.mulPoseMatrix(createTransformMatrix(
            		new Vector3f(0.5f, 0.27f, 0.0005f), Vector3f.ZERO, 0.5f));
            ItemStack stack = inventoryHandler.getStoredStacks().get(0).getStack();
            renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack, inventoryHandler.getStackInSlot(0).getCount(), 0.02f, tile.getDrawerOptions());
            matrixStack.popPose();
        }
        if (!inventoryHandler.getStoredStacks().get(1).getStack().isEmpty()){
            matrixStack.pushPose();
            matrixStack.mulPoseMatrix(createTransformMatrix(
            		new Vector3f(0.5f, 0.77f, 0.0005f), Vector3f.ZERO, 0.5f));
            ItemStack stack = inventoryHandler.getStoredStacks().get(1).getStack();
            renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack, inventoryHandler.getStackInSlot(1).getCount(), 0.02f, tile.getDrawerOptions());
            matrixStack.popPose();
        }
    }
    private void render4Slot(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, DrawerTile tile){
        BigInventoryHandler inventoryHandler = (BigInventoryHandler) tile.getStorage();
        if (!inventoryHandler.getStoredStacks().get(0).getStack().isEmpty()){ //BOTTOM RIGHT
            matrixStack.pushPose();
            matrixStack.mulPoseMatrix(createTransformMatrix(
            		new Vector3f(.75f, .27f, .0005f), Vector3f.ZERO, .5f));
            ItemStack stack = inventoryHandler.getStoredStacks().get(0).getStack();
            renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack, inventoryHandler.getStackInSlot(0).getCount(), 0.02f, tile.getDrawerOptions());
            matrixStack.popPose();
        }
        if (!inventoryHandler.getStoredStacks().get(1).getStack().isEmpty()){ //BOTTOM LEFT
            matrixStack.pushPose();
            matrixStack.mulPoseMatrix(createTransformMatrix(
            		new Vector3f(.25f, .27f, .0005f), Vector3f.ZERO, .5f));
            ItemStack stack = inventoryHandler.getStoredStacks().get(1).getStack();
            renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack, inventoryHandler.getStackInSlot(1).getCount(), 0.02f, tile.getDrawerOptions());
            matrixStack.popPose();
        }
        if (!inventoryHandler.getStoredStacks().get(2).getStack().isEmpty()){ //TOP RIGHT
            matrixStack.pushPose();
            matrixStack.mulPoseMatrix(createTransformMatrix(
            		new Vector3f(.75f, .77f, .0005f), Vector3f.ZERO, .5f));
            ItemStack stack = inventoryHandler.getStoredStacks().get(2).getStack();
            renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack, inventoryHandler.getStackInSlot(2).getCount(), 0.02f, tile.getDrawerOptions());
            matrixStack.popPose();
        }
        if (!inventoryHandler.getStoredStacks().get(3).getStack().isEmpty()){ //TOP LEFT
            matrixStack.pushPose();
            matrixStack.mulPoseMatrix(createTransformMatrix(
            		new Vector3f(.25f, .77f, .0005f), Vector3f.ZERO, .5f));
            ItemStack stack = inventoryHandler.getStoredStacks().get(3).getStack();
            renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack, inventoryHandler.getStackInSlot(3).getCount(), 0.02f, tile.getDrawerOptions());
            matrixStack.popPose();
        }
    }


    public static void renderStack(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, ItemStack stack, int amount, float scale, ControllableDrawerTile.DrawerOptions options){
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, Minecraft.getInstance().level, null, 0);
        if (model.isGui3d()){
        	float thickness = (float)FunctionalStorageClientConfig.DRAWER_RENDER_THICKNESS;
        	// Avoid scaling normal matrix by using mulPoseMatrix() instead of scale()
        	matrixStack.mulPoseMatrix(createTransformMatrix(
        			Vector3f.ZERO, Vector3f.ZERO, new Vector3f(.75f, .75f, thickness)));
        } else {
        	matrixStack.mulPoseMatrix(createTransformMatrix(
        			Vector3f.ZERO, Vector3f.ZERO, .4f));
        }
        
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(180));
        if (options.isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_RENDER))
        {
        	Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.FIXED, combinedLightIn, combinedOverlayIn, matrixStack, bufferIn, 0);  	
        }
        
    	matrixStack.mulPoseMatrix(createTransformMatrix(
    			Vector3f.ZERO, new Vector3f(0, 180, 0), 1));
        if (!model.isGui3d()){
        	matrixStack.mulPoseMatrix(createTransformMatrix(
        			Vector3f.ZERO, Vector3f.ZERO, new Vector3f(0.5f / 0.4f, 0.5f / 0.4f, 1)));
        } else {
        	matrixStack.mulPoseMatrix(createTransformMatrix(
        			Vector3f.ZERO, Vector3f.ZERO, .665f));
        }


        if (options.isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_NUMBERS))
            renderText(matrixStack, bufferIn, combinedOverlayIn, Component.literal(ChatFormatting.WHITE + "" + NumberUtils.getFormatedBigNumber(amount)), Direction.NORTH, scale);
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
