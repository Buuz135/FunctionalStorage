package com.buuz135.functionalstorage.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.item.component.SizeProvider;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class FSAttachments {
    public static final DeferredRegister<DataComponentType<?>> DR = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, FunctionalStorage.MOD_ID);

    public static final Supplier<DataComponentType<ConfigurationToolItem.ConfigurationAction>> CONFIGURATION_ACTION =
            register("configuration_action", () -> ConfigurationToolItem.ConfigurationAction.LOCKING, builder -> builder.persistent(ConfigurationToolItem.ConfigurationAction.CODEC));

    public static final Supplier<DataComponentType<LinkingToolItem.ActionMode>> ACTION_MODE =
            register("action_mode", () -> LinkingToolItem.ActionMode.ADD, builder -> builder.persistent(LinkingToolItem.ActionMode.CODEC));

    public static final Supplier<DataComponentType<LinkingToolItem.LinkingMode>> LINKING_MODE =
            register("linking_mode", () -> LinkingToolItem.LinkingMode.SINGLE, builder -> builder.persistent(LinkingToolItem.LinkingMode.CODEC));

    public static final Supplier<DataComponentType<BlockPos>> FIRST_POSITION =
            register("first_pos", () -> BlockPos.ZERO, builder -> builder.persistent(BlockPos.CODEC));
    public static final ComponentSupplier<BlockPos> CONTROLLER =
            register("controller_position", () -> BlockPos.ZERO, builder -> builder.persistent(BlockPos.CODEC));

    public static final Supplier<DataComponentType<String>> ENDER_FREQUENCY = register("ender_frequency", () -> "", op -> op.persistent(Codec.STRING));
    public static final Supplier<DataComponentType<Unit>> ENDER_SAFETY = register("ender_safety", () -> Unit.INSTANCE, op -> op.persistent(Codec.unit(Unit.INSTANCE)));
    public static final Supplier<DataComponentType<Direction>> DIRECTION = register("direction", () -> Direction.NORTH, op -> op.persistent(Direction.CODEC));
    public static final Supplier<DataComponentType<Integer>> SLOT = register("slot", () -> 0, op -> op.persistent(Codec.intRange(0, UpgradeItem.MAX_SLOT - 1)));
    public static final Supplier<DataComponentType<Boolean>> LOCKED = register("locked", () -> false, op -> op.persistent(Codec.BOOL));

    public static final Supplier<DataComponentType<CompoundTag>> TILE = register("tile", CompoundTag::new, op -> op.persistent(CompoundTag.CODEC));
    public static final Supplier<DataComponentType<CompoundTag>> STYLE = register("style", CompoundTag::new, op -> op.persistent(CompoundTag.CODEC));

    public static final Supplier<DataComponentType<SizeProvider>> ITEM_STORAGE_MODIFIER = register("item_storage_modifier", SizeProvider.CODEC);
    public static final Supplier<DataComponentType<SizeProvider>> FLUID_STORAGE_MODIFIER = register("fluid_storage_modifier", SizeProvider.CODEC);
    public static final Supplier<DataComponentType<SizeProvider>> CONTROLLER_RANGE_MODIFIER = register("controller_range_modifier", SizeProvider.CODEC);

    private static <T> Supplier<DataComponentType<T>> register(String name, Codec<T> codec) {
        return DR.register(name, () -> DataComponentType.<T>builder().persistent(codec).build())::get;
    }

    private static <T> ComponentSupplier<T> register(String name, Supplier<T> defaultVal, UnaryOperator<DataComponentType.Builder<T>> op) {
        var registered = DR.register(name, () -> op.apply(DataComponentType.builder()).build());
        return new ComponentSupplier<>(registered, defaultVal);
    }

    public static class ComponentSupplier<T> implements Supplier<DataComponentType<T>> {
        private final Supplier<DataComponentType<T>> type;
        private final Supplier<T> defaultSupplier;

        public ComponentSupplier(Supplier<DataComponentType<T>> type, Supplier<T> defaultSupplier) {
            this.type = type;
            this.defaultSupplier = Suppliers.memoize(defaultSupplier::get);
        }

        public T get(ItemStack stack) {
            return stack.getOrDefault(type, defaultSupplier.get());
        }

        @Override
        public DataComponentType<T> get() {
            return type.get();
        }
    }
}
