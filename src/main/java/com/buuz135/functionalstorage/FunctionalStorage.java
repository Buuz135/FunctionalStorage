package com.buuz135.functionalstorage;

import com.buuz135.functionalstorage.block.ArmoryCabinetBlock;
import com.buuz135.functionalstorage.block.CompactingDrawerBlock;
import com.buuz135.functionalstorage.block.CompactingFramedDrawerBlock;
import com.buuz135.functionalstorage.block.ControllerExtensionBlock;
import com.buuz135.functionalstorage.block.Drawer;
import com.buuz135.functionalstorage.block.DrawerBlock;
import com.buuz135.functionalstorage.block.DrawerControllerBlock;
import com.buuz135.functionalstorage.block.EnderDrawerBlock;
import com.buuz135.functionalstorage.block.FluidDrawerBlock;
import com.buuz135.functionalstorage.block.FramedBlock;
import com.buuz135.functionalstorage.block.FramedControllerExtensionBlock;
import com.buuz135.functionalstorage.block.FramedDrawerBlock;
import com.buuz135.functionalstorage.block.FramedDrawerControllerBlock;
import com.buuz135.functionalstorage.block.FramedFluidDrawerBlock;
import com.buuz135.functionalstorage.block.FramedSimpleCompactingDrawerBlock;
import com.buuz135.functionalstorage.block.SimpleCompactingDrawerBlock;
import com.buuz135.functionalstorage.block.config.FunctionalStorageConfig;
import com.buuz135.functionalstorage.block.tile.CompactingDrawerTile;
import com.buuz135.functionalstorage.block.tile.CompactingFramedDrawerTile;
import com.buuz135.functionalstorage.block.tile.DrawerControllerTile;
import com.buuz135.functionalstorage.block.tile.DrawerTile;
import com.buuz135.functionalstorage.block.tile.EnderDrawerTile;
import com.buuz135.functionalstorage.block.tile.FluidDrawerTile;
import com.buuz135.functionalstorage.block.tile.FramedDrawerControllerTile;
import com.buuz135.functionalstorage.block.tile.FramedDrawerTile;
import com.buuz135.functionalstorage.block.tile.FramedFluidDrawerTile;
import com.buuz135.functionalstorage.block.tile.FramedSimpleCompactingDrawerTile;
import com.buuz135.functionalstorage.block.tile.SimpleCompactingDrawerTile;
import com.buuz135.functionalstorage.client.ClientSetup;
import com.buuz135.functionalstorage.client.CompactingDrawerRenderer;
import com.buuz135.functionalstorage.client.ControllerRenderer;
import com.buuz135.functionalstorage.client.DrawerRenderer;
import com.buuz135.functionalstorage.client.EnderDrawerRenderer;
import com.buuz135.functionalstorage.client.FluidDrawerRenderer;
import com.buuz135.functionalstorage.client.SimpleCompactingDrawerRenderer;
import com.buuz135.functionalstorage.client.loader.FramedModel;
import com.buuz135.functionalstorage.data.FunctionalStorageBlockTagsProvider;
import com.buuz135.functionalstorage.data.FunctionalStorageBlockstateProvider;
import com.buuz135.functionalstorage.data.FunctionalStorageItemTagsProvider;
import com.buuz135.functionalstorage.data.FunctionalStorageLangProvider;
import com.buuz135.functionalstorage.data.FunctionalStorageRecipesProvider;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.inventory.item.CompactingStackItemHandler;
import com.buuz135.functionalstorage.inventory.item.DrawerStackItemHandler;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.item.FSItem;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.buuz135.functionalstorage.item.component.AndBehavior;
import com.buuz135.functionalstorage.item.component.CollectFluidsBehavior;
import com.buuz135.functionalstorage.item.component.CollectItemEntitiesBehavior;
import com.buuz135.functionalstorage.item.component.DelegateToItemBehavior;
import com.buuz135.functionalstorage.item.component.EmitRedstoneBehavior;
import com.buuz135.functionalstorage.item.component.ExecuteEveryBehavior;
import com.buuz135.functionalstorage.item.component.FunctionalUpgradeBehavior;
import com.buuz135.functionalstorage.item.component.GenerateFluidBehavior;
import com.buuz135.functionalstorage.item.component.GenerateItemBehavior;
import com.buuz135.functionalstorage.item.component.MoveFluidsBehavior;
import com.buuz135.functionalstorage.item.component.MoveItemsBehavior;
import com.buuz135.functionalstorage.network.EnderDrawerSyncMessage;
import com.buuz135.functionalstorage.recipe.*;
import com.buuz135.functionalstorage.util.DrawerWoodType;
import com.buuz135.functionalstorage.util.IWoodType;
import com.buuz135.functionalstorage.util.NumberUtils;
import com.buuz135.functionalstorage.util.TooltipUtil;
import com.hrznstudio.titanium.datagenerator.loot.TitaniumLootTableProvider;
import com.hrznstudio.titanium.event.handler.EventManager;
import com.hrznstudio.titanium.module.BlockWithTile;
import com.hrznstudio.titanium.module.ModuleController;
import com.hrznstudio.titanium.nbthandler.NBTManager;
import com.hrznstudio.titanium.network.NetworkHandler;
import com.hrznstudio.titanium.recipe.serializer.GenericSerializer;
import com.hrznstudio.titanium.tab.TitaniumTab;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(FunctionalStorage.MOD_ID)
public class FunctionalStorage extends ModuleController {

    public final static String MOD_ID = "functionalstorage";
    public static NetworkHandler NETWORK = new NetworkHandler(MOD_ID);

    static {
        NETWORK.registerMessage("ender_drawer_sync", EnderDrawerSyncMessage.class);
    }

    // Directly reference a Mojang's logger.
    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();

    public static ConcurrentLinkedQueue<IWoodType> WOOD_TYPES = new ConcurrentLinkedQueue<>();

    public static HashMap<DrawerType, List<BlockWithTile>> DRAWER_TYPES = new HashMap<>();
    public static List<Block> FRAMED_BLOCKS;
    public static BlockWithTile COMPACTING_DRAWER;
    public static BlockWithTile DRAWER_CONTROLLER;
    public static BlockWithTile ARMORY_CABINET;
    public static BlockWithTile ENDER_DRAWER;
    public static BlockWithTile FRAMED_COMPACTING_DRAWER;
    public static BlockWithTile FLUID_DRAWER_1;
    public static BlockWithTile FLUID_DRAWER_2;
    public static BlockWithTile FLUID_DRAWER_4;
    public static BlockWithTile CONTROLLER_EXTENSION;
    public static BlockWithTile SIMPLE_COMPACTING_DRAWER;
    public static BlockWithTile FRAMED_DRAWER_CONTROLLER;
    public static BlockWithTile FRAMED_CONTROLLER_EXTENSION;
    public static BlockWithTile FRAMED_SIMPLE_COMPACTING_DRAWER;
    public static BlockWithTile FRAMED_FLUID_DRAWER_1;
    public static BlockWithTile FRAMED_FLUID_DRAWER_2;
    public static BlockWithTile FRAMED_FLUID_DRAWER_4;

    public static DeferredHolder<Item, Item> LINKING_TOOL;
    public static HashMap<StorageUpgradeItem.StorageTier, DeferredHolder<Item, Item>> STORAGE_UPGRADES = new HashMap<>();
    public static DeferredHolder<Item, Item> COLLECTOR_UPGRADE;
    public static DeferredHolder<Item, Item> PULLING_UPGRADE;
    public static DeferredHolder<Item, Item> PUSHING_UPGRADE;
    public static DeferredHolder<Item, Item> VOID_UPGRADE;
    public static DeferredHolder<Item, Item> CONFIGURATION_TOOL;
    public static DeferredHolder<Item, Item> REDSTONE_UPGRADE;
    public static DeferredHolder<Item, Item> CREATIVE_UPGRADE;

    public static DeferredHolder<Item, Item> OBSIDIAN_UPGRADE;

    public static DeferredHolder<Item, Item> DRIPPING_UPGRADE;
    public static DeferredHolder<Item, Item> WATER_GENERATOR_UPGRADE;

    public static TitaniumTab TAB = new TitaniumTab(com.buuz135.functionalstorage.util.Utils.resourceLocation(MOD_ID, "main"));

    public static Holder<RecipeSerializer<?>> CUSTOM_COMPACTING_RECIPE_SERIALIZER;
    public static Holder<RecipeType<?>> CUSTOM_COMPACTING_RECIPE_TYPE;
    public static Holder<RecipeSerializer<?>> FRAMED_RECIPE_SERIALIZER;
    public static Holder<RecipeSerializer<?>> COPY_COMPONENTS_SERIALIZER;
    public static Holder<RecipeType<?>> FRAMED_RECIPE_TYPE;

    public FunctionalStorage(Dist dist, IEventBus modBus, ModContainer container) {
        super(container);
        NeoForgeMod.enableMilkFluid();
        FSAttachments.DR.register(modBus);
        if (dist.isClient()) {
            Runnable runnable = this::onClient;
            runnable.run();
        }
        NBTManager.getInstance().scanTileClassForAnnotations(FramedFluidDrawerTile.class);
        NBTManager.getInstance().scanTileClassForAnnotations(FramedDrawerTile.class);
        NBTManager.getInstance().scanTileClassForAnnotations(CompactingFramedDrawerTile.class);
        NBTManager.getInstance().scanTileClassForAnnotations(FluidDrawerTile.class);
        NBTManager.getInstance().scanTileClassForAnnotations(SimpleCompactingDrawerTile.class);
        NBTManager.getInstance().scanTileClassForAnnotations(FramedSimpleCompactingDrawerTile.class);

        EventManager.forge(PlayerInteractEvent.LeftClickBlock.class)
                .process(event -> {
                    var state = event.getLevel().getBlockState(event.getPos());
                    if (event.getLevel().getBlockState(event.getPos()).getBlock() instanceof Drawer<?> drawer) {
                        final int hit = drawer.getHit(state, event.getLevel(), event.getEntity());
                        if (hit != -1) {
                            var be = drawer.getBlockEntityAt(event.getLevel(), event.getPos());
                            if (be != null) {
                                be.onClicked(event.getEntity(), hit);
                                event.setCanceled(true);
                            }
                        }
                    }
                })
                .subscribe();

        modBus.addListener((final RegisterCapabilitiesEvent event) -> {
            event.registerItem(Capabilities.ItemHandler.ITEM, (object, context) -> {
                if (object.getItem() instanceof DrawerBlock.DrawerItem di) {
                    return di.initCapabilities(object);
                }
                return null;
            }, DRAWER_TYPES.values().stream().flatMap(List::stream).map(bl -> (ItemLike)bl.block().get()).toArray(ItemLike[]::new));

            event.registerItem(Capabilities.ItemHandler.ITEM, (object, context) -> {
                if (object.getItem() instanceof CompactingDrawerBlock.CompactingDrawerItem di) {
                    return di.initCapabilities(object);
                }
                return null;
            }, COMPACTING_DRAWER.asItem(), SIMPLE_COMPACTING_DRAWER.asItem(), FRAMED_COMPACTING_DRAWER.asItem(), FRAMED_SIMPLE_COMPACTING_DRAWER.asItem());
        });

        modBus.addListener((final NewRegistryEvent event) -> event.register(FunctionalUpgradeBehavior.REGISTRY));
    }


    @Override
    protected void initModules() {
        WOOD_TYPES.addAll(List.of(DrawerWoodType.values()));
        for (DrawerType value : DrawerType.values()) {
            for (IWoodType woodType : WOOD_TYPES) {
                var name = woodType.getName() + "_" + value.getSlots();
                LOGGER.debug("Registering drawer {}", name);
                if (woodType == DrawerWoodType.FRAMED){
                    var pair = getRegistries().registerBlockWithTileItem(name, () -> new FramedDrawerBlock(value), blockRegistryObject -> () ->
                            new DrawerBlock.DrawerItem((DrawerBlock) blockRegistryObject.get(), new Item.Properties(), TAB),TAB);
                    DRAWER_TYPES.computeIfAbsent(value, drawerType -> new ArrayList<>()).add(pair);
                } else {
                    DRAWER_TYPES.computeIfAbsent(value, drawerType -> new ArrayList<>()).add(getRegistries().registerBlockWithTileItem(name, () -> new DrawerBlock(woodType, value, BlockBehaviour.Properties.ofFullCopy(woodType.getPlanks())), blockRegistryObject -> () ->
                            new DrawerBlock.DrawerItem((DrawerBlock) blockRegistryObject.get(), new Item.Properties(), TAB),TAB));
                }
            }
        }
        FLUID_DRAWER_1 = getRegistries().registerBlockWithTileItem("fluid_1", () -> new FluidDrawerBlock(DrawerType.X_1, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)),
                blockRegistryObject -> () -> new FluidDrawerBlock.FluidDrawerItem((FluidDrawerBlock) blockRegistryObject.get(), new Item.Properties(), TAB),TAB);
        FLUID_DRAWER_2 = getRegistries().registerBlockWithTileItem("fluid_2", () -> new FluidDrawerBlock(DrawerType.X_2, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)),
                blockRegistryObject -> () -> new FluidDrawerBlock.FluidDrawerItem((FluidDrawerBlock) blockRegistryObject.get(), new Item.Properties(), TAB),TAB);
        FLUID_DRAWER_4 = getRegistries().registerBlockWithTileItem("fluid_4", () -> new FluidDrawerBlock(DrawerType.X_4, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)),
                blockRegistryObject -> () -> new FluidDrawerBlock.FluidDrawerItem((FluidDrawerBlock) blockRegistryObject.get(), new Item.Properties(), TAB),TAB);
        FRAMED_FLUID_DRAWER_1 = getRegistries().registerBlockWithTileItem("framed_fluid_1", () -> new FramedFluidDrawerBlock(DrawerType.X_1, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)),
                blockRegistryObject -> () -> new FluidDrawerBlock.FluidDrawerItem((FluidDrawerBlock) blockRegistryObject.get(), new Item.Properties(), TAB),TAB);
        FRAMED_FLUID_DRAWER_2 = getRegistries().registerBlockWithTileItem("framed_fluid_2", () -> new FramedFluidDrawerBlock(DrawerType.X_2, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)),
                blockRegistryObject -> () -> new FluidDrawerBlock.FluidDrawerItem((FluidDrawerBlock) blockRegistryObject.get(), new Item.Properties(), TAB),TAB);
        FRAMED_FLUID_DRAWER_4 = getRegistries().registerBlockWithTileItem("framed_fluid_4", () -> new FramedFluidDrawerBlock(DrawerType.X_4, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)),
                blockRegistryObject -> () -> new FluidDrawerBlock.FluidDrawerItem((FluidDrawerBlock) blockRegistryObject.get(), new Item.Properties(), TAB),TAB);
        COMPACTING_DRAWER = getRegistries().registerBlockWithTileItem("compacting_drawer", () -> new CompactingDrawerBlock("compacting_drawer", BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)),
                blockRegistryObject -> () ->
                        new CompactingDrawerBlock.CompactingDrawerItem(blockRegistryObject.get(), new Item.Properties(), 3), TAB);
        FRAMED_COMPACTING_DRAWER = getRegistries().registerBlockWithTileItem("compacting_framed_drawer", () -> new CompactingFramedDrawerBlock("compacting_framed_drawer"),
                blockRegistryObject -> () ->
                        new CompactingDrawerBlock.CompactingDrawerItem(blockRegistryObject.get(), new Item.Properties(), 3), TAB);
        DRAWER_CONTROLLER = getRegistries().registerBlockWithTile("storage_controller", DrawerControllerBlock::new, TAB);
        FRAMED_DRAWER_CONTROLLER = getRegistries().registerBlockWithTile("framed_storage_controller", FramedDrawerControllerBlock::new, TAB);
        CONTROLLER_EXTENSION = getRegistries().registerBlockWithTile("controller_extension", ControllerExtensionBlock::new, TAB);
        FRAMED_CONTROLLER_EXTENSION = getRegistries().registerBlockWithTile("framed_controller_extension", FramedControllerExtensionBlock::new, TAB);
        LINKING_TOOL = getRegistries().registerGeneric(Registries.ITEM, "linking_tool", LinkingToolItem::new);
        CONFIGURATION_TOOL = getRegistries().registerGeneric(Registries.ITEM, "configuration_tool", ConfigurationToolItem::new);
        for (StorageUpgradeItem.StorageTier value : StorageUpgradeItem.StorageTier.values()) {
            STORAGE_UPGRADES.put(value, getRegistries().registerGeneric(Registries.ITEM, value.name().toLowerCase(Locale.ROOT) + (value == StorageUpgradeItem.StorageTier.IRON ? "_downgrade" : "_upgrade"), () -> new StorageUpgradeItem(value)));
        }
        SIMPLE_COMPACTING_DRAWER = getRegistries().registerBlockWithTileItem("simple_compacting_drawer", () -> new SimpleCompactingDrawerBlock("simple_compacting_drawer", BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)),
                blockRegistryObject -> () ->
                        new CompactingDrawerBlock.CompactingDrawerItem(blockRegistryObject.get(), new Item.Properties(), 2), TAB);
        FRAMED_SIMPLE_COMPACTING_DRAWER = getRegistries().registerBlockWithTileItem("framed_simple_compacting_drawer", () -> new FramedSimpleCompactingDrawerBlock("framed_simple_compacting_drawer"),
                blockRegistryObject -> () ->
                        new CompactingDrawerBlock.CompactingDrawerItem(blockRegistryObject.get(), new Item.Properties(), 2), TAB);
        COLLECTOR_UPGRADE = getRegistries().registerGeneric(Registries.ITEM, "collector_upgrade", () -> new UpgradeItem(
                new AndBehavior(List.of(
                        new ExecuteEveryBehavior(FunctionalStorageConfig.UPGRADE_TICK, CollectItemEntitiesBehavior.INSTANCE),
                        new ExecuteEveryBehavior(FunctionalStorageConfig.UPGRADE_TICK * 3, CollectFluidsBehavior.INSTANCE)
                ))));
        PULLING_UPGRADE = getRegistries().registerGeneric(Registries.ITEM, "puller_upgrade", () -> new UpgradeItem(
                new ExecuteEveryBehavior(FunctionalStorageConfig.UPGRADE_TICK, new AndBehavior(List.of(
                        new MoveItemsBehavior(false, FunctionalStorageConfig.UPGRADE_PULL_ITEMS),
                        new MoveFluidsBehavior(false, FunctionalStorageConfig.UPGRADE_PULL_FLUID)
                )))));
        PUSHING_UPGRADE = getRegistries().registerGeneric(Registries.ITEM, "pusher_upgrade", () -> new UpgradeItem(
                new ExecuteEveryBehavior(FunctionalStorageConfig.UPGRADE_TICK, new AndBehavior(List.of(
                        new MoveItemsBehavior(true, FunctionalStorageConfig.UPGRADE_PUSH_ITEMS),
                        new MoveFluidsBehavior(true, FunctionalStorageConfig.UPGRADE_PUSH_FLUID)
                )))));
        VOID_UPGRADE = getRegistries().registerGeneric(Registries.ITEM, "void_upgrade", () -> new UpgradeItem(new Item.Properties(), UpgradeItem.Type.UTILITY));
        ARMORY_CABINET = getRegistries().registerBlockWithTile("armory_cabinet", ArmoryCabinetBlock::new, TAB);
        ENDER_DRAWER = getRegistries().registerBlockWithTile("ender_drawer", EnderDrawerBlock::new, TAB);
        REDSTONE_UPGRADE = getRegistries().registerGeneric(Registries.ITEM, "redstone_upgrade", () -> new UpgradeItem(EmitRedstoneBehavior.INSTANCE));
        CREATIVE_UPGRADE = getRegistries().registerGeneric(Registries.ITEM, "creative_vending_upgrade", () -> new UpgradeItem(new Item.Properties(), UpgradeItem.Type.STORAGE) {
            @Override
            public boolean isFoil(ItemStack p_41453_) {
                return true;
            }
        });
        DrawerlessWoodIngredient.TYPE = getRegistries().registerGeneric(NeoForgeRegistries.Keys.INGREDIENT_TYPES, DrawerlessWoodIngredient.NAME.getPath(), () -> new IngredientType<>(DrawerlessWoodIngredient.CODEC));
        TagWithoutComponentIngredient.TYPE = getRegistries().registerGeneric(NeoForgeRegistries.Keys.INGREDIENT_TYPES, TagWithoutComponentIngredient.NAME.getPath(), () -> new IngredientType<>(TagWithoutComponentIngredient.CODEC));


        this.addCreativeTab("main", () -> new ItemStack(DRAWER_CONTROLLER), MOD_ID, TAB);

        CUSTOM_COMPACTING_RECIPE_TYPE = getRegistries().registerGeneric(Registries.RECIPE_TYPE, "custom_compacting", () -> RecipeType.simple(com.buuz135.functionalstorage.util.Utils.resourceLocation(MOD_ID, "custom_compacting")));

        CUSTOM_COMPACTING_RECIPE_SERIALIZER = getRegistries().registerGeneric(Registries.RECIPE_SERIALIZER, "custom_compacting", () -> new GenericSerializer<>(CustomCompactingRecipe.class, CUSTOM_COMPACTING_RECIPE_TYPE::value, CustomCompactingRecipe.CODEC));

        FRAMED_RECIPE_TYPE = getRegistries().registerGeneric(Registries.RECIPE_TYPE, "framed_recipe", () -> RecipeType.simple(com.buuz135.functionalstorage.util.Utils.resourceLocation(MOD_ID, "framed_recipe")));

        FRAMED_RECIPE_SERIALIZER = getRegistries().registerGeneric(Registries.RECIPE_SERIALIZER, "framed_recipe", () -> new SimpleCraftingRecipeSerializer<>((c) -> new FramedDrawerRecipe()));
        COPY_COMPONENTS_SERIALIZER = getRegistries().registerGeneric(Registries.RECIPE_SERIALIZER, "copy_components", () -> new RecipeSerializer<CopyComponentsRecipe>() {
            @Override
            public MapCodec<CopyComponentsRecipe> codec() {
                return CopyComponentsRecipe.CODEC;
            }

            @Override
            public StreamCodec<RegistryFriendlyByteBuf, CopyComponentsRecipe> streamCodec() {
                return CopyComponentsRecipe.STREAM_CODEC;
            }
        });

        // TODO - remove in next lifecycle
        getRegistries().registerGeneric(FunctionalUpgradeBehavior.REGISTRY_KEY, "delegate_to_item", () -> DelegateToItemBehavior.CODEC);

        getRegistries().registerGeneric(FunctionalUpgradeBehavior.REGISTRY_KEY, "execute_every", () -> ExecuteEveryBehavior.CODEC);
        getRegistries().registerGeneric(FunctionalUpgradeBehavior.REGISTRY_KEY, "generate_item", () -> GenerateItemBehavior.CODEC);
        getRegistries().registerGeneric(FunctionalUpgradeBehavior.REGISTRY_KEY, "generate_fluid", () -> GenerateFluidBehavior.CODEC);
        getRegistries().registerGeneric(FunctionalUpgradeBehavior.REGISTRY_KEY, "move_items", () -> MoveItemsBehavior.CODEC);
        getRegistries().registerGeneric(FunctionalUpgradeBehavior.REGISTRY_KEY, "move_fluids", () -> MoveFluidsBehavior.CODEC);
        getRegistries().registerGeneric(FunctionalUpgradeBehavior.REGISTRY_KEY, "collect_item_entities", () -> CollectItemEntitiesBehavior.CODEC);
        getRegistries().registerGeneric(FunctionalUpgradeBehavior.REGISTRY_KEY, "collect_fluids", () -> CollectFluidsBehavior.CODEC);
        getRegistries().registerGeneric(FunctionalUpgradeBehavior.REGISTRY_KEY, "emit_redstone", () -> EmitRedstoneBehavior.CODEC);
        getRegistries().registerGeneric(FunctionalUpgradeBehavior.REGISTRY_KEY, "and", () -> AndBehavior.CODEC);

        DRIPPING_UPGRADE = getRegistries().registerGeneric(Registries.ITEM, "dripping_upgrade", () -> new FSItem(new Item.Properties()
                .component(FSAttachments.FUNCTIONAL_BEHAVIOR, new ExecuteEveryBehavior(20, new GenerateFluidBehavior(new FluidStack(Fluids.LAVA, 20))))));
        WATER_GENERATOR_UPGRADE = getRegistries().registerGeneric(Registries.ITEM, "water_generator_upgrade", () -> new FSItem(new Item.Properties()
                .component(FSAttachments.FUNCTIONAL_BEHAVIOR, new ExecuteEveryBehavior(1,new GenerateFluidBehavior(new FluidStack(Fluids.WATER, 2000))))));
        OBSIDIAN_UPGRADE = getRegistries().registerGeneric(Registries.ITEM, "obsidian_upgrade", () -> new FSItem(new Item.Properties()
                .component(FSAttachments.FUNCTIONAL_BEHAVIOR, new ExecuteEveryBehavior(300, new GenerateItemBehavior(new ItemStack(Items.OBSIDIAN))))));

        var eventBus = ModLoadingContext.get().getActiveContainer().getEventBus();

        eventBus.addListener((final ModifyDefaultComponentsEvent event) -> {
            // We modify the component here to make sure we have a reference to the correct holder
            // The void upgrade can only be installed at most once
            event.modify(VOID_UPGRADE.get(), b -> b.set(FSAttachments.INCOMPATIBLE_UPGRADES.get(), HolderSet.direct(VOID_UPGRADE.get().builtInRegistryHolder())));
        });

        eventBus.addListener(EventPriority.LOWEST, (final RegisterEvent regEvent) -> {
                    if (regEvent.getRegistryKey() == Registries.BLOCK) {
                        FRAMED_BLOCKS = getRegistries().getRegistry(Registries.BLOCK)
                                .getEntries().stream()
                                .filter(bl -> bl.value() instanceof FramedBlock)
                                .map(Holder::value)
                                .toList();
                    }
                });
    }

    public enum DrawerType {
        X_1(1, 32, "1x1", integer -> Pair.of(16, 16)),
        X_2(2, 16, "1x2", integer -> {
            if (integer == 0) return Pair.of(16, 28);
            return Pair.of(16, 4);
        }),
        X_4(4, 8, "2x2", integer -> {
            if (integer == 0) return Pair.of(28, 28);
            if (integer == 1) return Pair.of(4, 28);
            if (integer == 2) return Pair.of(28, 4);
            return Pair.of(4, 4);
        });

        private final int slots;
        private final int slotAmount;
        private final String displayName;
        private final TagKey<Item> tag;
        private final Function<Integer, Pair<Integer, Integer>> slotPosition;

        private DrawerType(int slots, int slotAmount, String displayName, Function<Integer, Pair<Integer, Integer>> slotPosition) {
            this.slots = slots;
            this.slotAmount = slotAmount;
            this.displayName = displayName;
            this.slotPosition = slotPosition;
            this.tag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "drawer_" + displayName));
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

        public Function<Integer, Pair<Integer, Integer>> getSlotPosition() {
            return slotPosition;
        }

        public TagKey<Item> getTag() {
            return tag;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void onClient() {
        EventManager.mod(EntityRenderersEvent.RegisterRenderers.class).process(registerRenderers -> {
            for (DrawerType value : DrawerType.values()) {
                DRAWER_TYPES.get(value).forEach(blockRegistryObject -> {
                    registerRenderers.registerBlockEntityRenderer((BlockEntityType<? extends DrawerTile>) blockRegistryObject.type().get(), p_173571_ -> new DrawerRenderer());
                });
            }
            registerRenderers.registerBlockEntityRenderer((BlockEntityType<? extends CompactingDrawerTile>) COMPACTING_DRAWER.type().get(), p_173571_ -> new CompactingDrawerRenderer());
            registerRenderers.registerBlockEntityRenderer((BlockEntityType<? extends CompactingDrawerTile>) FRAMED_COMPACTING_DRAWER.type().get(), p_173571_ -> new CompactingDrawerRenderer());
            registerRenderers.registerBlockEntityRenderer((BlockEntityType<? extends DrawerControllerTile>) DRAWER_CONTROLLER.type().get(), p -> new ControllerRenderer());
            registerRenderers.registerBlockEntityRenderer((BlockEntityType<? extends EnderDrawerTile>) ENDER_DRAWER.type().get(), p_173571_ -> new EnderDrawerRenderer());
            registerRenderers.registerBlockEntityRenderer((BlockEntityType<? extends FluidDrawerTile>) FLUID_DRAWER_1.type().get(), p_173571_ -> new FluidDrawerRenderer());
            registerRenderers.registerBlockEntityRenderer((BlockEntityType<? extends FluidDrawerTile>) FLUID_DRAWER_2.type().get(), p_173571_ -> new FluidDrawerRenderer());
            registerRenderers.registerBlockEntityRenderer((BlockEntityType<? extends FluidDrawerTile>) FLUID_DRAWER_4.type().get(), p_173571_ -> new FluidDrawerRenderer());
            registerRenderers.registerBlockEntityRenderer((BlockEntityType<? extends FluidDrawerTile>) FRAMED_FLUID_DRAWER_1.type().get(), p_173571_ -> new FluidDrawerRenderer());
            registerRenderers.registerBlockEntityRenderer((BlockEntityType<? extends FluidDrawerTile>) FRAMED_FLUID_DRAWER_2.type().get(), p_173571_ -> new FluidDrawerRenderer());
            registerRenderers.registerBlockEntityRenderer((BlockEntityType<? extends FluidDrawerTile>) FRAMED_FLUID_DRAWER_4.type().get(), p_173571_ -> new FluidDrawerRenderer());
            registerRenderers.registerBlockEntityRenderer((BlockEntityType<? extends SimpleCompactingDrawerTile>) SIMPLE_COMPACTING_DRAWER.type().get(), p_173571_ -> new SimpleCompactingDrawerRenderer());

            registerRenderers.registerBlockEntityRenderer((BlockEntityType<? extends FramedDrawerControllerTile>) FRAMED_DRAWER_CONTROLLER.type().get(), p -> new ControllerRenderer());
            registerRenderers.registerBlockEntityRenderer((BlockEntityType<? extends SimpleCompactingDrawerTile>) FRAMED_SIMPLE_COMPACTING_DRAWER.type().get(), p_173571_ -> new SimpleCompactingDrawerRenderer());

        }).subscribe();
        EventManager.mod(RegisterColorHandlersEvent.Item.class).process(item -> {
            item.getItemColors().register((stack, tint) -> {
                LinkingToolItem.LinkingMode linkingMode = LinkingToolItem.getLinkingMode(stack);
                LinkingToolItem.ActionMode linkingAction = LinkingToolItem.getActionMode(stack);
                if (tint != 0 && stack.has(FSAttachments.ENDER_FREQUENCY)) {
                    return FastColor.ARGB32.opaque(new Color(44, 150, 88).getRGB());
                }
                if (tint == 3 && stack.has(FSAttachments.CONTROLLER)) {
                    return FastColor.ARGB32.opaque(Color.RED.getRGB());
                }
                if (tint == 1) {
                    return FastColor.ARGB32.opaque(linkingMode.getColor().getValue());
                }
                if (tint == 2) {
                    return FastColor.ARGB32.opaque(linkingAction.getColor().getValue());
                }
                return -1;
            }, LINKING_TOOL.get());
            item.getItemColors().register((stack, tint) -> {
                ConfigurationToolItem.ConfigurationAction action = ConfigurationToolItem.getAction(stack);
                if (tint == 1) {
                    return FastColor.ARGB32.opaque(action.getColor().getValue());
                }
                return -1;
            }, CONFIGURATION_TOOL.get());
        }).subscribe();
        EventManager.mod(FMLClientSetupEvent.class).process(event -> {
            for (DrawerType value : DrawerType.values()) {
                DRAWER_TYPES.get(value).stream().map(BlockWithTile::getBlock).forEach(bl -> ItemBlockRenderTypes.setRenderLayer(bl, RenderType.cutout()));
            }
            ItemBlockRenderTypes.setRenderLayer(COMPACTING_DRAWER.getBlock(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(FRAMED_COMPACTING_DRAWER.getBlock(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ENDER_DRAWER.getBlock(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(FLUID_DRAWER_1.getBlock(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(FLUID_DRAWER_2.getBlock(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(FLUID_DRAWER_4.getBlock(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(SIMPLE_COMPACTING_DRAWER.getBlock(), RenderType.cutout());

            ItemBlockRenderTypes.setRenderLayer(FRAMED_DRAWER_CONTROLLER.getBlock(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(FRAMED_CONTROLLER_EXTENSION.getBlock(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(FRAMED_SIMPLE_COMPACTING_DRAWER.getBlock(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(FRAMED_FLUID_DRAWER_1.getBlock(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(FRAMED_FLUID_DRAWER_2.getBlock(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(FRAMED_FLUID_DRAWER_4.getBlock(), RenderType.cutout());
        }).subscribe();
        EventManager.forge(RenderTooltipEvent.Pre.class).process(itemTooltipEvent -> {
            if (itemTooltipEvent.getItemStack().getItem().equals(FunctionalStorage.ENDER_DRAWER.getBlock().asItem()) && itemTooltipEvent.getItemStack().has(FSAttachments.TILE)) {
                TooltipUtil.renderItems(itemTooltipEvent.getGraphics(), EnderDrawerBlock.getFrequencyDisplay(itemTooltipEvent.getItemStack().get(FSAttachments.TILE).getString("frequency")), itemTooltipEvent.getX() + 14, itemTooltipEvent.getY() + 11);
            }
            if (itemTooltipEvent.getItemStack().is(FunctionalStorage.LINKING_TOOL.get()) && itemTooltipEvent.getItemStack().has(FSAttachments.ENDER_FREQUENCY)) {
                TooltipUtil.renderItems(itemTooltipEvent.getGraphics(), EnderDrawerBlock.getFrequencyDisplay(itemTooltipEvent.getItemStack().get(FSAttachments.ENDER_FREQUENCY)), itemTooltipEvent.getX() + 14, itemTooltipEvent.getY() + 11);
            }
            var iItemHandler = itemTooltipEvent.getItemStack().getCapability(Capabilities.ItemHandler.ITEM);
            if (iItemHandler != null) {
                if (iItemHandler instanceof DrawerStackItemHandler drawerStackItemHandler) {
                    int i = 0;
                    for (BigInventoryHandler.BigStack storedStack : ((DrawerStackItemHandler) iItemHandler).getStoredStacks()) {
                        if (storedStack.getStack().getItem() != Items.AIR) {
                            TooltipUtil.renderItemAdvanced(itemTooltipEvent.getGraphics(), storedStack.getStack(), itemTooltipEvent.getX() + 20 + 32 * i, itemTooltipEvent.getY() + 11, 512, NumberUtils.getFormatedBigNumber(drawerStackItemHandler.isCreative() ? Integer.MAX_VALUE : storedStack.getAmount()));
                            ++i;
                        }
                    }
                }
                if (iItemHandler instanceof CompactingStackItemHandler compactingStackItemHandler) {
                    int pos = 0;

                    for (int i = compactingStackItemHandler.getSlots(); i >= 0; i--) {
                        var stack = compactingStackItemHandler.getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            TooltipUtil.renderItemAdvanced(itemTooltipEvent.getGraphics(), stack, itemTooltipEvent.getX() + 20 + 32 * pos, itemTooltipEvent.getY() + 11, 512, NumberUtils.getFormatedBigNumber(stack.getCount()) + "/" + NumberUtils.getFormatedBigNumber(iItemHandler.getSlotLimit(i)));
                            ++pos;
                        }
                    }
                }
            }
        }).subscribe();
        EventManager.mod(ModelEvent.RegisterGeometryLoaders.class).process(modelRegistryEvent -> {
            modelRegistryEvent.register(com.buuz135.functionalstorage.util.Utils.resourceLocation(MOD_ID, "framedblock"), FramedModel.Loader.INSTANCE);
        }).subscribe();
        ClientSetup.init();
    }

    @Override
    public void addDataProvider(GatherDataEvent event) {
        Lazy<List<Block>> blocksToProcess = Lazy.of(() ->
                BuiltInRegistries.BLOCK.stream()
                        .filter(basicBlock -> Optional.of(BuiltInRegistries.BLOCK.getKey(basicBlock))
                                .map(ResourceLocation::getNamespace)
                                .filter(MOD_ID::equalsIgnoreCase)
                                .isPresent())
                        .collect(Collectors.toList())
        );
        if (true) {
            //event.getGenerator().addProvider(true, new BlockItemModelGeneratorProvider(event.getGenerator(), MOD_ID, blocksToProcess));
            event.getGenerator().addProvider(true, new FunctionalStorageBlockstateProvider(event.getGenerator(), event.getExistingFileHelper(), blocksToProcess));
            event.getGenerator().addProvider(true, new TitaniumLootTableProvider(event.getGenerator(), blocksToProcess, event.getLookupProvider()));

            var blockTags = new FunctionalStorageBlockTagsProvider(event.getGenerator(), event.getLookupProvider(), MOD_ID, event.getExistingFileHelper());
            event.getGenerator().addProvider(true, blockTags);
            event.getGenerator().addProvider(true, new FunctionalStorageItemTagsProvider(event.getGenerator().getPackOutput(), event.getLookupProvider(), blockTags.contentsGetter(), MOD_ID, event.getExistingFileHelper()));
            event.getGenerator().addProvider(true, new FunctionalStorageLangProvider(event.getGenerator(), MOD_ID, "en_us"));

            event.getGenerator().addProvider(true, new ItemModelProvider(event.getGenerator().getPackOutput(), MOD_ID, event.getExistingFileHelper()) {
                @Override
                protected void registerModels() {
                    blocksToProcess.get().forEach(block -> {
                        if ((block instanceof DrawerBlock) || (block instanceof CompactingDrawerBlock) || (block instanceof SimpleCompactingDrawerBlock) || (block instanceof FluidDrawerBlock)){
                            withUnchecked(BuiltInRegistries.BLOCK.getKey(block).getPath(), ResourceLocation.fromNamespaceAndPath("minecraft", "builtin/entity"));
                        } else {
                            withUnchecked(BuiltInRegistries.BLOCK.getKey(block).getPath(), ResourceLocation.fromNamespaceAndPath(FunctionalStorage.MOD_ID, "block/" + BuiltInRegistries.BLOCK.getKey(block).getPath()));
                        }
                    });
                    for (StorageUpgradeItem.StorageTier storageTier : STORAGE_UPGRADES.keySet()) {
                        item(STORAGE_UPGRADES.get(storageTier).get());
                    }
                    item(COLLECTOR_UPGRADE.get());
                    item(PULLING_UPGRADE.get());
                    item(PUSHING_UPGRADE.get());
                    item(VOID_UPGRADE.get());
                    item(REDSTONE_UPGRADE.get());
                    item(CREATIVE_UPGRADE.get());
                    item(DRIPPING_UPGRADE.get());
                    item(WATER_GENERATOR_UPGRADE.get());
                    item(OBSIDIAN_UPGRADE.get());
                }

                private void item(Item item) {
                    withUnchecked(BuiltInRegistries.ITEM.getKey(item).getPath(), com.buuz135.functionalstorage.util.Utils.resourceLocation("minecraft:item/generated")).texture( "layer0", com.buuz135.functionalstorage.util.Utils.resourceLocation(MOD_ID, "item/" + BuiltInRegistries.ITEM.getKey(item).getPath()));
                }

                private ItemModelBuilder withUnchecked(String name, ResourceLocation parent){
                    return getBuilder(name).parent(new ModelFile.UncheckedModelFile(parent));
                }
            });
        }
        event.getGenerator().addProvider(true, new FunctionalStorageRecipesProvider(event.getGenerator(), blocksToProcess, event.getLookupProvider()));
    }
}
