package com.buuz135.functionalstorage;

import com.buuz135.functionalstorage.block.*;
import com.buuz135.functionalstorage.client.CompactingDrawerRenderer;
import com.buuz135.functionalstorage.client.ControllerRenderer;
import com.buuz135.functionalstorage.client.DrawerRenderer;
import com.buuz135.functionalstorage.client.EnderDrawerRenderer;
import com.buuz135.functionalstorage.data.FunctionalStorageBlockTagsProvider;
import com.buuz135.functionalstorage.data.FunctionalStorageBlockstateProvider;
import com.buuz135.functionalstorage.data.FunctionalStorageItemTagsProvider;
import com.buuz135.functionalstorage.data.FunctionalStorageLangProvider;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.buuz135.functionalstorage.network.EnderDrawerSyncMessage;
import com.buuz135.functionalstorage.util.DrawerWoodType;
import com.buuz135.functionalstorage.util.IWoodType;
import com.buuz135.functionalstorage.util.StorageTags;
import com.hrznstudio.titanium.block.BasicBlock;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.datagenerator.loot.TitaniumLootTableProvider;
import com.hrznstudio.titanium.datagenerator.model.BlockItemModelGeneratorProvider;
import com.hrznstudio.titanium.event.handler.EventManager;
import com.hrznstudio.titanium.module.ModuleController;
import com.hrznstudio.titanium.network.NetworkHandler;
import com.hrznstudio.titanium.recipe.generator.TitaniumRecipeProvider;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import com.hrznstudio.titanium.tab.AdvancedTitaniumTab;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.UpgradeRecipeBuilder;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("functionalstorage")
public class FunctionalStorage extends ModuleController {

    public static String MOD_ID = "functionalstorage";
    public static NetworkHandler NETWORK = new NetworkHandler(MOD_ID);

    static {
        NETWORK.registerMessage(EnderDrawerSyncMessage.class);
    }

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public static List<IWoodType> WOOD_TYPES = new ArrayList<>();

    public static HashMap<DrawerType, List<RegistryObject<Block>>> DRAWER_TYPES = new HashMap<>();
    public static RegistryObject<Block> COMPACTING_DRAWER;
    public static RegistryObject<Block> DRAWER_CONTROLLER;
    public static RegistryObject<Block> ARMORY_CABINET;
    public static RegistryObject<Block> ENDER_DRAWER;

    public static RegistryObject<Item> LINKING_TOOL;
    public static HashMap<StorageUpgradeItem.StorageTier, RegistryObject<Item>> STORAGE_UPGRADES = new HashMap<>();
    public static RegistryObject<Item> COLLECTOR_UPGRADE;
    public static RegistryObject<Item> PULLING_UPGRADE;
    public static RegistryObject<Item> PUSHING_UPGRADE;
    public static RegistryObject<Item> VOID_UPGRADE;
    public static RegistryObject<Item> CONFIGURATION_TOOL;

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
        DRAWER_CONTROLLER = getRegistries().register(Block.class, "storage_controller", DrawerControllerBlock::new);
        LINKING_TOOL = getRegistries().register(Item.class, "linking_tool", LinkingToolItem::new);
        for (StorageUpgradeItem.StorageTier value : StorageUpgradeItem.StorageTier.values()) {
            STORAGE_UPGRADES.put(value, getRegistries().register(Item.class, value.name().toLowerCase(Locale.ROOT) + (value == StorageUpgradeItem.StorageTier.IRON ? "_downgrade" : "_upgrade"), () -> new StorageUpgradeItem(value)));
        }
        COLLECTOR_UPGRADE = getRegistries().register(Item.class, "collector_upgrade", () -> new UpgradeItem(new Item.Properties(), UpgradeItem.Type.UTILITY));
        PULLING_UPGRADE = getRegistries().register(Item.class, "puller_upgrade", () -> new UpgradeItem(new Item.Properties(), UpgradeItem.Type.UTILITY));
        PUSHING_UPGRADE = getRegistries().register(Item.class, "pusher_upgrade", () -> new UpgradeItem(new Item.Properties(), UpgradeItem.Type.UTILITY));
        VOID_UPGRADE = getRegistries().register(Item.class, "void_upgrade", () -> new UpgradeItem(new Item.Properties(), UpgradeItem.Type.UTILITY));
        ARMORY_CABINET = getRegistries().register(Block.class, "armory_cabinet", ArmoryCabinetBlock::new);
        CONFIGURATION_TOOL = getRegistries().register(Item.class, "configuration_tool", ConfigurationToolItem::new);
        ENDER_DRAWER = getRegistries().register(Block.class, "ender_drawer", EnderDrawerBlock::new);
    }

    public enum DrawerType {
        X_1(1, 32 * 64, "1x1"),
        X_2(2, 16 * 64, "1x2"),
        X_4(4, 8 * 64, "2x2");

        private final int slots;
        private final int slotAmount;
        private final String displayName;

        private DrawerType(int slots, int slotAmount, String displayName) {
            this.slots = slots;
            this.slotAmount = slotAmount;
            this.displayName = displayName;
        }

        public int getSlots() {
            return slots;
        }

        public int getSlotAmount() {
            return slotAmount;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void onClient() {
        EventManager.mod(EntityRenderersEvent.RegisterRenderers.class).process(registerRenderers -> {
            for (DrawerType value : DrawerType.values()) {
                DRAWER_TYPES.get(value).forEach(blockRegistryObject -> {
                    registerRenderers.registerBlockEntityRenderer(((BasicTileBlock) blockRegistryObject.get()).getTileEntityType(), p_173571_ -> new DrawerRenderer());
                });
            }
            registerRenderers.registerBlockEntityRenderer(((BasicTileBlock) COMPACTING_DRAWER.get()).getTileEntityType(), p_173571_ -> new CompactingDrawerRenderer());
            registerRenderers.registerBlockEntityRenderer(((BasicTileBlock) DRAWER_CONTROLLER.get()).getTileEntityType(), p -> new ControllerRenderer());
            registerRenderers.registerBlockEntityRenderer(((BasicTileBlock) ENDER_DRAWER.get()).getTileEntityType(), p_173571_ -> new EnderDrawerRenderer());
        }).subscribe();
        EventManager.mod(ColorHandlerEvent.Item.class).process(item -> {
            item.getItemColors().register((stack, tint) -> {
                CompoundTag tag = stack.getOrCreateTag();
                LinkingToolItem.LinkingMode linkingMode = LinkingToolItem.getLinkingMode(stack);
                LinkingToolItem.ActionMode linkingAction = LinkingToolItem.getActionMode(stack);
                if (tint != 0 && stack.getOrCreateTag().contains(LinkingToolItem.NBT_ENDER)){
                    return new Color(44, 150, 88).getRGB();
                }
                if (tint == 3 && tag.contains(LinkingToolItem.NBT_CONTROLLER)) {
                    return Color.RED.getRGB();
                }
                if (tint == 1) {
                    return linkingMode.getColor().getValue();
                }
                if (tint == 2) {
                    return linkingAction.getColor().getValue();
                }
                return 0xffffff;
            }, LINKING_TOOL.get());
            item.getItemColors().register((stack, tint) -> {
                ConfigurationToolItem.ConfigurationAction action = ConfigurationToolItem.getAction(stack);
                if (tint == 1) {
                    return action.getColor().getValue();
                }
                return 0xffffff;
            }, CONFIGURATION_TOOL.get());
        }).subscribe();
        EventManager.mod(FMLClientSetupEvent.class).process(event -> {
            for (DrawerType value : DrawerType.values()) {
                for (RegistryObject<Block> blockRegistryObject : DRAWER_TYPES.get(value)) {
                    ItemBlockRenderTypes.setRenderLayer(blockRegistryObject.get(), RenderType.cutout());
                }
            }
            ItemBlockRenderTypes.setRenderLayer(COMPACTING_DRAWER.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ENDER_DRAWER.get(), RenderType.cutout());
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
                TitaniumShapedRecipeBuilder.shapedRecipe(STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.IRON).get())
                        .pattern("III").pattern("IDI").pattern("III")
                        .define('I', Tags.Items.INGOTS_IRON)
                        .define('D', StorageTags.DRAWER)
                        .save(consumer);
                TitaniumShapedRecipeBuilder.shapedRecipe(VOID_UPGRADE.get())
                        .pattern("III").pattern("IDI").pattern("III")
                        .define('I', Tags.Items.OBSIDIAN)
                        .define('D', StorageTags.DRAWER)
                        .save(consumer);
                TitaniumShapedRecipeBuilder.shapedRecipe(CONFIGURATION_TOOL.get())
                        .pattern("PPG").pattern("PDG").pattern("PEP")
                        .define('P', Items.PAPER)
                        .define('G', Tags.Items.INGOTS_GOLD)
                        .define('D', StorageTags.DRAWER)
                        .define('E', Items.EMERALD)
                        .save(consumer);
                TitaniumShapedRecipeBuilder.shapedRecipe(LINKING_TOOL.get())
                        .pattern("PPG").pattern("PDG").pattern("PEP")
                        .define('P', Items.PAPER)
                        .define('G', Tags.Items.INGOTS_GOLD)
                        .define('D', StorageTags.DRAWER)
                        .define('E', Items.DIAMOND)
                        .save(consumer);
                TitaniumShapedRecipeBuilder.shapedRecipe(STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.COPPER).get())
                        .pattern("IBI").pattern("CDC").pattern("IBI")
                        .define('I', Items.COPPER_INGOT)
                        .define('B', Items.COPPER_BLOCK)
                        .define('C', Tags.Items.CHESTS_WOODEN)
                        .define('D', StorageTags.DRAWER)
                        .save(consumer);
                TitaniumShapedRecipeBuilder.shapedRecipe(STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.GOLD).get())
                        .pattern("IBI").pattern("CDC").pattern("BIB")
                        .define('I', Tags.Items.INGOTS_GOLD)
                        .define('B', Tags.Items.STORAGE_BLOCKS_GOLD)
                        .define('C', Tags.Items.CHESTS_WOODEN)
                        .define('D', STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.COPPER).get())
                        .save(consumer);
                TitaniumShapedRecipeBuilder.shapedRecipe(STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.DIAMOND).get())
                        .pattern("IBI").pattern("CDC").pattern("BBB")
                        .define('I', Tags.Items.GEMS_DIAMOND)
                        .define('B', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                        .define('C', Tags.Items.CHESTS_WOODEN)
                        .define('D', STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.GOLD).get())
                        .save(consumer);
                UpgradeRecipeBuilder.smithing(Ingredient.of(STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.DIAMOND).get()), Ingredient.of(Items.NETHERITE_INGOT), STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.NETHERITE).get())
                        .unlocks("has_netherite_ingot", has(Items.NETHERITE_INGOT))
                        .save(consumer, STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.NETHERITE).get().getRegistryName());
                TitaniumShapedRecipeBuilder.shapedRecipe(DRAWER_CONTROLLER.get())
                        .pattern("IBI").pattern("CDC").pattern("IBI")
                        .define('I', Tags.Items.STONE)
                        .define('B', Tags.Items.STORAGE_BLOCKS_QUARTZ)
                        .define('C', StorageTags.DRAWER)
                        .define('D', Items.COMPARATOR)
                        .save(consumer);
                TitaniumShapedRecipeBuilder.shapedRecipe(ARMORY_CABINET.get())
                        .pattern("ICI").pattern("CDC").pattern("IBI")
                        .define('I', Tags.Items.STONE)
                        .define('B', Tags.Items.INGOTS_NETHERITE)
                        .define('C', StorageTags.DRAWER)
                        .define('D', Items.COMPARATOR)
                        .save(consumer);
                TitaniumShapedRecipeBuilder.shapedRecipe(PULLING_UPGRADE.get())
                        .pattern("ICI").pattern("IDI").pattern("IBI")
                        .define('I', Tags.Items.STONE)
                        .define('B', Tags.Items.DUSTS_REDSTONE)
                        .define('C', Items.HOPPER)
                        .define('D', StorageTags.DRAWER)
                        .save(consumer);
                TitaniumShapedRecipeBuilder.shapedRecipe(PUSHING_UPGRADE.get())
                        .pattern("IBI").pattern("IDI").pattern("IRI")
                        .define('I', Tags.Items.STONE)
                        .define('B', Tags.Items.DUSTS_REDSTONE)
                        .define('R', Items.HOPPER)
                        .define('D', StorageTags.DRAWER)
                        .save(consumer);
                TitaniumShapedRecipeBuilder.shapedRecipe(COLLECTOR_UPGRADE.get())
                        .pattern("IBI").pattern("RDR").pattern("IBI")
                        .define('I', Tags.Items.STONE)
                        .define('B', Items.HOPPER)
                        .define('R', Tags.Items.DUSTS_REDSTONE)
                        .define('D', StorageTags.DRAWER)
                        .save(consumer);
            }
        });
        event.getGenerator().addProvider(new FunctionalStorageItemTagsProvider(event.getGenerator(), new BlockTagsProvider(event.getGenerator()), MOD_ID, event.getExistingFileHelper()));
        event.getGenerator().addProvider(new FunctionalStorageLangProvider(event.getGenerator(), MOD_ID, "en_us"));
        event.getGenerator().addProvider(new FunctionalStorageBlockTagsProvider(event.getGenerator(), MOD_ID, event.getExistingFileHelper()));
        event.getGenerator().addProvider(new ItemModelProvider(event.getGenerator(), MOD_ID, event.getExistingFileHelper()) {
            @Override
            protected void registerModels() {
                for (StorageUpgradeItem.StorageTier storageTier : STORAGE_UPGRADES.keySet()) {
                    item(STORAGE_UPGRADES.get(storageTier).get());
                }
                item(COLLECTOR_UPGRADE.get());
                item(PULLING_UPGRADE.get());
                item(PUSHING_UPGRADE.get());
                item(VOID_UPGRADE.get());
            }

            private void item(Item item) {
                singleTexture(item.getRegistryName().getPath(), new ResourceLocation("minecraft:item/generated"), "layer0", new ResourceLocation(MOD_ID, "items/" + item.getRegistryName().getPath()));
            }
        });
        event.getGenerator().addProvider(new BlockModelProvider(event.getGenerator(), MOD_ID, event.getExistingFileHelper()) {
            @Override
            protected void registerModels() {
                for (DrawerType value : DrawerType.values()) {
                    for (RegistryObject<Block> blockRegistryObject : DRAWER_TYPES.get(value)) {
                        withExistingParent(blockRegistryObject.get().getRegistryName().getPath() + "_locked", modLoc(blockRegistryObject.get().getRegistryName().getPath()))
                                .texture("lock_icon", modLoc("blocks/lock"));
                    }
                }
                withExistingParent(COMPACTING_DRAWER.get().getRegistryName().getPath() + "_locked", modLoc(COMPACTING_DRAWER.get().getRegistryName().getPath()))
                        .texture("lock_icon", modLoc("blocks/lock"));
                withExistingParent(ENDER_DRAWER.get().getRegistryName().getPath() + "_locked", modLoc(ENDER_DRAWER.get().getRegistryName().getPath()))
                        .texture("lock_icon", modLoc("blocks/lock"));
            }
        });
    }
}
