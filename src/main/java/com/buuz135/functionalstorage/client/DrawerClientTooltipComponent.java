package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.util.TooltipUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class DrawerClientTooltipComponent implements ClientTooltipComponent {
    private static final int ITEM_SIZE = 16;
    private static final int PADDING = 4;
    private final Contents contents;

    public DrawerClientTooltipComponent(Contents contents) {
        this.contents = contents;
    }

    @Override
    public int getHeight(Font font) {
        return ITEM_SIZE + 2;
    }

    @Override
    public int getWidth(Font font) {
        if (contents.entries().isEmpty()) {
            return 0;
        }
        return PADDING * 2 + ITEM_SIZE + (contents.entries().size() - 1) * contents.spacing();
    }

    @Override
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
        for (int index = 0; index < contents.entries().size(); index++) {
            Entry entry = contents.entries().get(index);
            int itemX = x + PADDING + index * contents.spacing();
            if (entry.amount() == null) {
                TooltipUtil.renderItemIntoGUI(graphics, entry.stack(), itemX, y, index);
            } else {
                TooltipUtil.renderItemAdvanced(graphics, entry.stack(), itemX, y, index, entry.amount());
            }
        }
    }

    public record Contents(List<Entry> entries, int spacing) implements TooltipComponent {
        public Contents {
            entries = List.copyOf(entries);
        }
    }

    public record Entry(ItemStack stack, @Nullable String amount) {
        public Entry {
            stack = stack.copy();
        }
    }
}
