package com.buuz135.functionalstorage.client.model;

import com.buuz135.functionalstorage.util.CustomFramedDrawerModelData;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.model.data.ModelProperty;

import java.util.Map;

public class FramedDrawerModelData extends CustomFramedDrawerModelData {
    public static final ModelProperty<CustomFramedDrawerModelData> FRAMED_PROPERTY =
            CustomFramedDrawerModelData.MODEL_PROPERTY;

    public FramedDrawerModelData(Map<String, Item> design) {
        super(design);
    }
}
