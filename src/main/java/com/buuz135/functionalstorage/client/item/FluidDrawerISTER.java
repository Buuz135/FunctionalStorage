package com.buuz135.functionalstorage.client.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.client.FluidDrawerRenderer;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.util.Utils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class FluidDrawerISTER extends FunctionalStorageISTER{

    public static final FluidDrawerISTER SLOT_1 = new FluidDrawerISTER(FunctionalStorage.DrawerType.X_1);
    public static final FluidDrawerISTER SLOT_2 = new FluidDrawerISTER(FunctionalStorage.DrawerType.X_2);
    public static final FluidDrawerISTER SLOT_4 = new FluidDrawerISTER(FunctionalStorage.DrawerType.X_4);


    private final FunctionalStorage.DrawerType type;

    public FluidDrawerISTER(FunctionalStorage.DrawerType type) {
        this.type = type;
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {

    }

    @Override
    public void renderByItem(HolderLookup.Provider access, @NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light, int overlayLight) {
        var modelData = getData(stack);
        renderBlockItem(stack, displayContext, matrix, renderer, light, overlayLight, modelData);
        if (stack.has(FSAttachments.TILE)) {
            var options = new ControllableDrawerTile.DrawerOptions();
            options.deserializeNBT(access, stack.get(FSAttachments.TILE).getCompound("drawerOptions"));
            matrix.mulPose(Axis.YP.rotationDegrees(180));
            matrix.translate(-1,0,-1);
            var tileTag = stack.get(FSAttachments.TILE).getCompound("fluidHandler");
            if (type == FunctionalStorage.DrawerType.X_1){
                FluidStack fluidStack = deserialize(access, tileTag, 0);
                if (!fluidStack.isEmpty()){
                    int displayAmount = fluidStack.getAmount();
                    AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 15 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) fluidStack.getAmount()) * (12.5 / 16D), 15 / 16D);
                    FluidDrawerRenderer.renderFluidStack(matrix, renderer, light, overlayLight, fluidStack, displayAmount, fluidStack.getAmount(), 0.007f, options, bounds, false, false);
                }
            } else if (type == FunctionalStorage.DrawerType.X_2) {
                FluidStack fluidStack = deserialize(access, tileTag, 0);
                if (!fluidStack.isEmpty()){
                    int displayAmount = fluidStack.getAmount();
                    AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 15 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) fluidStack.getAmount()) * (5.5 / 16D), 15 / 16D);
                    FluidDrawerRenderer.renderFluidStack(matrix, renderer, light, overlayLight, fluidStack, displayAmount, fluidStack.getAmount(), 0.007f, options, bounds, false, true);
                }
                fluidStack = deserialize(access, tileTag, 1);
                if (!fluidStack.isEmpty()){
                    matrix.pushPose();
                    matrix.translate(0, 0.5, 0);
                    int displayAmount = fluidStack.getAmount();
                    AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 15 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) fluidStack.getAmount()) * (5.5 / 16D), 15 / 16D);
                    FluidDrawerRenderer.renderFluidStack(matrix, renderer, light, overlayLight, fluidStack, displayAmount, fluidStack.getAmount(), 0.007f, options, bounds, false, true);
                    matrix.popPose();
                }
            } else if (type == FunctionalStorage.DrawerType.X_4) {
                FluidStack fluidStack = deserialize(access, tileTag, 0);
                if (!fluidStack.isEmpty()){
                    matrix.pushPose();
                    matrix.translate(0.5, 0, 0);
                    int displayAmount = fluidStack.getAmount();
                    AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 8 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) fluidStack.getAmount()) * (5.5 / 16D), 15 / 16D);
                    FluidDrawerRenderer.renderFluidStack(matrix, renderer, light, overlayLight, fluidStack, displayAmount, fluidStack.getAmount(), 0.007f, options, bounds, true, true);
                    matrix.popPose();
                }
                fluidStack = deserialize(access, tileTag, 1);
                if (!fluidStack.isEmpty()){
                    matrix.pushPose();
                    int displayAmount = fluidStack.getAmount();
                    AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 8 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) fluidStack.getAmount()) * (5.5 / 16D), 15 / 16D);
                    FluidDrawerRenderer.renderFluidStack(matrix, renderer, light, overlayLight, fluidStack, displayAmount, fluidStack.getAmount(), 0.007f, options, bounds, true, true);
                    matrix.popPose();
                }
                fluidStack = deserialize(access, tileTag, 2);
                if (!fluidStack.isEmpty()){
                    matrix.pushPose();
                    matrix.translate(0.5, 0.5, 0);
                    int displayAmount = fluidStack.getAmount();
                    AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 8 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) fluidStack.getAmount()) * (5.5 / 16D), 15 / 16D);
                    FluidDrawerRenderer.renderFluidStack(matrix, renderer, light, overlayLight, fluidStack, displayAmount, fluidStack.getAmount(), 0.007f, options, bounds, true, true);
                    matrix.popPose();
                }
                fluidStack = deserialize(access, tileTag, 3);
                if (!fluidStack.isEmpty()){
                    matrix.pushPose();
                    matrix.translate(0, 0.5, 0);
                    int displayAmount = fluidStack.getAmount();
                    AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 8 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) fluidStack.getAmount()) * (5.5 / 16D), 15 / 16D);
                    FluidDrawerRenderer.renderFluidStack(matrix, renderer, light, overlayLight, fluidStack, displayAmount, fluidStack.getAmount(), 0.007f, options, bounds, true, true);
                    matrix.popPose();
                }
            }
        }
    }

    public static FluidStack deserialize(HolderLookup.Provider access, CompoundTag tileTag, int i) {
        var fluidTag = tileTag.getCompound(String.valueOf(i));
        return Utils.deserializeFluid(access, fluidTag.getCompound("Fluid"));
    }
}
