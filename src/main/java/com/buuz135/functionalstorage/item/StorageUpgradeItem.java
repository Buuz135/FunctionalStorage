package com.buuz135.functionalstorage.item;

import com.buuz135.functionalstorage.block.config.FunctionalStorageConfig;
import com.buuz135.functionalstorage.item.component.SizeProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class StorageUpgradeItem extends UpgradeItem {

    private final StorageTier storageTier;

    public StorageUpgradeItem(StorageTier tier) {
        super(getProps(tier), Type.STORAGE);
        this.storageTier = tier;
    }

    private static Properties getProps(StorageTier tier) {
        var props = new Properties();
        if (tier == StorageTier.IRON) {
            props = props.component(FSAttachments.ITEM_STORAGE_MODIFIER, new SizeProvider.SetBase(1)).component(FSAttachments.FLUID_STORAGE_MODIFIER, new SizeProvider.SetBase(1));
        } else {
            var level = (float) FunctionalStorageConfig.getLevelMult(tier.getLevel());
            props = props
                    .component(FSAttachments.ITEM_STORAGE_MODIFIER, new SizeProvider.ModifyFactor(level))
                    .component(FSAttachments.FLUID_STORAGE_MODIFIER, new SizeProvider.ModifyFactor(level / FunctionalStorageConfig.FLUID_DIVISOR))
                    .component(FSAttachments.CONTROLLER_RANGE_MODIFIER, new SizeProvider.ModifyFactor(level / FunctionalStorageConfig.RANGE_DIVISOR));
        }
        return props;
    }

    @Override
    public boolean isFoil(ItemStack p_41453_) {
        return storageTier == StorageTier.MAX_STORAGE;
    }

    @Override
    public Component getName(ItemStack p_41458_) {
        Component component = super.getName(p_41458_);
        if (component instanceof MutableComponent) {
            ((MutableComponent) component).setStyle(Style.EMPTY.withColor(storageTier == StorageTier.NETHERITE
                    ? Mth.hsvToRgb((System.currentTimeMillis() / 50L % 360L) / 360f, 1, 1)
                    : storageTier.getColor()));
        }
        return component;
    }

    public enum StorageTier {
        COPPER(1, ARGB.color(204, 109, 81)),
        GOLD(2, ARGB.color(233, 177, 21)),
        DIAMOND(3, ARGB.color(32, 197, 181)),
        NETHERITE(4, ARGB.color(49, 41, 42)),
        IRON(0, ARGB.color(130, 130, 130)),
        MAX_STORAGE(-1, ARGB.color(167, 54, 247))
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
