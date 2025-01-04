package com.buuz135.functionalstorage.item;

import com.buuz135.functionalstorage.block.config.FunctionalStorageConfig;
import com.buuz135.functionalstorage.item.component.SizeProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class StorageUpgradeItem extends UpgradeItem {

    private final StorageTier storageTier;

    public StorageUpgradeItem(StorageTier tier) {
        super(getProps(tier), Type.STORAGE);
        this.storageTier = tier;
    }

    private static Properties getProps(StorageTier tier) {
        var props = new Properties();
        if (tier == StorageTier.IRON) {
            props = props.component(FSAttachments.ITEM_STORAGE_MODIFIER, new SizeProvider.SetBase(1));
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
    @OnlyIn(Dist.CLIENT)
    public Component getName(ItemStack p_41458_) {
        Component component = super.getName(p_41458_);
        if (component instanceof MutableComponent) {
            ((MutableComponent) component).setStyle(Style.EMPTY.withColor(storageTier == StorageTier.NETHERITE && Minecraft.getInstance().level != null ? Mth.hsvToRgb((Minecraft.getInstance().level.getGameTime() % 360) / 360f, 1, 1) : storageTier.getColor()));
        }
        return component;
    }

    public enum StorageTier {
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
