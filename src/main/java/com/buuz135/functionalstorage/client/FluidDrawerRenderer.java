package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.FluidDrawerTile;
import com.buuz135.functionalstorage.fluid.BigFluidHandler;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.util.NumberUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;


public class FluidDrawerRenderer implements BlockEntityRenderer<FluidDrawerTile> {

    public static void renderFluidStack(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLight, int combinedOverlay, FluidStack stack, int amount, int maxAmount, float scale, ControllableDrawerTile.DrawerOptions options, AABB bounds, boolean halfText, boolean isSmallBar) {
        matrixStack.pushPose();
        IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(stack.getFluid());
        ResourceLocation texture = renderProperties.getStillTexture(stack);
        TextureAtlasSprite still = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
        VertexConsumer builder = bufferIn.getBuffer(RenderType.translucent());

        float[] color = decomposeColorF(renderProperties.getTintColor(stack));
        float red = color[1];
        float green = color[2];
        float blue = color[3];
        float alpha = amount == 0 ? 0.3f : color[0];

        float x1 = (float) bounds.minX;
        float x2 = (float) bounds.maxX;
        float y1 = (float) bounds.minY;
        float y2 = (float) bounds.maxY;
        float z1 = (float) bounds.minZ;
        float z2 = (float) bounds.maxZ;
        double bx1 = bounds.minX * 16;
        double bx2 = bounds.maxX * 16;
        double by1 = bounds.minY * 16;
        double by2 = bounds.maxY * 16;
        double bz1 = bounds.minZ * 16;
        double bz2 = bounds.maxZ * 16;


        Matrix4f posMat = matrixStack.last().pose();

        //TOP

        if (true) {
            float u1 = still.getU(bx1);
            float u2 = still.getU(bx2);
            float v1 = still.getV(bz1);
            float v2 = still.getV(bz2);
            builder.vertex(posMat, x1, y2, z2).color(red, green, blue, alpha).uv(u1, v2).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 1f, 0f).endVertex();
            builder.vertex(posMat, x2, y2, z2).color(red, green, blue, alpha).uv(u2, v2).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 1f, 0f).endVertex();
            builder.vertex(posMat, x2, y2, z1).color(red, green, blue, alpha).uv(u2, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 1f, 0f).endVertex();
            builder.vertex(posMat, x1, y2, z1).color(red, green, blue, alpha).uv(u1, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 1f, 0f).endVertex();
        }
        //FRONT
        if (true) {
            float u1 = still.getU(bx1);
            float u2 = still.getU(bx2);
            float v1 = still.getV(by1);
            float v2 = still.getV(by2);
            builder.vertex(posMat, x2, y1, z2).color(red, green, blue, alpha).uv(u2, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 0f, 1f).endVertex();
            builder.vertex(posMat, x2, y2, z2).color(red, green, blue, alpha).uv(u2, v2).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 0f, 1f).endVertex();
            builder.vertex(posMat, x1, y2, z2).color(red, green, blue, alpha).uv(u1, v2).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 0f, 1f).endVertex();
            builder.vertex(posMat, x1, y1, z2).color(red, green, blue, alpha).uv(u1, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 0f, 1f).endVertex();
        }

        matrixStack.popPose();
        if (options.isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_NUMBERS)) {
            matrixStack.pushPose();
            matrixStack.translate(0.5, 0.84, 0.97);
            if (halfText) matrixStack.translate(-0.25, 0, 0);
            DrawerRenderer.renderText(matrixStack, bufferIn, combinedOverlay, Component.literal(ChatFormatting.WHITE + "" + NumberUtils.getFormatedFluidBigNumber(amount)), Direction.NORTH, scale);
            matrixStack.popPose();
        }
        matrixStack.pushPose();
        matrixStack.translate(0.5, 0.453, 0.97);
        if (halfText) {
            matrixStack.scale(0.5f, 0.65f, 0.5f);
            matrixStack.translate(-0.5, -0.18, 0);
        }
        DrawerRenderer.renderIndicator(matrixStack, bufferIn, combinedLight, combinedOverlay, Math.min(1, amount / (float) maxAmount), options);
        matrixStack.popPose();

    }

    public static float[] decomposeColorF(int color) {
        float[] res = new float[4];
        res[0] = (color >> 24 & 0xff) / 255f;
        res[1] = (color >> 16 & 0xff) / 255f;
        res[2] = (color >> 8 & 0xff) / 255f;
        res[3] = (color & 0xff) / 255f;
        return res;
    }

    @Override
    public void render(FluidDrawerTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (Minecraft.getInstance().player != null && !tile.getBlockPos().closerThan(Minecraft.getInstance().player.getOnPos(), FunctionalStorageClientConfig.DRAWER_RENDER_RANGE)) {
            return;
        }
        matrixStack.pushPose();
        Direction facing = tile.getFacingDirection();
        matrixStack.mulPose(Axis.YP.rotationDegrees(-180));
        if (facing == Direction.NORTH) {
            //matrixStack.translate(0, 0, 1.016 / 16D);
            matrixStack.translate(-1, 0, -1);
        }
        if (facing == Direction.EAST) {
            matrixStack.translate(0, 0, -1);
            matrixStack.mulPose(Axis.YP.rotationDegrees(-90));
        }
        if (facing == Direction.SOUTH) {
            matrixStack.mulPose(Axis.YP.rotationDegrees(-180));
        }
        if (facing == Direction.WEST) {
            matrixStack.translate(-1, 0, 0);
            matrixStack.mulPose(Axis.YP.rotationDegrees(90));
        }
        combinedLightIn = LevelRenderer.getLightColor(tile.getLevel(), tile.getBlockPos().relative(facing));

        if (tile.getDrawerType() == FunctionalStorage.DrawerType.X_1)
            render1Slot(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
        if (tile.getDrawerType() == FunctionalStorage.DrawerType.X_2)
            render2Slot(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
        if (tile.getDrawerType() == FunctionalStorage.DrawerType.X_4)
            render4Slot(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
        matrixStack.pushPose();
        matrixStack.translate(0, 0, 0.9688);
        DrawerRenderer.renderUpgrades(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
        matrixStack.popPose();
        matrixStack.popPose();
    }

    private void render1Slot(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, FluidDrawerTile tile) {
        BigFluidHandler inventoryHandler = tile.getFluidHandler();
        if (!inventoryHandler.getFluidInTank(0).isEmpty() || (tile.isLocked() && !inventoryHandler.getFilterStack()[0].isEmpty())) {
            FluidStack fluidStack = inventoryHandler.getFluidInTank(0);
            int displayAmount = fluidStack.getAmount();
            if (fluidStack.isEmpty() && tile.isLocked() && !inventoryHandler.getFilterStack()[0].isEmpty()) {
                fluidStack = inventoryHandler.getFilterStack()[0];
                displayAmount = 0;
            }
            AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 15 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) inventoryHandler.getTankCapacity(0)) * (12.5 / 16D), 15 / 16D);
            renderFluidStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, fluidStack, displayAmount, inventoryHandler.getTankCapacity(0), 0.007f, tile.getDrawerOptions(), bounds, false, false);
        }

    }

    private void render2Slot(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, FluidDrawerTile tile) {
        BigFluidHandler inventoryHandler = tile.getFluidHandler();
        if (!inventoryHandler.getFluidInTank(0).isEmpty() || (tile.isLocked() && !inventoryHandler.getFilterStack()[0].isEmpty())) {
            FluidStack fluidStack = inventoryHandler.getFluidInTank(0);
            int displayAmount = fluidStack.getAmount();
            if (fluidStack.isEmpty() && tile.isLocked() && !inventoryHandler.getFilterStack()[0].isEmpty()) {
                fluidStack = inventoryHandler.getFilterStack()[0];
                displayAmount = 0;
            }
            AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 15 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) inventoryHandler.getTankCapacity(0)) * (5.5 / 16D), 15 / 16D);
            renderFluidStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, fluidStack, displayAmount, inventoryHandler.getTankCapacity(0), 0.007f, tile.getDrawerOptions(), bounds, false, true);
        }
        if (!inventoryHandler.getFluidInTank(1).isEmpty() || (tile.isLocked() && !inventoryHandler.getFilterStack()[1].isEmpty())) {
            matrixStack.pushPose();
            matrixStack.translate(0, 0.5, 0);
            FluidStack fluidStack = inventoryHandler.getFluidInTank(1);
            int displayAmount = fluidStack.getAmount();
            if (fluidStack.isEmpty() && tile.isLocked() && !inventoryHandler.getFilterStack()[1].isEmpty()) {
                fluidStack = inventoryHandler.getFilterStack()[1];
                displayAmount = 0;
            }
            AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 15 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) inventoryHandler.getTankCapacity(1)) * (5.5 / 16D), 15 / 16D);
            renderFluidStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, fluidStack, displayAmount, inventoryHandler.getTankCapacity(1), 0.007f, tile.getDrawerOptions(), bounds, false, true);
            matrixStack.popPose();
        }
    }

    private void render4Slot(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, FluidDrawerTile tile) {
        BigFluidHandler inventoryHandler = tile.getFluidHandler();
        if (!inventoryHandler.getFluidInTank(0).isEmpty() || (tile.isLocked() && !inventoryHandler.getFilterStack()[0].isEmpty())) {
            matrixStack.pushPose();
            matrixStack.translate(0.5, 0, 0);
            FluidStack fluidStack = inventoryHandler.getFluidInTank(0);
            int displayAmount = fluidStack.getAmount();
            if (fluidStack.isEmpty() && tile.isLocked() && !inventoryHandler.getFilterStack()[0].isEmpty()) {
                fluidStack = inventoryHandler.getFilterStack()[0];
                displayAmount = 0;
            }
            AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 8 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) inventoryHandler.getTankCapacity(0)) * (5.5 / 16D), 15 / 16D);
            renderFluidStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, fluidStack, displayAmount, inventoryHandler.getTankCapacity(0), 0.007f, tile.getDrawerOptions(), bounds, true, true);
            matrixStack.popPose();
        }
        if (!inventoryHandler.getFluidInTank(1).isEmpty() || (tile.isLocked() && !inventoryHandler.getFilterStack()[1].isEmpty())) {
            matrixStack.pushPose();
            FluidStack fluidStack = inventoryHandler.getFluidInTank(1);
            int displayAmount = fluidStack.getAmount();
            if (fluidStack.isEmpty() && tile.isLocked() && !inventoryHandler.getFilterStack()[1].isEmpty()) {
                fluidStack = inventoryHandler.getFilterStack()[1];
                displayAmount = 0;
            }
            AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 8 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) inventoryHandler.getTankCapacity(1)) * (5.5 / 16D), 15 / 16D);
            renderFluidStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, fluidStack, displayAmount, inventoryHandler.getTankCapacity(1), 0.007f, tile.getDrawerOptions(), bounds, true, true);
            matrixStack.popPose();
        }
        if (!inventoryHandler.getFluidInTank(2).isEmpty() || (tile.isLocked() && !inventoryHandler.getFilterStack()[2].isEmpty())) {
            matrixStack.pushPose();
            matrixStack.translate(0.5, 0.5, 0);
            FluidStack fluidStack = inventoryHandler.getFluidInTank(2);
            int displayAmount = fluidStack.getAmount();
            if (fluidStack.isEmpty() && tile.isLocked() && !inventoryHandler.getFilterStack()[2].isEmpty()) {
                fluidStack = inventoryHandler.getFilterStack()[2];
                displayAmount = 0;
            }
            AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 8 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) inventoryHandler.getTankCapacity(2)) * (5.5 / 16D), 15 / 16D);
            renderFluidStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, fluidStack, displayAmount, inventoryHandler.getTankCapacity(2), 0.007f, tile.getDrawerOptions(), bounds, true, true);
            matrixStack.popPose();
        }
        if (!inventoryHandler.getFluidInTank(3).isEmpty() || (tile.isLocked() && !inventoryHandler.getFilterStack()[3].isEmpty())) {
            matrixStack.pushPose();
            matrixStack.translate(0, 0.5, 0);
            FluidStack fluidStack = inventoryHandler.getFluidInTank(3);
            int displayAmount = fluidStack.getAmount();
            if (fluidStack.isEmpty() && tile.isLocked() && !inventoryHandler.getFilterStack()[3].isEmpty()) {
                fluidStack = inventoryHandler.getFilterStack()[3];
                displayAmount = 0;
            }
            AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 8 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) inventoryHandler.getTankCapacity(3)) * (5.5 / 16D), 15 / 16D);
            renderFluidStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, fluidStack, displayAmount, inventoryHandler.getTankCapacity(3), 0.007f, tile.getDrawerOptions(), bounds, true, true);
            matrixStack.popPose();
        }
    }

}
