package com.buuz135.functionalstorage.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import com.hrznstudio.titanium.nbthandler.INBTSerializable;
import net.neoforged.neoforge.model.data.ModelProperty;

import java.util.HashMap;
import java.util.Map;

public class CustomFramedDrawerModelData implements INBTSerializable<CompoundTag> {

    public static final ModelProperty<CustomFramedDrawerModelData> MODEL_PROPERTY = new ModelProperty<>();

    private Map<String, Item> design;
    private String code = "";

    public CustomFramedDrawerModelData(Map<String, Item> design) {
        this.design = Map.copyOf(design);
        generateCode();
    }

    public Map<String, Item> getDesign() {
        return design;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        design.forEach((key, item) -> tag.putString(key, BuiltInRegistries.ITEM.getKey(item).toString()));
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        Map<String, Item> loadedDesign = new HashMap<>();
        for (String key : tag.keySet()) {
            tag.getString(key).map(Utils::resourceLocation).map(BuiltInRegistries.ITEM::getValue).ifPresent(item -> loadedDesign.put(key, item));
        }
        design = Map.copyOf(loadedDesign);
        generateCode();
    }

    private void generateCode() {
        StringBuilder builder = new StringBuilder();
        design.forEach((key, item) -> builder.append(key).append(BuiltInRegistries.ITEM.getKey(item)));
        code = builder.toString();
    }

    public String getCode() {
        return code;
    }
}
