package com.buuz135.functionalstorage.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Unit;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class FSAttachments {
    public static final DeferredRegister<AttachmentType<?>> DR = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, FunctionalStorage.MOD_ID);

    public static final Supplier<AttachmentType<ConfigurationToolItem.ConfigurationAction>> CONFIGURATION_ACTION =
            register("configuration_action", () -> ConfigurationToolItem.ConfigurationAction.LOCKING, builder -> builder.serialize(ConfigurationToolItem.ConfigurationAction.CODEC));

    public static final Supplier<AttachmentType<LinkingToolItem.ActionMode>> ACTION_MODE =
            register("action_mode", () -> LinkingToolItem.ActionMode.ADD, builder -> builder.serialize(LinkingToolItem.ActionMode.CODEC));

    public static final Supplier<AttachmentType<LinkingToolItem.LinkingMode>> LINKING_MODE =
            register("linking_mode", () -> LinkingToolItem.LinkingMode.SINGLE, builder -> builder.serialize(LinkingToolItem.LinkingMode.CODEC));

    public static final Supplier<AttachmentType<BlockPos>> FIRST_POSITION =
            register("first_pos", () -> BlockPos.ZERO, builder -> builder.serialize(BlockPos.CODEC));
    public static final Supplier<AttachmentType<BlockPos>> CONTROLLER =
            register("controller_position", () -> BlockPos.ZERO, builder -> builder.serialize(BlockPos.CODEC));

    public static final Supplier<AttachmentType<String>> ENDER_FREQUENCY = register("ender_frequency", () -> "", op -> op.serialize(Codec.STRING));
    public static final Supplier<AttachmentType<Unit>> ENDER_SAFETY = register("ender_safety", () -> Unit.INSTANCE, op -> op.serialize(Codec.unit(Unit.INSTANCE)));
    public static final Supplier<AttachmentType<Direction>> DIRECTION = register("direction", () -> Direction.NORTH, op -> op.serialize(Direction.CODEC));
    public static final Supplier<AttachmentType<Integer>> SLOT = register("slot", () -> 0, op -> op.serialize(Codec.intRange(0, UpgradeItem.MAX_SLOT - 1)));
    public static final Supplier<AttachmentType<Boolean>> LOCKED = register("locked", () -> false, op -> op.serialize(Codec.BOOL));

    public static final Supplier<AttachmentType<CompoundTag>> TILE = register("tile", CompoundTag::new, op -> op.serialize(CompoundTag.CODEC));
    public static final Supplier<AttachmentType<CompoundTag>> STYLE = register("style", CompoundTag::new, op -> op.serialize(CompoundTag.CODEC));

    private static <T> Supplier<AttachmentType<T>> register(String name, Supplier<T> defaultVal, UnaryOperator<AttachmentType.Builder<T>> op) {
        return DR.register(name, () -> op.apply(AttachmentType.builder(defaultVal)).build());
    }
}
