package com.buuz135.functionalstorage.item.component;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public interface SizeProvider {
    BiMap<String, MapCodec<? extends SizeProvider>> REGISTRY = HashBiMap.create(Map.of(
            "modify_factor", ModifyFactor.CODEC,
            "set_base", SetBase.CODEC,
            "modify_base", ModifyBase.CODEC
    ));
    Codec<SizeProvider> CODEC = Codec.STRING.dispatch(t -> REGISTRY.inverse().get(t.codec()), REGISTRY::get);

    default float applyBaseModifier(float source) {
        return source;
    }

    default float applyFactorModifier(float source) {
        return source;
    }

    Component getTooltip(Component object);

    MapCodec<? extends SizeProvider> codec();

    static int calculate(IItemHandler upgrades, Supplier<DataComponentType<SizeProvider>> component, int baseIn, ItemStack[] replacements) {
        return (int) Math.floor(calculateAsFactor(upgrades, component, baseIn, replacements));
    }

    static int calculate(IItemHandler upgrades, Supplier<DataComponentType<SizeProvider>> component, int baseIn) {
        return (int) Math.floor(calculateAsFactor(upgrades, component, baseIn));
    }

    static float calculateAsFactor(IItemHandler upgrades, Supplier<DataComponentType<SizeProvider>> component, float baseIn) {
        return calculateAsFactor(upgrades, component, baseIn, new ItemStack[upgrades.getSlots()]);
    }

    static float calculateAsFactor(IItemHandler upgrades, Supplier<DataComponentType<SizeProvider>> component, float baseIn, ItemStack[] replacements) {
        float factor = 1f;
        float base = baseIn;
        for (int i = 0; i < upgrades.getSlots(); i++) {
            var stack = Objects.requireNonNullElse(replacements[i], upgrades.getStackInSlot(i));
            if (!stack.isEmpty()) {
                var comp = stack.get(component);
                if (comp != null) {
                    factor = comp.applyFactorModifier(factor);
                    base = comp.applyBaseModifier(base);
                }
            }
        }

        factor = Math.max(factor, 0f);
        base = Math.max(base, 0f);

        float result = base * factor;
        return (float) (Math.floor(result * 100f) / 100d);
    }

    record ModifyFactor(float factor) implements SizeProvider {
        private static final DecimalFormat TWO_DECIMALS = new DecimalFormat("#0.00");
        public static final MapCodec<ModifyFactor> CODEC = Codec.floatRange(0, Float.MAX_VALUE)
                .fieldOf("factor")
                .xmap(ModifyFactor::new, ModifyFactor::factor);

        @Override
        public float applyFactorModifier(float source) {
            return source * factor;
        }

        @Override
        public Component getTooltip(Component object) {
            if (factor < 1) {
                return Component.translatable("storageupgrade.desc.modify_factor_div", object, format(1 / factor));
            }
            return Component.translatable("storageupgrade.desc.modify_factor_mult", object, format(factor));
        }

        private static String format(float value) {
            if (Integer.valueOf((int)value).floatValue() == value) {
                return String.valueOf((int)value);
            }
            return TWO_DECIMALS.format(value);
        }

        @Override
        public MapCodec<? extends SizeProvider> codec() {
            return CODEC;
        }
    }

    record SetBase(float amount) implements SizeProvider {
        public SetBase(int amount) {
            this((float)amount);
        }

        public static final MapCodec<SetBase> CODEC = Codec.withAlternative(
                    Codec.floatRange(0, Float.MAX_VALUE),
                    Codec.intRange(1, Integer.MAX_VALUE).xmap(Integer::floatValue, Float::intValue)
                )
                .fieldOf("amount")
                .xmap(SetBase::new, SetBase::amount);

        @Override
        public float applyBaseModifier(float source) {
            return amount;
        }

        @Override
        public Component getTooltip(Component object) {
            return Component.translatable("storageupgrade.desc.set_base", object, amount);
        }

        @Override
        public MapCodec<? extends SizeProvider> codec() {
            return CODEC;
        }
    }

    record ModifyBase(float amount) implements SizeProvider {
        public ModifyBase(int amount) {
            this((float) amount);
        }
        public static final MapCodec<ModifyBase> CODEC = Codec.withAlternative(
                    Codec.FLOAT,
                    Codec.INT.xmap(Integer::floatValue, Float::intValue)
                )
                .fieldOf("amount")
                .xmap(ModifyBase::new, ModifyBase::amount);

        @Override
        public float applyBaseModifier(float source) {
            return source + amount;
        }

        @Override
        public Component getTooltip(Component object) {
            return amount < 0 ? Component.translatable("storageupgrade.desc.modify_base_dec", object, -amount) : Component.translatable("storageupgrade.desc.modify_base_inc", object, amount);
        }

        @Override
        public MapCodec<? extends SizeProvider> codec() {
            return CODEC;
        }
    }
}
