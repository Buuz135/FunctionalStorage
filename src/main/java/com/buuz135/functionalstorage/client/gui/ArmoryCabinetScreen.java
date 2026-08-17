package com.buuz135.functionalstorage.client.gui;

import com.buuz135.functionalstorage.inventory.ArmoryCabinetMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

public class ArmoryCabinetScreen extends AbstractContainerScreen<ArmoryCabinetMenu> {

    private static final Identifier TITANIUM_BACKGROUND = Identifier.fromNamespaceAndPath("titanium", "textures/gui/background.png");
    private static final Identifier SCROLLER_SPRITE = Identifier.withDefaultNamespace("container/creative_inventory/scroller");
    private static final Identifier SCROLLER_DISABLED_SPRITE = Identifier.withDefaultNamespace("container/creative_inventory/scroller_disabled");
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
        super(menu, inventory, title, 176, 194);
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
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int x = leftPos;
        int y = topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TITANIUM_BACKGROUND, x, y, 0, 0, imageWidth, 184, 256, 256);

        drawCabinetSlots(graphics);
        drawScrollbar(graphics);
    }


    private void drawCabinetSlots(GuiGraphicsExtractor graphics) {
        for (int row = 0; row < ArmoryCabinetMenu.VISIBLE_ROWS; row++) {
            for (int column = 0; column < ArmoryCabinetMenu.COLUMNS; column++) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, TITANIUM_BACKGROUND,
                        leftPos + 7 + column * 18, topPos + 18 + row * 18,
                        SLOT_BACKGROUND_U, SLOT_BACKGROUND_V, 18, 18, 256, 256);
            }
        }
    }

    private void drawScrollbar(GuiGraphicsExtractor graphics) {
        int barX = leftPos + SCROLLBAR_X;
        int barY = topPos + SCROLLBAR_Y;
        graphics.fill(barX, barY, barX + SCROLLBAR_WIDTH, barY + SCROLLBAR_HEIGHT, 0xFF8B8B8B);
        graphics.fill(barX + 1, barY + 1, barX + SCROLLBAR_WIDTH - 1, barY + SCROLLBAR_HEIGHT - 1, 0xFFCCCCCC);
        int max = menu.getMaxScrollRow();
        int thumbY = max <= 0 ? barY : barY + (int) ((SCROLLBAR_HEIGHT - THUMB_HEIGHT) * (menu.getScrollRow() / (float) max));
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                max <= 0 ? SCROLLER_DISABLED_SPRITE : SCROLLER_SPRITE,
                barX, thumbY, THUMB_WIDTH, THUMB_HEIGHT);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, 0x404040, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY - 9, 0x404040, false);
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
    public boolean keyPressed(KeyEvent event) {
        if (searchBox != null && searchBox.isFocused() && event.key() != GLFW.GLFW_KEY_ESCAPE) {
            searchBox.keyPressed(event);
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && isInScrollbar(event.x(), event.y())) {
            scrolling = true;
            updateScrollFromMouse(event.y());
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (scrolling) {
            updateScrollFromMouse(event.y());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        scrolling = false;
        return super.mouseReleased(event);
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
