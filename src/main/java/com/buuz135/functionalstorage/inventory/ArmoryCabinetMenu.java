package com.buuz135.functionalstorage.inventory;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ArmoryCabinetTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ArmoryCabinetMenu extends AbstractContainerMenu {

    public static final int COLUMNS = 8;
    public static final int VISIBLE_ROWS = 4;
    public static final int VISIBLE_SLOTS = COLUMNS * VISIBLE_ROWS;
    public static final int BUTTON_SCROLL_BASE = 10_000;
    public static final int BUTTON_QUERY_CLEAR = 20_000;
    public static final int BUTTON_QUERY_APPEND_BASE = 30_000;

    private final Inventory playerInventory;
    private final IItemHandler handler;
    private final ArmoryCabinetTile tile;
    private final List<ArmorySlot> armorySlots = new ArrayList<>();
    private final List<Integer> filteredSlots = new ArrayList<>();
    private String query = "";
    private int scrollRow;

    public ArmoryCabinetMenu(int id, Inventory playerInventory, RegistryFriendlyByteBuf data) {
        this(id, playerInventory, readClientData(playerInventory, data));
    }

    private ArmoryCabinetMenu(int id, Inventory playerInventory, ClientMenuData data) {
        this(id, playerInventory, data.handler(), data.pos());
    }

    public ArmoryCabinetMenu(int id, Inventory playerInventory, IItemHandler handler, BlockPos pos) {
        super((MenuType<ArmoryCabinetMenu>) FunctionalStorage.ARMORY_CABINET_MENU.get(), id);
        this.playerInventory = playerInventory;
        this.handler = handler;
        this.tile = playerInventory.player.level().getBlockEntity(pos) instanceof ArmoryCabinetTile armory ? armory : null;

        for (int row = 0; row < VISIBLE_ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                ArmorySlot slot = new ArmorySlot(handler, column + row * COLUMNS, 8 + column * 18, 19 + row * 18);
                armorySlots.add(slot);
                addSlot(slot);
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 102 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 160));
        }

        rebuildFilter();
    }

    private static IItemHandler getClientHandler(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof ArmoryCabinetTile armory) {
            return armory.getStorage();
        }
        return new ArmoryCabinetInventoryHandler() {
            @Override
            public void onChange() {
            }
        };
    }

    private static ClientMenuData readClientData(Inventory inventory, RegistryFriendlyByteBuf data) {
        BlockPos pos = data.readBlockPos();
        IItemHandler handler = getClientHandler(inventory, pos);
        if (handler instanceof ArmoryCabinetInventoryHandler armoryHandler) {
            armoryHandler.deserializeNBT(data.registryAccess(), data.readNbt());
        }
        return new ClientMenuData(handler, pos);
    }

    public void setQuery(String query) {
        this.query = query == null ? "" : query.toLowerCase(Locale.ROOT).trim();
        this.scrollRow = 0;
        rebuildFilter();
    }

    public void setScrollRow(int scrollRow) {
        this.scrollRow = Math.max(0, Math.min(scrollRow, getMaxScrollRow()));
        updateVisibleSlots();
    }

    public int getScrollRow() {
        return scrollRow;
    }

    public int getMaxScrollRow() {
        return Math.max(0, (filteredSlots.size() + COLUMNS - 1) / COLUMNS - VISIBLE_ROWS);
    }

    public int getFilteredSlotCount() {
        return filteredSlots.size();
    }

    public int getTotalArmorySlots() {
        return handler.getSlots();
    }

    private void rebuildFilter() {
        filteredSlots.clear();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (query.isEmpty() || matches(stack, query)) {
                filteredSlots.add(slot);
            }
        }
        setScrollRow(scrollRow);
    }

    private boolean matches(ItemStack stack, String query) {
        if (stack.isEmpty()) return false;
        if (stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(query)) return true;
        return stack.getTooltipLines(Item.TooltipContext.of(playerInventory.player.level()), playerInventory.player, TooltipFlag.NORMAL)
                .stream()
                .map(Component::getString)
                .map(text -> text.toLowerCase(Locale.ROOT))
                .anyMatch(text -> text.contains(query));
    }

    private void updateVisibleSlots() {
        int firstFiltered = scrollRow * COLUMNS;
        for (int i = 0; i < armorySlots.size(); i++) {
            int filteredIndex = firstFiltered + i;
            armorySlots.get(i).setArmoryIndex(filteredIndex < filteredSlots.size() ? filteredSlots.get(filteredIndex) : -1);
        }
        broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (button >= BUTTON_SCROLL_BASE && button < BUTTON_QUERY_CLEAR) {
            setScrollRow(button - BUTTON_SCROLL_BASE);
            return true;
        }
        if (button == BUTTON_QUERY_CLEAR) {
            setQuery("");
            return true;
        }
        if (button >= BUTTON_QUERY_APPEND_BASE && button <= BUTTON_QUERY_APPEND_BASE + Character.MAX_VALUE) {
            setQuery(query + (char) (button - BUTTON_QUERY_APPEND_BASE));
            return true;
        }
        return super.clickMenuButton(player, button);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < VISIBLE_SLOTS) {
                if (!moveItemStackTo(stack, VISIBLE_SLOTS, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {
                ItemStack remainder = stack;
                for (int armorySlot = 0; armorySlot < handler.getSlots() && !remainder.isEmpty(); armorySlot++) {
                    remainder = handler.insertItem(armorySlot, remainder, false);
                }
                if (remainder.getCount() == stack.getCount()) return ItemStack.EMPTY;
                stack.setCount(remainder.getCount());
                rebuildFilter();
            }
            if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return tile != null && !tile.isRemoved() && player.distanceToSqr(tile.getBlockPos().getX() + 0.5D, tile.getBlockPos().getY() + 0.5D, tile.getBlockPos().getZ() + 0.5D) <= 64.0D;
    }

    private static class ArmorySlot extends Slot {
        private final IItemHandler handler;
        private int armoryIndex;

        private ArmorySlot(IItemHandler handler, int visibleIndex, int x, int y) {
            super(new net.minecraft.world.SimpleContainer(0), visibleIndex, x, y);
            this.handler = handler;
            this.armoryIndex = visibleIndex;
        }

        private void setArmoryIndex(int armoryIndex) {
            this.armoryIndex = armoryIndex;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return armoryIndex >= 0 && handler.isItemValid(armoryIndex, stack);
        }

        @Override
        public ItemStack getItem() {
            return armoryIndex >= 0 ? handler.getStackInSlot(armoryIndex) : ItemStack.EMPTY;
        }

        @Override
        public void set(ItemStack stack) {
            if (armoryIndex >= 0 && handler instanceof net.neoforged.neoforge.items.IItemHandlerModifiable modifiable) {
                modifiable.setStackInSlot(armoryIndex, stack);
                setChanged();
            }
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return 1;
        }

        @Override
        public boolean mayPickup(Player player) {
            return armoryIndex >= 0 && !handler.extractItem(armoryIndex, 1, true).isEmpty();
        }

        @Override
        public ItemStack remove(int amount) {
            return armoryIndex >= 0 ? handler.extractItem(armoryIndex, amount, false) : ItemStack.EMPTY;
        }

        @Override
        public boolean isActive() {
            return armoryIndex >= 0;
        }
    }

    private record ClientMenuData(IItemHandler handler, BlockPos pos) {
    }
}
