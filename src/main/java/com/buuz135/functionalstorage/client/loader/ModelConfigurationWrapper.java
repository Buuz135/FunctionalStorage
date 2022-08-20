package com.buuz135.functionalstorage.client.loader;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.Nullable;

/**
 * Class from Mantle {@url https://github.com/SlimeKnights/Mantle/blob/1.18.2/src/main/java/slimeknights/mantle/client/}
 * <p>
 * Wrapper around a {@link IGeometryBakingContext} instance to allow easier extending, mostly for dynamic textures
 */
@SuppressWarnings("WeakerAccess")
public class ModelConfigurationWrapper implements IGeometryBakingContext {
    private final IGeometryBakingContext base;

    /**
     * Creates a new configuration wrapper
     * @param base  Base model configuration
     */
    public ModelConfigurationWrapper(IGeometryBakingContext base) {
        this.base = base;
    }

    public IGeometryBakingContext getOwner() {
        return base;
    }

    @Override
    public String getModelName() {
        return base.getModelName();
    }

    @Override
    public boolean hasMaterial(String name) {
        return base.hasMaterial(name);
    }

    @Override
    public Material getMaterial(String name) {
        return base.getMaterial(name);
    }


    @Override
    public RenderTypeGroup getRenderType(ResourceLocation name) {
        return IGeometryBakingContext.super.getRenderType(name);
    }

    @Override
    public boolean isGui3d() {
        return base.isGui3d();
    }

    @Override
    public boolean useBlockLight() {
        return base.useBlockLight();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return base.useAmbientOcclusion();
    }

    @Override
    public ItemTransforms getTransforms() {
        return base.getTransforms();
    }

    @Override
    public Transformation getRootTransform() {
        return base.getRootTransform();
    }

    @Override
    public @Nullable ResourceLocation getRenderTypeHint() {
        return base.getRenderTypeHint();
    }

    @Override
    public boolean isComponentVisible(String component, boolean fallback) {
        return base.isComponentVisible(component, fallback);
    }

}