package com.buuz135.functionalstorage.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.FluidDrawerTile;
import com.buuz135.functionalstorage.item.component.FunctionalUpgradeBehavior;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.item.BasicItem;
import com.hrznstudio.titanium.util.FacingUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class UpgradeItem extends FSItem {

    public static final int MAX_SLOT = 4;

    public static Direction getDirection(ItemStack stack){
        if (stack.has(FSAttachments.DIRECTION)) {
            Item item = stack.getItem();
            if (item.equals(FunctionalStorage.PULLING_UPGRADE.get()) || item.equals(FunctionalStorage.PUSHING_UPGRADE.get()) || item.equals(FunctionalStorage.COLLECTOR_UPGRADE.get())) {
                return stack.get(FSAttachments.DIRECTION);
            }
        }
        return Direction.NORTH;
    }

    private final Type type;

    public UpgradeItem(Properties properties, Type type) {
        super(properties);
        this.type = type;
    }

    public UpgradeItem(FunctionalUpgradeBehavior behavior) {
        this(new Properties().component(FSAttachments.FUNCTIONAL_BEHAVIOR, behavior), Type.UTILITY);
    }

    @Override
    public void onCraftedBy(ItemStack p_41447_, Level p_41448_, Player p_41449_) {
        super.onCraftedBy(p_41447_, p_41448_, p_41449_);
        initNbt(p_41447_);
    }

    private ItemStack initNbt(ItemStack stack){
        Item item = stack.getItem();
        if (item.equals(FunctionalStorage.PULLING_UPGRADE.get()) || item.equals(FunctionalStorage.PUSHING_UPGRADE.get()) || item.equals(FunctionalStorage.COLLECTOR_UPGRADE.get())){
            stack.set(FSAttachments.DIRECTION, Direction.NORTH);
        }
        if (item.equals(FunctionalStorage.REDSTONE_UPGRADE.get())){
            stack.set(FSAttachments.SLOT, 0);
        }
        return stack;
    }

    @Override
    public void verifyComponentsAfterLoad(ItemStack stack) {
        super.verifyComponentsAfterLoad(stack);
        Item item = stack.getItem();
        if (item.equals(FunctionalStorage.PULLING_UPGRADE.get()) || item.equals(FunctionalStorage.PUSHING_UPGRADE.get()) || item.equals(FunctionalStorage.COLLECTOR_UPGRADE.get())){
            stack.set(FSAttachments.DIRECTION, Direction.NORTH);
        }
        if (item.equals(FunctionalStorage.REDSTONE_UPGRADE.get())){
            stack.set(FSAttachments.SLOT, 0);
        }
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack first, ItemStack second, Slot p_150894_, ClickAction clickAction, Player p_150896_, SlotAccess p_150897_) {
        if (clickAction == ClickAction.SECONDARY && first.getCount() == 1){
            Item item = first.getItem();
            if (item.equals(FunctionalStorage.PULLING_UPGRADE.get()) || item.equals(FunctionalStorage.PUSHING_UPGRADE.get()) || item.equals(FunctionalStorage.COLLECTOR_UPGRADE.get())){
                Direction direction = getDirection(first);
                Direction next = Direction.values()[(Arrays.asList(Direction.values()).indexOf(direction) + 1 ) % Direction.values().length];
                first.set(FSAttachments.DIRECTION, next);
                p_150896_.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1);
                return true;
            }
            if (item.equals(FunctionalStorage.REDSTONE_UPGRADE.get())){
                int slot = first.getOrDefault(FSAttachments.SLOT, 0);
                first.set(FSAttachments.SLOT, (slot + 1) % MAX_SLOT);
                p_150896_.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1);
                return true;
            }
        }
        return super.overrideOtherStackedOnMe(first, second, p_150894_, clickAction, p_150896_, p_150897_);
    }

    @Override
    public void addTooltipDetails(@Nullable BasicItem.Key key, ItemStack stack, List<Component> tooltip, boolean advanced) {
        super.addTooltipDetails(key, stack, tooltip, advanced);
        tooltip.add(Component.translatable("upgrade.type").withStyle(ChatFormatting.YELLOW).append(Component.translatable("upgrade.type." + getType().name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.WHITE)));
        Item item = stack.getItem();
        if (isDirectionUpgrade(item) && stack.has(FSAttachments.DIRECTION)) {
            tooltip.add(Component.translatable("item.utility.direction").withStyle(ChatFormatting.YELLOW).append(Component.translatable(WordUtils.capitalize(getDirection(stack).getName().toLowerCase(Locale.ROOT))).withStyle(ChatFormatting.WHITE)));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("item.utility.direction.desc").withStyle(ChatFormatting.GRAY));
        }
        if (item.equals(FunctionalStorage.REDSTONE_UPGRADE.get()) && stack.has(FSAttachments.SLOT)) {
            tooltip.add(Component.translatable("item.utility.slot").withStyle(ChatFormatting.YELLOW).append(Component.literal(stack.get(FSAttachments.SLOT).toString()).withStyle(ChatFormatting.WHITE)));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("item.utility.direction.desc").withStyle(ChatFormatting.GRAY));
        }

    }

    public static boolean isDirectionUpgrade(Item item) {
        return (item.equals(FunctionalStorage.PULLING_UPGRADE.get()) || item.equals(FunctionalStorage.PUSHING_UPGRADE.get()) || item.equals(FunctionalStorage.COLLECTOR_UPGRADE.get()));
    }

    @Nullable
    public Component getDescription(ItemStack stack, ControllableDrawerTile<?> tile) {
        var dir = tile.getBlockState().getValue(RotatableBlock.FACING_HORIZONTAL);
        var type = tile instanceof FluidDrawerTile ? "fluid" : "item";
        if (this == FunctionalStorage.PUSHING_UPGRADE.get()) {
            return Component.translatable("drawer_upgrade.functionalstorage.push." + type, getRelativeDirection(
                    getDirection(stack), dir
            ).withStyle(ChatFormatting.GOLD));
        } else if (this == FunctionalStorage.PULLING_UPGRADE.get()) {
            return Component.translatable("drawer_upgrade.functionalstorage.pull." + type, getRelativeDirection(
                    getDirection(stack), dir
            ).withStyle(ChatFormatting.GOLD));
        } else if (this == FunctionalStorage.COLLECTOR_UPGRADE.get()) {
            return Component.translatable("drawer_upgrade.functionalstorage.collect." + type, getRelativeDirection(
                    getDirection(stack), dir
            ).withStyle(ChatFormatting.GOLD));
        } else if (this == FunctionalStorage.VOID_UPGRADE.get()) {
            return Component.translatable("drawer_upgrade.functionalstorage.void." + type);
        } else if (this == FunctionalStorage.REDSTONE_UPGRADE.get()) {
            return Component.translatable("drawer_upgrade.functionalstorage.redstone", Component.literal(
                    String.valueOf(stack.getOrDefault(FSAttachments.SLOT, 0))
            ).withStyle(ChatFormatting.RED));
        }
        return null;
    }

    public static MutableComponent getRelativeDirection(Direction upgrade, Direction facing) {
        return Component.translatable(
                "tooltip.titanium.facing_handler." + FacingUtil.getFacingRelative(upgrade, facing).name().toLowerCase()).withStyle(ChatFormatting.WHITE);
    }

    @Override
    public boolean hasTooltipDetails(@Nullable BasicItem.Key key) {
        return key == null;
    }

    public enum Type{
        STORAGE,
        UTILITY
    }
}
