package com.buuz135.functionalstorage.compat.jade;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.EnderDrawerBlock;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.EnderDrawerTile;
import com.buuz135.functionalstorage.block.tile.FluidDrawerTile;
import com.buuz135.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.inventory.CompactingInventoryHandler;
import com.buuz135.functionalstorage.inventory.EnderInventoryHandler;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.buuz135.functionalstorage.network.EnderDrawerSyncMessage;
import com.buuz135.functionalstorage.util.NumberUtils;
import com.buuz135.functionalstorage.world.EnderSavedData;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.fluids.FluidStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.ProgressStyle;
import snownee.jade.api.view.FluidView;

import java.util.ArrayList;

public enum DrawerComponentProvider implements IBlockComponentProvider {
    INSTANCE;
    public static final ResourceLocation ITEM_STORAGE = com.buuz135.functionalstorage.util.Utils.resourceLocation("minecraft:item_storage");

    public static final ResourceLocation ID = com.buuz135.functionalstorage.util.Utils.resourceLocation(FunctionalStorage.MOD_ID, "drawer");

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        iTooltip.remove(ITEM_STORAGE);
        iTooltip.remove(com.buuz135.functionalstorage.util.Utils.resourceLocation("minecraft:fluid_storage"));

        var helper = IElementHelper.get();
        if (blockAccessor.getBlockEntity() instanceof ControllableDrawerTile<?> controllable) {
            if (blockAccessor.getBlockEntity() instanceof ItemControllableDrawerTile<?> tile) {
                var stacks = new ArrayList<Pair<ItemStack, Integer>>();
                if (tile instanceof EnderDrawerTile ed && ed.getFrequency() != null) {
                    var inv = EnderSavedData.getInstance(Minecraft.getInstance().level).getFrequency(ed.getFrequency());
                    for (int slot = 0; slot < inv.getSlots(); slot++) {
                        var stack = inv.getStoredStacks().get(slot);
                        if (stack.getStack().getItem() != Items.AIR) {
                            stacks.add(new Pair<>(stack.getStack().copyWithCount(stack.getAmount()), inv.getSlotLimit(slot)));
                        }
                    }
                } else if (tile.getStorage() instanceof BigInventoryHandler bigInv) {
                    for (int slot = 0; slot < bigInv.getStoredStacks().size(); slot++) {
                        var stack = bigInv.getStoredStacks().get(slot);
                        if (stack.getStack().getItem() != Items.AIR) {
                            stacks.add(new Pair<>(stack.getStack().copyWithCount(bigInv.isCreative() ? Integer.MAX_VALUE : stack.getAmount()), bigInv.getSlotLimit(slot)));
                        }
                    }
                } else if (tile.getStorage() instanceof CompactingInventoryHandler compacting) {
                    var results = compacting.getResultList();
                    for (int i = 0; i < results.size(); i++) {
                        var result = results.get(i);
                        if (result.getResult().getItem() != Items.AIR) {
                            stacks.add(new Pair<>(result.getResult().copyWithCount(compacting.isCreative() ? Integer.MAX_VALUE :compacting.getStackInSlot(i).getCount()), compacting.getSlotLimit(i)));
                        }
                    }
                }

                if (!stacks.isEmpty()) {
                    var contentsBox = helper.tooltip();
                    iTooltip.add(helper.text(Component.translatable("drawer.block.contents")));
                    for (var stack : stacks) {
                        // Account for locked slots too
                        boolean wasEmpty = stack.getFirst().getCount() == 0;
                        if (stack.getFirst().getCount() == 0) stack.getFirst().setCount(1);
                        IElement icon = helper.item(stack.getFirst().copy(), 0.86f, "").size(new Vec2(.86f * 18, .86f * 18)).translate(new Vec2(0, -1));
                        if (wasEmpty) stack.getFirst().shrink(1);
                        contentsBox.add(icon);
                        contentsBox.append(
                                helper.text(Component.literal("x ").append(NumberUtils.getFormatedBigNumber(stack.getFirst().getCount()) + " / " + NumberUtils.getFormatedBigNumber(stack.getSecond())))
                                        .translate(new Vec2(4, (.86f * 18 - 10) / 2))
                        );
                    }
                    iTooltip.add(helper.box(contentsBox, BoxStyle.getNestedBox()));
                }
            } else if (blockAccessor.getBlockEntity() instanceof FluidDrawerTile tile) {
                if (!tile.isInventoryEmpty()) {
                    var stacks = new ArrayList<Pair<FluidStack, Integer>>();
                    for (int slot = 0; slot < tile.getFluidHandler().getTanks(); slot++) {
                        var stack = tile.getFluidHandler().getTankList()[slot];
                        if (stack.getFluid().getFluid() != Fluids.EMPTY) {
                            stacks.add(new Pair<>(stack.getFluid().copyWithAmount(stack.getFluidAmount()), tile.getFluidHandler().getTankCapacity(slot)));
                        }
                    }

                    if (!stacks.isEmpty()) {
                        var contentsBox = helper.tooltip();
                        iTooltip.add(helper.text(Component.translatable("drawer.block.contents")));
                        for (var stack : stacks) {
                            var view = new FluidView(helper.fluid(JadeFluidObject.of(stack.getFirst().getFluid())));
                            ProgressStyle progressStyle = helper.progressStyle().overlay(view.overlay);
                            contentsBox.add(helper.progress((float) stack.getFirst().getAmount() / stack.getSecond(), Component.empty().append(stack.getFirst().getHoverName()).append(Component.literal(" x ").append(NumberUtils.getFormatedFluidBigNumber(stack.getFirst().getAmount()) + " / " + NumberUtils.getFormatedFluidBigNumber(stack.getSecond()))), progressStyle, BoxStyle.getNestedBox(), true));
                        }
                        iTooltip.add(helper.box(contentsBox, BoxStyle.getNestedBox()));
                    }
                }
            }

            if (controllable instanceof EnderDrawerTile ender && ender.getFrequency() != null) {
                var freq = EnderDrawerBlock.getFrequencyDisplay(ender.getFrequency());
                var contentsBox = helper.tooltip();
                iTooltip.add(helper.text(Component.translatable("linkingtool.ender.frequency")));
                for (var stack : freq) {
                    contentsBox.append(helper.item(stack));
                }
                iTooltip.add(helper.box(contentsBox, BoxStyle.getNestedBox()));
            }

            if (Screen.hasShiftDown()) {
                var upInv = controllable.getUtilityUpgrades();
                for (int i = 0; i < upInv.getSlots(); i++) {
                    var stack = upInv.getStackInSlot(i);
                    if (stack.getItem() instanceof UpgradeItem ui) {
                        iTooltip.add(ui.getDescription(stack, controllable));
                    }
                }
                if (controllable.getStorageMultiplier() > 1) {
                    iTooltip.add(Component.translatable("drawer.block.multiplier",
                            Component.literal("x" + controllable.getStorageMultiplier()).withStyle(ChatFormatting.GOLD)));
                }
            }
        }
    }

    @Override
    public int getDefaultPriority() {
        return 4999;
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}
