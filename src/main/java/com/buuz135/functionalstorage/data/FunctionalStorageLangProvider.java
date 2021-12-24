package com.buuz135.functionalstorage.data;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.DrawerBlock;
import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.text.WordUtils;

public class FunctionalStorageLangProvider extends LanguageProvider {

    public FunctionalStorageLangProvider(DataGenerator gen, String modid, String locale) {
        super(gen, modid, locale);
    }

    @Override
    protected void addTranslations() {
        this.add("itemGroup.functionalstorage", "Functional Storage");
        for (FunctionalStorage.DrawerType drawerType : FunctionalStorage.DRAWER_TYPES.keySet()) {
            for (RegistryObject<Block> blockRegistryObject : FunctionalStorage.DRAWER_TYPES.get(drawerType)) {
                DrawerBlock drawerBlock = (DrawerBlock) blockRegistryObject.get();
                this.add(drawerBlock, WordUtils.capitalize(drawerBlock.getWoodType().getName().replace('_', ' ').toLowerCase()) + " Drawer (" +drawerBlock.getType().getDisplayName() +")");
            }
        }
        this.add(FunctionalStorage.COMPACTING_DRAWER.get(), "Compacting Drawer");
        this.add("linkingtool.linkingmode", "Linking Mode: ");
        this.add("linkingtool.linkingmode.single", "Single");
        this.add("linkingtool.linkingmode.single.desc", "Links a drawer to a controller");
        this.add("linkingtool.linkingmode.multiple", "Multiple");
        this.add("linkingtool.linkingmode.multiple.desc", "Links multiple drawers between 2 points");
        this.add("linkingtool.controller", "Controller: ");
        this.add("linkingtool.linkingaction", "Linking Action: ");
        this.add("linkingtool.use", "Sneak + Right Click in the air to change modes. \nRight Click in the air to change actions. \nRight click a controller to setup the tool then use it nearby drawers to link. \n\nWhile holding the tool it will show the connected drawers to the selected controller.");
        this.add("linkingtool.linkingaction.add", "Add");
        this.add("linkingtool.linkingaction.remove", "Remove");
        this.add("key.categories.utility", "Utility");
        this.add("key.categories.storage", "Storage");
        this.add(FunctionalStorage.LINKING_TOOL.get(), "Linking Tool");
        this.add(FunctionalStorage.DRAWER_CONTROLLER.get(), "Storage Controller");
        this.add("storageupgrade.desc", "Multiplies the block storage by ");
        for (StorageUpgradeItem.StorageTier storageTier : FunctionalStorage.STORAGE_UPGRADES.keySet()) {
            this.add(FunctionalStorage.STORAGE_UPGRADES.get(storageTier).get(), WordUtils.capitalize(storageTier.name().toLowerCase()) + (storageTier == StorageUpgradeItem.StorageTier.IRON ? " Downgrade" : " Upgrade"));
        }
        this.add(FunctionalStorage.COLLECTOR_UPGRADE.get(), WordUtils.capitalize(FunctionalStorage.COLLECTOR_UPGRADE.get().getRegistryName().getPath().replace('_', ' ').toLowerCase()) );
        this.add(FunctionalStorage.PULLING_UPGRADE.get(), WordUtils.capitalize(FunctionalStorage.PULLING_UPGRADE.get().getRegistryName().getPath().replace('_', ' ').toLowerCase()) );
        this.add(FunctionalStorage.PUSHING_UPGRADE.get(), WordUtils.capitalize(FunctionalStorage.PUSHING_UPGRADE.get().getRegistryName().getPath().replace('_', ' ').toLowerCase()) );
        this.add(FunctionalStorage.VOID_UPGRADE.get(), WordUtils.capitalize(FunctionalStorage.VOID_UPGRADE.get().getRegistryName().getPath().replace('_', ' ').toLowerCase()) );
        this.add(FunctionalStorage.ARMORY_CABINET.get(), "Armory Cabinet");
        this.add(FunctionalStorage.CONFIGURATION_TOOL.get(), "Configuration Tool");
        this.add("item.utility.downgrade", "Downgrades the slots to a max of 64 items");
        this.add("item.utility.direction", "Direction: ");
        this.add("item.utility.direction.desc", "Right click in a GUI to change direction");
    }
}
