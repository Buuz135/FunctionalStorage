package com.buuz135.functionalstorage.inventory.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.item.component.SizeProvider;
import com.buuz135.functionalstorage.util.Utils;
import com.hrznstudio.titanium.component.inventory.InventoryComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class DrawerStackItemHandler extends BigInventoryHandler {
    private final ItemAccess access;

    private final float storageMultiplier;
    private final boolean isVoid;
    private final boolean isCreative;

    private boolean isLocked;

    public DrawerStackItemHandler(ItemAccess access, FunctionalStorage.DrawerType drawerType) {
        super(drawerType);
        this.access = access;

        float storageMultiplier = 1f;
        boolean isVoid = false, isCreative = false;

        if (access.getResource().has(FSAttachments.TILE)) {
            var tile = access.getResource().get(FSAttachments.TILE);
            var titaniumData = tile.contains("TitaniumData") ? tile.getCompoundOrEmpty("TitaniumData") : tile;
            var registryAccess = Utils.registryAccess();
            deserializeNBT(registryAccess, titaniumData.getCompoundOrEmpty("handler"));

            var upgrades = new InventoryComponent<>("storage_upgrades", 0, 0, 4);
            upgrades.deserializeNBT(registryAccess, tile.getCompoundOrEmpty("storageUpgrades"));
            storageMultiplier = SizeProvider.calculateAsFactor(upgrades, FSAttachments.ITEM_STORAGE_MODIFIER, drawerType.getSlotAmount());
            isCreative = ResourceHandlerUtil.contains(upgrades, ItemResource.of(FunctionalStorage.CREATIVE_UPGRADE));

            for (Tag tag : titaniumData.getCompoundOrEmpty("utilityUpgrades").getListOrEmpty("Items")) {
                ItemStack itemStack = Utils.deserialize(registryAccess, (CompoundTag) tag);
                if (itemStack.getItem().equals(FunctionalStorage.VOID_UPGRADE.get())) {
                    isVoid = true;
                }
            }

            isLocked = true; // Drawers that have been placed in the world are to be assumed locked, fresh drawers can have items inserted into them
        }

        this.storageMultiplier = storageMultiplier;
        this.isVoid = isVoid;
        this.isCreative = isCreative;
    }

    @Override
    public boolean isVoid() {
        return isVoid;
    }

    @Override
    public boolean isCreative() {
        return isCreative;
    }

    @Override
    public void onContentsUpdated(TransactionContext context) {
        if (!isLocked) { // If not locked already and we have inserted any item into the drawer, lock it.
            for (int i = 0; i < size(); i++) {
                if (!getResource(i).isEmpty()) {
                    isLocked = true;
                    break;
                }
            }
        }

        var newCompound = access.getResource().get(FSAttachments.TILE).copy();
        var titaniumData = newCompound.getCompoundOrEmpty("TitaniumData");
        titaniumData.put("handler", serializeNBT(Utils.registryAccess()));
        newCompound.put("TitaniumData", titaniumData);

        var newItem = access.getResource().with(FSAttachments.TILE, newCompound);
        access.exchange(newItem, 1, context);
    }

    @Override
    public void onChange() {

    }

    @Override
    public float getMultiplier() {
        return storageMultiplier;
    }

    @Override
    public boolean isLocked() {
        return isLocked;
    }
}
