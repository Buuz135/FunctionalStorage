package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.item.component.SizeProvider;
import com.buuz135.functionalstorage.util.StorageTransferUtil;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.component.inventory.InventoryComponent;
import com.hrznstudio.titanium.util.RayTraceUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

public abstract class ItemControllableDrawerTile<T extends ItemControllableDrawerTile<T>> extends ControllableDrawerTile<T> {

    private static HashMap<UUID, Long> INTERACTION_LOGGER = new HashMap<>();
    private int removeTicks = 0;
    private ResourceHandler<ItemResource> transferHandler;

    public ItemControllableDrawerTile(BasicTileBlock<T> base, BlockEntityType<T> entityType, BlockPos pos, BlockState state, DrawerProperties props) {
        super(base, entityType, pos, state, props);
    }

    @Override
    public void initClient() {
        super.initClient();
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        super.serverTick(level, pos, state, blockEntity);
        this.removeTicks = Math.max(this.removeTicks - 1, 0);
    }

    @Override
    public InteractionResult onSlotActivated(Player playerIn, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ, int slot) {
        ItemStack stack = playerIn.getItemInHand(hand);
        if (super.onActivated(playerIn, hand, facing, hitX, hitY, hitZ) == InteractionResult.SUCCESS) {
            return InteractionResult.SUCCESS;
        }
        if (slot != -1 && isServer()) {
            ItemResource resource = ItemResource.of(stack);
            int inserted = StorageTransferUtil.insert(getStorage(), slot, resource, stack.getCount(), false);
            if (!stack.isEmpty() && inserted > 0) {
                StorageTransferUtil.insert(getStorage(), slot, resource, inserted, true);
                playerIn.setItemInHand(hand, inserted == stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - inserted));
                return InteractionResult.SUCCESS;
            } else if (System.currentTimeMillis() - INTERACTION_LOGGER.getOrDefault(playerIn.getUUID(), System.currentTimeMillis()) < 300 && !getStorage().getResource(slot).isEmpty()) {
                var inventory = playerIn.getInventory();
                for (int inventorySlot = 0; inventorySlot < inventory.getContainerSize(); inventorySlot++) {
                    ItemStack itemStack = inventory.getItem(inventorySlot);
                    ItemResource inventoryResource = ItemResource.of(itemStack);
                    int inventoryInserted = StorageTransferUtil.insert(getStorage(), slot, inventoryResource, itemStack.getCount(), false);
                    if (!itemStack.isEmpty() && inventoryInserted > 0) {
                        StorageTransferUtil.insert(getStorage(), slot, inventoryResource, inventoryInserted, true);
                        itemStack.shrink(inventoryInserted);
                    }
                }
            }
            INTERACTION_LOGGER.put(playerIn.getUUID(), System.currentTimeMillis());
        }
        if (super.onSlotActivated(playerIn, hand, facing, hitX, hitY, hitZ, slot) == InteractionResult.SUCCESS) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    public abstract int getStorageSlotAmount();

    public void onClicked(Player playerIn, int slot) {
        if (isServer() && slot != -1 && this.removeTicks == 0) {
            this.removeTicks = 3;
            HitResult rayTraceResult = RayTraceUtils.rayTraceSimple(this.level, playerIn, 16, 0);
            if (rayTraceResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockResult = (BlockHitResult) rayTraceResult;
                    Direction facing = blockResult.getDirection();
                    if (facing.equals(this.getFacingDirection())) {
                        if (preventInteraction(blockResult.getBlockPos(), playerIn)) return;
                    ItemResource resource = getStorage().getResource(slot);
                    int extractedAmount = StorageTransferUtil.extract(getStorage(), slot, resource, playerIn.isShiftKeyDown() ? resource.getMaxStackSize() : 1, true);
                    ItemStack extracted = resource.toStack(extractedAmount);
                    if (!playerIn.addItem(extracted)) playerIn.drop(extracted, false);
                }
            }
        }
    }

    private boolean preventInteraction(BlockPos pos, Player player) {
        if (ModList.get().isLoaded("ftbchunks")) {
            try {
                return (boolean) Class.forName("com.buuz135.functionalstorage.compat.ftb.FTBChunksManager")
                        .getMethod("preventInteraction", BlockPos.class, Player.class)
                        .invoke(null, pos, player);
            } catch (ReflectiveOperationException ignored) {
                return false;
            }
        }
        return false;
    }

    public abstract ResourceHandler<ItemResource> getStorage();

    @Override
    public InventoryComponent<ControllableDrawerTile<T>> getStorageUpgradesConstructor() {
        return new InventoryComponent<ControllableDrawerTile<T>>("storage_upgrades", 10, 70, getStorageSlotAmount()) {
            @NotNull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (isStorageUpgradeLocked()) return ItemStack.EMPTY;
                ItemStack stack = this.getStackInSlot(slot);
                if (stack.has(FSAttachments.ITEM_STORAGE_MODIFIER)) {
                    var replacement = new ItemStack[this.getSlots()];
                    replacement[slot] = ItemStack.EMPTY;

                    var newSize = (double) SizeProvider.calculateAsFactor(this, FSAttachments.ITEM_STORAGE_MODIFIER, baseSize, replacement);
                    if (!canChangeMultiplier(newSize)) {
                        return ItemStack.EMPTY;
                    }
                }
                return super.extractItem(slot, amount, simulate);
            }
        }
                .setInputFilter((stack, integer) -> {
                    if (isStorageUpgradeLocked()) return false;
                    if (!canUseStorageUpgradeWithCreative(stack, integer)) return false;
                    if (stack.is(FunctionalStorage.CREATIVE_UPGRADE)) return true;
                    if (!stack.has(FSAttachments.ITEM_STORAGE_MODIFIER)) return false;

                    var replacement = new ItemStack[getStorageUpgrades().getSlots()];
                    replacement[integer] = stack;

                    var newSize = (double) SizeProvider.calculateAsFactor(getStorageUpgrades(), FSAttachments.ITEM_STORAGE_MODIFIER, baseSize, replacement);
                    if (!canChangeMultiplier(newSize)) {
                        return false;
                    }

                    return true;
                })
                .setOnSlotChanged((stack, integer) -> {
                    setNeedsUpgradeCache(true);
                    updateComparatorOutput();
                })
                .setSlotLimit(1);
    }

    protected boolean canChangeMultiplier(double newSizeMultiplier) {
        for (int i = 0; i < getStorage().size(); i++) {
            ItemResource resource = getStorage().getResource(i);
            if (!resource.isEmpty() && getStorage().getAmountAsLong(i) > Math.min(Integer.MAX_VALUE, Math.floor(newSizeMultiplier * resource.getMaxStackSize()))) {
                return false;
            }
        }
        return true;
    }

    public boolean isEverythingEmpty() {
        if (getPriority() != 0) {
            return false;
        }
        for (int i = 0; i < getStorage().size(); i++) {
            if (!getStorage().getResource(i).isEmpty()) {
                return false;
            }
        }
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
        for (int i = 0; i < getStorage().size(); i++) {
            if (!getStorage().getResource(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getTitleColor() {
        return ChatFormatting.DARK_GRAY.getColor();
    }

    @Override
    public ResourceHandler<ItemResource> getItemHandler(@Nullable Direction direction) {
        if (transferHandler == null) transferHandler = getStorage();
        return transferHandler;
    }


}
