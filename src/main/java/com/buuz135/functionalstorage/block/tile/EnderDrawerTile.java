package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.client.gui.DrawerInfoGuiAddon;
import com.buuz135.functionalstorage.inventory.EnderInventoryHandler;
import com.buuz135.functionalstorage.network.EnderDrawerSyncMessage;
import com.buuz135.functionalstorage.world.EnderSavedData;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class EnderDrawerTile extends ControllableDrawerTile<EnderDrawerTile> {

    @Save
    private String frequency;
    private LazyOptional<IItemHandler> lazyStorage;

    public EnderDrawerTile(BasicTileBlock<EnderDrawerTile> base, BlockEntityType<EnderDrawerTile> blockEntityType, BlockPos pos, BlockState state) {
        super(base, blockEntityType, pos, state);
        this.frequency = UUID.randomUUID().toString();
        this.lazyStorage = LazyOptional.empty();
    }

    @Override
    public void setLevel(Level p_155231_) {
        super.setLevel(p_155231_);
        this.lazyStorage.invalidate();
        this.lazyStorage = LazyOptional.of(() -> EnderSavedData.getInstance(this.level).getFrequency(this.frequency));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void initClient() {
        super.initClient();
        addGuiAddonFactory(() -> new DrawerInfoGuiAddon(64, 16,
                new ResourceLocation(FunctionalStorage.MOD_ID, "textures/blocks/ender_front.png"),
                1,
                FunctionalStorage.DrawerType.X_1.getSlotPosition(),
                integer -> getStorage().getStackInSlot(integer),
                integer -> getStorage().getSlotLimit(integer)
        ));
    }

    @Nonnull
    @Override
    public <U> LazyOptional<U> getCapability(@Nonnull Capability<U> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return lazyStorage.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state, EnderDrawerTile blockEntity) {
        super.serverTick(level, pos, state, blockEntity);
        if (level.getGameTime() % 20 == 0){
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

    @Override
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
    public void load(CompoundTag compound) {
        String oldFreq = this.frequency;
        super.load(compound);
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
    public IItemHandler getStorage() {
        return lazyStorage.resolve().get();
    }

    @Override
    public LazyOptional<IItemHandler> getOptional() {
        return lazyStorage;
    }

    @Override
    public int getBaseSize(int lost) {
        return 1;
    }

    public void setFrequency(String frequency){
        if (frequency == null) return;
        this.frequency = frequency;
        this.lazyStorage.invalidate();
        this.lazyStorage = LazyOptional.of(() -> EnderSavedData.getInstance(this.level).getFrequency(this.frequency));
        this.markForUpdate();
    }

    public String getFrequency() {
        return frequency;
    }
}
