package com.buuz135.functionalstorage.compat.jade;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.EnderDrawerBlock;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.EnderDrawerTile;
import com.buuz135.functionalstorage.block.tile.FluidDrawerTile;
import com.buuz135.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.inventory.CompactingInventoryHandler;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.buuz135.functionalstorage.util.NumberUtils;
import com.buuz135.functionalstorage.world.EnderSavedData;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;

import java.util.ArrayList;

public enum DrawerComponentProvider implements IBlockComponentProvider {
    INSTANCE;
    public static final Identifier ITEM_STORAGE = com.buuz135.functionalstorage.util.Utils.resourceLocation("minecraft:item_storage");

    public static final Identifier ID = com.buuz135.functionalstorage.util.Utils.resourceLocation(FunctionalStorage.MOD_ID, "drawer");

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        iTooltip.remove(ITEM_STORAGE);
        iTooltip.remove(com.buuz135.functionalstorage.util.Utils.resourceLocation("minecraft:fluid_storage"));

        if (blockAccessor.getBlockEntity() instanceof ControllableDrawerTile<?> controllable) {
            if (blockAccessor.getBlockEntity() instanceof ItemControllableDrawerTile<?> tile) {
                var stacks = new ArrayList<Pair<ItemStack, Integer>>();
                if (tile instanceof EnderDrawerTile ed && ed.getFrequency() != null) {
                    var inv = EnderSavedData.getInstance(Minecraft.getInstance().level).getFrequency(ed.getFrequency());
                    for (int slot = 0; slot < inv.size(); slot++) {
                        if (inv.isVoid() && slot + 1 == inv.size()) continue;
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
                    var contentsBox = JadeUI.tooltip();
                    for (var stack : stacks) {
                        boolean wasEmpty = stack.getFirst().getCount() == 0;
                        if (stack.getFirst().getCount() == 0) stack.getFirst().setCount(1);
                        Element icon = JadeUI.item(stack.getFirst().copy(), 0.75f, "").size((int) (0.75f * 18), (int) (0.75f * 18)).offset(0, -1);
                        if (wasEmpty) stack.getFirst().shrink(1);
                        contentsBox.add(icon);
                        contentsBox.append(
                                JadeUI.text(Component.literal("x ").append(NumberUtils.getFormatedBigNumber(stack.getFirst().getCount()) + " / " + NumberUtils.getFormatedBigNumber(stack.getSecond())))
                                        .offset(4, 1 +(int) ((.75f * 18 - 10) / 2))
                        );
                    }
                    iTooltip.add(JadeUI.box(contentsBox, BoxStyle.transparent()));
                }
            } else if (blockAccessor.getBlockEntity() instanceof FluidDrawerTile tile) {
                if (!tile.isInventoryEmpty()) {
                    var stacks = new ArrayList<Pair<FluidStack, Integer>>();
                    for (int slot = 0; slot < tile.getFluidHandler().getTanks(); slot++) {
                        var stack = tile.getFluidHandler().getFluidInTank(slot);
                        if (stack.getFluid() != Fluids.EMPTY) {
                            stacks.add(new Pair<>(stack.copy(), tile.getFluidHandler().getTankCapacity(slot)));
                        }
                    }

                    if (!stacks.isEmpty()) {
                        var contentsBox = JadeUI.tooltip();
                        for (var stack : stacks) {
                            contentsBox.add(JadeUI.fluid(JadeFluidObject.of(stack.getFirst().getFluid(), stack.getFirst().getAmount(), stack.getFirst().getComponentsPatch())).size(14, 14));
                            contentsBox.append(JadeUI.text(Component.empty().append(stack.getFirst().getHoverName()).append(Component.literal(" x ").append(NumberUtils.getFormatedFluidBigNumber(stack.getFirst().getAmount()) + " / " + NumberUtils.getFormatedFluidBigNumber(stack.getSecond())))).offset(4, 4));
                        }
                        iTooltip.add(JadeUI.box(contentsBox, BoxStyle.transparent()));
                    }
                }
            }

            if (controllable instanceof EnderDrawerTile ender && ender.getFrequency() != null) {
                var freq = EnderDrawerBlock.getFrequencyDisplay(ender.getFrequency());
                var contentsBox = JadeUI.tooltip();
                iTooltip.add(JadeUI.text(Component.translatable("linkingtool.ender.frequency")));
                for (var stack : freq) {
                    contentsBox.append(JadeUI.item(stack));
                }
                iTooltip.add(JadeUI.box(contentsBox, BoxStyle.nestedBox()));
            }

            if (JadeUI.hasShiftDown()) {
                var upInv = controllable.getUtilityUpgrades();
                for (int i = 0; i < upInv.getSlots(); i++) {
                    var stack = upInv.getStackInSlot(i);
                    if (stack.getItem() instanceof UpgradeItem ui) {
                        iTooltip.add(ui.getDescription(stack, controllable));
                    }
                }

                float mult = 1f;

                for (int i = 0; i < controllable.getStorageUpgrades().getSlots(); i++) {
                    var stack = controllable.getStorageUpgrades().getStackInSlot(i);
                    var prov = stack.get(controllable.sizeUpgradeComponent);
                    if (prov != null) {
                        mult = prov.applyFactorModifier(mult);
                    }
                }

                if (mult > 1) {
                    iTooltip.add(Component.translatable("drawer.block.multiplier",
                            Component.literal("x" + (int) mult).withStyle(ChatFormatting.GOLD)));
                }
            }
        }
    }

    @Override
    public int getDefaultPriority() {
        return 4999;
    }

    @Override
    public Identifier getUid() {
        return ID;
    }
}
