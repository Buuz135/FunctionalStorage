package com.buuz135.functionalstorage.client;

import static com.buuz135.functionalstorage.util.MathUtils.createTransformMatrix;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.EnderDrawerTile;
import com.buuz135.functionalstorage.inventory.EnderInventoryHandler;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.world.EnderSavedData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class EnderDrawerRenderer implements BlockEntityRenderer<EnderDrawerTile> {

    @Override
    public void render(EnderDrawerTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
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
        render1Slot(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
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
            		new Vector3f(.969f, .031f, .469f/16.0f), Vector3f.ZERO, scale));
            Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(FunctionalStorage.VOID_UPGRADE.get()), ItemTransforms.TransformType.NONE, combinedLightIn, combinedOverlayIn, matrixStack, bufferIn, 0);
            matrixStack.popPose();
        }
    }

    private void render1Slot(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, EnderDrawerTile tile){
        EnderInventoryHandler inventoryHandler =  EnderSavedData.getInstance(tile.getLevel()).getFrequency(tile.getFrequency());
        if (!inventoryHandler.getStoredStacks().get(0).getStack().isEmpty()){
            matrixStack.translate(0.5, 0.5, 0.0005f);
            ItemStack stack = inventoryHandler.getStoredStacks().get(0).getStack();
            DrawerRenderer.renderStack(matrixStack,  bufferIn, combinedLightIn, combinedOverlayIn, stack, inventoryHandler.getStoredStacks().get(0).getAmount(), 0.015f, tile.getDrawerOptions());
        }
    }

}
