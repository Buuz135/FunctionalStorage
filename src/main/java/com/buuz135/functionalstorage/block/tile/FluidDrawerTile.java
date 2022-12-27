package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.fluid.BigFluidHandler;
import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.component.inventory.InventoryComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidDrawerTile extends ControllableDrawerTile<FluidDrawerTile> {

    public LazyOptional<IFluidHandler> fluidHandlerLazyOptional;
    @Save
    private BigFluidHandler fluidHandler;
    private FunctionalStorage.DrawerType type;

    public FluidDrawerTile(BasicTileBlock<FluidDrawerTile> base, BlockEntityType<FluidDrawerTile> blockEntityType, BlockPos pos, BlockState state, FunctionalStorage.DrawerType type) {
        super(base, blockEntityType, pos, state);
        this.type = type;
        this.fluidHandler = new BigFluidHandler(type.getSlots(), getTankCapacity()) {
            @Override
            public void onChange() {
                syncObject(fluidHandler);
            }
        };
        this.fluidHandlerLazyOptional = LazyOptional.of(() -> fluidHandler);
    }

    private int getTankCapacity() {
        long maxCap = ((type.getSlotAmount() / 64) / 2) * 1000L * getStorageMultiplier();
        return (int) Math.min(Integer.MAX_VALUE, maxCap);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void initClient() {
        super.initClient();
    }

    @Nonnull
    @Override
    public <U> LazyOptional<U> getCapability(@Nonnull Capability<U> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return LazyOptional.empty();
        }
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return fluidHandlerLazyOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public InteractionResult onSlotActivated(Player playerIn, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ, int slot) {
        ItemStack stack = playerIn.getItemInHand(hand);
        if (stack.getItem().equals(FunctionalStorage.CONFIGURATION_TOOL.get()) || stack.getItem().equals(FunctionalStorage.LINKING_TOOL.get()))
            return InteractionResult.PASS;
        if (slot != -1 && !playerIn.getItemInHand(hand).isEmpty()) {
            InteractionResult result = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).map(iFluidHandlerItem -> {
                int amount = Minecraft.getInstance().player.containerMenu.getCarried().getItem() instanceof BucketItem ? FluidType.BUCKET_VOLUME : Integer.MAX_VALUE;
                amount = this.fluidHandler.getTankList()[slot].fill(iFluidHandlerItem.drain(amount, IFluidHandler.FluidAction.SIMULATE), IFluidHandler.FluidAction.EXECUTE);
                if (!playerIn.isCreative()) {
                    iFluidHandlerItem.drain(amount, IFluidHandler.FluidAction.EXECUTE);
                    playerIn.setItemInHand(hand, iFluidHandlerItem.getContainer().copy());
                }
                return InteractionResult.SUCCESS;
            }).orElse(InteractionResult.PASS);
            if (result == InteractionResult.SUCCESS) {
                return result;
            }
        }
        return super.onSlotActivated(playerIn, hand, facing, hitX, hitY, hitZ, slot);
    }

    @Override
    public void onClicked(Player playerIn, int slot) {
        ItemStack stack = playerIn.getItemInHand(playerIn.getUsedItemHand());
        if (slot != -1 && !stack.isEmpty()) {
            InteractionResult result = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).map(iFluidHandlerItem -> {
                int amount = Minecraft.getInstance().player.containerMenu.getCarried().getItem() instanceof BucketItem ? FluidType.BUCKET_VOLUME : Integer.MAX_VALUE;
                amount = iFluidHandlerItem.fill(this.fluidHandler.getTankList()[slot].drain(amount, IFluidHandler.FluidAction.SIMULATE), IFluidHandler.FluidAction.EXECUTE);
                if (!playerIn.isCreative()) {
                    this.fluidHandler.getTankList()[slot].drain(amount, IFluidHandler.FluidAction.EXECUTE);
                    playerIn.setItemInHand(playerIn.getUsedItemHand(), iFluidHandlerItem.getContainer().copy());
                }
                return InteractionResult.SUCCESS;
            }).orElse(InteractionResult.PASS);
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

    @Override
    public int getBaseSize(int lost) {
        return type.getSlotAmount();
    }

    public BigFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    @Override
    public InventoryComponent<ControllableDrawerTile<FluidDrawerTile>> getStorageUpgradesConstructor() {
        return new InventoryComponent<ControllableDrawerTile<FluidDrawerTile>>("storage_upgrades", 10, 70, getStorageSlotAmount()) {
            @NotNull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                ItemStack stack = this.getStackInSlot(slot);
                if (stack.getItem() instanceof StorageUpgradeItem) {
                    int mult = 1;
                    for (int i = 0; i < getStorageUpgrades().getSlots(); i++) {
                        if (getStorageUpgrades().getStackInSlot(i).getItem() instanceof StorageUpgradeItem) {
                            if (i == slot) continue;
                            if (mult == 1)
                                mult = ((StorageUpgradeItem) getStorageUpgrades().getStackInSlot(i).getItem()).getStorageMultiplier();
                            else
                                mult *= ((StorageUpgradeItem) getStorageUpgrades().getStackInSlot(i).getItem()).getStorageMultiplier();
                        }
                    }
                }
                return super.extractItem(slot, amount, simulate);
            }
        }
                .setInputFilter((stack, integer) -> {
                    if (stack.getItem().equals(FunctionalStorage.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.IRON).get())) {

                    }
                    return stack.getItem() instanceof UpgradeItem && ((UpgradeItem) stack.getItem()).getType() == UpgradeItem.Type.STORAGE;
                })
                .setOnSlotChanged((stack, integer) -> {
                    setNeedsUpgradeCache(true);
                    for (BigFluidHandler.CustomFluidTank customFluidTank : this.fluidHandler.getTankList()) {
                        customFluidTank.setCapacity(getTankCapacity());
                    }
                })
                .setSlotLimit(1);
    }


}
