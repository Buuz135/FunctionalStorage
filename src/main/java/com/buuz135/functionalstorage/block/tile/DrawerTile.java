package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.client.gui.DrawerInfoGuiAddon;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.util.IWoodType;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
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
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class DrawerTile extends ItemControllableDrawerTile<DrawerTile> {

    @Save
    public BigInventoryHandler handler;
    private FunctionalStorage.DrawerType type;
    private IWoodType woodType;

    public DrawerTile(BasicTileBlock<DrawerTile> base, BlockEntityType<DrawerTile> blockEntityType, BlockPos pos, BlockState state, FunctionalStorage.DrawerType type, IWoodType woodType) {
        super(base, blockEntityType, pos, state, new DrawerProperties(type.getSlotAmount(), FSAttachments.ITEM_STORAGE_MODIFIER));
        this.type = type;
        this.woodType = woodType;
        this.handler = new BigInventoryHandler(type) {
            @Override
            public void onChange() {
                DrawerTile.this.markForUpdate();
            }

            @Override
            public float getMultiplier() {
                return getStorageMultiplier();
            }

            @Override
            public boolean isVoid() {
                return DrawerTile.this.isVoid();
            }

            @Override
            public boolean isLocked() {
                return DrawerTile.this.isLocked();
            }

            @Override
            public boolean isCreative() {
                return DrawerTile.this.isCreative();
            }


        };
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void initClient() {
        super.initClient();
        addGuiAddonFactory(() -> new DrawerInfoGuiAddon(64, 16,
                com.buuz135.functionalstorage.util.Utils.resourceLocation(FunctionalStorage.MOD_ID, "textures/block/" + woodType.getName() + "_front_" + type.getSlots() + ".png"),
                type.getSlots(),
                type.getSlotPosition(),
                integer -> getHandler().getStackInSlot(integer),
                integer -> getHandler().getSlotLimit(integer),
                integer -> getHandler().getStoredStacks().get(integer).getStack()
        ));
    }

    public InteractionResult onSlotActivated(Player playerIn, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ, int slot) {
        ItemStack stack = playerIn.getItemInHand(hand);
        if (stack.getItem().equals(FunctionalStorage.CONFIGURATION_TOOL.get()) || stack.getItem().equals(FunctionalStorage.LINKING_TOOL.get())) return InteractionResult.PASS;
        if (slot != -1 && !playerIn.getItemInHand(hand).isEmpty()){
            BigInventoryHandler.BigStack bigStack = getHandler().getStoredStacks().get(slot);
            if (bigStack.getStack().isEmpty()){
                bigStack.setStack(playerIn.getItemInHand(hand));
            }
        }
        return super.onSlotActivated(playerIn, hand, facing, hitX, hitY, hitZ, slot);
    }

    @NotNull
    @Override
    public DrawerTile getSelf() {
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
    public IItemHandler getStorage() {
        return handler;
    }

    public BigInventoryHandler getHandler() {
        return handler;
    }

}
