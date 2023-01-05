package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.client.gui.FluidDrawerInfoGuiAddon;
import com.buuz135.functionalstorage.fluid.BigFluidHandler;
import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.component.inventory.InventoryComponent;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.wrappers.BucketPickupHandlerWrapper;
import net.minecraftforge.fluids.capability.wrappers.FluidBlockWrapper;
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
        this.fluidHandlerLazyOptional = LazyOptional.of(() -> fluidHandler);
    }

    private int getTankCapacity(int storageMultiplier) {
        long maxCap = ((type.getSlotAmount() / 64)) * 1000L * storageMultiplier;
        return (int) Math.min(Integer.MAX_VALUE, maxCap);
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
                new ResourceLocation(FunctionalStorage.MOD_ID, "textures/blocks/fluid_front" + finalSlotName + ".png"),
                type.getSlots(),
                type.getSlotPosition(),
                this::getFluidHandler,
                integer -> getFluidHandler().getTankCapacity(integer)
        ));
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
    public double getStorageDiv() {
        return 2;
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState stateOwn, FluidDrawerTile blockEntity) {
        super.serverTick(level, pos, stateOwn, blockEntity);
        if (level.getGameTime() % 4 == 0) {
            for (int i = 0; i < this.getUtilityUpgrades().getSlots(); i++) {
                var stack = this.getUtilityUpgrades().getStackInSlot(i);
                if (!stack.isEmpty()) {
                    var item = stack.getItem();
                    if (item.equals(FunctionalStorage.PUSHING_UPGRADE.get())) {
                        var direction = UpgradeItem.getDirection(stack);
                        TileUtil.getTileEntity(level, pos.relative(direction)).ifPresent(blockEntity1 -> {
                            blockEntity1.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite()).ifPresent(otherFluidHandler -> {
                                for (int tankId = 0; tankId < this.getFluidHandler().getTanks(); tankId++) {
                                    var fluidTank = this.fluidHandler.getTankList()[tankId];
                                    if (fluidTank.getFluid().isEmpty()) continue;
                                    var extracted = fluidTank.drain(500, IFluidHandler.FluidAction.SIMULATE);
                                    if (extracted.isEmpty()) continue;
                                    var insertedAmount = otherFluidHandler.fill(extracted, IFluidHandler.FluidAction.EXECUTE);
                                    if (insertedAmount > 0) {
                                        fluidTank.drain(insertedAmount, IFluidHandler.FluidAction.EXECUTE);
                                        this.fluidHandler.onChange();
                                        break;
                                    }
                                }
                            });
                        });
                    }
                    if (item.equals(FunctionalStorage.PULLING_UPGRADE.get())) {
                        var direction = UpgradeItem.getDirection(stack);
                        TileUtil.getTileEntity(level, pos.relative(direction)).ifPresent(blockEntity1 -> {
                            blockEntity1.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite()).ifPresent(otherFluidHandler -> {
                                for (int tankId = 0; tankId < this.getFluidHandler().getTanks(); tankId++) {
                                    var fluidTank = this.fluidHandler.getTankList()[tankId];
                                    var extracted = otherFluidHandler.drain(500, IFluidHandler.FluidAction.SIMULATE);
                                    if (extracted.isEmpty()) continue;
                                    var insertedAmount = fluidTank.fill(extracted, IFluidHandler.FluidAction.EXECUTE);
                                    if (insertedAmount > 0) {
                                        otherFluidHandler.drain(insertedAmount, IFluidHandler.FluidAction.EXECUTE);
                                        this.fluidHandler.onChange();
                                        break;
                                    }
                                }
                            });
                        });
                    }
                    if (item.equals(FunctionalStorage.COLLECTOR_UPGRADE.get()) && level.getGameTime() % 20 == 0) {
                        var direction = UpgradeItem.getDirection(stack);
                        var fluidstate = this.level.getFluidState(this.getBlockPos().relative(direction));
                        if (!fluidstate.isEmpty() && fluidstate.isSource()) {
                            BlockState state = level.getBlockState(pos.relative(direction));
                            Block block = state.getBlock();
                            IFluidHandler targetFluidHandler = null;
                            if (block instanceof IFluidBlock) {
                                targetFluidHandler = new FluidBlockWrapper((IFluidBlock) block, level, pos.relative(direction));
                            } else if (block instanceof BucketPickup) {
                                targetFluidHandler = new BucketPickupHandlerWrapper((BucketPickup) block, level, pos.relative(direction));
                            }
                            if (targetFluidHandler != null) {
                                var drained = targetFluidHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
                                if (!drained.isEmpty()) {
                                    for (int tankId = 0; tankId < this.getFluidHandler().getTanks(); tankId++) {
                                        var fluidTank = this.fluidHandler.getTankList()[tankId];
                                        var insertedAmount = fluidTank.fill(drained, IFluidHandler.FluidAction.SIMULATE);
                                        if (insertedAmount == drained.getAmount()) {
                                            fluidTank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                                            targetFluidHandler.drain(insertedAmount, IFluidHandler.FluidAction.EXECUTE);
                                            this.fluidHandler.onChange();
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public InteractionResult onSlotActivated(Player playerIn, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ, int slot) {
        ItemStack stack = playerIn.getItemInHand(hand);
        if (stack.getItem().equals(FunctionalStorage.CONFIGURATION_TOOL.get()) || stack.getItem().equals(FunctionalStorage.LINKING_TOOL.get()))
            return InteractionResult.PASS;
        if (slot != -1 && !playerIn.getItemInHand(hand).isEmpty()) {
            var interactionResult = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).map(iFluidHandlerItem -> {
                return playerIn.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(iItemHandler -> {
                    var result = FluidUtil.tryEmptyContainerAndStow(stack, this.fluidHandler.getTankList()[slot], iItemHandler, Integer.MAX_VALUE, playerIn, true);
                    if (result.isSuccess()) {
                        playerIn.setItemInHand(playerIn.getUsedItemHand(), result.getResult());
                        return InteractionResult.SUCCESS;
                    }
                    return InteractionResult.PASS;
                }).orElse(InteractionResult.PASS);
            }).orElse(InteractionResult.PASS);
            if (interactionResult == InteractionResult.SUCCESS) {
                return interactionResult;
            }
        }
        return super.onSlotActivated(playerIn, hand, facing, hitX, hitY, hitZ, slot);
    }

    @Override
    public void onClicked(Player playerIn, int slot) {
        ItemStack stack = playerIn.getItemInHand(playerIn.getUsedItemHand());
        if (slot != -1 && !stack.isEmpty()) {
            stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).ifPresent(iFluidHandlerItem -> {
                playerIn.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(iItemHandler -> {
                    var result = FluidUtil.tryFillContainerAndStow(stack, this.fluidHandler.getTankList()[slot], iItemHandler, Integer.MAX_VALUE, playerIn, true);
                    if (result.isSuccess()) {
                        playerIn.setItemInHand(playerIn.getUsedItemHand(), result.getResult());
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

    @Override
    public int getBaseSize(int lost) {
        return type.getSlotAmount();
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
                            var calculated = ((StorageUpgradeItem) getStorageUpgrades().getStackInSlot(i).getItem()).getStorageMultiplier() / getStorageDiv();
                            if (mult == 1)
                                mult = (int) calculated;
                            else
                                mult *= calculated;
                        }
                    }
                    for (int i = 0; i < getFluidHandler().getTanks(); i++) {
                        if (getFluidHandler().getFluidInTank(i).isEmpty()) continue;
                        if (getFluidHandler().getFluidInTank(i).getAmount() > getTankCapacity(mult)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
                return super.extractItem(slot, amount, simulate);
            }
        }
                .setInputFilter((stack, integer) -> {
                    if (stack.getItem().equals(FunctionalStorage.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.IRON).get())) {
                        return false;
                    }
                    return stack.getItem() instanceof UpgradeItem && ((UpgradeItem) stack.getItem()).getType() == UpgradeItem.Type.STORAGE;
                })
                .setOnSlotChanged((stack, integer) -> {
                    setNeedsUpgradeCache(true);
                    this.fluidHandler.setCapacity(getTankCapacity(getStorageMultiplier()));
                    syncObject(this.fluidHandler);
                })
                .setSlotLimit(1);
    }


}
