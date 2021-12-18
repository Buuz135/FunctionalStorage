package com.buuz135.functionalstorage;

import com.buuz135.functionalstorage.block.DrawerBlock;
import com.buuz135.functionalstorage.client.DrawerRenderer;
import com.buuz135.functionalstorage.data.FunctionalStorageBlockstateProvider;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.datagenerator.model.BlockItemModelGeneratorProvider;
import com.hrznstudio.titanium.event.handler.EventManager;
import com.hrznstudio.titanium.module.ModuleController;
import com.hrznstudio.titanium.tab.AdvancedTitaniumTab;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("functionalstorage")
public class FunctionalStorage extends ModuleController {

    public static String MOD_ID = "functionalstorage";

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public static List<String> WOOD_TYPES = new ArrayList<>();
    public static HashMap<DrawerType, List<RegistryObject<Block>>> DRAWER_TYPES = new HashMap<>();
    public static AdvancedTitaniumTab TAB = new AdvancedTitaniumTab("functionalstorage", true);

    public FunctionalStorage() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::onClient);
    }

    public void addWood(Block block){
        WOOD_TYPES.add(block.getRegistryName().getPath());
    }


    @Override
    protected void initModules() {
        addWood(Blocks.OAK_WOOD);
        addWood(Blocks.SPRUCE_WOOD);
        addWood(Blocks.BIRCH_WOOD);
        addWood(Blocks.JUNGLE_WOOD);
        addWood(Blocks.ACACIA_WOOD);
        addWood(Blocks.CRIMSON_HYPHAE);
        addWood(Blocks.WARPED_HYPHAE);

        for (DrawerType value : DrawerType.values()) {
            for (String woodType : WOOD_TYPES) {
                String name = woodType + "_" + value.name().toLowerCase(Locale.ROOT);
                DRAWER_TYPES.computeIfAbsent(value, drawerType -> new ArrayList<>()).add(getRegistries().register(Block.class, name, () -> new DrawerBlock(name, value)));
            }
            DRAWER_TYPES.get(value).forEach(blockRegistryObject -> TAB.addIconStacks(() -> new ItemStack(blockRegistryObject.get())));
        }

    }

    public enum DrawerType{
        X_1(1, 32 * 64), X_2(2, 16 * 64), X_4(4, 8 * 64);

        private final int slots;
        private final int slotAmount;

        private DrawerType(int slots, int slotAmount){
            this.slots = slots;

            this.slotAmount = slotAmount;
        }

        public int getSlots() {
            return slots;
        }

        public int getSlotAmount() {
            return slotAmount;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void onClient(){
        EventManager.mod(EntityRenderersEvent.RegisterRenderers.class).process(registerRenderers -> {
            for (DrawerType value : DrawerType.values()) {
                DRAWER_TYPES.get(value).forEach(blockRegistryObject -> {
                  registerRenderers.registerBlockEntityRenderer(((BasicTileBlock)blockRegistryObject.get()).getTileEntityType(), p_173571_ -> new DrawerRenderer());
                });
            }
        }).subscribe();
    }

    @Override
    public void addDataProvider(GatherDataEvent event) {
        NonNullLazy<List<Block>> blocksToProcess = NonNullLazy.of(() ->
                ForgeRegistries.BLOCKS.getValues()
                        .stream()
                        .filter(basicBlock -> Optional.ofNullable(basicBlock.getRegistryName())
                                .map(ResourceLocation::getNamespace)
                                .filter(MOD_ID::equalsIgnoreCase)
                                .isPresent())
                        .collect(Collectors.toList())
        );
        event.getGenerator().addProvider(new BlockItemModelGeneratorProvider(event.getGenerator(), MOD_ID, blocksToProcess));
        event.getGenerator().addProvider(new FunctionalStorageBlockstateProvider(event.getGenerator(), event.getExistingFileHelper(), blocksToProcess));
    }
}
