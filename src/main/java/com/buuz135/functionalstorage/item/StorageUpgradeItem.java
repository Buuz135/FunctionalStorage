package com.buuz135.functionalstorage.item;

import com.buuz135.functionalstorage.block.config.FunctionalStorageConfig;
import com.hrznstudio.titanium.item.BasicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.List;

public class StorageUpgradeItem extends UpgradeItem{

    private final StorageTier storageTier;

    public StorageUpgradeItem(StorageTier tier) {
        super(new Properties(), Type.STORAGE);
        this.storageTier = tier;
    }

    public int getStorageMultiplier() {
        return FunctionalStorageConfig.getLevelMult(storageTier.getLevel());
    }

    public StorageTier getStorageTier() {
        return storageTier;
    }

    @Override
    public void addTooltipDetails(@Nullable BasicItem.Key key, ItemStack stack, List<Component> tooltip, boolean advanced) {
        super.addTooltipDetails(key, stack, tooltip, advanced);
        if (storageTier == StorageTier.IRON){
            tooltip.add(Component.translatable("item.utility.downgrade").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("storageupgrade.desc.item").withStyle(ChatFormatting.GRAY).append(new DecimalFormat().format(FunctionalStorageConfig.getLevelMult(this.storageTier.getLevel()))));
            tooltip.add(Component.translatable("storageupgrade.desc.fluid").withStyle(ChatFormatting.GRAY).append(new DecimalFormat().format(FunctionalStorageConfig.getLevelMult(this.storageTier.getLevel()) / FunctionalStorageConfig.FLUID_DIVISOR)));
            tooltip.add(Component.translatable("storageupgrade.desc.range", new DecimalFormat().format(FunctionalStorageConfig.getLevelMult(this.storageTier.getLevel()) / FunctionalStorageConfig.RANGE_DIVISOR)).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean hasTooltipDetails(@Nullable BasicItem.Key key) {
        return key == null;
    }

    @Override
    public boolean isFoil(ItemStack p_41453_) {
        return storageTier == StorageTier.MAX_STORAGE;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Component getName(ItemStack p_41458_) {
        Component component = super.getName(p_41458_);
        if (component instanceof MutableComponent) {
            ((MutableComponent) component).setStyle(Style.EMPTY.withColor(storageTier == StorageTier.NETHERITE && Minecraft.getInstance().level != null ? Mth.hsvToRgb((Minecraft.getInstance().level.getGameTime() % 360) / 360f, 1, 1) : storageTier.getColor()));
        }
        return component;
    }

    public static enum StorageTier {
        COPPER(1, Mth.color(204/255f, 109/255f, 81/255f)),
        GOLD(2, Mth.color(233/255f, 177/255f, 21/255f)),
        DIAMOND(3, Mth.color(32/255f, 197/255f, 181/255f)),
        NETHERITE(4, Mth.color(49, 41, 42)),
        IRON(0, Mth.color(130/255f, 130/255f, 130/255f)),
        MAX_STORAGE(-1, Mth.color(167/255f, 54/255f, 247/255f))
        ;

        private final int level;
        private final int color;

        StorageTier(int level, int color) {
            this.level = level;
            this.color = color;
        }

        public int getLevel() {
            return level;
        }

        public int getColor() {
            return color;
        }
    }
}
