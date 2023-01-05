package com.buuz135.functionalstorage.item;

import com.hrznstudio.titanium.item.BasicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

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
            tooltip.add(new TranslatableComponent("storageupgrade.desc.item").withStyle(ChatFormatting.GRAY).append(this.storageTier.getStorageMultiplier() + ""));
            tooltip.add(new TranslatableComponent("storageupgrade.desc.fluid").withStyle(ChatFormatting.GRAY).append(this.storageTier.getStorageMultiplier() / 2 + ""));
        }
    }

    @Override
    public boolean hasTooltipDetails(@Nullable BasicItem.Key key) {
        return key == null;
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public Component getName(ItemStack p_41458_) {
        Component component = super.getName(p_41458_);
        if (component instanceof TranslatableComponent) {
            ((TranslatableComponent) component).setStyle(Style.EMPTY.withColor(storageTier == StorageTier.NETHERITE && Minecraft.getInstance().level != null ? Mth.hsvToRgb((Minecraft.getInstance().level.getGameTime() % 360) / 360f, 1, 1) : storageTier.getColor()));
        }
        return component;
    }

    public static enum StorageTier {
        COPPER(8, Mth.color(204, 109, 81)),
        GOLD(16, Mth.color(233, 177, 21)),
        DIAMOND(24, Mth.color(32, 197, 181)),
        NETHERITE(32, Mth.color(49, 41, 42)),
        IRON(1, Mth.color(130, 130, 130));

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
