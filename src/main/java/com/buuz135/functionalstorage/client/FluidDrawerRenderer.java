package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.Drawer;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.FluidDrawerTile;
import com.buuz135.functionalstorage.fluid.BigFluidHandler;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.util.NumberUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import static com.buuz135.functionalstorage.util.MathUtils.createTransformMatrix;


public class FluidDrawerRenderer implements BlockEntityRenderer<FluidDrawerTile, FluidDrawerRenderer.FluidDrawerRenderState> {

    public static class FluidDrawerRenderState extends BlockEntityRenderState {
        public FluidDrawerTile tile;
    }

    @Override
    public FluidDrawerRenderState createRenderState() {
        return new FluidDrawerRenderState();
    }

    @Override
    public void extractRenderState(FluidDrawerTile tile, FluidDrawerRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(tile, state, breakProgress);
        state.tile = tile;
    }

    public static void renderFluidStack(PoseStack matrixStack, SubmitNodeCollector collector, int combinedLight, FluidStack stack, int amount, int maxAmount, float scale, ControllableDrawerTile.DrawerOptions options, AABB bounds, boolean halfText, boolean isSmallBar) {
        if (options.isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_RENDER)) {
            matrixStack.pushPose();

            FluidStateModelSet modelSet = Minecraft.getInstance().getModelManager().getFluidStateModelSet();
            FluidModel fluidModel = modelSet.get(stack.getFluid().defaultFluidState());
            TextureAtlasSprite still = fluidModel.stillMaterial().sprite();

            FluidTintSource tintSource = fluidModel.fluidTintSource();
            int tint = tintSource != null ? tintSource.colorAsStack(stack) : 0xFFFFFFFF;
            float[] color = decomposeColorF(tint);
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
            float bx1 = (float) bounds.minX;
            float bx2 = (float) bounds.maxX;
            float by1 = (float) bounds.minY;
            float by2 = (float) bounds.maxY;
            float bz1 = (float) bounds.minZ;
            float bz2 = (float) bounds.maxZ;

            float u1top = still.getU(bx1), u2top = still.getU(bx2);
            float v1top = still.getV(bz1), v2top = still.getV(bz2);
            float u1front = still.getU(bx1), u2front = still.getU(bx2);
            float v1front = still.getV(by1), v2front = still.getV(by2);

            final float finalRed = red, finalGreen = green, finalBlue = blue, finalAlpha = alpha;

            collector.submitCustomGeometry(matrixStack, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), (pose, buffer) -> {
                // TOP
                buffer.addVertex(pose, x1, y2, z2).setColor(finalRed, finalGreen, finalBlue, finalAlpha).setUv(u1top, v2top).setOverlay(OverlayTexture.NO_OVERLAY).setLight(combinedLight).setNormal(0f, 1f, 0f);
                buffer.addVertex(pose, x2, y2, z2).setColor(finalRed, finalGreen, finalBlue, finalAlpha).setUv(u2top, v2top).setOverlay(OverlayTexture.NO_OVERLAY).setLight(combinedLight).setNormal(0f, 1f, 0f);
                buffer.addVertex(pose, x2, y2, z1).setColor(finalRed, finalGreen, finalBlue, finalAlpha).setUv(u2top, v1top).setOverlay(OverlayTexture.NO_OVERLAY).setLight(combinedLight).setNormal(0f, 1f, 0f);
                buffer.addVertex(pose, x1, y2, z1).setColor(finalRed, finalGreen, finalBlue, finalAlpha).setUv(u1top, v1top).setOverlay(OverlayTexture.NO_OVERLAY).setLight(combinedLight).setNormal(0f, 1f, 0f);
                // FRONT
                buffer.addVertex(pose, x2, y1, z2).setColor(finalRed, finalGreen, finalBlue, finalAlpha).setUv(u2front, v1front).setOverlay(OverlayTexture.NO_OVERLAY).setLight(combinedLight).setNormal(0f, 0f, 1f);
                buffer.addVertex(pose, x2, y2, z2).setColor(finalRed, finalGreen, finalBlue, finalAlpha).setUv(u2front, v2front).setOverlay(OverlayTexture.NO_OVERLAY).setLight(combinedLight).setNormal(0f, 0f, 1f);
                buffer.addVertex(pose, x1, y2, z2).setColor(finalRed, finalGreen, finalBlue, finalAlpha).setUv(u1front, v2front).setOverlay(OverlayTexture.NO_OVERLAY).setLight(combinedLight).setNormal(0f, 0f, 1f);
                buffer.addVertex(pose, x1, y1, z2).setColor(finalRed, finalGreen, finalBlue, finalAlpha).setUv(u1front, v1front).setOverlay(OverlayTexture.NO_OVERLAY).setLight(combinedLight).setNormal(0f, 0f, 1f);
            });
            matrixStack.popPose();
        }

        if (options.isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_NUMBERS)) {
            matrixStack.pushPose();
            matrixStack.translate(0.5, 0.84, 0.97);
            if (halfText) matrixStack.translate(-0.25, 0, 0);
            DrawerRenderer.renderText(matrixStack, collector, combinedLight, Component.literal(ChatFormatting.WHITE + "" + NumberUtils.getFormatedFluidBigNumber(amount)), Direction.NORTH, scale);
            matrixStack.popPose();
        }
        matrixStack.pushPose();
        matrixStack.translate(0.5, 0.453, 0.97);
        if (halfText) {
            matrixStack.scale(0.5f, 0.65f, 0.5f);
            matrixStack.translate(-0.5, -0.18, 0);
        }
        DrawerRenderer.renderIndicator(matrixStack, collector, combinedLight, Math.min(1, amount / (float) maxAmount), options);
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
    public int getViewDistance() {
        return FunctionalStorageClientConfig.DRAWER_RENDER_RANGE;
    }

    @Override
    public void submit(FluidDrawerRenderState state, PoseStack matrixStack, SubmitNodeCollector collector, CameraRenderState camera) {
        FluidDrawerTile tile = state.tile;
        if (tile == null) return;
        matrixStack.pushPose();
        Direction subfacing = tile.getFacingDirection();

        if (tile.getBlockState().hasProperty(Drawer.FACING_ALL)) {
            Direction facing = tile.getBlockState().getValue(Drawer.FACING_ALL);
            if (subfacing == Direction.UP) {
                matrixStack.mulPose(createTransformMatrix(new Vector3f(1, 0, 0), new Vector3f(90, 0, 0), 1));
                if (facing == Direction.EAST) {
                    matrixStack.mulPose(createTransformMatrix(new Vector3f(-1, 0, 0), new Vector3f(0, 0, -90), 1));
                } else if (facing == Direction.WEST) {
                    matrixStack.mulPose(createTransformMatrix(new Vector3f(0, 1, 0), new Vector3f(0, 0, 90), 1));
                }
            }
            if (subfacing == Direction.DOWN) {
                matrixStack.mulPose(createTransformMatrix(new Vector3f(0, 1, 0), new Vector3f(-90, 0, -180), 1));
                if (facing == Direction.WEST) {
                    matrixStack.mulPose(createTransformMatrix(new Vector3f(-1, 0, 0), new Vector3f(0, 0, -90), 1));
                } else if (facing == Direction.EAST) {
                    matrixStack.mulPose(createTransformMatrix(new Vector3f(0, 1, 0), new Vector3f(0, 0, 90), 1));
                }
            }
            if (facing == Direction.NORTH) {
                matrixStack.mulPose(createTransformMatrix(new Vector3f(-1, 1, 0), new Vector3f(0, 0, 180), 1));
            }
        }
        Direction facing = tile.getFacingDirection();
        matrixStack.mulPose(Axis.YP.rotationDegrees(-180));
        if (subfacing == Direction.NORTH) {
            matrixStack.translate(-1, 0, -1);
        }
        if (subfacing == Direction.EAST) {
            matrixStack.translate(0, 0, -1);
            matrixStack.mulPose(Axis.YP.rotationDegrees(-90));
        }
        if (subfacing == Direction.SOUTH) {
            matrixStack.mulPose(Axis.YP.rotationDegrees(-180));
        }
        if (subfacing == Direction.WEST) {
            matrixStack.translate(-1, 0, 0);
            matrixStack.mulPose(Axis.YP.rotationDegrees(90));
        }
        int lightCoords = tile.getLevel() != null
                ? LevelRenderer.getLightCoords(tile.getLevel(), tile.getBlockPos().relative(subfacing))
                : state.lightCoords;

        if (tile.getDrawerType() == FunctionalStorage.DrawerType.X_1)
            render1Slot(matrixStack, collector, lightCoords, tile);
        if (tile.getDrawerType() == FunctionalStorage.DrawerType.X_2)
            render2Slot(matrixStack, collector, lightCoords, tile);
        if (tile.getDrawerType() == FunctionalStorage.DrawerType.X_4)
            render4Slot(matrixStack, collector, lightCoords, tile);
        matrixStack.pushPose();
        matrixStack.translate(0, 0, 0.9688);
        DrawerRenderer.renderUpgrades(matrixStack, collector, lightCoords, tile);
        matrixStack.popPose();
        matrixStack.popPose();
    }

    private void render1Slot(PoseStack matrixStack, SubmitNodeCollector collector, int lightCoords, FluidDrawerTile tile) {
        BigFluidHandler inventoryHandler = tile.getFluidHandler();
        if (!inventoryHandler.getFluidInTank(0).isEmpty() || (tile.isLocked() && !inventoryHandler.getFilterStack()[0].isEmpty())) {
            matrixStack.pushPose();
            FluidStack fluidStack = inventoryHandler.getFluidInTank(0);
            int displayAmount = fluidStack.getAmount();
            if (fluidStack.isEmpty() && tile.isLocked() && !inventoryHandler.getFilterStack()[0].isEmpty()) {
                fluidStack = inventoryHandler.getFilterStack()[0];
                displayAmount = 0;
            }
            AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 15 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) inventoryHandler.getTankCapacity(0)) * (12.5 / 16D), 15 / 16D);
            renderFluidStack(matrixStack, collector, lightCoords, fluidStack, displayAmount, inventoryHandler.getTankCapacity(0), 0.007f, tile.getDrawerOptions(), bounds, false, false);
            matrixStack.popPose();
        }
    }

    private void render2Slot(PoseStack matrixStack, SubmitNodeCollector collector, int lightCoords, FluidDrawerTile tile) {
        BigFluidHandler inventoryHandler = tile.getFluidHandler();
        if (!inventoryHandler.getFluidInTank(0).isEmpty() || (tile.isLocked() && !inventoryHandler.getFilterStack()[0].isEmpty())) {
            matrixStack.pushPose();
            FluidStack fluidStack = inventoryHandler.getFluidInTank(0);
            int displayAmount = fluidStack.getAmount();
            if (fluidStack.isEmpty() && tile.isLocked() && !inventoryHandler.getFilterStack()[0].isEmpty()) {
                fluidStack = inventoryHandler.getFilterStack()[0];
                displayAmount = 0;
            }
            AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 15 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) inventoryHandler.getTankCapacity(0)) * (5.5 / 16D), 15 / 16D);
            renderFluidStack(matrixStack, collector, lightCoords, fluidStack, displayAmount, inventoryHandler.getTankCapacity(0), 0.007f, tile.getDrawerOptions(), bounds, false, true);
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
            AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 15 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) inventoryHandler.getTankCapacity(1)) * (5.5 / 16D), 15 / 16D);
            renderFluidStack(matrixStack, collector, lightCoords, fluidStack, displayAmount, inventoryHandler.getTankCapacity(1), 0.007f, tile.getDrawerOptions(), bounds, false, true);
            matrixStack.popPose();
        }
    }

    private void render4Slot(PoseStack matrixStack, SubmitNodeCollector collector, int lightCoords, FluidDrawerTile tile) {
        BigFluidHandler inventoryHandler = tile.getFluidHandler();
        if (!inventoryHandler.getFluidInTank(0).isEmpty() || (tile.isLocked() && !inventoryHandler.getFilterStack()[0].isEmpty())) {
            matrixStack.pushPose();
            matrixStack.translate(0, 0.5, 0);
            FluidStack fluidStack = inventoryHandler.getFluidInTank(0);
            int displayAmount = fluidStack.getAmount();
            if (fluidStack.isEmpty() && tile.isLocked() && !inventoryHandler.getFilterStack()[0].isEmpty()) {
                fluidStack = inventoryHandler.getFilterStack()[0];
                displayAmount = 0;
            }
            AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 8 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) inventoryHandler.getTankCapacity(0)) * (5.5 / 16D), 15 / 16D);
            renderFluidStack(matrixStack, collector, lightCoords, fluidStack, displayAmount, inventoryHandler.getTankCapacity(0), 0.007f, tile.getDrawerOptions(), bounds, true, true);
            matrixStack.popPose();
        }
        if (!inventoryHandler.getFluidInTank(1).isEmpty() || (tile.isLocked() && !inventoryHandler.getFilterStack()[1].isEmpty())) {
            matrixStack.pushPose();
            matrixStack.translate(0.5, 0.5, 0);
            FluidStack fluidStack = inventoryHandler.getFluidInTank(1);
            int displayAmount = fluidStack.getAmount();
            if (fluidStack.isEmpty() && tile.isLocked() && !inventoryHandler.getFilterStack()[1].isEmpty()) {
                fluidStack = inventoryHandler.getFilterStack()[1];
                displayAmount = 0;
            }
            AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 8 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) inventoryHandler.getTankCapacity(1)) * (5.5 / 16D), 15 / 16D);
            renderFluidStack(matrixStack, collector, lightCoords, fluidStack, displayAmount, inventoryHandler.getTankCapacity(1), 0.007f, tile.getDrawerOptions(), bounds, true, true);
            matrixStack.popPose();
        }
        if (!inventoryHandler.getFluidInTank(2).isEmpty() || (tile.isLocked() && !inventoryHandler.getFilterStack()[2].isEmpty())) {
            matrixStack.pushPose();
            FluidStack fluidStack = inventoryHandler.getFluidInTank(2);
            int displayAmount = fluidStack.getAmount();
            if (fluidStack.isEmpty() && tile.isLocked() && !inventoryHandler.getFilterStack()[2].isEmpty()) {
                fluidStack = inventoryHandler.getFilterStack()[2];
                displayAmount = 0;
            }
            AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 8 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) inventoryHandler.getTankCapacity(2)) * (5.5 / 16D), 15 / 16D);
            renderFluidStack(matrixStack, collector, lightCoords, fluidStack, displayAmount, inventoryHandler.getTankCapacity(2), 0.007f, tile.getDrawerOptions(), bounds, true, true);
            matrixStack.popPose();
        }
        if (!inventoryHandler.getFluidInTank(3).isEmpty() || (tile.isLocked() && !inventoryHandler.getFilterStack()[3].isEmpty())) {
            matrixStack.pushPose();
            matrixStack.translate(0.5, 0, 0);
            FluidStack fluidStack = inventoryHandler.getFluidInTank(3);
            int displayAmount = fluidStack.getAmount();
            if (fluidStack.isEmpty() && tile.isLocked() && !inventoryHandler.getFilterStack()[3].isEmpty()) {
                fluidStack = inventoryHandler.getFilterStack()[3];
                displayAmount = 0;
            }
            AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 8 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) inventoryHandler.getTankCapacity(3)) * (5.5 / 16D), 15 / 16D);
            renderFluidStack(matrixStack, collector, lightCoords, fluidStack, displayAmount, inventoryHandler.getTankCapacity(3), 0.007f, tile.getDrawerOptions(), bounds, true, true);
            matrixStack.popPose();
        }
    }
}
