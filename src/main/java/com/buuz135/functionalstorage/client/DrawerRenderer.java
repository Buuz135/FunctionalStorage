package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.DrawerTile;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.util.NumberUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;


import static com.buuz135.functionalstorage.util.MathUtils.createTransformMatrix;

public class DrawerRenderer extends BaseDrawerRenderer<DrawerTile> {

    @Override
    public final void renderItems(DrawerTile tile, PoseStack matrixStack, SubmitNodeCollector collector, int lightCoords) {
        if (tile.getDrawerType() == FunctionalStorage.DrawerType.X_1) render1Slot(matrixStack, collector, lightCoords, tile);
        if (tile.getDrawerType() == FunctionalStorage.DrawerType.X_2) render2Slot(matrixStack, collector, lightCoords, tile);
        if (tile.getDrawerType() == FunctionalStorage.DrawerType.X_4) render4Slot(matrixStack, collector, lightCoords, tile);
        matrixStack.popPose();
    }

    public static void renderUpgrades(PoseStack matrixStack, SubmitNodeCollector collector, int lightCoords, ControllableDrawerTile<?> tile) {
        float scale = 0.0625f;
        if (tile.getDrawerOptions().isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_UPGRADES)) {
            matrixStack.pushPose();
            matrixStack.translate(0.031, 0.031f, 0.472 / 16D);
            for (int i = 0; i < tile.getStorageUpgrades().getSlots(); i++) {
                ItemStack stack = tile.getStorageUpgrades().getStackInSlot(i);
                if (!stack.isEmpty()) {
                    matrixStack.pushPose();
                    matrixStack.scale(scale, scale, scale);
                    ItemStackRenderState itemState = new ItemStackRenderState();
                    Minecraft.getInstance().getItemModelResolver().updateForTopItem(itemState, stack, ItemDisplayContext.NONE, null, null, 0);
                    itemState.submit(matrixStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, 0);
                    matrixStack.popPose();
                    matrixStack.translate(scale, 0, 0);
                }
            }
            matrixStack.popPose();
        }
        if (tile.isVoid()) {
            matrixStack.pushPose();
            matrixStack.mulPose(createTransformMatrix(
                    new Vector3f(0.969f, 0.031f, 0.469f / 16.0f), new Vector3f(0), scale));
            ItemStackRenderState itemState = new ItemStackRenderState();
            Minecraft.getInstance().getItemModelResolver().updateForTopItem(itemState, new ItemStack(FunctionalStorage.VOID_UPGRADE.get()), ItemDisplayContext.NONE, null, null, 0);
            itemState.submit(matrixStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, 0);
            matrixStack.popPose();
        }
    }

    public static void renderIndicator(PoseStack matrixStack, SubmitNodeCollector collector, int lightCoords, float progress, ControllableDrawerTile.DrawerOptions options) {
        int indicatorValue = options.getAdvancedValue(ConfigurationToolItem.ConfigurationAction.INDICATOR);
        if (indicatorValue != 0) {
            TextureAtlasSprite still = Minecraft.getInstance().getAtlasManager()
                    .getAtlasOrThrow(AtlasIds.BLOCKS)
                    .getSprite(com.buuz135.functionalstorage.util.Utils.resourceLocation(FunctionalStorage.MOD_ID, "block/indicator"));

            float red = 1, green = 1, blue = 1, alpha = 1;
            float x1 = -4 / 16F;
            float x2bg = x1 + 0.5f;
            float y1 = -6.65F / 16F;
            float y2 = y1 + 1.25f / 16F;
            float z2 = 0;
            float bx1 = 0 / 16F, bx2 = 8 / 16F, bz1 = 0 / 16F, bz2 = 2 / 16F;
            float u1 = still.getU(bx1);
            float u2bg = still.getU(bx2);
            float v1bg = still.getV(bz1);
            float v2bg = still.getV(bz2);

            final int finalIndicatorValue = indicatorValue;
            final float finalProgress = progress;
            final float finalU2progress = still.getU(bx2 * progress);
            final float finalX2progress = x1 + 0.5f * progress;
            final float v1front = still.getV(8 / 16F);
            final float v2front = still.getV(10 / 16F);

            collector.submitCustomGeometry(matrixStack, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), (pose, buffer) -> {
                if (finalIndicatorValue != 3) {
                    buffer.addVertex(pose, x2bg, y1, z2).setColor(red, green, blue, alpha).setUv(u2bg, v1bg).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lightCoords).setNormal(0f, 0f, 1f);
                    buffer.addVertex(pose, x2bg, y2, z2).setColor(red, green, blue, alpha).setUv(u2bg, v2bg).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lightCoords).setNormal(0f, 0f, 1f);
                    buffer.addVertex(pose, x1, y2, z2).setColor(red, green, blue, alpha).setUv(u1, v2bg).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lightCoords).setNormal(0f, 0f, 1f);
                    buffer.addVertex(pose, x1, y1, z2).setColor(red, green, blue, alpha).setUv(u1, v1bg).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lightCoords).setNormal(0f, 0f, 1f);
                }
                float zFront = 0.0001f;
                if (finalIndicatorValue == 1 || finalProgress >= 1) {
                    buffer.addVertex(pose, finalX2progress, y1, zFront).setColor(red, green, blue, alpha).setUv(finalU2progress, v1front).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lightCoords).setNormal(0f, 0f, 1f);
                    buffer.addVertex(pose, finalX2progress, y2, zFront).setColor(red, green, blue, alpha).setUv(finalU2progress, v2front).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lightCoords).setNormal(0f, 0f, 1f);
                    buffer.addVertex(pose, x1, y2, zFront).setColor(red, green, blue, alpha).setUv(u1, v2front).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lightCoords).setNormal(0f, 0f, 1f);
                    buffer.addVertex(pose, x1, y1, zFront).setColor(red, green, blue, alpha).setUv(u1, v1front).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lightCoords).setNormal(0f, 0f, 1f);
                }
            });
        }
    }

    private void render1Slot(PoseStack matrixStack, SubmitNodeCollector collector, int lightCoords, DrawerTile tile) {
        BigInventoryHandler inventoryHandler = (BigInventoryHandler) tile.getStorage();
        if (!inventoryHandler.getStoredStacks().get(0).getStack().isEmpty()) {
            matrixStack.translate(0.5, 0.5, 0.0005f);
            ItemStack stack = inventoryHandler.getStoredStacks().get(0).getStack();
            renderStack(matrixStack, collector, lightCoords, stack, inventoryHandler.getStackInSlot(0).getCount(), inventoryHandler.getSlotLimit(0), 0.015f, tile.getDrawerOptions(), tile.getLevel());
        }
    }

    private void render2Slot(PoseStack matrixStack, SubmitNodeCollector collector, int lightCoords, DrawerTile tile) {
        BigInventoryHandler inventoryHandler = (BigInventoryHandler) tile.getStorage();
        if (!inventoryHandler.getStoredStacks().get(0).getStack().isEmpty()) {
            matrixStack.pushPose();
            matrixStack.mulPose(createTransformMatrix(new Vector3f(0.5f, 0.27f, 0.0005f), new Vector3f(0), new Vector3f(.5f, .5f, 1.0f)));
            ItemStack stack = inventoryHandler.getStoredStacks().get(0).getStack();
            renderStack(matrixStack, collector, lightCoords, stack, inventoryHandler.getStackInSlot(0).getCount(), inventoryHandler.getSlotLimit(0), 0.02f, tile.getDrawerOptions(), tile.getLevel());
            matrixStack.popPose();
        }
        if (!inventoryHandler.getStoredStacks().get(1).getStack().isEmpty()) {
            matrixStack.pushPose();
            matrixStack.mulPose(createTransformMatrix(
                    new Vector3f(0.5f, 0.77f, 0.0005f), new Vector3f(0), new Vector3f(.5f, .5f, 1.0f)));
            ItemStack stack = inventoryHandler.getStoredStacks().get(1).getStack();
            renderStack(matrixStack, collector, lightCoords, stack, inventoryHandler.getStackInSlot(1).getCount(), inventoryHandler.getSlotLimit(1), 0.02f, tile.getDrawerOptions(), tile.getLevel());
            matrixStack.popPose();
        }
    }

    private void render4Slot(PoseStack matrixStack, SubmitNodeCollector collector, int lightCoords, DrawerTile tile) {
        BigInventoryHandler inventoryHandler = (BigInventoryHandler) tile.getStorage();
        if (!inventoryHandler.getStoredStacks().get(0).getStack().isEmpty()) {
            matrixStack.pushPose();
            matrixStack.mulPose(createTransformMatrix(
                    new Vector3f(.75f, .27f, .0005f), new Vector3f(0), new Vector3f(.5f, .5f, 1.0f)));
            ItemStack stack = inventoryHandler.getStoredStacks().get(0).getStack();
            renderStack(matrixStack, collector, lightCoords, stack, inventoryHandler.getStackInSlot(0).getCount(), inventoryHandler.getSlotLimit(0), 0.02f, tile.getDrawerOptions(), tile.getLevel());
            matrixStack.popPose();
        }
        if (!inventoryHandler.getStoredStacks().get(1).getStack().isEmpty()) {
            matrixStack.pushPose();
            matrixStack.mulPose(createTransformMatrix(
                    new Vector3f(.25f, .27f, .0005f), new Vector3f(0), new Vector3f(.5f, .5f, 1.0f)));
            ItemStack stack = inventoryHandler.getStoredStacks().get(1).getStack();
            renderStack(matrixStack, collector, lightCoords, stack, inventoryHandler.getStackInSlot(1).getCount(), inventoryHandler.getSlotLimit(1), 0.02f, tile.getDrawerOptions(), tile.getLevel());
            matrixStack.popPose();
        }
        if (!inventoryHandler.getStoredStacks().get(2).getStack().isEmpty()) {
            matrixStack.pushPose();
            matrixStack.mulPose(createTransformMatrix(
                    new Vector3f(.75f, .77f, .0005f), new Vector3f(0), new Vector3f(.5f, .5f, 1.0f)));
            ItemStack stack = inventoryHandler.getStoredStacks().get(2).getStack();
            renderStack(matrixStack, collector, lightCoords, stack, inventoryHandler.getStackInSlot(2).getCount(), inventoryHandler.getSlotLimit(2), 0.02f, tile.getDrawerOptions(), tile.getLevel());
            matrixStack.popPose();
        }
        if (!inventoryHandler.getStoredStacks().get(3).getStack().isEmpty()) {
            matrixStack.pushPose();
            matrixStack.mulPose(createTransformMatrix(
                    new Vector3f(.25f, .77f, .0005f), new Vector3f(0), new Vector3f(.5f, .5f, 1.0f)));
            ItemStack stack = inventoryHandler.getStoredStacks().get(3).getStack();
            renderStack(matrixStack, collector, lightCoords, stack, inventoryHandler.getStackInSlot(3).getCount(), inventoryHandler.getSlotLimit(3), 0.02f, tile.getDrawerOptions(), tile.getLevel());
            matrixStack.popPose();
        }
    }


    public static void renderStack(PoseStack matrixStack, SubmitNodeCollector collector, int lightCoords, ItemStack stack, int amount, int maxAmount, float scale, ControllableDrawerTile.DrawerOptions options, Level level) {
        renderIndicator(matrixStack, collector, lightCoords, Math.min(1, amount / (float) maxAmount), options);

        ItemStackRenderState itemState = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(itemState, stack, ItemDisplayContext.FIXED, null, null, 0);
        boolean is3d = itemState.usesBlockLight();

        if (is3d) {
            float thickness = (float) FunctionalStorageClientConfig.DRAWER_RENDER_THICKNESS;
            matrixStack.mulPose(createTransformMatrix(
                    new Vector3f(0), new Vector3f(0), new Vector3f(.75f, .75f, thickness)));
        } else {
            matrixStack.mulPose(createTransformMatrix(
                    new Vector3f(0), new Vector3f(0), .4f));
        }

        matrixStack.mulPose(Axis.YP.rotationDegrees(180));
        if (options.isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_RENDER)) {
            itemState.submit(matrixStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, 0);
        }

        matrixStack.mulPose(createTransformMatrix(
                new Vector3f(0), new Vector3f(0, 180, 0), 1));
        if (!is3d) {
            matrixStack.mulPose(createTransformMatrix(
                    new Vector3f(0), new Vector3f(0), new Vector3f(0.5f / 0.4f, 0.5f / 0.4f, 1)));
        } else {
            matrixStack.mulPose(createTransformMatrix(
                    new Vector3f(0), new Vector3f(0), .665f));
        }

        if (options.isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_NUMBERS))
            renderText(matrixStack, collector, lightCoords, Component.literal(ChatFormatting.WHITE + "" + NumberUtils.getFormatedBigNumber(amount)), Direction.NORTH, scale);
    }


    public static void renderText(PoseStack matrix, SubmitNodeCollector collector, int lightCoords, Component text, Direction side, float maxScale) {
        matrix.translate(0, -0.745, 0.01);

        Font font = Minecraft.getInstance().font;

        int requiredWidth = Math.max(font.width(text), 1);
        int requiredHeight = font.lineHeight + 2;
        float scaler = 0.4F;
        float displayWidth = 1;
        float displayHeight = 1;
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
        collector.submitText(matrix, offsetX - realWidth / 2f, 3 + offsetY - realHeight / 2f,
                text.getVisualOrderText(), false, Font.DisplayMode.NORMAL, lightCoords, 0xFFFFFFFF, 0, 0);
    }
}
