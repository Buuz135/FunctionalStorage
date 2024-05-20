package com.buuz135.functionalstorage.client.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.client.DrawerRenderer;
import com.buuz135.functionalstorage.client.FluidDrawerRenderer;
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
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.HashMap;

import static com.buuz135.functionalstorage.util.MathUtils.createTransformMatrix;

public class FluidDrawerISTER extends FunctionalStorageISTER{

    public static FluidDrawerISTER SLOT_1 = new FluidDrawerISTER(FunctionalStorage.DrawerType.X_1);
    public static FluidDrawerISTER SLOT_2 = new FluidDrawerISTER(FunctionalStorage.DrawerType.X_2);
    public static FluidDrawerISTER SLOT_4 = new FluidDrawerISTER(FunctionalStorage.DrawerType.X_4);


    private final FunctionalStorage.DrawerType type;
    private final Object2ObjectArrayMap<Integer, ModelData> modelCache;

    public FluidDrawerISTER(FunctionalStorage.DrawerType type) {
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
        if (stack.hasTag() && stack.getTag().contains("Tile")){
            var options = new ControllableDrawerTile.DrawerOptions();
            if (stack.getTag().contains("Tile")) options.deserializeNBT(stack.getTagElement("Tile").getCompound("drawerOptions"));
            matrix.mulPose(Axis.YP.rotationDegrees(180));
            matrix.translate(-1,0,-1);
            var tileTag = stack.getTag().getCompound("Tile").getCompound("fluidHandler");
            if (type == FunctionalStorage.DrawerType.X_1){
                FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(tileTag.getCompound(0 + ""));
                if (!fluidStack.isEmpty()){
                    int displayAmount = fluidStack.getAmount();
                    AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 15 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) fluidStack.getAmount()) * (12.5 / 16D), 15 / 16D);
                    FluidDrawerRenderer.renderFluidStack(matrix, renderer, light, overlayLight, fluidStack, displayAmount, fluidStack.getAmount(), 0.007f, options, bounds, false, false);
                }
            } else if (type == FunctionalStorage.DrawerType.X_2) {
                FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(tileTag.getCompound(0 + ""));
                if (!fluidStack.isEmpty()){
                    int displayAmount = fluidStack.getAmount();
                    AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 15 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) fluidStack.getAmount()) * (5.5 / 16D), 15 / 16D);
                    FluidDrawerRenderer.renderFluidStack(matrix, renderer, light, overlayLight, fluidStack, displayAmount, fluidStack.getAmount(), 0.007f, options, bounds, false, true);
                }
                fluidStack = FluidStack.loadFluidStackFromNBT(tileTag.getCompound(1 + ""));
                if (!fluidStack.isEmpty()){
                    matrix.pushPose();
                    matrix.translate(0, 0.5, 0);
                    int displayAmount = fluidStack.getAmount();
                    AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 15 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) fluidStack.getAmount()) * (5.5 / 16D), 15 / 16D);
                    FluidDrawerRenderer.renderFluidStack(matrix, renderer, light, overlayLight, fluidStack, displayAmount, fluidStack.getAmount(), 0.007f, options, bounds, false, true);
                    matrix.popPose();
                }
            } else if (type == FunctionalStorage.DrawerType.X_4) {
                FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(tileTag.getCompound(0 + ""));
                if (!fluidStack.isEmpty()){
                    matrix.pushPose();
                    matrix.translate(0.5, 0, 0);
                    int displayAmount = fluidStack.getAmount();
                    AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 8 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) fluidStack.getAmount()) * (5.5 / 16D), 15 / 16D);
                    FluidDrawerRenderer.renderFluidStack(matrix, renderer, light, overlayLight, fluidStack, displayAmount, fluidStack.getAmount(), 0.007f, options, bounds, true, true);
                    matrix.popPose();
                }
                fluidStack = FluidStack.loadFluidStackFromNBT(tileTag.getCompound(1 + ""));
                if (!fluidStack.isEmpty()){
                    matrix.pushPose();
                    int displayAmount = fluidStack.getAmount();
                    AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 8 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) fluidStack.getAmount()) * (5.5 / 16D), 15 / 16D);
                    FluidDrawerRenderer.renderFluidStack(matrix, renderer, light, overlayLight, fluidStack, displayAmount, fluidStack.getAmount(), 0.007f, options, bounds, true, true);
                    matrix.popPose();
                }
                fluidStack = FluidStack.loadFluidStackFromNBT(tileTag.getCompound(2 + ""));
                if (!fluidStack.isEmpty()){
                    matrix.pushPose();
                    matrix.translate(0.5, 0.5, 0);
                    int displayAmount = fluidStack.getAmount();
                    AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 8 / 16D, 1.25 / 16D + (fluidStack.getAmount() / (double) fluidStack.getAmount()) * (5.5 / 16D), 15 / 16D);
                    FluidDrawerRenderer.renderFluidStack(matrix, renderer, light, overlayLight, fluidStack, displayAmount, fluidStack.getAmount(), 0.007f, options, bounds, true, true);
                    matrix.popPose();
                }
                fluidStack = FluidStack.loadFluidStackFromNBT(tileTag.getCompound(3 + ""));
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
}
