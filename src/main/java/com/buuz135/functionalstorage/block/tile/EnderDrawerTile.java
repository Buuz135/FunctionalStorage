package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.inventory.EnderInventoryHandler;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.network.EnderDrawerSyncMessage;
import com.buuz135.functionalstorage.world.EnderSavedData;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class EnderDrawerTile extends ItemControllableDrawerTile<EnderDrawerTile> {

    @Save
    private String frequency;
    private ResourceHandler<ItemResource> storage;

    public EnderDrawerTile(BasicTileBlock<EnderDrawerTile> base, BlockEntityType<EnderDrawerTile> blockEntityType, BlockPos pos, BlockState state) {
        super(base, blockEntityType, pos, state, new DrawerProperties(32, FSAttachments.ITEM_STORAGE_MODIFIER));
        this.frequency = UUID.randomUUID().toString();
    }

    @Override
    public void setLevel(Level p_155231_) {
        super.setLevel(p_155231_);
        this.invalidateCapabilities();
        this.storage = EnderSavedData.getInstance(this.level).getFrequency(this.frequency);
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state, EnderDrawerTile blockEntity) {
        super.serverTick(level, pos, state, blockEntity);
        if (level.getGameTime() % 10 == 0){
            FunctionalStorage.NETWORK.sendToNearby(level, pos, 32, new EnderDrawerSyncMessage(frequency, ((EnderInventoryHandler)getStorage())));
        }
        if (level.getGameTime() % 10 == 0) {
            EnderInventoryHandler handler = EnderSavedData.getInstance(this.level).getFrequency(this.frequency);
            if (handler.isLocked() != isLocked()) {
                super.setLocked(handler.isLocked());
            }
            if (!handler.isVoid()){
                for (int i = 0; i < getUtilityUpgrades().getSlots(); i++) {
                    ItemStack stack = getUtilityUpgrades().getStackInSlot(i);
                    if (!stack.isEmpty() && stack.is(FunctionalStorage.VOID_UPGRADE.get())){
                        handler.setVoidItems(true);
                        stack.shrink(1);
                        break;
                    }
                }
            }
        }
    }

    public InteractionResult onSlotActivated(Player playerIn, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ, int slot) {
        InteractionResult result = super.onSlotActivated(playerIn, hand, facing, hitX, hitY, hitZ, slot);
        if (slot != -1){
            FunctionalStorage.NETWORK.sendToNearby(level, this.getBlockPos(), 32, new EnderDrawerSyncMessage(frequency, ((EnderInventoryHandler)getStorage())));
        }
        return result;
    }

    @Override
    public void onClicked(Player playerIn, int slot) {
        super.onClicked(playerIn, slot);
        if (slot != -1){
            FunctionalStorage.NETWORK.sendToNearby(level, this.getBlockPos(), 32, new EnderDrawerSyncMessage(frequency, ((EnderInventoryHandler)getStorage())));
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        String oldFreq = this.frequency;
        super.loadAdditional(input);
        if (!this.frequency.equalsIgnoreCase(oldFreq) && level instanceof ServerLevel){
            setFrequency(this.frequency);
        }
    }

    @Override
    public void setLocked(boolean locked) {
        super.setLocked(locked);
        EnderSavedData.getInstance(this.level).getFrequency(this.frequency).setLocked(locked);
    }

    @Override
    public boolean isVoid() {
        return EnderSavedData.getInstance(this.level).getFrequency(this.frequency).isVoid();
    }

    @NotNull
    @Override
    public EnderDrawerTile getSelf() {
        return this;
    }
    @Override
    public int getStorageSlotAmount() {
        return 0;
    }

    @Override
    public ResourceHandler<ItemResource> getStorage() {
        return this.storage;
    }

    @Override
    public boolean isEverythingEmpty() {
        EnderInventoryHandler inventoryHandler = EnderSavedData.getInstance(this.level).getFrequency(this.frequency);
        for (int i = 0; i < inventoryHandler.size(); i++) {
            if (!inventoryHandler.getStackInSlot(i).isEmpty()) {
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

    public void setFrequency(String frequency) {
        if (frequency == null) return;
        this.frequency = frequency;
        this.invalidateCapabilities();
        this.storage = EnderSavedData.getInstance(this.level).getFrequency(this.frequency);
        this.markForUpdate();
    }

    public String getFrequency() {
        return frequency;
    }
}
