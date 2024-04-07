package com.buuz135.functionalstorage.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.hrznstudio.titanium.item.BasicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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

public class UpgradeItem extends BasicItem {

    public static final int MAX_SLOT = 4;

    public static Direction getDirection(ItemStack stack){
        if (stack.hasData(FSAttachments.DIRECTION)) {
            Item item = stack.getItem();
            if (item.equals(FunctionalStorage.PULLING_UPGRADE.get()) || item.equals(FunctionalStorage.PUSHING_UPGRADE.get()) || item.equals(FunctionalStorage.COLLECTOR_UPGRADE.get())) {
                return stack.getData(FSAttachments.DIRECTION);
            }
        }
        return Direction.NORTH;
    }

    private final Type type;

    public UpgradeItem(Properties properties, Type type) {
        super(properties);
        setItemGroup(FunctionalStorage.TAB);
        this.type = type;
    }

    @Override
    public void onCraftedBy(ItemStack p_41447_, Level p_41448_, Player p_41449_) {
        super.onCraftedBy(p_41447_, p_41448_, p_41449_);
        initNbt(p_41447_);
    }

    private ItemStack initNbt(ItemStack stack){
        Item item = stack.getItem();
        if (item.equals(FunctionalStorage.PULLING_UPGRADE.get()) || item.equals(FunctionalStorage.PUSHING_UPGRADE.get()) || item.equals(FunctionalStorage.COLLECTOR_UPGRADE.get())){
            stack.setData(FSAttachments.DIRECTION, Direction.NORTH);
        }
        if (item.equals(FunctionalStorage.REDSTONE_UPGRADE.get())){
            stack.setData(FSAttachments.SLOT, 0);
        }
        return stack;
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
                first.setData(FSAttachments.DIRECTION, next);
                p_150896_.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1);
                return true;
            }
            if (item.equals(FunctionalStorage.REDSTONE_UPGRADE.get())){
                int slot = first.getData(FSAttachments.SLOT);
                first.setData(FSAttachments.SLOT, (slot + 1) % MAX_SLOT);
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
        if (item.equals(FunctionalStorage.PULLING_UPGRADE.get()) || item.equals(FunctionalStorage.PUSHING_UPGRADE.get()) || item.equals(FunctionalStorage.COLLECTOR_UPGRADE.get())) {
            tooltip.add(Component.translatable("item.utility.direction").withStyle(ChatFormatting.YELLOW).append(Component.translatable(WordUtils.capitalize(getDirection(stack).getName().toLowerCase(Locale.ROOT))).withStyle(ChatFormatting.WHITE)));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("item.utility.direction.desc").withStyle(ChatFormatting.GRAY));
        }
        if (item.equals(FunctionalStorage.REDSTONE_UPGRADE.get())) {
            tooltip.add(Component.translatable("item.utility.slot").withStyle(ChatFormatting.YELLOW).append(Component.literal(stack.getData(FSAttachments.SLOT).toString()).withStyle(ChatFormatting.WHITE)));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("item.utility.direction.desc").withStyle(ChatFormatting.GRAY));
        }

    }

    @Override
    public boolean hasTooltipDetails(@Nullable BasicItem.Key key) {
        return key == null;
    }

    public static enum Type{
        STORAGE,
        UTILITY
    }
}
