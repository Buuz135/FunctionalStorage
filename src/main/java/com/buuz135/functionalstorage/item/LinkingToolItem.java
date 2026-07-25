package com.buuz135.functionalstorage.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.EnderDrawerTile;
import com.buuz135.functionalstorage.block.tile.StorageControllerTile;
import com.buuz135.functionalstorage.inventory.EnderInventoryHandler;
import com.buuz135.functionalstorage.world.EnderSavedData;
import com.hrznstudio.titanium.event.handler.EventManager;
import com.hrznstudio.titanium.item.BasicItem;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LinkingToolItem extends FSItem {

    public static LinkingMode getLinkingMode(ItemStack stack) {
        return stack.getOrDefault(FSAttachments.LINKING_MODE, LinkingMode.SINGLE);
    }

    public static ActionMode getActionMode(ItemStack stack) {
        return stack.getOrDefault(FSAttachments.ACTION_MODE, ActionMode.ADD);
    }

    static {
        EventManager.forge(PlayerInteractEvent.LeftClickBlock.class).filter(leftClickBlock -> leftClickBlock.getSide() == LogicalSide.SERVER && leftClickBlock.getItemStack().is(FunctionalStorage.LINKING_TOOL.get())).process(leftClickBlock -> {
            ItemStack stack = leftClickBlock.getItemStack();
            BlockEntity blockEntity = leftClickBlock.getLevel().getBlockEntity(leftClickBlock.getPos());
            if (blockEntity instanceof EnderDrawerTile tile) {
                stack.set(FSAttachments.ENDER_FREQUENCY, tile.getFrequency());
                leftClickBlock.getEntity().displayClientMessage(Component.translatable("linkingtool.ender.stored").setStyle(Style.EMPTY.withColor(LinkingMode.SINGLE.color)), true);
                leftClickBlock.setCanceled(true);
            }
        }).subscribe();
    }

    public LinkingToolItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(FSAttachments.ENDER_FREQUENCY);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof EnderDrawerTile tile) {
            stack.set(FSAttachments.ENDER_FREQUENCY, tile.getFrequency());
            player.displayClientMessage(Component.translatable("linkingtool.ender.stored").setStyle(Style.EMPTY.withColor(LinkingMode.SINGLE.color)), true);
            return false;
        }
        return super.canAttackBlock(state,level, pos, player);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        LinkingMode linkingMode = getLinkingMode(stack);
        ActionMode linkingAction = getActionMode(stack);
        if (blockEntity instanceof EnderDrawerTile){
            if (stack.has(FSAttachments.ENDER_FREQUENCY)){
                String frequency = stack.get(FSAttachments.ENDER_FREQUENCY);
                EnderInventoryHandler inventory = EnderSavedData.getInstance(context.getLevel()).getFrequency(((EnderDrawerTile) blockEntity).getFrequency());
                if (inventory.getStackInSlot(0).isEmpty() || (context.getPlayer().isShiftKeyDown() && stack.has(FSAttachments.ENDER_SAFETY))) {
                    ((EnderDrawerTile) blockEntity).setFrequency(frequency);
                    context.getPlayer().displayClientMessage(Component.translatable("linkingtool.ender.changed").setStyle(Style.EMPTY.withColor(linkingMode.color)), true);
                    stack.remove(FSAttachments.ENDER_SAFETY);
                } else {
                    context.getPlayer().displayClientMessage(Component.translatable("linkingtool.ender.warning").withStyle(ChatFormatting.RED), true);
                    stack.set(FSAttachments.ENDER_SAFETY, Unit.INSTANCE);
                }
                return InteractionResult.SUCCESS;
            }
        }
        if (blockEntity instanceof StorageControllerTile) {
            stack.set(FSAttachments.CONTROLLER, pos);
            context.getPlayer().playSound(SoundEvents.ITEM_FRAME_ADD_ITEM, 0.5f, 1);
            context.getPlayer().displayClientMessage(Component.translatable("linkingtool.controller.configured").withStyle(ChatFormatting.GREEN), true);
            stack.remove(FSAttachments.ENDER_FREQUENCY);
            return InteractionResult.SUCCESS;
        } else if (blockEntity instanceof ControllableDrawerTile && stack.has(FSAttachments.CONTROLLER)) {
            BlockEntity controller = level.getBlockEntity(stack.get(FSAttachments.CONTROLLER));
            if (controller instanceof StorageControllerTile) {
                if (linkingMode == LinkingMode.SINGLE) {
                    if (((StorageControllerTile) controller).addConnectedDrawers(linkingAction, pos)){
                        if (linkingAction == ActionMode.ADD){
                            context.getPlayer().displayClientMessage(Component.translatable("linkingtool.single_drawer.linked").setStyle(Style.EMPTY.withColor(linkingMode.color)), true);
                        }else {
                            context.getPlayer().displayClientMessage(Component.translatable("linkingtool.single_drawer.removed").setStyle(Style.EMPTY.withColor(linkingMode.color)), true);
                        }
                    }
                } else {
                    if (stack.has(FSAttachments.FIRST_POSITION)) {
                        BlockPos firstPos = stack.get(FSAttachments.FIRST_POSITION);
                        AABB aabb = new AABB(Math.min(firstPos.getX(), pos.getX()), Math.min(firstPos.getY(), pos.getY()), Math.min(firstPos.getZ(), pos.getZ()), Math.max(firstPos.getX(), pos.getX()) + 1, Math.max(firstPos.getY(), pos.getY()) + 1, Math.max(firstPos.getZ(), pos.getZ()) + 1);
                        if (((StorageControllerTile) controller).addConnectedDrawers(linkingAction, getBlockPosInAABB(aabb).toArray(BlockPos[]::new))){
                            if (linkingAction == ActionMode.ADD){
                                context.getPlayer().displayClientMessage(Component.translatable("linkingtool.multiple_drawer.linked").setStyle(Style.EMPTY.withColor(linkingMode.color)), true);
                            }else {
                                context.getPlayer().displayClientMessage(Component.translatable("linkingtool.multiple_drawer.removed").setStyle(Style.EMPTY.withColor(linkingMode.color)), true);
                            }
                        }
                        stack.remove(FSAttachments.FIRST_POSITION);
                    } else {
                        stack.set(FSAttachments.FIRST_POSITION, pos);
                    }
                }
                context.getPlayer().playSound(SoundEvents.ITEM_FRAME_ROTATE_ITEM, 0.5f, 1);
                return InteractionResult.SUCCESS;
            }
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty()) {
            if (stack.has(FSAttachments.ENDER_FREQUENCY)) {
                if (player.isShiftKeyDown()) {
                    stack.remove(FSAttachments.ENDER_FREQUENCY);
                    player.displayClientMessage(Component.translatable("linkingtool.drawer.clear").setStyle(Style.EMPTY.withColor(ActionMode.ADD.getColor())), true);
                }
            } else {
                if (player.isShiftKeyDown()) {
                    LinkingMode linkingMode = getLinkingMode(stack);
                    LinkingMode newMode = linkingMode == LinkingMode.SINGLE ? LinkingMode.MULTIPLE : LinkingMode.SINGLE;
                    stack.set(FSAttachments.LINKING_MODE, newMode);
                    player.displayClientMessage(Component.translatable("linkingtool.linkingmode.swapped",Component.translatable("linkingtool.linkingmode." + newMode.name().toLowerCase(Locale.ROOT))).setStyle(Style.EMPTY.withColor(LinkingMode.MULTIPLE.getColor())), true);
                    stack.remove(FSAttachments.FIRST_POSITION);
                } else {
                    ActionMode linkingMode = getActionMode(stack);
                    ActionMode newMode = linkingMode == ActionMode.ADD ? ActionMode.REMOVE : ActionMode.ADD;
                    stack.set(FSAttachments.ACTION_MODE, newMode);
                    player.displayClientMessage(Component.translatable("linkingtool.linkingaction.swapped",Component.translatable("linkingtool.linkingaction." + newMode.name().toLowerCase(Locale.ROOT))).setStyle(Style.EMPTY.withColor(ActionMode.REMOVE.getColor())), true);
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
        LinkingMode linkingMode = getLinkingMode(stack);
        ActionMode linkingAction = getActionMode(stack);
        if (key == null) {
            if (stack.has(FSAttachments.ENDER_FREQUENCY)) {
                MutableComponent text = Component.translatable("linkingtool.ender.frequency");
                //frequencyDisplay.forEach(item -> text.append(item.getName(new ItemStack(item))));
                tooltip.add(text.withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal(""));
                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("linkingtool.ender.clear").withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(Component.translatable("linkingtool.linkingmode").withStyle(ChatFormatting.YELLOW)
                        .append(Component.translatable("linkingtool.linkingmode." + linkingMode.name().toLowerCase(Locale.ROOT)).withStyle(Style.EMPTY.withColor(linkingMode.getColor()))));
                tooltip.add(Component.translatable("linkingtool.linkingaction").withStyle(ChatFormatting.YELLOW)
                        .append(Component.translatable("linkingtool.linkingaction." + linkingAction.name().toLowerCase(Locale.ROOT)).withStyle(Style.EMPTY.withColor(linkingAction.getColor()))));
                if (stack.has(FSAttachments.CONTROLLER)) {
                    var pos = stack.get(FSAttachments.CONTROLLER);
                    tooltip.add(Component.translatable("linkingtool.controller").withStyle(ChatFormatting.YELLOW)
                            .append(Component.literal(pos.getX() + "" + ChatFormatting.WHITE + ", " + ChatFormatting.DARK_AQUA + pos.getY() + ChatFormatting.WHITE + ", " + ChatFormatting.DARK_AQUA + pos.getZ()).withStyle(ChatFormatting.DARK_AQUA)));
                } else {
                    tooltip.add(Component.translatable("linkingtool.controller").withStyle(ChatFormatting.YELLOW).append(Component.literal("???").withStyle(ChatFormatting.DARK_AQUA)));
                }
                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("linkingtool.linkingmode." + linkingMode.name().toLowerCase(Locale.ROOT) + ".desc").withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("linkingtool.use").withStyle(ChatFormatting.GRAY));
            }
        }
    }

    public static List<BlockPos> getBlockPosInAABB(AABB axisAlignedBB) {
        List<BlockPos> blocks = new ArrayList<>();
        for (double y = axisAlignedBB.minY; y < axisAlignedBB.maxY; ++y) {
            for (double x = axisAlignedBB.minX; x < axisAlignedBB.maxX; ++x) {
                for (double z = axisAlignedBB.minZ; z < axisAlignedBB.maxZ; ++z) {
                    blocks.add(new BlockPos((int) x, (int) y, (int) z));
                }
            }
        }
        return blocks;
    }

    @Override
    public boolean hasTooltipDetails(@Nullable BasicItem.Key key) {
        return key == null;
    }

    public enum LinkingMode implements StringRepresentable {
        SINGLE(TextColor.fromRgb(Color.cyan.getRGB())),
        MULTIPLE(TextColor.fromRgb(Color.GREEN.getRGB()));

        public static final Codec<LinkingMode> CODEC = StringRepresentable.fromValues(LinkingMode::values);

        private final TextColor color;

        LinkingMode(TextColor color) {
            this.color = color;
        }

        public TextColor getColor() {
            return color;
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public enum ActionMode implements StringRepresentable {
        ADD(TextColor.fromRgb(new Color(40, 131, 250).getRGB())),
        REMOVE(TextColor.fromRgb(new Color(250, 145, 40).getRGB()));

        public static final Codec<ActionMode> CODEC = StringRepresentable.fromValues(ActionMode::values);

        private final TextColor color;

        ActionMode(TextColor color) {
            this.color = color;
        }

        public TextColor getColor() {
            return color;
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
