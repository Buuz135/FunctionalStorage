package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.DrawerTile;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.util.NumberUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Vector3f;


import static com.buuz135.functionalstorage.util.MathUtils.createTransformMatrix;

public class DrawerRenderer extends BaseDrawerRenderer<DrawerTile> {
	
    @Override
    public final void renderItems(DrawerTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
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
                    Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE, combinedLightIn, combinedOverlayIn, matrixStack, bufferIn, tile.getLevel(), 0);
                    matrixStack.popPose();
                    matrixStack.translate(scale,0,0);
                }
            }
            matrixStack.popPose();
        }
        if (tile.isVoid()){
            matrixStack.pushPose();
            matrixStack.mulPose(createTransformMatrix(
            		new Vector3f(0.969f,0.031f,0.469f/16.0f), new Vector3f(0), scale));
            Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(FunctionalStorage.VOID_UPGRADE.get()), ItemDisplayContext.NONE, combinedLightIn, combinedOverlayIn, matrixStack, bufferIn, tile.getLevel(),0);
            matrixStack.popPose();
        }
    }

    public static void renderIndicator(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, float progress, ControllableDrawerTile.DrawerOptions options) {
        var indicatiorValue = options.getAdvancedValue(ConfigurationToolItem.ConfigurationAction.INDICATOR);
        if (indicatiorValue != 0) {
            TextureAtlasSprite still = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(com.buuz135.functionalstorage.util.Utils.resourceLocation(FunctionalStorage.MOD_ID, "block/indicator"));
            VertexConsumer builder = bufferIn.getBuffer(RenderType.translucent());
            Matrix4f posMat = matrixStack.last().pose();
            float red = 1;
            float green = 1;
            float blue = 1;
            float alpha = 1;
            float x1 = -4 / 16F;
            float x2 = x1 + 0.5f;
            float y1 = -6.65F / 16F;
            float y2 = y1 + 1.25f / 16F;
            float z2 = 0;
            float bx1 = 0;
            float bx2 = 8;
            float bz1 = 0;
            float bz2 = 2;
            float u1 = still.getU(bx1);
            float u2 = still.getU(bx2);
            float v1 = still.getV(bz1);
            float v2 = still.getV(bz2);
            if (indicatiorValue != 3) { //HIDE IN MODE 3 NO BG
                builder.addVertex(posMat, x2, y1, z2).setColor(red, green, blue, alpha).setUv(u2, v1).setOverlay(combinedOverlayIn).setNormal(0f, 0f, 1f);
                builder.addVertex(posMat, x2, y2, z2).setColor(red, green, blue, alpha).setUv(u2, v2).setOverlay(combinedOverlayIn).setNormal(0f, 0f, 1f);
                builder.addVertex(posMat, x1, y2, z2).setColor(red, green, blue, alpha).setUv(u1, v2).setOverlay(combinedOverlayIn).setNormal(0f, 0f, 1f);
                builder.addVertex(posMat, x1, y1, z2).setColor(red, green, blue, alpha).setUv(u1, v1).setOverlay(combinedOverlayIn).setNormal(0f, 0f, 1f);
            }

            u2 = still.getU((bx2 * progress));
            x2 = x1 + 0.5f * progress;
            z2 = 0.0001f;
            v1 = still.getV(8);
            v2 = still.getV(10);
            if (indicatiorValue == 1 || progress >= 1) {
                builder.addVertex(posMat, x2, y1, z2).setColor(red, green, blue, alpha).setUv(u2, v1).setOverlay(combinedOverlayIn).setNormal(0f, 0f, 1f);
                builder.addVertex(posMat, x2, y2, z2).setColor(red, green, blue, alpha).setUv(u2, v2).setOverlay(combinedOverlayIn).setNormal(0f, 0f, 1f);
                builder.addVertex(posMat, x1, y2, z2).setColor(red, green, blue, alpha).setUv(u1, v2).setOverlay(combinedOverlayIn).setNormal(0f, 0f, 1f);
                builder.addVertex(posMat, x1, y1, z2).setColor(red, green, blue, alpha).setUv(u1, v1).setOverlay(combinedOverlayIn).setNormal(0f, 0f, 1f);
            }
        }
    }

    private void render1Slot(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, DrawerTile tile){
        BigInventoryHandler inventoryHandler = (BigInventoryHandler) tile.getStorage();
        if (!inventoryHandler.getStoredStacks().get(0).getStack().isEmpty()){
            matrixStack.translate(0.5, 0.5, 0.0005f);
            ItemStack stack = inventoryHandler.getStoredStacks().get(0).getStack();
            renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack, inventoryHandler.getStackInSlot(0).getCount(), inventoryHandler.getSlotLimit(0),0.015f, tile.getDrawerOptions(), tile.getLevel());
        }
    }

    private void render2Slot(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, DrawerTile tile){
        BigInventoryHandler inventoryHandler = (BigInventoryHandler) tile.getStorage();
        if (!inventoryHandler.getStoredStacks().get(0).getStack().isEmpty()){
            matrixStack.pushPose();
            matrixStack.mulPose(createTransformMatrix(new Vector3f(0.5f, 0.27f, 0.0005f), new Vector3f(0), new Vector3f(.5f, .5f, 1.0f)));
            ItemStack stack = inventoryHandler.getStoredStacks().get(0).getStack();
            renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack, inventoryHandler.getStackInSlot(0).getCount(), inventoryHandler.getSlotLimit(0),0.02f, tile.getDrawerOptions(), tile.getLevel());
            matrixStack.popPose();
        }
        if (!inventoryHandler.getStoredStacks().get(1).getStack().isEmpty()){
            matrixStack.pushPose();
            matrixStack.mulPose(createTransformMatrix(
            		new Vector3f(0.5f, 0.77f, 0.0005f), new Vector3f(0), new Vector3f(.5f, .5f, 1.0f)));
            ItemStack stack = inventoryHandler.getStoredStacks().get(1).getStack();
            renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack, inventoryHandler.getStackInSlot(1).getCount(), inventoryHandler.getSlotLimit(1),0.02f, tile.getDrawerOptions(), tile.getLevel());
            matrixStack.popPose();
        }
    }
    private void render4Slot(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, DrawerTile tile){
        BigInventoryHandler inventoryHandler = (BigInventoryHandler) tile.getStorage();
        if (!inventoryHandler.getStoredStacks().get(0).getStack().isEmpty()){ //BOTTOM RIGHT
            matrixStack.pushPose();
            matrixStack.mulPose(createTransformMatrix(
            		new Vector3f(.75f, .27f, .0005f), new Vector3f(0), new Vector3f(.5f, .5f, 1.0f)));
            ItemStack stack = inventoryHandler.getStoredStacks().get(0).getStack();
            renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack, inventoryHandler.getStackInSlot(0).getCount(), inventoryHandler.getSlotLimit(0),0.02f, tile.getDrawerOptions(), tile.getLevel());
            matrixStack.popPose();
        }
        if (!inventoryHandler.getStoredStacks().get(1).getStack().isEmpty()){ //BOTTOM LEFT
            matrixStack.pushPose();
            matrixStack.mulPose(createTransformMatrix(
            		new Vector3f(.25f, .27f, .0005f), new Vector3f(0), new Vector3f(.5f, .5f, 1.0f)));
            ItemStack stack = inventoryHandler.getStoredStacks().get(1).getStack();
            renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack, inventoryHandler.getStackInSlot(1).getCount(), inventoryHandler.getSlotLimit(1),0.02f, tile.getDrawerOptions(), tile.getLevel());
            matrixStack.popPose();
        }
        if (!inventoryHandler.getStoredStacks().get(2).getStack().isEmpty()){ //TOP RIGHT
            matrixStack.pushPose();
            matrixStack.mulPose(createTransformMatrix(
            		new Vector3f(.75f, .77f, .0005f), new Vector3f(0), new Vector3f(.5f, .5f, 1.0f)));
            ItemStack stack = inventoryHandler.getStoredStacks().get(2).getStack();
            renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack, inventoryHandler.getStackInSlot(2).getCount(), inventoryHandler.getSlotLimit(2),0.02f, tile.getDrawerOptions(), tile.getLevel());
            matrixStack.popPose();
        }
        if (!inventoryHandler.getStoredStacks().get(3).getStack().isEmpty()){ //TOP LEFT
            matrixStack.pushPose();
            matrixStack.mulPose(createTransformMatrix(
            		new Vector3f(.25f, .77f, .0005f), new Vector3f(0), new Vector3f(.5f, .5f, 1.0f)));
            ItemStack stack = inventoryHandler.getStoredStacks().get(3).getStack();
            renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack, inventoryHandler.getStackInSlot(3).getCount(), inventoryHandler.getSlotLimit(3),0.02f, tile.getDrawerOptions(), tile.getLevel());
            matrixStack.popPose();
        }
    }


    public static void renderStack(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, ItemStack stack, int amount, int maxAmount, float scale, ControllableDrawerTile.DrawerOptions options, Level level){
        renderIndicator(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, Math.min(1, amount / (float) maxAmount), options);

        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, Minecraft.getInstance().level, null, 0);
        if (model.isGui3d()){
        	float thickness = (float)FunctionalStorageClientConfig.DRAWER_RENDER_THICKNESS;
        	// Avoid scaling normal matrix by using mulPose() instead of scale()
        	matrixStack.mulPose(createTransformMatrix(
        			new Vector3f(0), new Vector3f(0), new Vector3f(.75f, .75f, thickness)));
        } else {
        	matrixStack.mulPose(createTransformMatrix(
        			new Vector3f(0), new Vector3f(0), .4f));
        }

        matrixStack.mulPose(Axis.YP.rotationDegrees(180));
        if (options.isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_RENDER))
        {
        	Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, combinedLightIn, combinedOverlayIn, matrixStack, bufferIn, level,0);
        }

    	matrixStack.mulPose(createTransformMatrix(
    			new Vector3f(0), new Vector3f(0, 180, 0), 1));
        if (!model.isGui3d()){
        	matrixStack.mulPose(createTransformMatrix(
        			new Vector3f(0), new Vector3f(0), new Vector3f(0.5f / 0.4f, 0.5f / 0.4f, 1)));
        } else {
        	matrixStack.mulPose(createTransformMatrix(
        			new Vector3f(0), new Vector3f(0), .665f));
        }


        if (options.isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_NUMBERS))
            renderText(matrixStack, bufferIn, combinedOverlayIn, Component.literal(ChatFormatting.WHITE + "" + NumberUtils.getFormatedBigNumber(amount)), Direction.NORTH, scale);
    }


    /* Thanks Mekanism */
    public static void renderText(PoseStack matrix, MultiBufferSource renderer, int overlayLight, Component text, Direction side, float maxScale) {

        matrix.translate(0, -0.745, 0.01);


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
        font.drawInBatch(text, offsetX - realWidth / 2, 3 + offsetY - realHeight / 2, overlayLight, false, matrix.last().pose(), renderer, Font.DisplayMode.NORMAL,  0, 0xF000F0);

    }
}
