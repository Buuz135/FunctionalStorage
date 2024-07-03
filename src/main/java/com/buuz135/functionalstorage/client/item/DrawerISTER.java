package com.buuz135.functionalstorage.client.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.client.DrawerRenderer;
import com.buuz135.functionalstorage.client.model.FramedDrawerModelData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.HashMap;

import static com.buuz135.functionalstorage.util.MathUtils.createTransformMatrix;

public class DrawerISTER extends FunctionalStorageISTER{

    public static DrawerISTER SLOT_1 = new DrawerISTER(FunctionalStorage.DrawerType.X_1);
    public static DrawerISTER SLOT_2 = new DrawerISTER(FunctionalStorage.DrawerType.X_2);
    public static DrawerISTER SLOT_4 = new DrawerISTER(FunctionalStorage.DrawerType.X_4);


    private final FunctionalStorage.DrawerType type;
    private final Object2ObjectArrayMap<Integer, ModelData> modelCache;

    public DrawerISTER(FunctionalStorage.DrawerType type) {
        this.type = type;
        this.modelCache = new Object2ObjectArrayMap<>();
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {

    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light, int overlayLight) {
        var modelData = ModelData.EMPTY;
        if (stack.hasTag() && stack.getTag().contains("Style")){
            modelData = modelCache.computeIfAbsent(stack.hashCode(), new Object2ObjectFunction<Integer, ModelData>() {
                @Override
                public ModelData get(Object o) {
                    var tag = stack.getTag().getCompound("Style");
                    HashMap<String, Item> data = new HashMap<>();
                    data.put("particle", ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString("particle"))));
                    data.put("front", ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString("front"))));
                    data.put("side", ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString("side"))));
                    data.put("front_divider", ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString("front_divider"))));
                    var framedDrawerModelData = new FramedDrawerModelData(data);
                    return ModelData.builder().with(FramedDrawerModelData.FRAMED_PROPERTY, framedDrawerModelData).build();
                }
            });
        }
        renderBlockItem(stack, displayContext, matrix, renderer, light, overlayLight, modelData);
        if (stack.hasTag()){
            var options = new ControllableDrawerTile.DrawerOptions();
            if (stack.getTag().contains("Tile")) options.deserializeNBT(stack.getTagElement("Tile").getCompound("drawerOptions"));
            matrix.mulPose(Axis.YP.rotationDegrees(180));
            matrix.translate(-1,0,0);
            stack.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
                if (type == FunctionalStorage.DrawerType.X_1){
                    if (!iItemHandler.getStackInSlot(0).isEmpty()){
                        matrix.translate(0.5, 0.5, 0);
                        DrawerRenderer.renderStack(matrix, renderer, light, overlayLight, iItemHandler.getStackInSlot(0), iItemHandler.getStackInSlot(0).getCount(), iItemHandler.getSlotLimit(0), 0.02f, options , Minecraft.getInstance().level);
                    }
                } else if (type == FunctionalStorage.DrawerType.X_2) {
                    if (!iItemHandler.getStackInSlot(0).isEmpty()){
                        matrix.pushPose();
                        matrix.mulPoseMatrix(createTransformMatrix(new Vector3f(0.5f, 0.27f, 0.0005f), new Vector3f(0), new Vector3f(.5f, .5f, 1.0f)));
                        ItemStack insideStack = iItemHandler.getStackInSlot(0);
                        DrawerRenderer.renderStack(matrix, renderer, light, overlayLight, insideStack, insideStack.getCount(), iItemHandler.getSlotLimit(0),0.02f, options, Minecraft.getInstance().level);
                        matrix.popPose();
                    }
                    if (!iItemHandler.getStackInSlot(1).isEmpty()){
                        matrix.pushPose();
                        matrix.mulPoseMatrix(createTransformMatrix(
                                new Vector3f(0.5f, 0.77f, 0.0005f), new Vector3f(0), new Vector3f(.5f, .5f, 1.0f)));
                        ItemStack insideStack = iItemHandler.getStackInSlot(1);
                        DrawerRenderer.renderStack(matrix, renderer, light, overlayLight, insideStack, insideStack.getCount(), iItemHandler.getSlotLimit(1),0.02f, options, Minecraft.getInstance().level);
                        matrix.popPose();
                    }
                } else if (type == FunctionalStorage.DrawerType.X_4) {
                    if (!iItemHandler.getStackInSlot(0).isEmpty()){ //BOTTOM RIGHT
                        matrix.pushPose();
                        matrix.mulPoseMatrix(createTransformMatrix(
                                new Vector3f(.75f, .27f, .0005f), new Vector3f(0), new Vector3f(.5f, .5f, 1.0f)));
                        ItemStack insideStack = iItemHandler.getStackInSlot(0);
                        DrawerRenderer.renderStack(matrix, renderer, light, overlayLight, insideStack, insideStack.getCount(), iItemHandler.getSlotLimit(0),0.02f, options, Minecraft.getInstance().level);
                        matrix.popPose();
                    }
                    if (!iItemHandler.getStackInSlot(1).isEmpty()){ //BOTTOM LEFT
                        matrix.pushPose();
                        matrix.mulPoseMatrix(createTransformMatrix(
                                new Vector3f(.25f, .27f, .0005f), new Vector3f(0), new Vector3f(.5f, .5f, 1.0f)));
                        ItemStack insideStack = iItemHandler.getStackInSlot(1);
                        DrawerRenderer.renderStack(matrix, renderer, light, overlayLight, insideStack, insideStack.getCount(), iItemHandler.getSlotLimit(1),0.02f, options, Minecraft.getInstance().level);
                        matrix.popPose();
                    }
                    if (!iItemHandler.getStackInSlot(2).isEmpty()){ //TOP RIGHT
                        matrix.pushPose();
                        matrix.mulPoseMatrix(createTransformMatrix(
                                new Vector3f(.75f, .77f, .0005f), new Vector3f(0), new Vector3f(.5f, .5f, 1.0f)));
                        ItemStack insideStack = iItemHandler.getStackInSlot(2);
                        DrawerRenderer.renderStack(matrix, renderer, light, overlayLight, insideStack, insideStack.getCount(), iItemHandler.getSlotLimit(2),0.02f, options, Minecraft.getInstance().level);
                        matrix.popPose();
                    }
                    if (!iItemHandler.getStackInSlot(3).isEmpty()){ //TOP LEFT
                        matrix.pushPose();
                        matrix.mulPoseMatrix(createTransformMatrix(
                                new Vector3f(.25f, .77f, .0005f), new Vector3f(0), new Vector3f(.5f, .5f, 1.0f)));
                        ItemStack insideStack = iItemHandler.getStackInSlot(3);
                        DrawerRenderer.renderStack(matrix, renderer, light, overlayLight, insideStack, insideStack.getCount(), iItemHandler.getSlotLimit(3),0.02f, options, Minecraft.getInstance().level);
                        matrix.popPose();
                    }
                }

            });
        }

    }
}
