package com.buuz135.functionalstorage.compat.jade;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.inventory.CompactingInventoryHandler;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.buuz135.functionalstorage.util.NumberUtils;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

import java.util.ArrayList;

public enum DrawerComponentProvider implements IBlockComponentProvider {
    INSTANCE;
    public static final ResourceLocation ITEM_STORAGE = new ResourceLocation("minecraft:item_storage");

    public static final ResourceLocation ID = new ResourceLocation(FunctionalStorage.MOD_ID, "drawer");

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        iTooltip.remove(ITEM_STORAGE);

        var helper = IElementHelper.get();
        if (blockAccessor.getBlockEntity() instanceof ItemControllableDrawerTile<?> tile) {
            if (!tile.isEverythingEmpty()) {
                var stacks = new ArrayList<Pair<ItemStack, Integer>>();
                if (tile.getStorage() instanceof BigInventoryHandler bigInv) {
                    for (int slot = 0; slot < tile.getStorage().getSlots(); slot++) {
                        var stack = bigInv.getStoredStacks().get(slot);
                        if (stack.getStack().getItem() != Items.AIR) {
                            stacks.add(new Pair<>(stack.getStack().copyWithCount(stack.getAmount()), bigInv.getSlotLimit(slot)));
                        }
                    }
                } else if (tile.getStorage() instanceof CompactingInventoryHandler compacting) {
                    var results = compacting.getResultList();
                    for (int i = 0; i < results.size(); i++) {
                        var result = results.get(i);
                        if (result.getResult().getItem() != Items.AIR) {
                            stacks.add(new Pair<>(result.getResult().copyWithCount(compacting.getStackInSlot(i).getCount()), compacting.getSlotLimit(i)));
                        }
                    }
                }

                if (!stacks.isEmpty()) {
                    var contentsBox = helper.tooltip();
                    iTooltip.add(helper.text(Component.literal("Contents:")));
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

                if (Screen.hasShiftDown()) {
                    var upInv = tile.getUtilityUpgrades();
                    for (int i = 0; i < upInv.getSlots(); i++) {
                        var stack = upInv.getStackInSlot(i);
                        if (stack.getItem() instanceof UpgradeItem ui) {
                            iTooltip.add(ui.getDescription(stack, tile));
                        }
                    }
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
