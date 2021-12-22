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
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
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
import java.util.List;
import java.util.Locale;

public class LinkingToolItem extends BasicItem {

    public static final String NBT_MODE = "Mode";
    public static final String NBT_CONTROLLER = "Controller";
    public static final String NBT_ACTION = "Action";

    public LinkingToolItem() {
        super(new Properties().tab(FunctionalStorage.TAB).stacksTo(1));
    }

    @Override
    public void onCraftedBy(ItemStack p_41447_, Level p_41448_, Player p_41449_) {
        super.onCraftedBy(p_41447_, p_41448_, p_41449_);
        initNbt(p_41447_);
    }

    private ItemStack initNbt(ItemStack stack){
        stack.getOrCreateTag().putString(NBT_MODE, LinkingMode.SINGLE.name());
        stack.getOrCreateTag().putString(NBT_ACTION, ActionMode.ADD.name());
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
        LinkingMode linkingMode = LinkingMode.valueOf(stack.getOrCreateTag().getString(NBT_MODE));
        ActionMode linkingAction = ActionMode.valueOf(stack.getOrCreateTag().getString(NBT_ACTION));
        if (blockEntity instanceof DrawerControllerTile){
            CompoundTag controller = new CompoundTag();
            controller.putInt("X", pos.getX());
            controller.putInt("Y", pos.getY());
            controller.putInt("Z", pos.getZ());
            stack.getOrCreateTag().put(NBT_CONTROLLER, controller);
            context.getPlayer().playSound(SoundEvents.ITEM_FRAME_ADD_ITEM, 0.5f, 1);
            context.getPlayer().displayClientMessage(new TextComponent("Controller configured to the tool").withStyle(ChatFormatting.GREEN), true);
            return InteractionResult.SUCCESS;
        } else if (blockEntity instanceof ControllableDrawerTile && stack.getOrCreateTag().contains(NBT_CONTROLLER)){
            CompoundTag controllerNBT = stack.getOrCreateTag().getCompound(NBT_CONTROLLER);
            BlockEntity controller = level.getBlockEntity(new BlockPos(controllerNBT.getInt("X"), controllerNBT.getInt("Y"), controllerNBT.getInt("Z")));
            if (controller instanceof DrawerControllerTile){
                if (linkingMode == LinkingMode.SINGLE){
                    ((ControllableDrawerTile<?>) blockEntity).setControllerPos(controller.getBlockPos());
                    ((DrawerControllerTile) controller).addConnectedDrawers(linkingAction, pos);
                    context.getPlayer().displayClientMessage(new TextComponent("Linked drawer to the controller").setStyle(Style.EMPTY.withColor(linkingMode.color)), true);
                }//TODO add to the drawer
                context.getPlayer().playSound(SoundEvents.ITEM_FRAME_ROTATE_ITEM, 0.5f, 1);
                return InteractionResult.SUCCESS;
            }
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty() && stack.hasTag()){
            if (player.isShiftKeyDown()){
                LinkingMode linkingMode = LinkingMode.valueOf(stack.getOrCreateTag().getString(NBT_MODE));
                if (linkingMode == LinkingMode.SINGLE){
                    stack.getOrCreateTag().putString(NBT_MODE, LinkingMode.MULTIPLE.name());
                    player.displayClientMessage(new TextComponent("Swapped mode to " + LinkingMode.MULTIPLE.name().toLowerCase(Locale.ROOT)).setStyle(Style.EMPTY.withColor(LinkingMode.MULTIPLE.getColor())), true);
                } else {
                    stack.getOrCreateTag().putString(NBT_MODE, LinkingMode.SINGLE.name());
                    player.displayClientMessage(new TextComponent("Swapped mode to " + LinkingMode.SINGLE.name().toLowerCase(Locale.ROOT)).setStyle(Style.EMPTY.withColor(LinkingMode.SINGLE.getColor())), true);
                }
            } else {
                ActionMode linkingMode = ActionMode.valueOf(stack.getOrCreateTag().getString(NBT_ACTION));
                if (linkingMode == ActionMode.ADD){
                    stack.getOrCreateTag().putString(NBT_ACTION, ActionMode.REMOVE.name());
                    player.displayClientMessage(new TextComponent("Swapped action to " + ActionMode.REMOVE.name().toLowerCase(Locale.ROOT)).setStyle(Style.EMPTY.withColor(ActionMode.REMOVE.getColor())), true);
                } else {
                    stack.getOrCreateTag().putString(NBT_ACTION, ActionMode.ADD.name());
                    player.displayClientMessage(new TextComponent("Swapped action to " + ActionMode.ADD.name().toLowerCase(Locale.ROOT)).setStyle(Style.EMPTY.withColor(ActionMode.ADD.getColor())), true);
                }
            }
            player.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 0.5f, 1);
            return InteractionResultHolder.success(stack);
        }
        return super.use(p_41432_, player, hand);
    }

    @Override
    public void addTooltipDetails(@Nullable BasicItem.Key key, ItemStack stack, List<Component> tooltip, boolean advanced) {
        super.addTooltipDetails(key, stack, tooltip, advanced);
        if (stack.hasTag()){
            LinkingMode linkingMode = LinkingMode.valueOf(stack.getOrCreateTag().getString(NBT_MODE));
            ActionMode linkingAction = ActionMode.valueOf(stack.getOrCreateTag().getString(NBT_ACTION));
            if (key == null){
                tooltip.add(new TranslatableComponent("linkingtool.linkingmode").withStyle(ChatFormatting.YELLOW)
                        .append(new TranslatableComponent("linkingtool.linkingmode." + linkingMode.name().toLowerCase(Locale.ROOT) ).withStyle(Style.EMPTY.withColor(linkingMode.getColor()))));
                tooltip.add(new TranslatableComponent("linkingtool.linkingaction").withStyle(ChatFormatting.YELLOW)
                        .append(new TranslatableComponent("linkingtool.linkingaction." + linkingAction.name().toLowerCase(Locale.ROOT)).withStyle(Style.EMPTY.withColor(linkingAction.getColor()))));
                if (stack.getOrCreateTag().contains(NBT_CONTROLLER)){
                    tooltip.add(new TranslatableComponent("linkingtool.controller").withStyle(ChatFormatting.YELLOW)
                            .append(new TextComponent(stack.getOrCreateTag().getCompound(NBT_CONTROLLER).getInt("X") +""+ ChatFormatting.WHITE + ", " + ChatFormatting.DARK_AQUA + stack.getOrCreateTag().getCompound(NBT_CONTROLLER).getInt("Y") +  ChatFormatting.WHITE + ", " + ChatFormatting.DARK_AQUA + stack.getOrCreateTag().getCompound(NBT_CONTROLLER).getInt("Z")).withStyle(ChatFormatting.DARK_AQUA)));
                } else {
                    tooltip.add(new TranslatableComponent("linkingtool.controller").withStyle(ChatFormatting.YELLOW).append(new TextComponent("???").withStyle(ChatFormatting.DARK_AQUA)));
                }
                tooltip.add(new TextComponent(""));
                tooltip.add(new TranslatableComponent("linkingtool.linkingmode." + linkingMode.name().toLowerCase(Locale.ROOT) + ".desc").withStyle(ChatFormatting.GRAY));
                tooltip.add(new TranslatableComponent("linkingtool.use").withStyle(ChatFormatting.GRAY));

            }
        }
    }

    @Override
    public boolean hasTooltipDetails(@Nullable BasicItem.Key key) {
        return key == null;
    }

    public enum LinkingMode{
        SINGLE(TextColor.fromRgb(Color.cyan.getRGB())),
        MULTIPLE(TextColor.fromRgb(Color.GREEN.getRGB()));

        private final TextColor color;

        LinkingMode(TextColor color) {
            this.color = color;
        }

        public TextColor getColor() {
            return color;
        }
    }

    public enum ActionMode{
        ADD(TextColor.fromRgb(new Color(40, 131, 250).getRGB())),
        REMOVE(TextColor.fromRgb(new Color(250, 145, 40).getRGB()));

        private final TextColor color;

        ActionMode(TextColor color) {
            this.color = color;
        }

        public TextColor getColor() {
            return color;
        }
    }
}
