package com.buuz135.functionalstorage.client.gui;

import com.buuz135.functionalstorage.component.ControllerConsolidateButton;
import com.hrznstudio.titanium.client.screen.addon.BasicButtonAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ControllerConsolidateGuiAddon extends BasicButtonAddon {
    private final ControllerConsolidateButton button;

    public ControllerConsolidateGuiAddon(ControllerConsolidateButton button) {
        super(button);
        this.button = button;
    }

    @Override
    public void drawBackgroundLayer(GuiGraphicsExtractor graphics, Screen screen, IAssetProvider provider,
                                    int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        int x = guiX + getPosX();
        int y = guiY + getPosY();
        boolean hovered = mouseX >= x && mouseX < x + getXSize() && mouseY >= y && mouseY < y + getYSize();
        boolean running = button.state() == ControllerConsolidateButton.RUNNING;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                Identifier.withDefaultNamespace(running ? "widget/button_disabled" : hovered ? "widget/button_highlighted" : "widget/button"),
                x, y, getXSize(), getYSize());
        var font = Minecraft.getInstance().font;
        var label = fit(Component.translatable("gui.functionalstorage.consolidate"), getXSize() - 8);
        graphics.text(font, label, x + (getXSize() - font.width(label)) / 2, y + (getYSize() - 8) / 2,
                running ? 0xffa0a0a0 : 0xffffffff, true);
        graphics.text(font, fit(status(), 156), guiX + 10, guiY + 48, 0xff404040, false);
    }

    private Component fit(Component label, int width) {
        var font = Minecraft.getInstance().font;
        if (font.width(label) > width) {
            return Component.literal(font.plainSubstrByWidth(label.getString(), width - font.width("…")) + "…");
        }
        return label;
    }

    private Component status() {
        var progress = button.progress();
        return switch (button.state()) {
            case ControllerConsolidateButton.RUNNING -> progress.checked() < progress.drawers()
                    ? Component.translatable("gui.functionalstorage.consolidate.checking", progress.checked(), progress.drawers())
                    : Component.translatable("gui.functionalstorage.consolidate.moving", progress.processedTypes(), progress.types());
            case ControllerConsolidateButton.DONE -> Component.translatable("gui.functionalstorage.consolidate.done", button.result().freed());
            case ControllerConsolidateButton.STOPPED -> Component.translatable("gui.functionalstorage.consolidate.stopped");
            case ControllerConsolidateButton.FAILED -> Component.translatable("gui.functionalstorage.consolidate.error");
            default -> Component.empty();
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return button == 0 && this.button.state() != ControllerConsolidateButton.RUNNING && super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public List<Component> getTooltipLines() {
        var lines = new ArrayList<Component>();
        lines.add(Component.translatable("gui.functionalstorage.consolidate.tooltip"));
        if (button.state() != ControllerConsolidateButton.IDLE) lines.add(status());
        var result = button.result();
        if (button.state() == ControllerConsolidateButton.RUNNING) {
            lines.add(Component.translatable("gui.functionalstorage.consolidate.running"));
        } else if (button.state() != ControllerConsolidateButton.IDLE) {
            lines.add(Component.translatable(button.state() == ControllerConsolidateButton.FAILED
                    ? "gui.functionalstorage.consolidate.failed" : "gui.functionalstorage.consolidate.result",
                    result.moved(), result.freed()));
        }
        return lines;
    }
}
