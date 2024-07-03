package com.buuz135.functionalstorage.client.item;

import com.buuz135.functionalstorage.client.model.FramedDrawerModelData;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public abstract class FunctionalStorageISTER extends BlockEntityWithoutLevelRenderer {

    protected final Int2ObjectMap<ModelData> modelCache = new Int2ObjectArrayMap<>();

    public FunctionalStorageISTER() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    protected EntityModelSet getEntityModels() {
        //Just have this method as a helper for what we pass as entity models rather than bothering to
        // use an AT to access it directly
        return Minecraft.getInstance().getEntityModels();
    }

    protected BlockEntityRenderDispatcher getBlockEntityRenderDispatcher() {
        //Just have this method as a helper for what we pass as the block entity render dispatcher
        // rather than bothering to use an AT to access it directly
        return Minecraft.getInstance().getBlockEntityRenderDispatcher();
    }

    protected Camera getCamera() {
        return getBlockEntityRenderDispatcher().camera;
    }

    @Override
    public abstract void onResourceManagerReload(@NotNull ResourceManager resourceManager);

    public abstract void renderByItem(HolderLookup.Provider access, @NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer,
                                      int light, int overlayLight);

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderByItem(Minecraft.getInstance().level.registryAccess(), stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
    }

    /**
     * @implNote Heavily based on/from vanilla's ItemRenderer#render code that calls the renderByItem method on the ISBER
     */
    protected void renderBlockItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer,
                                   int light, int overlayLight, ModelData modelData) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return;
        }
        Block block = blockItem.getBlock();
        boolean fabulous;
        if (displayContext != ItemDisplayContext.GUI && !displayContext.firstPerson()) {
            fabulous = !(block instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock);
        } else {
            fabulous = true;
        }
        //ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();

        // Pop off the transformations applied by ItemRenderer before calling this
        matrix.popPose();
        matrix.pushPose();

        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        BlockState defaultState = block.defaultBlockState();

        BakedModel mainModel = minecraft.getModelManager().getBlockModelShaper().getBlockModel(defaultState);
        mainModel = mainModel.applyTransform(displayContext, matrix, isLeftHand(displayContext));
        matrix.translate(-.5, -.5, -.5); // Replicate ItemRenderer's translation
        long seed = 42;
        RandomSource random = RandomSource.create();
        boolean glint = stack.hasFoil();
        for (BakedModel model : mainModel.getRenderPasses(stack, fabulous)) {
            for (RenderType renderType : model.getRenderTypes(stack, fabulous)) {
                VertexConsumer consumer = null;
                if (fabulous) {
                    consumer = ItemRenderer.getFoilBufferDirect(renderer, renderType, true, glint);
                } else {
                    consumer = ItemRenderer.getFoilBuffer(renderer, renderType, true, glint);
                }
                for (Direction direction : Direction.values()) {
                    random.setSeed(seed);
                    itemRenderer.renderQuadList(matrix, consumer, model.getQuads(defaultState, direction, random, modelData, null), stack, light, overlayLight);
                }
                random.setSeed(seed);
                itemRenderer.renderQuadList(matrix, consumer, model.getQuads(defaultState, null, random, modelData, null), stack, light, overlayLight);
            }
        }


    }

    protected ModelData getData(ItemStack stack) {
        ModelData modelData = ModelData.EMPTY;
        if (stack.has(FSAttachments.STYLE)) {
            var tag = stack.get(FSAttachments.STYLE);
            modelData = modelCache.computeIfAbsent(tag.hashCode(), o -> {
                HashMap<String, Item> data = new HashMap<>();
                data.put("particle", BuiltInRegistries.ITEM.get(ResourceLocation.parse(tag.getString("particle"))));
                data.put("front", BuiltInRegistries.ITEM.get(ResourceLocation.parse(tag.getString("front"))));
                data.put("side", BuiltInRegistries.ITEM.get(ResourceLocation.parse(tag.getString("side"))));
                data.put("front_divider", BuiltInRegistries.ITEM.get(ResourceLocation.parse(tag.getString("front_divider"))));
                var framedDrawerModelData = new FramedDrawerModelData(data);
                return ModelData.builder().with(FramedDrawerModelData.FRAMED_PROPERTY, framedDrawerModelData).build();
            });
        }
        return modelData;
    }

    private static boolean isLeftHand(ItemDisplayContext type)
    {
        return type == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || type == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
    }
}
