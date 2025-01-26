package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.client.gui.FluidDrawerInfoGuiAddon;
import com.buuz135.functionalstorage.fluid.BigFluidHandler;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import com.buuz135.functionalstorage.item.component.SizeProvider;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.component.inventory.InventoryComponent;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

public class FluidDrawerTile extends ControllableDrawerTile<FluidDrawerTile> {
    @Save
    public BigFluidHandler fluidHandler;
    private final FunctionalStorage.DrawerType type;

    public FluidDrawerTile(BasicTileBlock<FluidDrawerTile> base, BlockEntityType<FluidDrawerTile> blockEntityType, BlockPos pos, BlockState state, FunctionalStorage.DrawerType type) {
        super(base, blockEntityType, pos, state, new DrawerProperties(type.getSlotAmount(), FSAttachments.FLUID_STORAGE_MODIFIER));
        this.type = type;
        this.fluidHandler = new BigFluidHandler(type.getSlots(), getTankCapacity(getStorageMultiplier())) {
            @Override
            public void onChange() {
                syncObject(fluidHandler);
            }

            @Override
            public boolean isDrawerLocked() {
                return isLocked();
            }

            @Override
            public boolean isDrawerVoid() {
                return isVoid();
            }

            @Override
            public boolean isDrawerCreative() {
                return isCreative();
            }
        };
    }

    private int getTankCapacity(int storageMultiplier) {
        return (int) Math.min(Integer.MAX_VALUE, storageMultiplier * 1000L);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void initClient() {
        super.initClient();
        var slotName = "";
        if (type.getSlots() == 2) {
            slotName = "_2";
        }
        if (type.getSlots() == 4) {
            slotName = "_4";
        }
        String finalSlotName = slotName;
        addGuiAddonFactory(() -> new FluidDrawerInfoGuiAddon(64, 16,
                com.buuz135.functionalstorage.util.Utils.resourceLocation(FunctionalStorage.MOD_ID, "textures/block/fluid_front" + finalSlotName + ".png"),
                type.getSlots(),
                type.getSlotPosition(),
                this::getFluidHandler,
                integer -> getFluidHandler().getTankCapacity(integer)
        ));
    }

    @Override
    public InteractionResult onSlotActivated(Player playerIn, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ, int slot) {
        ItemStack stack = playerIn.getItemInHand(hand);
        if (stack.getItem().equals(FunctionalStorage.CONFIGURATION_TOOL.get()) || stack.getItem().equals(FunctionalStorage.LINKING_TOOL.get()))
            return InteractionResult.PASS;
        if (slot != -1 && !playerIn.getItemInHand(hand).isEmpty()) {
            var interactionResult = Optional.ofNullable(stack.getCapability(Capabilities.FluidHandler.ITEM)).map(iFluidHandlerItem -> Optional.ofNullable(playerIn.getCapability(Capabilities.ItemHandler.ENTITY)).map(iItemHandler -> {
                var result = FluidUtil.tryEmptyContainerAndStow(stack, this.fluidHandler.getTankList()[slot], iItemHandler, Integer.MAX_VALUE, playerIn, true);
                if (result.isSuccess()) {
                    playerIn.setItemInHand(hand, result.getResult().copy());
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.PASS;
            }).orElse(InteractionResult.PASS)).orElse(InteractionResult.PASS);
            if (interactionResult == InteractionResult.SUCCESS) {
                return interactionResult;
            }
        }
        return super.onSlotActivated(playerIn, hand, facing, hitX, hitY, hitZ, slot);
    }

    @Override
    public void onClicked(Player playerIn, int slot) {
        ItemStack stack = playerIn.getItemInHand(InteractionHand.MAIN_HAND);
        if (slot != -1 && !stack.isEmpty()) {
            Optional.ofNullable(stack.getCapability(Capabilities.FluidHandler.ITEM)).ifPresent(iFluidHandlerItem -> {
                Optional.ofNullable(playerIn.getCapability(Capabilities.ItemHandler.ENTITY)).ifPresent(iItemHandler -> {
                    var result = FluidUtil.tryFillContainerAndStow(stack, this.fluidHandler.getTankList()[slot], iItemHandler, Integer.MAX_VALUE, playerIn, true);
                    if (result.isSuccess()) {
                        playerIn.setItemInHand(InteractionHand.MAIN_HAND, result.getResult());
                    }
                });
            });
        }
    }

    @NotNull
    @Override
    public FluidDrawerTile getSelf() {
        return this;
    }

    public FunctionalStorage.DrawerType getDrawerType() {
        return type;
    }

    @Override
    public int getStorageSlotAmount() {
        return 4;
    }

    public BigFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    @Override
    public void setLocked(boolean locked) {
        super.setLocked(locked);
        this.fluidHandler.lockHandler();
        syncObject(this.fluidHandler);
    }

    public boolean isEverythingEmpty() {
        for (int i = 0; i < getFluidHandler().getTanks(); i++) {
            if (!getFluidHandler().getFluidInTank(i).isEmpty()) {
                return false;
            }
        }
        if (isLocked()) return false;
        for (int i = 0; i < getStorageUpgrades().getSlots(); i++) {
            if (!getStorageUpgrades().getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        for (int i = 0; i < getUtilityUpgrades().getSlots(); i++) {
            if (!getUtilityUpgrades().getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean isInventoryEmpty() {
        for (int i = 0; i < getFluidHandler().getTanks(); i++) {
            if (!getFluidHandler().getFluidInTank(i).isEmpty()) {
                return false;
            }
        }
        return false;
    }

    @Override
    public InventoryComponent<ControllableDrawerTile<FluidDrawerTile>> getStorageUpgradesConstructor() {
        return new InventoryComponent<ControllableDrawerTile<FluidDrawerTile>>("storage_upgrades", 10, 70, getStorageSlotAmount()) {
            @NotNull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (isStorageUpgradeLocked()) return ItemStack.EMPTY;
                ItemStack stack = this.getStackInSlot(slot);
                if (stack.has(FSAttachments.FLUID_STORAGE_MODIFIER)) {
                    var replacement = new ItemStack[this.getSlots()];
                    replacement[slot] = stack;

                    var newSize = SizeProvider.calculate(this, FSAttachments.FLUID_STORAGE_MODIFIER, baseSize, replacement);
                    for (int i = 0; i < getFluidHandler().getTanks(); i++) {
                        var stored = getFluidHandler().getFluidInTank(i);
                        if (stored.getAmount() > Math.min(Integer.MAX_VALUE, getTankCapacity(newSize))) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
                return super.extractItem(slot, amount, simulate);
            }
        }
                .setInputFilter((stack, integer) -> {
                    if (isStorageUpgradeLocked()) return false;
                    if (stack.getItem().equals(FunctionalStorage.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.IRON).get())) {
                        return true;
                    }
                    return stack.has(FSAttachments.FLUID_STORAGE_MODIFIER) || stack.is(FunctionalStorage.CREATIVE_UPGRADE);
                })
                .setOnSlotChanged((stack, integer) -> {
                    setNeedsUpgradeCache(true);
                    this.fluidHandler.setCapacity(getTankCapacity(getStorageMultiplier()));
                    syncObject(this.fluidHandler);
                })
                .setSlotLimit(1);
    }


    @Override
    public IFluidHandler getFluidHandler(@Nullable Direction direction) {
        return fluidHandler;
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction direction) {
        return null;
    }
}
