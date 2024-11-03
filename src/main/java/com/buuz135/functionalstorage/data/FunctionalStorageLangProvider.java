package com.buuz135.functionalstorage.data;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.DrawerBlock;
import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.stream.Collectors;

public class FunctionalStorageLangProvider extends LanguageProvider {

    public FunctionalStorageLangProvider(DataGenerator gen, String modid, String locale) {
        super(gen.getPackOutput(), modid, locale);
    }

    @Override
    protected void addTranslations() {
        this.add("itemGroup.functionalstorage", "Functional Storage");
        for (FunctionalStorage.DrawerType drawerType : FunctionalStorage.DRAWER_TYPES.keySet()) {
            for (RegistryObject<Block> blockRegistryObject : FunctionalStorage.DRAWER_TYPES.get(drawerType).stream().map(Pair::getLeft).collect(Collectors.toList())) {
                DrawerBlock drawerBlock = (DrawerBlock) blockRegistryObject.get();
                this.add(drawerBlock, WordUtils.capitalize(drawerBlock.getWoodType().getName().replace('_', ' ').toLowerCase()) + " Drawer (" + drawerBlock.getType().getDisplayName() + ")");
            }
        }
        this.add(FunctionalStorage.FLUID_DRAWER_1.getLeft().get(), "Fluid Drawer (" + FunctionalStorage.DrawerType.X_1.getDisplayName() + ")");
        this.add(FunctionalStorage.FLUID_DRAWER_2.getLeft().get(), "Fluid Drawer (" + FunctionalStorage.DrawerType.X_2.getDisplayName() + ")");
        this.add(FunctionalStorage.FLUID_DRAWER_4.getLeft().get(), "Fluid Drawer (" + FunctionalStorage.DrawerType.X_4.getDisplayName() + ")");
        this.add(FunctionalStorage.SIMPLE_COMPACTING_DRAWER.getLeft().get(), "Simple Compacting Drawer");
        this.add(FunctionalStorage.FRAMED_SIMPLE_COMPACTING_DRAWER.getLeft().get(), "Framed Simple Compacting Drawer");
        this.add(FunctionalStorage.COMPACTING_DRAWER.getLeft().get(), "Compacting Drawer");
        this.add(FunctionalStorage.FRAMED_COMPACTING_DRAWER.getLeft().get(), "Framed Compacting Drawer");
        this.add(FunctionalStorage.ENDER_DRAWER.getLeft().get(), "Ender Drawer");
        this.add(FunctionalStorage.CONTROLLER_EXTENSION.getLeft().get(), "Controller Access Point");
        this.add("linkingtool.linkingmode", "Linking Mode: ");
        this.add("linkingtool.linkingmode.single", "Single");
        this.add("linkingtool.linkingmode.single.desc", "Links a drawer to a controller");
        this.add("linkingtool.linkingmode.multiple", "Multiple");
        this.add("linkingtool.linkingmode.multiple.desc", "Links multiple drawers between 2 points");
        this.add("linkingtool.controller", "Controller: ");
        this.add("linkingtool.linkingaction", "Linking Action: ");
        this.add("linkingtool.use", "* Sneak + Right Click in the air to change modes or to clear frequency. \n* Left Click an Ender Drawer to store its Frequency. \n* Right Click in the air to change actions. \n\nRight click a controller to setup the tool then use it nearby drawers to link. \n\nWhile holding the tool it will show the connected drawers to the selected controller.");
        this.add("linkingtool.linkingaction.add", "Add");
        this.add("linkingtool.linkingaction.remove", "Remove");
        this.add("key.categories.utility", "Utility");
        this.add("key.categories.storage", "Storage");
        this.add(FunctionalStorage.LINKING_TOOL.get(), "Linking Tool");
        this.add(FunctionalStorage.DRAWER_CONTROLLER.getLeft().get(), "Storage Controller");
        this.add(FunctionalStorage.FRAMED_DRAWER_CONTROLLER.getLeft().get(), "Framed Storage Controller");
        this.add(FunctionalStorage.FRAMED_CONTROLLER_EXTENSION.getLeft().get(), "Framed Controller Access Point");
        this.add("storageupgrade.desc.item", "Multiplies the block item storage by ");
        this.add("storageupgrade.desc.fluid", "Multiplies the block fluid storage by ");
        this.add("storageupgrade.desc.range", "Increases controller radius by %s blocks");
        for (StorageUpgradeItem.StorageTier storageTier : FunctionalStorage.STORAGE_UPGRADES.keySet()) {
            this.add(FunctionalStorage.STORAGE_UPGRADES.get(storageTier).get(), WordUtils.capitalize(storageTier.name().replace("_", " ").toLowerCase()) + (storageTier == StorageUpgradeItem.StorageTier.IRON ? " Downgrade" : " Upgrade"));
        }
        this.add(FunctionalStorage.COLLECTOR_UPGRADE.get(), WordUtils.capitalize(ForgeRegistries.ITEMS.getKey(FunctionalStorage.COLLECTOR_UPGRADE.get()).getPath().replace('_', ' ').toLowerCase()));
        this.add(FunctionalStorage.PULLING_UPGRADE.get(), WordUtils.capitalize(ForgeRegistries.ITEMS.getKey(FunctionalStorage.PULLING_UPGRADE.get()).getPath().replace('_', ' ').toLowerCase()));
        this.add(FunctionalStorage.PUSHING_UPGRADE.get(), WordUtils.capitalize(ForgeRegistries.ITEMS.getKey(FunctionalStorage.PUSHING_UPGRADE.get()).getPath().replace('_', ' ').toLowerCase()));
        this.add(FunctionalStorage.VOID_UPGRADE.get(), WordUtils.capitalize(ForgeRegistries.ITEMS.getKey(FunctionalStorage.VOID_UPGRADE.get()).getPath().replace('_', ' ').toLowerCase()));
        this.add(FunctionalStorage.REDSTONE_UPGRADE.get(), WordUtils.capitalize(ForgeRegistries.ITEMS.getKey(FunctionalStorage.REDSTONE_UPGRADE.get()).getPath().replace('_', ' ').toLowerCase()));
        this.add(FunctionalStorage.CREATIVE_UPGRADE.get(), WordUtils.capitalize(ForgeRegistries.ITEMS.getKey(FunctionalStorage.CREATIVE_UPGRADE.get()).getPath().replace('_', ' ').toLowerCase()));
        this.add(FunctionalStorage.ARMORY_CABINET.getLeft().get(), "Armory Cabinet");
        this.add(FunctionalStorage.CONFIGURATION_TOOL.get(), "Configuration Tool");
        this.add("item.utility.downgrade", "Downgrades the slots to a max of 64 items");
        this.add("item.utility.direction", "Direction: ");
        this.add("item.utility.direction.desc", "Right click in a GUI to change direction");
        this.add("configurationtool.configmode", "Config Mode: ");
        this.add("configurationtool.configmode.locking", "Locking");
        this.add("configurationtool.configmode.toggle_numbers", "Hide/Show Amounts");
        this.add("configurationtool.configmode.toggle_render", "Hide/Show Item/Fluid Renders");
        this.add("configurationtool.configmode.toggle_upgrades", "Hide/Show Upgrade Renders");
        this.add("configurationtool.configmode.indicator", "Swap indicator modes");
        this.add("configurationtool.configmode.indicator.mode_0", "Hidden");
        this.add("configurationtool.configmode.indicator.mode_1", "Show progress bar");
        this.add("configurationtool.configmode.indicator.mode_2", "Show progress bar only when full");
        this.add("configurationtool.configmode.indicator.mode_3", "Show progress bar only when full without background");
        this.add("configurationtool.use", "Sneak + Right Click in the air to change modes. Right click a drawer to toggle the option.");
        this.add("upgrade.type", "Type: ");
        this.add("upgrade.type.storage", "Storage");
        this.add("upgrade.type.utility", "Utility");
        this.add("linkingtool.ender.frequency", "Frequency: ");
        this.add("linkingtool.ender.clear", "Sneak + Right Click in the air to clear frequency.");
        this.add("drawer.block.contents", "Contents: ");
        this.add("frameddrawer.use", "How 2 Change Texture: \nInside a crafting window place the block you want use the texture of for the outside of the drawer in the first slot of the crafting window, on the second slot put the block that will be used for the texture on the inside of the framed drawer and on the third slot put a framed drawer. You can change the drawer divider texture by adding a block into the 4th slot\n");
        this.add("item.utility.slot", "Slot: ");
        this.add("item.utility.slot.desc", "Right click in a GUI to change slot");
        this.add("gui.functionalstorage.item", "Item: ");
        this.add("gui.functionalstorage.amount", "Amount: ");
        this.add("gui.functionalstorage.slot", "Slot: ");
        this.add("gui.functionalstorage.fluid", "Fluid: ");
        this.add("gui.functionalstorage.open_gui", "Right Click while Crouching to open the GUI");
        this.add("gui.functionalstorage.storage", "Storage");
        this.add("gui.functionalstorage.utility", "Utility");
        this.add("gui.functionalstorage.storage_range", "Range");
    }
}
