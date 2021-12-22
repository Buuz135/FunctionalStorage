package com.buuz135.functionalstorage.item;

import com.hrznstudio.titanium.item.BasicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class StorageUpgradeItem extends UpgradeItem{

    private final StorageTier storageTier;

    public StorageUpgradeItem(StorageTier tier) {
        super(new Properties(), Type.STORAGE);
        this.storageTier = tier;
    }

    public int getStorageMultiplier() {
        return storageTier.storageMultiplier;
    }

    public StorageTier getStorageTier() {
        return storageTier;
    }

    @Override
    public void addTooltipDetails(@Nullable BasicItem.Key key, ItemStack stack, List<Component> tooltip, boolean advanced) {
        super.addTooltipDetails(key, stack, tooltip, advanced);
        if (storageTier == StorageTier.IRON){
            tooltip.add(new TranslatableComponent("item.utility.downgrade").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(new TranslatableComponent("storageupgrade.desc").withStyle(ChatFormatting.GRAY).append(this.storageTier.getStorageMultiplier() + ""));
        }
    }

    @Override
    public boolean hasTooltipDetails(@Nullable BasicItem.Key key) {
        return key == null;
    }


    @Override
    public Component getName(ItemStack p_41458_) {
        Component component = super.getName(p_41458_);
        if (component instanceof TranslatableComponent){
            ((TranslatableComponent) component).setStyle(Style.EMPTY.withColor(storageTier == StorageTier.NETHERITE && Minecraft.getInstance().level != null ? Color.HSBtoRGB((Minecraft.getInstance().level.getGameTime() % 360) / 360f , 1, 1) : storageTier.getColor()));
        }
        return component;
    }

    public static enum StorageTier{
        COPPER(8, new Color(204,109,81).getRGB()),
        GOLD(16, new Color(233,177,21).getRGB()),
        DIAMOND(24, new Color(32,197,181).getRGB()),
        NETHERITE(32, new Color(49,41,42).getRGB()),
        IRON(1, new Color(130,130,130).getRGB());

        private final int storageMultiplier;
        private final int color;

        StorageTier(int storageMultiplier, int color) {
            this.storageMultiplier = storageMultiplier;
            this.color = color;
        }

        public int getStorageMultiplier() {
            return storageMultiplier;
        }

        public int getColor() {
            return color;
        }
    }
}
