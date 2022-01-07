package com.buuz135.functionalstorage.compat.top;


import com.buuz135.functionalstorage.FunctionalStorage;
import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IItemStyle;
import mcjty.theoneprobe.apiimpl.styles.ItemStyle;
import mcjty.theoneprobe.network.NetworkTools;
import mcjty.theoneprobe.rendering.RenderHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class CustomElementItemStack implements IElement {

    public static ResourceLocation RL = new ResourceLocation(FunctionalStorage.MOD_ID, "drawer_element");

    private final ItemStack itemStack;
    private final IItemStyle style;
    private final String display;

    public CustomElementItemStack(ItemStack itemStack, String display, IItemStyle style) {
        this.itemStack = itemStack;
        this.style = style;
        this.display = display;
    }

    public CustomElementItemStack(FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            this.itemStack = NetworkTools.readItemStack(buf);
        } else {
            this.itemStack = ItemStack.EMPTY;
        }

        this.style = (new ItemStyle()).width(buf.readInt()).height(buf.readInt());
        this.display = buf.readUtf();
    }

    public void render(PoseStack matrixStack, int x, int y) {
        ItemRenderer itemRender = Minecraft.getInstance().getItemRenderer();
        if (!itemStack.isEmpty()) {
            int size = itemStack.getCount();

            if (!RenderHelper.renderItemStack(Minecraft.getInstance(), itemRender, itemStack, matrixStack, x + (style.getWidth() - 18) / 2, y + (style.getHeight() - 18) / 2, display)) {
                Minecraft var10000 = Minecraft.getInstance();
                ChatFormatting var10004 = ChatFormatting.RED;
                RenderHelper.renderText(var10000, matrixStack, x, y, var10004 + "ERROR: " + itemStack.getHoverName());
            }
        }

    }

    public int getWidth() {
        return this.style.getWidth();
    }

    public int getHeight() {
        return this.style.getHeight();
    }

    public void toBytes(FriendlyByteBuf buf) {
        if (!this.itemStack.isEmpty()) {
            buf.writeBoolean(true);
            NetworkTools.writeItemStack(buf, this.itemStack);
        } else {
            buf.writeBoolean(false);
        }

        buf.writeInt(this.style.getWidth());
        buf.writeInt(this.style.getHeight());
        buf.writeUtf(this.display);
    }

    public ResourceLocation getID() {
        return RL;
    }
}
