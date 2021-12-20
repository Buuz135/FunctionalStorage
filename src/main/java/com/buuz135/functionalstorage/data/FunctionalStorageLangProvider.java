package com.buuz135.functionalstorage.data;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.DrawerBlock;
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
                this.add(drawerBlock, WordUtils.capitalize(drawerBlock.getWoodType().getName().replace('_', ' ').toLowerCase()) + " Drawer (" +drawerBlock.getType().getSlots() + "x" + drawerBlock.getType().getSlots() + ")");
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
        this.add("linkingtool.use", "Sneak + Right Click in the air to change modes. Right Click in the air to change actions. Right click a controller to setup the tool then use it nearby drawers to link.");
        this.add("linkingtool.linkingaction.add", "Add");
        this.add("linkingtool.linkingaction.remove", "Remove");
        this.add(FunctionalStorage.LINKING_TOOL.get(), "Linking Tool");
        this.add(FunctionalStorage.DRAWER_CONTROLLER.get(), "Storage Controller");
    }
}
