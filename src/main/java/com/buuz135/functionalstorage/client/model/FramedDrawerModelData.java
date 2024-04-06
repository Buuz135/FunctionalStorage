/*
 * This file is part of Industrial Foregoing.
 *
 * Copyright 2021, Buuz135
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.buuz135.functionalstorage.client.model;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.registries.ForgeRegistries;
import java.util.HashMap;
import java.util.Map;

public class FramedDrawerModelData implements INBTSerializable<CompoundTag> {

    public static final ModelProperty<FramedDrawerModelData> FRAMED_PROPERTY = new ModelProperty<>();
    private Map<String, Item> design;

    private String code = "";

    public FramedDrawerModelData(Map<String, Item> design) {
        this.design = design;
        this.generateCode();
    }

    public Map<String, Item> getDesign() {
        return design;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = new CompoundTag();
        design.forEach((s, item) -> compoundTag.putString(s, ForgeRegistries.ITEMS.getKey(item).toString()));
        return compoundTag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        design = new HashMap<>();
        for (String allKey : nbt.getAllKeys()) {
            design.put(allKey, ForgeRegistries.ITEMS.getValue(new ResourceLocation(nbt.getString(allKey))));
        }
        this.generateCode();
    }

    private void generateCode(){
        this.code = "";
        this.design.forEach((s, item) -> {
            this.code += (s + ForgeRegistries.ITEMS.getKey(item).toString());
        });
    }

    public String getCode() {
        return code;
    }
}
