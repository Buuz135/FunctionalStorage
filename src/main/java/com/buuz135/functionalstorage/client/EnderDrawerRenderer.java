package com.buuz135.functionalstorage.client;

import static com.buuz135.functionalstorage.util.MathUtils.createTransformMatrix;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.EnderDrawerTile;
import com.buuz135.functionalstorage.inventory.EnderInventoryHandler;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.world.EnderSavedData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

import static com.buuz135.functionalstorage.util.MathUtils.createTransformMatrix;

public class EnderDrawerRenderer extends BaseDrawerRenderer<EnderDrawerTile> {

    @Override
    public void renderItems(EnderDrawerTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        render1Slot(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
        matrixStack.popPose();
    }

    private void render1Slot(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, EnderDrawerTile tile){
        EnderInventoryHandler inventoryHandler =  EnderSavedData.getInstance(tile.getLevel()).getFrequency(tile.getFrequency());
        if (!inventoryHandler.getStoredStacks().get(0).getStack().isEmpty()){
            matrixStack.translate(0.5, 0.5, 0.0005f);
            ItemStack stack = inventoryHandler.getStoredStacks().get(0).getStack();
            DrawerRenderer.renderStack(matrixStack,  bufferIn, combinedLightIn, combinedOverlayIn, stack, inventoryHandler.getStoredStacks().get(0).getAmount(), inventoryHandler.getSlotLimit(0), 0.015f, tile.getDrawerOptions(), tile.getLevel());
        }
    }

}
