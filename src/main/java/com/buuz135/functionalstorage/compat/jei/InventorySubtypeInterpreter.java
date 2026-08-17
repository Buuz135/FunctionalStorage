package com.buuz135.functionalstorage.compat.jei;

import com.buuz135.functionalstorage.item.FSAttachments;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;


public class InventorySubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {

    @Override
    public @Nullable Object getSubtypeData(ItemStack ingredient, UidContext context) {
        return ingredient.get(FSAttachments.TILE);
    }
}
