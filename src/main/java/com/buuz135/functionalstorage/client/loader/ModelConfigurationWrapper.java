package com.buuz135.functionalstorage.client.loader;

import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.Material;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometryPart;

import javax.annotation.Nullable;

/**
 * Class from Mantle {@url https://github.com/SlimeKnights/Mantle/blob/1.18.2/src/main/java/slimeknights/mantle/client/}
 *
 * Wrapper around a {@link IModelConfiguration} instance to allow easier extending, mostly for dynamic textures
 */
@SuppressWarnings("WeakerAccess")
public class ModelConfigurationWrapper implements IModelConfiguration {
    private final IModelConfiguration base;

    /**
     * Creates a new configuration wrapper
     * @param base  Base model configuration
     */
    public ModelConfigurationWrapper(IModelConfiguration base) {
        this.base = base;
    }

    @Nullable
    @Override
    public UnbakedModel getOwnerModel() {
        return base.getOwnerModel();
    }

    @Override
    public String getModelName() {
        return base.getModelName();
    }

    @Override
    public boolean isTexturePresent(String name) {
        return base.isTexturePresent(name);
    }

    @Override
    public Material resolveTexture(String name) {
        return base.resolveTexture(name);
    }

    @Override
    public boolean isShadedInGui() {
        return base.isShadedInGui();
    }

    @Override
    public boolean isSideLit() {
        return base.isSideLit();
    }

    @Override
    public boolean useSmoothLighting() {
        return base.useSmoothLighting();
    }

    @Override
    public ItemTransforms getCameraTransforms() {
        return base.getCameraTransforms();
    }

    @Override
    public ModelState getCombinedTransform() {
        return base.getCombinedTransform();
    }

    @Override
    public boolean getPartVisibility(IModelGeometryPart part, boolean fallback) {
        return base.getPartVisibility(part, fallback);
    }
}