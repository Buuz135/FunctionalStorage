package com.buuz135.functionalstorage.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.hrznstudio.titanium.item.BasicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class UpgradeItem extends BasicItem {


    public static Direction getDirection(ItemStack stack){
        if (stack.hasTag()){
            Item item = stack.getItem();
            if (item.equals(FunctionalStorage.PULLING_UPGRADE.get()) || item.equals(FunctionalStorage.PUSHING_UPGRADE.get()) || item.equals(FunctionalStorage.COLLECTOR_UPGRADE.get())){
                return Direction.byName(stack.getOrCreateTag().getString("Direction"));
            }
        }
        return Direction.NORTH;
    }

    private final Type type;

    public UpgradeItem(Properties properties, Type type) {
        super(properties.tab(FunctionalStorage.TAB));
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
            stack.getOrCreateTag().putString("Direction", Direction.values()[0].name());
        }
        return stack;
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (allowdedIn(group)) {
            items.add(initNbt(new ItemStack(this)));
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
                first.getOrCreateTag().putString("Direction", next.name());
                p_150896_.playSound(SoundEvents.UI_BUTTON_CLICK, 0.5f, 1);
                return true;
            }
        }
        return super.overrideOtherStackedOnMe(first, second, p_150894_, clickAction, p_150896_, p_150897_);
    }

    @Override
    public void addTooltipDetails(@Nullable BasicItem.Key key, ItemStack stack, List<Component> tooltip, boolean advanced) {
        super.addTooltipDetails(key, stack, tooltip, advanced);
        tooltip.add(new TranslatableComponent("upgrade.type").withStyle(ChatFormatting.YELLOW).append(new TranslatableComponent("upgrade.type." + getType().name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.WHITE)));
        Item item = stack.getItem();
        if (item.equals(FunctionalStorage.PULLING_UPGRADE.get()) || item.equals(FunctionalStorage.PUSHING_UPGRADE.get()) || item.equals(FunctionalStorage.COLLECTOR_UPGRADE.get())){
            tooltip.add(new TranslatableComponent("item.utility.direction").withStyle(ChatFormatting.YELLOW).append(new TranslatableComponent(WordUtils.capitalize(getDirection(stack).name().toLowerCase(Locale.ROOT))).withStyle(ChatFormatting.WHITE)));
            tooltip.add(new TextComponent(""));
            tooltip.add(new TranslatableComponent("item.utility.direction.desc").withStyle(ChatFormatting.GRAY));
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
