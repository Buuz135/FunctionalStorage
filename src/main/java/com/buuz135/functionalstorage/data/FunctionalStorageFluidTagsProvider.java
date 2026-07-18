package com.buuz135.functionalstorage.data;

import com.buuz135.functionalstorage.util.StorageTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class FunctionalStorageFluidTagsProvider extends FluidTagsProvider {

    public FunctionalStorageFluidTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(StorageTags.FLUID_DRAWER_STORAGE_DENYLIST);
    }

    @Override
    public String getName() {
        return "Functional Storage Fluid Tags";
    }
}
