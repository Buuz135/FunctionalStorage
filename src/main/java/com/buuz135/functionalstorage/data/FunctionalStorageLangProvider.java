package com.buuz135.functionalstorage.data;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.DrawerBlock;
import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import com.hrznstudio.titanium.module.BlockWithTile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.apache.commons.lang3.text.WordUtils;

import java.util.stream.Collectors;

public class FunctionalStorageLangProvider extends LanguageProvider {

    public FunctionalStorageLangProvider(DataGenerator gen, String modid, String locale) {
        super(gen.getPackOutput(), modid, locale);
    }

    @Override
    protected void addTranslations() {
        this.add("itemGroup.functionalstorage", "Functional Storage");
        for (FunctionalStorage.DrawerType drawerType : FunctionalStorage.DRAWER_TYPES.keySet()) {
            for (var blockRegistryObject : FunctionalStorage.DRAWER_TYPES.get(drawerType).stream().map(BlockWithTile::block).collect(Collectors.toList())) {
                DrawerBlock drawerBlock = (DrawerBlock) blockRegistryObject.get();
                this.add(drawerBlock, WordUtils.capitalize(drawerBlock.getWoodType().getName().replace('_', ' ').toLowerCase()) + " Drawer (" + drawerBlock.getType().getDisplayName() + ")");
            }
        }
        this.add(FunctionalStorage.FLUID_DRAWER_1.getBlock(), "Fluid Drawer (" + FunctionalStorage.DrawerType.X_1.getDisplayName() + ")");
        this.add(FunctionalStorage.FLUID_DRAWER_2.getBlock(), "Fluid Drawer (" + FunctionalStorage.DrawerType.X_2.getDisplayName() + ")");
        this.add(FunctionalStorage.FLUID_DRAWER_4.getBlock(), "Fluid Drawer (" + FunctionalStorage.DrawerType.X_4.getDisplayName() + ")");
        this.add(FunctionalStorage.FRAMED_FLUID_DRAWER_1.getBlock(), "Framed Fluid Drawer (" + FunctionalStorage.DrawerType.X_1.getDisplayName() + ")");
        this.add(FunctionalStorage.FRAMED_FLUID_DRAWER_2.getBlock(), "Framed Fluid Drawer (" + FunctionalStorage.DrawerType.X_2.getDisplayName() + ")");
        this.add(FunctionalStorage.FRAMED_FLUID_DRAWER_4.getBlock(), "Framed Fluid Drawer (" + FunctionalStorage.DrawerType.X_4.getDisplayName() + ")");

        this.add(FunctionalStorage.SIMPLE_COMPACTING_DRAWER.getBlock(), "Simple Compacting Drawer");
        this.add(FunctionalStorage.COMPACTING_DRAWER.getBlock(), "Compacting Drawer");
        this.add(FunctionalStorage.FRAMED_SIMPLE_COMPACTING_DRAWER.getBlock(), "Framed Simple Compacting Drawer");
        this.add(FunctionalStorage.FRAMED_COMPACTING_DRAWER.getBlock(), "Framed Compacting Drawer");

        this.add(FunctionalStorage.ENDER_DRAWER.getBlock(), "Ender Drawer");
        this.add(FunctionalStorage.CONTROLLER_EXTENSION.getBlock(), "Controller Access Point");
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
        this.add(FunctionalStorage.DRAWER_CONTROLLER.getBlock(), "Storage Controller");
        this.add(FunctionalStorage.FRAMED_DRAWER_CONTROLLER.getBlock(), "Framed Storage Controller");
        this.add(FunctionalStorage.FRAMED_CONTROLLER_EXTENSION.getBlock(), "Framed Controller Access Point");

        this.add("storageupgrade.desc.modify_factor_mult", "Multiplies %s by %s");
        this.add("storageupgrade.desc.modify_factor_div", "Decreases %s by %s");
        this.add("storageupgrade.desc.set_base", "Sets the base %s to %s");
        this.add("storageupgrade.desc.modify_base_inc", "Increases the base %s with %s");
        this.add("storageupgrade.desc.modify_base_dec", "Decreases the base %s with %s");
        this.add("functionalupgrade.desc.execute_every_tick", "Every %s ticks:");
        this.add("functionalupgrade.desc.generate_fluid", "Generates %s mb of %s");
        this.add("functionalupgrade.desc.generate_item", "Generates %s %s");


        this.add("storageupgrade.obj.item_storage", "the block item storage");
        this.add("storageupgrade.obj.fluid_storage", "the block fluid storage");
        this.add("storageupgrade.obj.controller_range", "controller radius");

        for (StorageUpgradeItem.StorageTier storageTier : FunctionalStorage.STORAGE_UPGRADES.keySet()) {
            this.add(FunctionalStorage.STORAGE_UPGRADES.get(storageTier).get(), WordUtils.capitalize(storageTier.name().replace("_", " ").toLowerCase()) + (storageTier == StorageUpgradeItem.StorageTier.IRON ? " Downgrade" : " Upgrade"));
        }
        this.add(FunctionalStorage.COLLECTOR_UPGRADE.get(), WordUtils.capitalize(BuiltInRegistries.ITEM.getKey(FunctionalStorage.COLLECTOR_UPGRADE.get()).getPath().replace('_', ' ').toLowerCase()));
        this.add(FunctionalStorage.PULLING_UPGRADE.get(), WordUtils.capitalize(BuiltInRegistries.ITEM.getKey(FunctionalStorage.PULLING_UPGRADE.get()).getPath().replace('_', ' ').toLowerCase()));
        this.add(FunctionalStorage.PUSHING_UPGRADE.get(), WordUtils.capitalize(BuiltInRegistries.ITEM.getKey(FunctionalStorage.PUSHING_UPGRADE.get()).getPath().replace('_', ' ').toLowerCase()));
        this.add(FunctionalStorage.VOID_UPGRADE.get(), WordUtils.capitalize(BuiltInRegistries.ITEM.getKey(FunctionalStorage.VOID_UPGRADE.get()).getPath().replace('_', ' ').toLowerCase()));
        this.add(FunctionalStorage.REDSTONE_UPGRADE.get(), WordUtils.capitalize(BuiltInRegistries.ITEM.getKey(FunctionalStorage.REDSTONE_UPGRADE.get()).getPath().replace('_', ' ').toLowerCase()));
        this.add(FunctionalStorage.CREATIVE_UPGRADE.get(), WordUtils.capitalize(BuiltInRegistries.ITEM.getKey(FunctionalStorage.CREATIVE_UPGRADE.get()).getPath().replace('_', ' ').toLowerCase()));
        this.add(FunctionalStorage.ARMORY_CABINET.getBlock(), "Armory Cabinet");
        this.add(FunctionalStorage.CONFIGURATION_TOOL.get(), "Configuration Tool");
        this.add(FunctionalStorage.DRIPPING_UPGRADE.get(), WordUtils.capitalize(BuiltInRegistries.ITEM.getKey(FunctionalStorage.DRIPPING_UPGRADE.get()).getPath().replace('_', ' ').toLowerCase()));
        this.add(FunctionalStorage.WATER_GENERATOR_UPGRADE.get(), WordUtils.capitalize(BuiltInRegistries.ITEM.getKey(FunctionalStorage.WATER_GENERATOR_UPGRADE.get()).getPath().replace('_', ' ').toLowerCase()));
        this.add(FunctionalStorage.OBSIDIAN_UPGRADE.get(), "Obsidian Generator Upgrade");


        this.add("item.utility.downgrade", "Downgrades the slots to a max of 64 items");
        this.add("item.utility.direction", "Direction: ");
        this.add("item.utility.direction.desc", "Right click in a GUI to change direction");
        this.add("configurationtool.configmode", "Config Mode: ");
        this.add("configurationtool.configmode.locking", "Locking");
        this.add("configurationtool.configmode.swapped", "Swapped mode to ");
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
        this.add("linkingtool.multiple_drawer.linked", "Linked drawers to the controller");
        this.add("linkingtool.multiple_drawer.removed", "Removed drawers from the controller");
        this.add("linkingtool.single_drawer.linked", "Linked drawer to the controller");
        this.add("linkingtool.single_drawer.removed", "Removed drawer from the controller");
        this.add("linkingtool.controller.configured", "Controller configured to the tool");
        this.add("linkingtool.drawer.clear", "Cleared drawer frequency");
        this.add("linkingtool.ender.changed", "Changed drawer frequency");
        this.add("linkingtool.ender.stored", "Stored frequency in the tool");
        this.add("linkingtool.ender.warning", "Cannot change frequency, there are items in the drawer. Sneak + Right Click again to ignore this safety");
        this.add("linkingtool.linkingaction.swapped", "Swapped action to %s");
        this.add("linkingtool.linkingmode.swapped", "Swapped mode to %s");
        this.add("drawer.block.contents", "Contents: ");
        this.add("drawer.block.multiplier", "Storage multiplier: %s");
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
        this.add("gui.functionalstorage.empty", "Empty");

        this.add("drawer_upgrade.functionalstorage.sides.up", "up");
        this.add("drawer_upgrade.functionalstorage.sides.down", "down");
        this.add("drawer_upgrade.functionalstorage.sides.left", "left");
        this.add("drawer_upgrade.functionalstorage.sides.right", "right");
        this.add("drawer_upgrade.functionalstorage.sides.front", "front");
        this.add("drawer_upgrade.functionalstorage.sides.back", "back");

        this.add("drawer_upgrade.functionalstorage.void.item", "Voids excess items");
        this.add("drawer_upgrade.functionalstorage.void.fluid", "Voids excess fluids");
        this.add("drawer_upgrade.functionalstorage.pull.item", "Pulls items: %s");
        this.add("drawer_upgrade.functionalstorage.pull.fluid", "Pulls fluids: %s");
        this.add("drawer_upgrade.functionalstorage.push.item", "Pushes items: %s");
        this.add("drawer_upgrade.functionalstorage.push.fluid", "Pushes fluids: %s");
        this.add("drawer_upgrade.functionalstorage.collect.item", "Collects item entities: %s");
        this.add("drawer_upgrade.functionalstorage.collect.fluid", "Picks up fluids: %s");

        this.add("drawer_upgrade.functionalstorage.redstone", "Emitting redstone signal for slot %s");
        this.add("drawer_upgrade.functionalstorage.relative_direction", "Relative direction: %s");

        this.add("config.jade.plugin_functionalstorage.armory", "Functional Storage Armory");
        this.add("config.jade.plugin_functionalstorage.drawer", "Functional Storage Drawers");

        this.add("drawer.block.upgrades", "Upgrades:");
        this.add("drawer.block.upgrades.is_creative", "Creative");
        this.add("drawer.block.upgrades.is_void", "Void");
        this.add("drawer.block.upgrades.none", "None");
    }
}
