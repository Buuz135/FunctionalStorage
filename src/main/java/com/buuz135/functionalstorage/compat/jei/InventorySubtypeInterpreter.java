package com.buuz135.functionalstorage.compat.jei;

import com.buuz135.functionalstorage.item.FSAttachments;
import com.hrznstudio.titanium.item.AugmentWrapper;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class InventorySubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {

    @Override
    public @Nullable Object getSubtypeData(ItemStack ingredient, UidContext context) {
        return ingredient.get(FSAttachments.TILE);
    }

    @Override
    public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
        var result = new StringBuilder();
        result.append(ingredient.getOrDefault(FSAttachments.TILE, new CompoundTag()));
        return result.toString();
    }
}
