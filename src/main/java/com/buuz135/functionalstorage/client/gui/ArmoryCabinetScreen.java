package com.buuz135.functionalstorage.client.gui;

import com.buuz135.functionalstorage.inventory.ArmoryCabinetMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

public class ArmoryCabinetScreen extends AbstractContainerScreen<ArmoryCabinetMenu> {

    private static final ResourceLocation TITANIUM_BACKGROUND = ResourceLocation.fromNamespaceAndPath("titanium", "textures/gui/background.png");
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller_disabled");
    private static final int SLOT_AREA_TOP = 29;
    private static final int SLOT_AREA_HEIGHT = ArmoryCabinetMenu.VISIBLE_ROWS * 18;
    private static final int SLOT_BACKGROUND_U = 1;
    private static final int SLOT_BACKGROUND_V = 185;
    private static final int SEARCH_X = 96;
    private static final int SEARCH_Y = 4;
    private static final int SEARCH_WIDTH = 72;
    private static final int SEARCH_HEIGHT = 12;
    private static final int SCROLLBAR_X = 158;
    private static final int SCROLLBAR_Y = 18;
    private static final int SCROLLBAR_WIDTH = 12;
    private static final int SCROLLBAR_HEIGHT = 72;
    private static final int THUMB_WIDTH = 12;
    private static final int THUMB_HEIGHT = 15;
    private EditBox searchBox;
    private boolean scrolling;

    public ArmoryCabinetScreen(ArmoryCabinetMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 194;
        inventoryLabelY = 101;
    }

    @Override
    protected void init() {
        super.init();
        searchBox = new EditBox(font, leftPos + SEARCH_X, topPos + SEARCH_Y, SEARCH_WIDTH, SEARCH_HEIGHT, Component.translatable("gui.functionalstorage.armory_search"));
        searchBox.setMaxLength(50);
        searchBox.setResponder(this::onSearchChanged);
        addRenderableWidget(searchBox);
    }

    private void onSearchChanged(String text) {
        menu.setQuery(text);
        syncQuery(text);
    }

    private void syncQuery(String text) {
        if (minecraft == null || minecraft.gameMode == null) return;
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ArmoryCabinetMenu.BUTTON_QUERY_CLEAR);
        text.chars().limit(50).forEach(value -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ArmoryCabinetMenu.BUTTON_QUERY_APPEND_BASE + value));
        syncScroll();
    }

    private void syncScroll() {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ArmoryCabinetMenu.BUTTON_SCROLL_BASE + menu.getScrollRow());
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.blit(TITANIUM_BACKGROUND, x, y, 0, 0, imageWidth, 184);

        drawCabinetSlots(graphics);
        drawScrollbar(graphics);
    }


    private void drawCabinetSlots(GuiGraphics graphics) {
        for (int row = 0; row < ArmoryCabinetMenu.VISIBLE_ROWS; row++) {
            for (int column = 0; column < ArmoryCabinetMenu.COLUMNS; column++) {
                graphics.blit(TITANIUM_BACKGROUND, leftPos + 7 + column * 18, topPos + 18 + row * 18, SLOT_BACKGROUND_U, SLOT_BACKGROUND_V, 18, 18);
            }
        }
    }

    private void drawScrollbar(GuiGraphics graphics) {
        int barX = leftPos + SCROLLBAR_X;
        int barY = topPos + SCROLLBAR_Y;
        graphics.fill(barX, barY, barX + SCROLLBAR_WIDTH, barY + SCROLLBAR_HEIGHT, 0xFF8B8B8B);
        graphics.fill(barX + 1, barY + 1, barX + SCROLLBAR_WIDTH - 1, barY + SCROLLBAR_HEIGHT - 1, 0xFFCCCCCC);
        int max = menu.getMaxScrollRow();
        int thumbY = max <= 0 ? barY : barY + (int) ((SCROLLBAR_HEIGHT - THUMB_HEIGHT) * (menu.getScrollRow() / (float) max));
        graphics.blitSprite(max <= 0 ? SCROLLER_DISABLED_SPRITE : SCROLLER_SPRITE, barX, thumbY, THUMB_WIDTH, THUMB_HEIGHT);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY - 9, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isInCabinetArea(mouseX, mouseY)) {
            menu.setScrollRow(menu.getScrollRow() - (int) Math.signum(scrollY));
            syncScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchBox != null && searchBox.isFocused() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            searchBox.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isInScrollbar(mouseX, mouseY)) {
            scrolling = true;
            updateScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (scrolling) {
            updateScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        scrolling = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean isInCabinetArea(double mouseX, double mouseY) {
        return mouseX >= leftPos + 8 && mouseX < leftPos + 8 + ArmoryCabinetMenu.COLUMNS * 18 && mouseY >= topPos + SLOT_AREA_TOP && mouseY < topPos + SLOT_AREA_TOP + SLOT_AREA_HEIGHT;
    }

    private boolean isInScrollbar(double mouseX, double mouseY) {
        return mouseX >= leftPos + SCROLLBAR_X && mouseX < leftPos + SCROLLBAR_X + SCROLLBAR_WIDTH && mouseY >= topPos + SCROLLBAR_Y && mouseY < topPos + SCROLLBAR_Y + SCROLLBAR_HEIGHT;
    }

    private void updateScrollFromMouse(double mouseY) {
        int max = menu.getMaxScrollRow();
        int relative = (int) (mouseY - (topPos + SCROLLBAR_Y) - THUMB_HEIGHT / 2.0D);
        menu.setScrollRow(max <= 0 ? 0 : Math.round((relative / (float) (SCROLLBAR_HEIGHT - THUMB_HEIGHT)) * max));
        syncScroll();
    }
}
