package com.buuz135.functionalstorage;

import com.buuz135.functionalstorage.block.CompactingDrawerBlock;
import com.buuz135.functionalstorage.block.DrawerBlock;
import com.buuz135.functionalstorage.client.CompactingDrawerRenderer;
import com.buuz135.functionalstorage.client.DrawerRenderer;
import com.buuz135.functionalstorage.data.FunctionalStorageBlockstateProvider;
import com.buuz135.functionalstorage.data.FunctionalStorageLangProvider;
import com.buuz135.functionalstorage.data.FunctionalStorageTagsProvider;
import com.buuz135.functionalstorage.util.DrawerWoodType;
import com.buuz135.functionalstorage.util.IWoodType;
import com.hrznstudio.titanium.block.BasicBlock;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.datagenerator.loot.TitaniumLootTableProvider;
import com.hrznstudio.titanium.datagenerator.model.BlockItemModelGeneratorProvider;
import com.hrznstudio.titanium.event.handler.EventManager;
import com.hrznstudio.titanium.module.ModuleController;
import com.hrznstudio.titanium.recipe.generator.TitaniumRecipeProvider;
import com.hrznstudio.titanium.tab.AdvancedTitaniumTab;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.tags.BlockTagsProvider;
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
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("functionalstorage")
public class FunctionalStorage extends ModuleController {

    public static String MOD_ID = "functionalstorage";

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public static List<IWoodType> WOOD_TYPES = new ArrayList<>();
    public static HashMap<DrawerType, List<RegistryObject<Block>>> DRAWER_TYPES = new HashMap<>();
    public static RegistryObject<Block> COMPACTING_DRAWER;

    public static AdvancedTitaniumTab TAB = new AdvancedTitaniumTab("functionalstorage", true);

    public FunctionalStorage() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::onClient);
    }


    @Override
    protected void initModules() {
        WOOD_TYPES.addAll(List.of(DrawerWoodType.values()));
        for (DrawerType value : DrawerType.values()) {
            for (IWoodType woodType : WOOD_TYPES) {
                String name = woodType.getName() + "_" + value.getSlots();
                DRAWER_TYPES.computeIfAbsent(value, drawerType -> new ArrayList<>()).add(getRegistries().register(Block.class, name, () -> new DrawerBlock(woodType, value)));
            }
            DRAWER_TYPES.get(value).forEach(blockRegistryObject -> TAB.addIconStacks(() -> new ItemStack(blockRegistryObject.get())));
        }
        COMPACTING_DRAWER = getRegistries().register(Block.class, "compacting_drawer", () -> new CompactingDrawerBlock("compacting_drawer"));
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
            registerRenderers.registerBlockEntityRenderer(((BasicTileBlock)COMPACTING_DRAWER.get()).getTileEntityType(), p_173571_ -> new CompactingDrawerRenderer());

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
        event.getGenerator().addProvider(new TitaniumLootTableProvider(event.getGenerator(), blocksToProcess));
        event.getGenerator().addProvider(new TitaniumRecipeProvider(event.getGenerator()) {
            @Override
            public void register(Consumer<FinishedRecipe> consumer) {
                blocksToProcess.get().stream().map(block -> (BasicBlock) block).forEach(basicBlock -> basicBlock.registerRecipe(consumer));
            }
        });
        event.getGenerator().addProvider(new FunctionalStorageTagsProvider(event.getGenerator(),new BlockTagsProvider(event.getGenerator()),  MOD_ID, event.getExistingFileHelper()));
        event.getGenerator().addProvider(new FunctionalStorageLangProvider(event.getGenerator(), MOD_ID, "en_us"));
    }
}
