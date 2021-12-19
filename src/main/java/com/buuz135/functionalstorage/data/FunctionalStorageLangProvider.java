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
    }
}
