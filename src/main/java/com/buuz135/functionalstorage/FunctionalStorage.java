package com.buuz135.functionalstorage;

import com.buuz135.functionalstorage.block.*;
import com.buuz135.functionalstorage.block.tile.*;
import com.buuz135.functionalstorage.client.*;
import com.buuz135.functionalstorage.client.loader.FramedModel;
import com.buuz135.functionalstorage.data.*;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.inventory.item.CompactingStackItemHandler;
import com.buuz135.functionalstorage.inventory.item.DrawerStackItemHandler;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.buuz135.functionalstorage.network.EnderDrawerSyncMessage;
import com.buuz135.functionalstorage.recipe.CustomCompactingRecipe;
import com.buuz135.functionalstorage.recipe.DrawerlessWoodIngredient;
import com.buuz135.functionalstorage.recipe.FramedDrawerRecipe;
import com.buuz135.functionalstorage.util.DrawerWoodType;
import com.buuz135.functionalstorage.util.IWoodType;
import com.buuz135.functionalstorage.util.NumberUtils;
import com.buuz135.functionalstorage.util.TooltipUtil;
import com.hrznstudio.titanium.datagenerator.loot.TitaniumLootTableProvider;
import com.hrznstudio.titanium.datagenerator.model.BlockItemModelGeneratorProvider;
import com.hrznstudio.titanium.event.handler.EventManager;
import com.hrznstudio.titanium.module.ModuleController;
import com.hrznstudio.titanium.nbthandler.NBTManager;
import com.hrznstudio.titanium.network.NetworkHandler;
import com.hrznstudio.titanium.recipe.generator.IJSONGenerator;
import com.hrznstudio.titanium.recipe.generator.IJsonFile;
import com.hrznstudio.titanium.recipe.generator.TitaniumSerializableProvider;
import com.hrznstudio.titanium.recipe.serializer.GenericSerializer;
import com.hrznstudio.titanium.tab.AdvancedTitaniumTab;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(FunctionalStorage.MOD_ID)
public class FunctionalStorage extends ModuleController {

    public final static String MOD_ID = "functionalstorage";
    public static NetworkHandler NETWORK = new NetworkHandler(MOD_ID);

    static {
        NETWORK.registerMessage(EnderDrawerSyncMessage.class);
    }

    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public static ConcurrentLinkedQueue<IWoodType> WOOD_TYPES = new ConcurrentLinkedQueue<>();

    public static HashMap<DrawerType, List<Pair<RegistryObject<Block>, RegistryObject<BlockEntityType<?>>>>> DRAWER_TYPES = new HashMap<>();
    public static Pair<RegistryObject<Block>, RegistryObject<BlockEntityType<?>>> COMPACTING_DRAWER;
    public static Pair<RegistryObject<Block>, RegistryObject<BlockEntityType<?>>> DRAWER_CONTROLLER;
    public static Pair<RegistryObject<Block>, RegistryObject<BlockEntityType<?>>> ARMORY_CABINET;
    public static Pair<RegistryObject<Block>, RegistryObject<BlockEntityType<?>>> ENDER_DRAWER;
    public static Pair<RegistryObject<Block>, RegistryObject<BlockEntityType<?>>> FRAMED_COMPACTING_DRAWER;
    public static Pair<RegistryObject<Block>, RegistryObject<BlockEntityType<?>>> FLUID_DRAWER_1;
    public static Pair<RegistryObject<Block>, RegistryObject<BlockEntityType<?>>> FLUID_DRAWER_2;
    public static Pair<RegistryObject<Block>, RegistryObject<BlockEntityType<?>>> FLUID_DRAWER_4;
    public static Pair<RegistryObject<Block>, RegistryObject<BlockEntityType<?>>> CONTROLLER_EXTENSION;
    public static Pair<RegistryObject<Block>, RegistryObject<BlockEntityType<?>>> SIMPLE_COMPACTING_DRAWER;
    public static Pair<RegistryObject<Block>, RegistryObject<BlockEntityType<?>>> FRAMED_DRAWER_CONTROLLER;
    public static Pair<RegistryObject<Block>, RegistryObject<BlockEntityType<?>>> FRAMED_CONTROLLER_EXTENSION;
    public static Pair<RegistryObject<Block>, RegistryObject<BlockEntityType<?>>> FRAMED_SIMPLE_COMPACTING_DRAWER;


    public static RegistryObject<Item> LINKING_TOOL;
    public static HashMap<StorageUpgradeItem.StorageTier, RegistryObject<Item>> STORAGE_UPGRADES = new HashMap<>();
    public static RegistryObject<Item> COLLECTOR_UPGRADE;
    public static RegistryObject<Item> PULLING_UPGRADE;
    public static RegistryObject<Item> PUSHING_UPGRADE;
    public static RegistryObject<Item> VOID_UPGRADE;
    public static RegistryObject<Item> CONFIGURATION_TOOL;
    public static RegistryObject<Item> REDSTONE_UPGRADE;
    public static RegistryObject<Item> CREATIVE_UPGRADE;

    public static RegistryObject<RecipeSerializer<?>> CUSTOM_COMPACTING_RECIPE_SERIALIZER;
    public static RegistryObject<RecipeType<?>> CUSTOM_COMPACTING_RECIPE_TYPE;

    public static AdvancedTitaniumTab TAB = new AdvancedTitaniumTab("functionalstorage", true);

    public FunctionalStorage() {
        ForgeMod.enableMilkFluid();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::onClient);
        EventManager.forge(BlockEvent.BreakEvent.class).process(breakEvent -> {
            if (breakEvent.getPlayer().isCreative()) {
                if (breakEvent.getState().getBlock() instanceof DrawerBlock) {
                    int hit = ((DrawerBlock) breakEvent.getState().getBlock()).getHit(breakEvent.getState(), breakEvent.getPlayer().getLevel(), breakEvent.getPos(), breakEvent.getPlayer());
                    if (hit != -1) {
                        breakEvent.setCanceled(true);
                        ((DrawerBlock) breakEvent.getState().getBlock()).attack(breakEvent.getState(), breakEvent.getPlayer().getLevel(), breakEvent.getPos(), breakEvent.getPlayer());
                    }
                }
                if (breakEvent.getState().getBlock() instanceof CompactingDrawerBlock) {
                    int hit = ((CompactingDrawerBlock) breakEvent.getState().getBlock()).getHit(breakEvent.getState(), breakEvent.getPlayer().getLevel(), breakEvent.getPos(), breakEvent.getPlayer());
                    if (hit != -1) {
                        breakEvent.setCanceled(true);
                        ((CompactingDrawerBlock) breakEvent.getState().getBlock()).attack(breakEvent.getState(), breakEvent.getPlayer().getLevel(), breakEvent.getPos(), breakEvent.getPlayer());
                    }
                }
                if (breakEvent.getState().getBlock() instanceof EnderDrawerBlock) {
                    int hit = ((EnderDrawerBlock) breakEvent.getState().getBlock()).getHit(breakEvent.getState(), breakEvent.getPlayer().getLevel(), breakEvent.getPos(), breakEvent.getPlayer());
                    if (hit != -1) {
                        breakEvent.setCanceled(true);
                        ((EnderDrawerBlock) breakEvent.getState().getBlock()).attack(breakEvent.getState(), breakEvent.getPlayer().getLevel(), breakEvent.getPos(), breakEvent.getPlayer());
                    }
                }
                if (breakEvent.getState().getBlock() instanceof FluidDrawerBlock) {
                    int hit = ((FluidDrawerBlock) breakEvent.getState().getBlock()).getHit(breakEvent.getState(), breakEvent.getPlayer().getLevel(), breakEvent.getPos(), breakEvent.getPlayer());
                    if (hit != -1) {
                        breakEvent.setCanceled(true);
                        ((FluidDrawerBlock) breakEvent.getState().getBlock()).attack(breakEvent.getState(), breakEvent.getPlayer().getLevel(), breakEvent.getPos(), breakEvent.getPlayer());
                    }
                }
                if (breakEvent.getState().getBlock() instanceof SimpleCompactingDrawerBlock) {
                    int hit = ((SimpleCompactingDrawerBlock) breakEvent.getState().getBlock()).getHit(breakEvent.getState(), breakEvent.getPlayer().getLevel(), breakEvent.getPos(), breakEvent.getPlayer());
                    if (hit != -1) {
                        breakEvent.setCanceled(true);
                        ((SimpleCompactingDrawerBlock) breakEvent.getState().getBlock()).attack(breakEvent.getState(), breakEvent.getPlayer().getLevel(), breakEvent.getPos(), breakEvent.getPlayer());
                    }
                }
            }
        }).subscribe();
        EventManager.mod(FMLCommonSetupEvent.class).process(fmlCommonSetupEvent -> {
            CraftingHelper.register(DrawerlessWoodIngredient.NAME, DrawerlessWoodIngredient.SERIALIZER);
        }).subscribe();
        NBTManager.getInstance().scanTileClassForAnnotations(FramedDrawerTile.class);
        NBTManager.getInstance().scanTileClassForAnnotations(CompactingFramedDrawerTile.class);
        NBTManager.getInstance().scanTileClassForAnnotations(FluidDrawerTile.class);
        NBTManager.getInstance().scanTileClassForAnnotations(SimpleCompactingDrawerTile.class);
        NBTManager.getInstance().scanTileClassForAnnotations(FramedSimpleCompactingDrawerTile.class);
    }


    @Override
    protected void initModules() {
        WOOD_TYPES.addAll(List.of(DrawerWoodType.values()));
        for (DrawerType value : DrawerType.values()) {
            for (IWoodType woodType : WOOD_TYPES) {
                var name = woodType.getName() + "_" + value.getSlots();
                if (woodType == DrawerWoodType.FRAMED){
                    var pair = getRegistries().registerBlockWithTileItem(name, () -> new FramedDrawerBlock(value), blockRegistryObject -> () ->
                            new DrawerBlock.DrawerItem((DrawerBlock) blockRegistryObject.get(), new Item.Properties().tab(TAB)));
                    DRAWER_TYPES.computeIfAbsent(value, drawerType -> new ArrayList<>()).add(pair);
                } else {
                    DRAWER_TYPES.computeIfAbsent(value, drawerType -> new ArrayList<>()).add(getRegistries().registerBlockWithTileItem(name, () -> new DrawerBlock(woodType, value, BlockBehaviour.Properties.copy(woodType.getPlanks())), blockRegistryObject -> () ->
                            new DrawerBlock.DrawerItem((DrawerBlock) blockRegistryObject.get(), new Item.Properties().tab(TAB))));
                }
            }
            DRAWER_TYPES.get(value).forEach(blockRegistryObject -> TAB.addIconStacks(() -> new ItemStack(blockRegistryObject.getLeft().get())));
        }
        COMPACTING_DRAWER = getRegistries().registerBlockWithTileItem("compacting_drawer", () -> new CompactingDrawerBlock("compacting_drawer", BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS)),
                blockRegistryObject -> () ->
                        new CompactingDrawerBlock.CompactingDrawerItem(blockRegistryObject.get(), new Item.Properties().tab(TAB), 3));
        FRAMED_COMPACTING_DRAWER = getRegistries().registerBlockWithTileItem("compacting_framed_drawer", () -> new CompactingFramedDrawerBlock("compacting_framed_drawer"),
                blockRegistryObject -> () ->
                        new CompactingDrawerBlock.CompactingDrawerItem(blockRegistryObject.get(), new Item.Properties().tab(TAB), 3));
        FLUID_DRAWER_1 = getRegistries().registerBlockWithTile("fluid_1", () -> new FluidDrawerBlock(DrawerType.X_1, BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS)));
        FLUID_DRAWER_2 = getRegistries().registerBlockWithTile("fluid_2", () -> new FluidDrawerBlock(DrawerType.X_2, BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS)));
        FLUID_DRAWER_4 = getRegistries().registerBlockWithTile("fluid_4", () -> new FluidDrawerBlock(DrawerType.X_4, BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS)));
        DRAWER_CONTROLLER = getRegistries().registerBlockWithTile("storage_controller", DrawerControllerBlock::new);
        CONTROLLER_EXTENSION = getRegistries().registerBlockWithTile("controller_extension", ControllerExtensionBlock::new);
        LINKING_TOOL = getRegistries().registerGeneric(ForgeRegistries.ITEMS.getRegistryKey(), "linking_tool", LinkingToolItem::new);
        for (StorageUpgradeItem.StorageTier value : StorageUpgradeItem.StorageTier.values()) {
            STORAGE_UPGRADES.put(value, getRegistries().registerGeneric(ForgeRegistries.ITEMS.getRegistryKey(), value.name().toLowerCase(Locale.ROOT) + (value == StorageUpgradeItem.StorageTier.IRON ? "_downgrade" : "_upgrade"), () -> new StorageUpgradeItem(value)));
        }
        SIMPLE_COMPACTING_DRAWER = getRegistries().registerBlockWithTileItem("simple_compacting_drawer", () -> new SimpleCompactingDrawerBlock("simple_compacting_drawer", BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS)),
                blockRegistryObject -> () ->
                        new CompactingDrawerBlock.CompactingDrawerItem(blockRegistryObject.get(), new Item.Properties().tab(TAB), 2));
        FRAMED_SIMPLE_COMPACTING_DRAWER = getRegistries().registerBlockWithTileItem("framed_simple_compacting_drawer", () -> new FramedSimpleCompactingDrawerBlock("framed_simple_compacting_drawer"),
                blockRegistryObject -> () ->
                        new CompactingDrawerBlock.CompactingDrawerItem(blockRegistryObject.get(), new Item.Properties().tab(TAB), 2));
        COLLECTOR_UPGRADE = getRegistries().registerGeneric(ForgeRegistries.ITEMS.getRegistryKey(), "collector_upgrade", () -> new UpgradeItem(new Item.Properties(), UpgradeItem.Type.UTILITY));
        PULLING_UPGRADE = getRegistries().registerGeneric(ForgeRegistries.ITEMS.getRegistryKey(), "puller_upgrade", () -> new UpgradeItem(new Item.Properties(), UpgradeItem.Type.UTILITY));
        PUSHING_UPGRADE = getRegistries().registerGeneric(ForgeRegistries.ITEMS.getRegistryKey(), "pusher_upgrade", () -> new UpgradeItem(new Item.Properties(), UpgradeItem.Type.UTILITY));
        VOID_UPGRADE = getRegistries().registerGeneric(ForgeRegistries.ITEMS.getRegistryKey(), "void_upgrade", () -> new UpgradeItem(new Item.Properties(), UpgradeItem.Type.UTILITY));
        ARMORY_CABINET = getRegistries().registerBlockWithTile("armory_cabinet", ArmoryCabinetBlock::new);
        CONFIGURATION_TOOL = getRegistries().registerGeneric(ForgeRegistries.ITEMS.getRegistryKey(), "configuration_tool", ConfigurationToolItem::new);
        ENDER_DRAWER = getRegistries().registerBlockWithTile("ender_drawer", EnderDrawerBlock::new);
        REDSTONE_UPGRADE = getRegistries().registerGeneric(ForgeRegistries.ITEMS.getRegistryKey(), "redstone_upgrade", () -> new UpgradeItem(new Item.Properties(), UpgradeItem.Type.UTILITY));
        CREATIVE_UPGRADE = getRegistries().registerGeneric(ForgeRegistries.ITEMS.getRegistryKey(), "creative_vending_upgrade", () -> new UpgradeItem(new Item.Properties(), UpgradeItem.Type.STORAGE) {
            @Override
            public boolean isFoil(ItemStack p_41453_) {
                return true;
            }
        });

        FRAMED_DRAWER_CONTROLLER = getRegistries().registerBlockWithTile("framed_storage_controller", FramedDrawerControllerBlock::new);
        FRAMED_CONTROLLER_EXTENSION = getRegistries().registerBlockWithTile("framed_controller_extension", FramedControllerExtensionBlock::new);

        getRegistries().registerGeneric(ForgeRegistries.RECIPE_SERIALIZERS.getRegistryKey(), "framed_recipe", () -> FramedDrawerRecipe.SERIALIZER);

        CUSTOM_COMPACTING_RECIPE_TYPE = getRegistries().registerGeneric(ForgeRegistries.RECIPE_TYPES.getRegistryKey(), "custom_compacting", () -> RecipeType.simple(new ResourceLocation(MOD_ID, "custom_compacting")));

        CUSTOM_COMPACTING_RECIPE_SERIALIZER = getRegistries().registerGeneric(ForgeRegistries.RECIPE_SERIALIZERS.getRegistryKey(), "custom_compacting", () -> new GenericSerializer<>(CustomCompactingRecipe.class, CUSTOM_COMPACTING_RECIPE_TYPE));
    }

    public enum DrawerType {
        X_1(1, 32 * 64, "1x1", integer -> Pair.of(16, 16)),
        X_2(2, 16 * 64, "1x2", integer -> {
            if (integer == 0) return Pair.of(16, 28);
            return Pair.of(16, 4);
        }),
        X_4(4, 8 * 64, "2x2", integer -> {
            if (integer == 0) return Pair.of(28, 28);
            if (integer == 1) return Pair.of(4, 28);
            if (integer == 2) return Pair.of(28, 4);
            return Pair.of(4, 4);
        });

        private final int slots;
        private final int slotAmount;
        private final String displayName;
        private final Function<Integer, Pair<Integer, Integer>> slotPosition;

        private DrawerType(int slots, int slotAmount, String displayName, Function<Integer, Pair<Integer, Integer>> slotPosition) {
            this.slots = slots;
            this.slotAmount = slotAmount;
            this.displayName = displayName;
            this.slotPosition = slotPosition;
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
    }

    @OnlyIn(Dist.CLIENT)
    public void onClient() {
        EventManager.mod(EntityRenderersEvent.RegisterRenderers.class).process(registerRenderers -> {
            for (DrawerType value : DrawerType.values()) {
                DRAWER_TYPES.get(value).forEach(blockRegistryObject -> {
                    registerRenderers.registerBlockEntityRenderer((BlockEntityType<? extends DrawerTile>) blockRegistryObject.getRight().get(), p_173571_ -> new DrawerRenderer());
                });
            }
            registerRenderers.registerBlockEntityRenderer((BlockEntityType<? extends CompactingDrawerTile>) COMPACTING_DRAWER.getRight().get(), p_173571_ -> new CompactingDrawerRenderer());
            registerRenderers.registerBlockEntityRenderer((BlockEntityType<? extends CompactingDrawerTile>) FRAMED_COMPACTING_DRAWER.getRight().get(), p_173571_ -> new CompactingDrawerRenderer());
            registerRenderers.registerBlockEntityRenderer((BlockEntityType<? extends DrawerControllerTile>) DRAWER_CONTROLLER.getRight().get(), p -> new ControllerRenderer());
            registerRenderers.registerBlockEntityRenderer((BlockEntityType<? extends EnderDrawerTile>) ENDER_DRAWER.getRight().get(), p_173571_ -> new EnderDrawerRenderer());
            registerRenderers.registerBlockEntityRenderer((BlockEntityType<? extends FluidDrawerTile>) FLUID_DRAWER_1.getRight().get(), p_173571_ -> new FluidDrawerRenderer());
            registerRenderers.registerBlockEntityRenderer((BlockEntityType<? extends FluidDrawerTile>) FLUID_DRAWER_2.getRight().get(), p_173571_ -> new FluidDrawerRenderer());
            registerRenderers.registerBlockEntityRenderer((BlockEntityType<? extends FluidDrawerTile>) FLUID_DRAWER_4.getRight().get(), p_173571_ -> new FluidDrawerRenderer());
            registerRenderers.registerBlockEntityRenderer((BlockEntityType<? extends SimpleCompactingDrawerTile>) SIMPLE_COMPACTING_DRAWER.getRight().get(), p_173571_ -> new SimpleCompactingDrawerRenderer());
            registerRenderers.registerBlockEntityRenderer((BlockEntityType<? extends FramedSimpleCompactingDrawerTile>) FRAMED_SIMPLE_COMPACTING_DRAWER.getRight().get(), p_173571_ -> new SimpleCompactingDrawerRenderer());

            registerRenderers.registerBlockEntityRenderer((BlockEntityType<? extends FramedDrawerControllerTile>) FRAMED_DRAWER_CONTROLLER.getRight().get(), p -> new ControllerRenderer());
        }).subscribe();
        EventManager.mod(RegisterColorHandlersEvent.Item.class).process(item -> {
            item.getItemColors().register((stack, tint) -> {
                CompoundTag tag = stack.getOrCreateTag();
                LinkingToolItem.LinkingMode linkingMode = LinkingToolItem.getLinkingMode(stack);
                LinkingToolItem.ActionMode linkingAction = LinkingToolItem.getActionMode(stack);
                if (tint != 0 && stack.getOrCreateTag().contains(LinkingToolItem.NBT_ENDER)) {
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
                for (RegistryObject<Block> blockRegistryObject : DRAWER_TYPES.get(value).stream().map(Pair::getLeft).collect(Collectors.toList())) {
                    ItemBlockRenderTypes.setRenderLayer(blockRegistryObject.get(), RenderType.cutout());
                }
            }
            ItemBlockRenderTypes.setRenderLayer(COMPACTING_DRAWER.getLeft().get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(FRAMED_COMPACTING_DRAWER.getLeft().get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ENDER_DRAWER.getLeft().get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(FLUID_DRAWER_1.getLeft().get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(FLUID_DRAWER_2.getLeft().get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(FLUID_DRAWER_4.getLeft().get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(SIMPLE_COMPACTING_DRAWER.getLeft().get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(FRAMED_SIMPLE_COMPACTING_DRAWER.getLeft().get(), RenderType.cutout());

            ItemBlockRenderTypes.setRenderLayer(FRAMED_DRAWER_CONTROLLER.getLeft().get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(FRAMED_CONTROLLER_EXTENSION.getLeft().get(), RenderType.cutout());
        }).subscribe();
        EventManager.forge(RenderTooltipEvent.Pre.class).process(itemTooltipEvent -> {
            if (itemTooltipEvent.getItemStack().getItem().equals(FunctionalStorage.ENDER_DRAWER.getLeft().get().asItem()) && itemTooltipEvent.getItemStack().hasTag()) {
                TooltipUtil.renderItems(itemTooltipEvent.getPoseStack(), EnderDrawerBlock.getFrequencyDisplay(itemTooltipEvent.getItemStack().getTag().getCompound("Tile").getString("frequency")), itemTooltipEvent.getX() + 14, itemTooltipEvent.getY() + 11);
            }
            if (itemTooltipEvent.getItemStack().is(FunctionalStorage.LINKING_TOOL.get()) && itemTooltipEvent.getItemStack().getOrCreateTag().contains(LinkingToolItem.NBT_ENDER)) {
                TooltipUtil.renderItems(itemTooltipEvent.getPoseStack(), EnderDrawerBlock.getFrequencyDisplay(itemTooltipEvent.getItemStack().getOrCreateTag().getString(LinkingToolItem.NBT_ENDER)), itemTooltipEvent.getX() + 14, itemTooltipEvent.getY() + 11);
            }
            itemTooltipEvent.getItemStack().getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
                if (iItemHandler instanceof DrawerStackItemHandler) {
                    int i = 0;
                    for (BigInventoryHandler.BigStack storedStack : ((DrawerStackItemHandler) iItemHandler).getStoredStacks()) {
                        TooltipUtil.renderItemAdvanced(itemTooltipEvent.getPoseStack(), storedStack.getStack(), itemTooltipEvent.getX() + 20 + 26 * i, itemTooltipEvent.getY() + 11, 512, NumberUtils.getFormatedBigNumber(storedStack.getAmount()) + "/" + NumberUtils.getFormatedBigNumber(iItemHandler.getSlotLimit(i)));
                        ++i;
                    }
                }
                if (iItemHandler instanceof CompactingStackItemHandler compactingStackItemHandler) {
                    int pos = 0;

                    for (int i = compactingStackItemHandler.getSlots(); i >= 0; i--) {
                        var stack = compactingStackItemHandler.getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            TooltipUtil.renderItemAdvanced(itemTooltipEvent.getPoseStack(), stack, itemTooltipEvent.getX() + 20 + 32 * pos, itemTooltipEvent.getY() + 11, 512, NumberUtils.getFormatedBigNumber(stack.getCount()) + "/" + NumberUtils.getFormatedBigNumber(iItemHandler.getSlotLimit(i)));
                            ++pos;
                        }
                    }
                }
            });
        }).subscribe();
        EventManager.mod(ModelEvent.RegisterGeometryLoaders.class).process(modelRegistryEvent -> {
            modelRegistryEvent.register("framedblock", FramedModel.Loader.INSTANCE);
        }).subscribe();
        EventManager.mod(TextureStitchEvent.Pre.class).process(pre -> {
            if (pre.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS))
                pre.addSprite(new ResourceLocation(MOD_ID, "gui/indicator"));
        }).subscribe();
    }

    @Override
    public void addDataProvider(GatherDataEvent event) {
        NonNullLazy<List<Block>> blocksToProcess = NonNullLazy.of(() ->
                ForgeRegistries.BLOCKS.getValues()
                        .stream()
                        .filter(basicBlock -> Optional.ofNullable(ForgeRegistries.BLOCKS.getKey(basicBlock))
                                .map(ResourceLocation::getNamespace)
                                .filter(MOD_ID::equalsIgnoreCase)
                                .isPresent())
                        .collect(Collectors.toList())
        );
        if (true) {
            event.getGenerator().addProvider(true, new BlockItemModelGeneratorProvider(event.getGenerator(), MOD_ID, blocksToProcess));
            event.getGenerator().addProvider(true, new FunctionalStorageBlockstateProvider(event.getGenerator(), event.getExistingFileHelper(), blocksToProcess));
            event.getGenerator().addProvider(true, new TitaniumLootTableProvider(event.getGenerator(), blocksToProcess));

            event.getGenerator().addProvider(true, new FunctionalStorageItemTagsProvider(event.getGenerator(), new BlockTagsProvider(event.getGenerator()), MOD_ID, event.getExistingFileHelper()));
            event.getGenerator().addProvider(true, new FunctionalStorageLangProvider(event.getGenerator(), MOD_ID, "en_us"));
            event.getGenerator().addProvider(true, new FunctionalStorageBlockTagsProvider(event.getGenerator(), MOD_ID, event.getExistingFileHelper()));
            event.getGenerator().addProvider(true, new ItemModelProvider(event.getGenerator(), MOD_ID, event.getExistingFileHelper()) {
                @Override
                protected void registerModels() {
                    blocksToProcess.get().forEach(block -> withExistingParent(ForgeRegistries.BLOCKS.getKey(block).getPath(), new ResourceLocation(FunctionalStorage.MOD_ID, "block/" + ForgeRegistries.BLOCKS.getKey(block).getPath())));
                    for (StorageUpgradeItem.StorageTier storageTier : STORAGE_UPGRADES.keySet()) {
                        item(STORAGE_UPGRADES.get(storageTier).get());
                    }
                    item(COLLECTOR_UPGRADE.get());
                    item(PULLING_UPGRADE.get());
                    item(PUSHING_UPGRADE.get());
                    item(VOID_UPGRADE.get());
                    item(REDSTONE_UPGRADE.get());
                    item(CREATIVE_UPGRADE.get());
                }

                private void item(Item item) {
                    singleTexture(ForgeRegistries.ITEMS.getKey(item).getPath(), new ResourceLocation("minecraft:item/generated"), "layer0", new ResourceLocation(MOD_ID, "items/" + ForgeRegistries.ITEMS.getKey(item).getPath()));
                }
            });
            event.getGenerator().addProvider(true, new BlockModelProvider(event.getGenerator(), MOD_ID, event.getExistingFileHelper()) {
                @Override
                protected void registerModels() {
                    for (DrawerType value : DrawerType.values()) {
                        for (RegistryObject<Block> blockRegistryObject : DRAWER_TYPES.get(value).stream().map(Pair::getLeft).collect(Collectors.toList())) {
                            if (blockRegistryObject.get() instanceof FramedDrawerBlock) {
                                continue;
                            }
                            withExistingParent(ForgeRegistries.BLOCKS.getKey(blockRegistryObject.get()).getPath() + "_locked", modLoc(ForgeRegistries.BLOCKS.getKey(blockRegistryObject.get()).getPath()))
                                    .texture("lock_icon", modLoc("blocks/lock"));
                        }
                    }
                    withExistingParent(ForgeRegistries.BLOCKS.getKey(COMPACTING_DRAWER.getLeft().get()).getPath() + "_locked", modLoc(ForgeRegistries.BLOCKS.getKey(COMPACTING_DRAWER.getLeft().get()).getPath()))
                            .texture("lock_icon", modLoc("blocks/lock"));
                    withExistingParent(ForgeRegistries.BLOCKS.getKey(ENDER_DRAWER.getLeft().get()).getPath() + "_locked", modLoc(ForgeRegistries.BLOCKS.getKey(ENDER_DRAWER.getLeft().get()).getPath()))
                            .texture("lock_icon", modLoc("blocks/lock"));
                    withExistingParent(ForgeRegistries.BLOCKS.getKey(FLUID_DRAWER_1.getLeft().get()).getPath() + "_locked", modLoc(ForgeRegistries.BLOCKS.getKey(FLUID_DRAWER_1.getLeft().get()).getPath()))
                            .texture("lock_icon", modLoc("blocks/lock"));
                    withExistingParent(ForgeRegistries.BLOCKS.getKey(FLUID_DRAWER_2.getLeft().get()).getPath() + "_locked", modLoc(ForgeRegistries.BLOCKS.getKey(FLUID_DRAWER_2.getLeft().get()).getPath()))
                            .texture("lock_icon", modLoc("blocks/lock"));
                    withExistingParent(ForgeRegistries.BLOCKS.getKey(FLUID_DRAWER_4.getLeft().get()).getPath() + "_locked", modLoc(ForgeRegistries.BLOCKS.getKey(FLUID_DRAWER_4.getLeft().get()).getPath()))
                            .texture("lock_icon", modLoc("blocks/lock"));
                    withExistingParent(ForgeRegistries.BLOCKS.getKey(SIMPLE_COMPACTING_DRAWER.getLeft().get()).getPath() + "_locked", modLoc(ForgeRegistries.BLOCKS.getKey(SIMPLE_COMPACTING_DRAWER.getLeft().get()).getPath()))
                            .texture("lock_icon", modLoc("blocks/lock"));
//                    withExistingParent(ForgeRegistries.BLOCKS.getKey(FRAMED_COMPACTING_DRAWER.getLeft().get()).getPath() + "_locked", modLoc(ForgeRegistries.BLOCKS.getKey(FRAMED_COMPACTING_DRAWER.getLeft().get()).getPath()))
//                            .texture("lock_icon", modLoc("blocks/lock"));
                }
            });
        }
        event.getGenerator().addProvider(true, new FunctionalStorageRecipesProvider(event.getGenerator(), blocksToProcess));
        event.getGenerator().addProvider(true, new TitaniumSerializableProvider(event.getGenerator(), MOD_ID) {
            @Override
            public void add(Map<IJsonFile, IJSONGenerator> serializables) {
                new CustomCompactingRecipe(new ResourceLocation("clay"), new ItemStack(Items.CLAY_BALL, 4), new ItemStack(Items.CLAY));
                new CustomCompactingRecipe(new ResourceLocation("glowstone"), new ItemStack(Items.GLOWSTONE_DUST, 4), new ItemStack(Items.GLOWSTONE));
                new CustomCompactingRecipe(new ResourceLocation("melon"), new ItemStack(Items.MELON_SLICE, 9), new ItemStack(Items.MELON));
                new CustomCompactingRecipe(new ResourceLocation("quartz"), new ItemStack(Items.QUARTZ, 4), new ItemStack(Items.QUARTZ_BLOCK));
                new CustomCompactingRecipe(new ResourceLocation("ice"), new ItemStack(Items.ICE, 9), new ItemStack(Items.PACKED_ICE));
                new CustomCompactingRecipe(new ResourceLocation("packed_ice"), new ItemStack(Items.PACKED_ICE, 9), new ItemStack(Items.BLUE_ICE));
                new CustomCompactingRecipe(new ResourceLocation("amethyst"), new ItemStack(Items.AMETHYST_SHARD, 9), new ItemStack(Items.AMETHYST_BLOCK));

                CustomCompactingRecipe.RECIPES.forEach(customCompactingRecipe -> serializables.put(customCompactingRecipe, customCompactingRecipe));
            }
        });
    }
}
