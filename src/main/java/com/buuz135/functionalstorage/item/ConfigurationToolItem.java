package com.buuz135.functionalstorage.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.DrawerControllerTile;
import com.hrznstudio.titanium.item.BasicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ConfigurationToolItem extends BasicItem {

    public static final String NBT_MODE = "Mode";

    public ConfigurationToolItem() {
        super(new Properties().tab(FunctionalStorage.TAB).stacksTo(1));
    }

    @Override
    public void onCraftedBy(ItemStack p_41447_, Level p_41448_, Player p_41449_) {
        super.onCraftedBy(p_41447_, p_41448_, p_41449_);
        initNbt(p_41447_);
    }

    private ItemStack initNbt(ItemStack stack){
        stack.getOrCreateTag().putString(NBT_MODE, ConfigurationAction.LOCKING.name());
        return stack;
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (allowdedIn(group)) {
            items.add(initNbt(new ItemStack(this)));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        ConfigurationAction configuractionAction = ConfigurationAction.valueOf(stack.getOrCreateTag().getString(NBT_MODE));
        if (blockEntity instanceof ControllableDrawerTile){
            if (configuractionAction == ConfigurationAction.LOCKING){
                ((ControllableDrawerTile<?>) blockEntity).toggleLocking();
            }else{
                ((ControllableDrawerTile<?>) blockEntity).toggleOption(configuractionAction);
            }
           return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty() && stack.hasTag()){
            if (player.isShiftKeyDown()){
                ConfigurationAction action = ConfigurationAction.valueOf(stack.getOrCreateTag().getString(NBT_MODE));
                ConfigurationAction newAction = ConfigurationAction.values()[(Arrays.asList(ConfigurationAction.values()).indexOf(action) + 1 ) % ConfigurationAction.values().length];
                stack.getOrCreateTag().putString(NBT_MODE, newAction.name());
                player.displayClientMessage(new TextComponent("Swapped mode to ").setStyle(Style.EMPTY.withColor(newAction.getColor()))
                        .append(new TranslatableComponent("configurationtool.configmode." + newAction.name().toLowerCase(Locale.ROOT))), true);
                player.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 0.5f, 1);
                return InteractionResultHolder.success(stack);
            }
        }
        return super.use(p_41432_, player, hand);
    }

    @Override
    public void addTooltipDetails(@Nullable BasicItem.Key key, ItemStack stack, List<Component> tooltip, boolean advanced) {
        super.addTooltipDetails(key, stack, tooltip, advanced);
        if (stack.hasTag()){
            ConfigurationAction linkingMode = ConfigurationAction.valueOf(stack.getOrCreateTag().getString(NBT_MODE));
            if (key == null){
                tooltip.add(new TranslatableComponent("configurationtool.configmode").withStyle(ChatFormatting.YELLOW)
                        .append(new TranslatableComponent("configurationtool.configmode." + linkingMode.name().toLowerCase(Locale.ROOT) ).withStyle(Style.EMPTY.withColor(linkingMode.getColor()))));

            }
        }
    }

    @Override
    public boolean hasTooltipDetails(@Nullable BasicItem.Key key) {
        return key == null;
    }

    public enum ConfigurationAction{
        LOCKING(TextColor.fromRgb(new Color(40, 131, 250).getRGB())),
        TOGGLE_NUMBERS(TextColor.fromRgb(new Color(250, 145, 40).getRGB())),
        TOGGLE_RENDER(TextColor.fromRgb(new Color(100, 250, 40).getRGB())),
        TOGGLE_UPGRADES(TextColor.fromRgb(new Color(166, 40, 250).getRGB()));

        private final TextColor color;

        ConfigurationAction(TextColor color) {
            this.color = color;
        }

        public TextColor getColor() {
            return color;
        }
    }
}
