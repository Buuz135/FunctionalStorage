package com.buuz135.functionalstorage.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.hrznstudio.titanium.item.BasicItem;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ConfigurationToolItem extends BasicItem {

    public static ConfigurationAction getAction(ItemStack stack) {
        return stack.getOrDefault(FSAttachments.CONFIGURATION_ACTION, ConfigurationAction.LOCKING);
    }

    public ConfigurationToolItem() {
        super(new Properties().stacksTo(1));
        setItemGroup(FunctionalStorage.TAB);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        ConfigurationAction configuractionAction = getAction(stack);
        if (blockEntity instanceof ControllableDrawerTile) {
            if (configuractionAction == ConfigurationAction.LOCKING) {
                ((ControllableDrawerTile<?>) blockEntity).toggleLocking();
            } else {
                ((ControllableDrawerTile<?>) blockEntity).toggleOption(configuractionAction);
                if (configuractionAction.getMax() > 1) {
                    context.getPlayer().displayClientMessage(
                            Component.translatable("configurationtool.configmode.indicator.mode_" + ((ControllableDrawerTile<?>) blockEntity).getDrawerOptions().getAdvancedValue(configuractionAction)).setStyle(Style.EMPTY.withColor(configuractionAction.getColor())), true);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty()) {
            if (player.isShiftKeyDown()) {
                ConfigurationAction action = getAction(stack);
                ConfigurationAction newAction = ConfigurationAction.values()[(Arrays.asList(ConfigurationAction.values()).indexOf(action) + 1) % ConfigurationAction.values().length];
                stack.set(FSAttachments.CONFIGURATION_ACTION, newAction);
                player.displayClientMessage(Component.literal("Swapped mode to ").setStyle(Style.EMPTY.withColor(newAction.getColor()))
                        .append(Component.translatable("configurationtool.configmode." + newAction.name().toLowerCase(Locale.ROOT))), true);
                player.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 0.5f, 1);
                return InteractionResultHolder.success(stack);
            }
        }
        return super.use(p_41432_, player, hand);
    }

    @Override
    public void addTooltipDetails(@Nullable BasicItem.Key key, ItemStack stack, List<Component> tooltip, boolean advanced) {
        super.addTooltipDetails(key, stack, tooltip, advanced);
        ConfigurationAction linkingMode = getAction(stack);
        if (key == null) {
            tooltip.add(Component.translatable("configurationtool.configmode").withStyle(ChatFormatting.YELLOW)
                    .append(Component.translatable("configurationtool.configmode." + linkingMode.name().toLowerCase(Locale.ROOT)).withStyle(Style.EMPTY.withColor(linkingMode.getColor()))));
            tooltip.add(Component.literal("").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("configurationtool.use").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean hasTooltipDetails(@Nullable BasicItem.Key key) {
        return key == null;
    }

    public enum ConfigurationAction implements StringRepresentable {
        LOCKING(TextColor.fromRgb(new Color(40, 131, 250).getRGB()), 1),
        TOGGLE_NUMBERS(TextColor.fromRgb(new Color(250, 145, 40).getRGB()), 1),
        TOGGLE_RENDER(TextColor.fromRgb(new Color(100, 250, 40).getRGB()), 1),
        TOGGLE_UPGRADES(TextColor.fromRgb(new Color(166, 40, 250).getRGB()), 1),
        INDICATOR(TextColor.fromRgb(new Color(255, 40, 40).getRGB()), 3); //0 NO , 1 - PROGRESS, 2 - ONLY FULL, 3 - ONLY FULL WITHOUT BG


        public static final Codec<ConfigurationAction> CODEC = StringRepresentable.fromValues(ConfigurationAction::values);
        private final TextColor color;
        private final int max;

        ConfigurationAction(TextColor color, int max) {
            this.color = color;
            this.max = max;
        }

        public TextColor getColor() {
            return color;
        }

        public int getMax() {
            return max;
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
