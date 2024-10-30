package com.buuz135.functionalstorage.recipe;


import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.FramedBlock;
import com.buuz135.functionalstorage.block.FramedDrawerBlock;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class FramedDrawerRecipe extends CustomRecipe {

    public FramedDrawerRecipe() {
        super(CraftingBookCategory.MISC);
    }


    public static boolean isBlockItem(@Nullable ItemStack stack) {
        return stack == null || (!stack.isEmpty() && stack.getItem() instanceof BlockItem);
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {
        if (inv.size() < 3) return false;
        var first = inv.getItem(0);
        var second = inv.getItem(1);
        var drawer = inv.getItem(2);

        return !drawer.isEmpty() && isBlockItem(first) && isBlockItem(second) && (inv.size() < 4 || inv.getItem(3).isEmpty() || inv.getItem(3).getItem() instanceof BlockItem)
                && drawer.getItem() instanceof BlockItem bi && bi.getBlock() instanceof FramedBlock;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registryAccess) {
        return FramedDrawerBlock.fill(inv.getItem(0), inv.getItem(1), inv.getItem(2).copy(), inv.size() >= 4 ? inv.getItem(3) : ItemStack.EMPTY);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return FunctionalStorage.FRAMED_RECIPE_SERIALIZER.value();
    }
}
