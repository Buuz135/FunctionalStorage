package com.buuz135.functionalstorage.util;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public final class TooltipUtil {
    private TooltipUtil() {
    }

    public static void renderItems(GuiGraphicsExtractor graphics, List<ItemStack> items, int x, int y) {
        for (int i = 0; i < items.size(); i++) {
            renderItemIntoGUI(graphics, items.get(i), x + 18 * i, y, 512);
        }
    }

    public static void renderItemIntoGUI(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, int z) {
        graphics.item(stack, x, y, z);
    }

    public static void renderItemAdvanced(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, int z, String amount) {
        graphics.item(stack, x, y, z);
        renderItemStackOverlay(graphics, net.minecraft.client.Minecraft.getInstance().font, stack, x, y, amount, amount.length() - 2);
    }

    public static void renderItemStackOverlay(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int x, int y,
                                               @Nullable String text, int scaled) {
        if (stack.isEmpty() || stack.getCount() == 1 && text == null) {
            return;
        }
        String value = text == null ? String.valueOf(stack.getCount()) : text;
        if (text == null && stack.getCount() < 1) {
            value = ChatFormatting.RED + value;
        }
        float scale = scaled >= 2 ? 0.5F : scaled == 1 ? 0.75F : 1.0F;
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.scale(scale, scale);
        int drawX = Math.round((x + 17) / scale) - font.width(value);
        int drawY = Math.round((y + 9) / scale);
        graphics.text(font, value, drawX, drawY, 0xFFFFFFFF, true);
        pose.popMatrix();
    }
}
