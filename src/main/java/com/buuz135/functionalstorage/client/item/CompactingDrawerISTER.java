package com.buuz135.functionalstorage.client.item;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.client.DrawerRenderer;
import com.buuz135.functionalstorage.client.model.FramedDrawerModelData;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.IdentityHashMap;

import static com.buuz135.functionalstorage.util.MathUtils.createTransformMatrix;

public class CompactingDrawerISTER extends FunctionalStorageISTER {

    public static CompactingDrawerISTER NORMAL = new CompactingDrawerISTER(false);
    public static CompactingDrawerISTER SIMPLE = new CompactingDrawerISTER(true);

    private final boolean simple;
    private final IdentityHashMap<ItemStack, ModelData> modelCache;

    public CompactingDrawerISTER(boolean simple) {
        this.simple = simple;
        this.modelCache = new IdentityHashMap<>();
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
            options.deserializeNBT(Minecraft.getInstance().level.registryAccess(), stack.get(FSAttachments.TILE).getCompound("drawerOptions"));
            matrix.mulPose(Axis.YP.rotationDegrees(180));
            matrix.translate(-1, 0, 0);
            var iItemHandler = stack.getCapability(Capabilities.ItemHandler.ITEM);
            if (iItemHandler != null) {
                if (simple) {
                    ItemStack internalStack = iItemHandler.getStackInSlot(0);
                    if (!internalStack.isEmpty()) {
                        matrix.pushPose();
                        matrix.mulPose(createTransformMatrix(
                                new Vector3f(0.5f, 0.27f, 0.0005f), new Vector3f(0), new Vector3f(.5f, .5f, 1.0f)));
                        DrawerRenderer.renderStack(matrix, renderer, light, overlayLight, internalStack, internalStack.getCount(), iItemHandler.getSlotLimit(0), 0.02f, options, Minecraft.getInstance().level);
                        matrix.popPose();
                    }
                    internalStack = iItemHandler.getStackInSlot(1);
                    if (!internalStack.isEmpty()) {
                        matrix.pushPose();
                        matrix.mulPose(createTransformMatrix(
                                new Vector3f(0.5f, 0.77f, 0.0005f), new Vector3f(0), new Vector3f(.5f, .5f, 1.0f)));
                        DrawerRenderer.renderStack(matrix, renderer, light, overlayLight, internalStack, internalStack.getCount(), iItemHandler.getSlotLimit(1), 0.02f, options, Minecraft.getInstance().level);
                        matrix.popPose();
                    }
                } else {
                    ItemStack internalStack = iItemHandler.getStackInSlot(0);
                    if (!internalStack.isEmpty()) {
                        matrix.pushPose();
                        matrix.mulPose(createTransformMatrix(
                                new Vector3f(.75f, .27f, .0005f), new Vector3f(0), new Vector3f(.5f, .5f, 1.0f)));
                        DrawerRenderer.renderStack(matrix, renderer, light, overlayLight, internalStack, internalStack.getCount(), iItemHandler.getSlotLimit(0), 0.02f, options, Minecraft.getInstance().level);
                        matrix.popPose();
                    }
                    internalStack = iItemHandler.getStackInSlot(1);
                    if (!internalStack.isEmpty()) {
                        matrix.pushPose();
                        matrix.mulPose(createTransformMatrix(
                                new Vector3f(.25f, .27f, .0005f), new Vector3f(0), new Vector3f(.5f, .5f, 1.0f)));
                        DrawerRenderer.renderStack(matrix, renderer, light, overlayLight, internalStack, internalStack.getCount(), iItemHandler.getSlotLimit(1), 0.02f, options, Minecraft.getInstance().level);
                        matrix.popPose();
                    }
                    internalStack = iItemHandler.getStackInSlot(2);
                    if (!internalStack.isEmpty()) {
                        matrix.pushPose();
                        matrix.mulPose(createTransformMatrix(
                                new Vector3f(.5f, .77f, .0005f), new Vector3f(0), new Vector3f(.5f, .5f, 1.0f)));
                        DrawerRenderer.renderStack(matrix, renderer, light, overlayLight, internalStack, internalStack.getCount(), iItemHandler.getSlotLimit(2), 0.02f, options, Minecraft.getInstance().level);
                        matrix.popPose();
                    }
                }
            }
        }
    }
}
