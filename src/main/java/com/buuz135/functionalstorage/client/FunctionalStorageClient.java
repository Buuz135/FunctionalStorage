package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.Drawer;
import com.buuz135.functionalstorage.block.EnderDrawerBlock;
import com.buuz135.functionalstorage.block.tile.*;
import com.buuz135.functionalstorage.client.gui.ArmoryCabinetScreen;
import com.buuz135.functionalstorage.client.item.DrawerSpecialRenderer;
import com.buuz135.functionalstorage.client.item.FramedSpecialRenderer;
import com.buuz135.functionalstorage.client.loader.FramedModel;
import com.buuz135.functionalstorage.inventory.item.CompactingStackItemHandler;
import com.buuz135.functionalstorage.inventory.item.DrawerStackItemHandler;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.buuz135.functionalstorage.util.NumberUtils;
import com.buuz135.functionalstorage.util.Utils;
import com.mojang.datafixers.util.Either;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.client.screen.container.BasicAddonScreen;
import com.hrznstudio.titanium.event.handler.EventManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

public final class FunctionalStorageClient {
    private FunctionalStorageClient() {
    }

    @SuppressWarnings("unchecked")
    public static void init() {
        EventManager.mod(EntityRenderersEvent.RegisterRenderers.class).process(event -> {
            for (FunctionalStorage.DrawerType type : FunctionalStorage.DrawerType.values()) {
                FunctionalStorage.DRAWER_TYPES.get(type).forEach(block -> event.registerBlockEntityRenderer(
                        (BlockEntityType<? extends DrawerTile>) block.type().get(), context -> new DrawerRenderer()));
            }
            event.registerBlockEntityRenderer((BlockEntityType<? extends CompactingDrawerTile>) FunctionalStorage.COMPACTING_DRAWER.type().get(), context -> new CompactingDrawerRenderer());
            event.registerBlockEntityRenderer((BlockEntityType<? extends CompactingDrawerTile>) FunctionalStorage.FRAMED_COMPACTING_DRAWER.type().get(), context -> new CompactingDrawerRenderer());
            event.registerBlockEntityRenderer((BlockEntityType<? extends EnderDrawerTile>) FunctionalStorage.ENDER_DRAWER.type().get(), context -> new EnderDrawerRenderer());
            event.registerBlockEntityRenderer((BlockEntityType<? extends FluidDrawerTile>) FunctionalStorage.FLUID_DRAWER_1.type().get(), context -> new FluidDrawerRenderer());
            event.registerBlockEntityRenderer((BlockEntityType<? extends FluidDrawerTile>) FunctionalStorage.FLUID_DRAWER_2.type().get(), context -> new FluidDrawerRenderer());
            event.registerBlockEntityRenderer((BlockEntityType<? extends FluidDrawerTile>) FunctionalStorage.FLUID_DRAWER_4.type().get(), context -> new FluidDrawerRenderer());
            event.registerBlockEntityRenderer((BlockEntityType<? extends FluidDrawerTile>) FunctionalStorage.FRAMED_FLUID_DRAWER_1.type().get(), context -> new FluidDrawerRenderer());
            event.registerBlockEntityRenderer((BlockEntityType<? extends FluidDrawerTile>) FunctionalStorage.FRAMED_FLUID_DRAWER_2.type().get(), context -> new FluidDrawerRenderer());
            event.registerBlockEntityRenderer((BlockEntityType<? extends FluidDrawerTile>) FunctionalStorage.FRAMED_FLUID_DRAWER_4.type().get(), context -> new FluidDrawerRenderer());
            event.registerBlockEntityRenderer((BlockEntityType<? extends SimpleCompactingDrawerTile>) FunctionalStorage.SIMPLE_COMPACTING_DRAWER.type().get(), context -> new SimpleCompactingDrawerRenderer());
            event.registerBlockEntityRenderer((BlockEntityType<? extends SimpleCompactingDrawerTile>) FunctionalStorage.FRAMED_SIMPLE_COMPACTING_DRAWER.type().get(), context -> new SimpleCompactingDrawerRenderer());
            event.registerBlockEntityRenderer((BlockEntityType<? extends StorageControllerTile<?>>) FunctionalStorage.DRAWER_CONTROLLER.type().get(), context -> new ControllerRenderer());
            event.registerBlockEntityRenderer((BlockEntityType<? extends StorageControllerTile<?>>) FunctionalStorage.FRAMED_DRAWER_CONTROLLER.type().get(), context -> new ControllerRenderer());
        }).subscribe();

        EventManager.mod(RegisterMenuScreensEvent.class).process(event ->
                event.register((net.minecraft.world.inventory.MenuType<com.buuz135.functionalstorage.inventory.ArmoryCabinetMenu>)
                        FunctionalStorage.ARMORY_CABINET_MENU.get(), ArmoryCabinetScreen::new)).subscribe();

        EventManager.mod(RegisterColorHandlersEvent.ItemTintSources.class).process(event ->
                event.register(Utils.resourceLocation(FunctionalStorage.MOD_ID, "tool"), ToolTintSource.MAP_CODEC)).subscribe();
        EventManager.mod(RegisterSpecialModelRendererEvent.class).process(event ->
                event.register(Utils.resourceLocation(FunctionalStorage.MOD_ID, "drawer"), DrawerSpecialRenderer.Unbaked.MAP_CODEC)).subscribe();
        EventManager.mod(RegisterSpecialModelRendererEvent.class).process(event ->
                event.register(Utils.resourceLocation(FunctionalStorage.MOD_ID, "framed"), FramedSpecialRenderer.Unbaked.MAP_CODEC)).subscribe();
        EventManager.mod(ModelEvent.ModifyBakingResult.class).process(FramedModel::wrapModels).subscribe();
        EventManager.mod(RegisterClientTooltipComponentFactoriesEvent.class).process(event ->
                event.register(DrawerClientTooltipComponent.Contents.class, DrawerClientTooltipComponent::new)).subscribe();


        EventManager.forge(ItemTooltipEvent.class).filter(
                        event -> UpgradeItem.isDirectionUpgrade(event.getItemStack().getItem()) && event.getItemStack().has(FSAttachments.DIRECTION)
                ).filter(event -> Minecraft.getInstance().screen != null && Minecraft.getInstance().screen instanceof BasicAddonScreen bcs && (bcs.getMenu().getObject() instanceof ItemControllableDrawerTile<?> || bcs.getMenu().getObject() instanceof FluidDrawerTile))
                .process(event -> {
                    var sc = (BasicAddonScreen) Minecraft.getInstance().screen;
                    var blockstate = ((ControllableDrawerTile<?>) sc.getMenu().getObject()).getBlockState();
                    if (blockstate.hasProperty(Drawer.FACING_HORIZONTAL_CUSTOM)) {
                        var direction = blockstate.getValue(Drawer.FACING_HORIZONTAL_CUSTOM);
                        if (blockstate.hasProperty(Drawer.FACING_ALL) && (direction == Direction.UP || direction == Direction.DOWN)) {
                            var subdirection = blockstate.getValue(RotatableBlock.FACING_ALL);
                            event.getToolTip().add(3, Component.translatable("drawer_upgrade.functionalstorage.relative_direction", UpgradeItem.getRelativeDirectionVertical(direction, subdirection, UpgradeItem.getDirection(event.getItemStack()))).withStyle(ChatFormatting.YELLOW));
                        } else {
                            event.getToolTip().add(3, Component.translatable("drawer_upgrade.functionalstorage.relative_direction", UpgradeItem.getRelativeDirection(direction, UpgradeItem.getDirection(event.getItemStack()))).withStyle(ChatFormatting.YELLOW));
                        }
                    } else if (blockstate.hasProperty(Drawer.FACING_HORIZONTAL)) {
                        var direction = blockstate.getValue(Drawer.FACING_HORIZONTAL);
                        event.getToolTip().add(3, Component.translatable("drawer_upgrade.functionalstorage.relative_direction", UpgradeItem.getRelativeDirection(direction, UpgradeItem.getDirection(event.getItemStack()))).withStyle(ChatFormatting.YELLOW));
                    }
                }).subscribe();

        EventManager.forge(ItemTooltipEvent.class).process(event -> {
            var stack = event.getItemStack();
            var tooltip = event.getToolTip();

            var item = stack.get(FSAttachments.ITEM_STORAGE_MODIFIER);
            if (item != null) {
                tooltip.add(item.getTooltip(Component.translatable("storageupgrade.obj.item_storage")).copy().withStyle(ChatFormatting.GRAY));
            }
            var fluid = stack.get(FSAttachments.FLUID_STORAGE_MODIFIER);
            if (fluid != null) {
                tooltip.add(fluid.getTooltip(Component.translatable("storageupgrade.obj.fluid_storage")).copy().withStyle(ChatFormatting.GRAY));
            }
            var range = stack.get(FSAttachments.CONTROLLER_RANGE_MODIFIER);
            if (range != null) {
                tooltip.add(range.getTooltip(Component.translatable("storageupgrade.obj.controller_range")).copy().withStyle(ChatFormatting.GRAY));
            }
            var functional = stack.get(FSAttachments.FUNCTIONAL_BEHAVIOR);
            if (functional != null && stack.getItem() != FunctionalStorage.PUSHING_UPGRADE.get() && stack.getItem() != FunctionalStorage.PULLING_UPGRADE.get()) {
                tooltip.addAll(functional.getTooltip());
            }
        }).subscribe();

        EventManager.forge(RenderGuiEvent.Post.class).process(event -> renderFluidDrawerHint(event.getGuiGraphics())).subscribe();
        EventManager.forge(RenderTooltipEvent.GatherComponents.class).process(FunctionalStorageClient::addDrawerTooltipContents).subscribe();

    }

    private static void addDrawerTooltipContents(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        DrawerClientTooltipComponent.Contents contents = null;
        if (stack.is(FunctionalStorage.ENDER_DRAWER.getBlock().asItem()) && stack.has(FSAttachments.TILE)) {
            var tile = stack.get(FSAttachments.TILE);
            var titaniumData = tile.contains("TitaniumData") ? tile.getCompoundOrEmpty("TitaniumData") : tile;
            String frequency = titaniumData.getStringOr("frequency", "");
            if (!frequency.isEmpty()) {
                contents = frequencyContents(frequency);
            }
        } else if (stack.is(FunctionalStorage.LINKING_TOOL.get()) && stack.has(FSAttachments.ENDER_FREQUENCY)) {
            contents = frequencyContents(stack.get(FSAttachments.ENDER_FREQUENCY));
        }

        var handler = stack.getCapability(Capabilities.Item.ITEM, ItemAccess.forStack(stack));
        if (handler instanceof DrawerStackItemHandler drawerHandler) {
            List<DrawerClientTooltipComponent.Entry> entries = new java.util.ArrayList<>();
            for (var storedStack : drawerHandler.getStoredStacks()) {
                if (!storedStack.getStack().isEmpty()) {
                    String amount = NumberUtils.getFormatedBigNumber(drawerHandler.isCreative() ? Integer.MAX_VALUE : storedStack.getAmount());
                    entries.add(new DrawerClientTooltipComponent.Entry(storedStack.getStack(), amount));
                }
            }
            if (!entries.isEmpty()) contents = new DrawerClientTooltipComponent.Contents(entries, 32);
        } else if (handler instanceof CompactingStackItemHandler compactingHandler) {
            List<DrawerClientTooltipComponent.Entry> entries = new java.util.ArrayList<>();
            for (int slot = 0; slot < compactingHandler.getSlots(); slot++) {
                ItemStack stored = compactingHandler.getStackInSlot(slot);
                if (!stored.isEmpty()) {
                    String amount = NumberUtils.getFormatedBigNumber(stored.getCount()) + "/" + NumberUtils.getFormatedBigNumber(compactingHandler.getSlotLimit(slot));
                    entries.add(new DrawerClientTooltipComponent.Entry(stored, amount));
                }
            }
            if (!entries.isEmpty()) contents = new DrawerClientTooltipComponent.Contents(entries, 32);
        }

        if (contents != null) {
            var elements = event.getTooltipElements();
            elements.add(Math.min(2, elements.size()), Either.right(contents));
        }
    }

    private static DrawerClientTooltipComponent.Contents frequencyContents(String frequency) {
        return new DrawerClientTooltipComponent.Contents(EnderDrawerBlock.getFrequencyDisplay(frequency).stream()
                .map(item -> new DrawerClientTooltipComponent.Entry(item, null)).toList(), 18);
    }

    private static void renderFluidDrawerHint(GuiGraphicsExtractor guiGraphics) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.screen != null || minecraft.player == null || minecraft.level == null) {
            return;
        }
        if (!(minecraft.hitResult instanceof BlockHitResult blockHitResult) || blockHitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }
        if (!(minecraft.level.getBlockEntity(blockHitResult.getBlockPos()) instanceof FluidDrawerTile)) {
            return;
        }
        if (!isFluidContainer(minecraft.player.getItemInHand(InteractionHand.MAIN_HAND)) && !isFluidContainer(minecraft.player.getItemInHand(InteractionHand.OFF_HAND))) {
            return;
        }



        List<ClientTooltipComponent> tooltip = List.of(
                Component.translatable("gui.functionalstorage.fluid_drawer_hint").withStyle(ChatFormatting.GOLD),
                Component.translatable("gui.functionalstorage.fluid_drawer_hint.empty", minecraft.options.keyUse.getTranslatedKeyMessage()).withStyle(ChatFormatting.GRAY),
                Component.translatable("gui.functionalstorage.fluid_drawer_hint.fill", minecraft.options.keyAttack.getTranslatedKeyMessage()).withStyle(ChatFormatting.GRAY)
        ).stream().map(mutableComponent -> ClientTooltipComponent.create(mutableComponent.getVisualOrderText())).toList();
        guiGraphics.tooltip(minecraft.font, tooltip, guiGraphics.guiWidth() / 2 + 12, guiGraphics.guiHeight() / 2 + 12,
                (i, i1, i2, i3, i4, i5) -> new Vector2i(minecraft.getWindow().getGuiScaledWidth() / 2 + 10, minecraft.getWindow().getGuiScaledHeight() / 2 -4), null
                );
    }

    private static boolean isFluidContainer(ItemStack stack) {
        return !stack.isEmpty() && stack.getCapability(Capabilities.Fluid.ITEM, ItemAccess.forStack(stack)) != null;
    }
}
