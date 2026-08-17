package com.buuz135.functionalstorage.client.item;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.client.DrawerRenderer;
import com.buuz135.functionalstorage.client.FluidDrawerRenderer;
import com.buuz135.functionalstorage.inventory.item.CompactingStackItemHandler;
import com.buuz135.functionalstorage.inventory.item.DrawerStackItemHandler;
import com.buuz135.functionalstorage.inventory.item.FluidDrawerStackItemHandler;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.util.Utils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.buuz135.functionalstorage.util.MathUtils.createTransformMatrix;


public final class DrawerSpecialRenderer implements SpecialModelRenderer<DrawerSpecialRenderer.RenderData> {
    private final Kind kind;

    private DrawerSpecialRenderer(Kind kind) {
        this.kind = kind;
    }

    @Override
    public void submit(@Nullable RenderData data, PoseStack pose, SubmitNodeCollector collector, int light, int overlay, boolean foil, int outlineColor) {
        if (data == null) return;

        // Match the north-facing block model coordinates used by the former ISTER.
        pose.mulPose(Axis.YP.rotationDegrees(180));
        pose.mulPose(Axis.XN.rotationDegrees(90));
        pose.mulPose(Axis.YP.rotationDegrees(90));
        pose.translate(-1, 0, -1);
        pose.mulPose(Axis.YP.rotationDegrees(-90));
        pose.mulPose(Axis.XP.rotationDegrees(90));

        if (kind == Kind.FLUID) {
            pose.translate(0, -1, -1);
            renderFluids(data, pose, collector, light);
        } else {
            pose.translate(0, -1, 0);
            renderItems(data, pose, collector, light);
        }
    }

    private void renderItems(RenderData data, PoseStack pose, SubmitNodeCollector collector, int light) {
        List<ItemEntry> entries = data.items();
        if (kind == Kind.DRAWER) {
            if (entries.size() == 1) {
                renderItem(entries, 0, pose, collector, light, new Vector3f(0.5F, 0.5F, 0.0005F), new Vector3f(1));
            } else if (entries.size() == 2) {
                renderItem(entries, 0, pose, collector, light, new Vector3f(0.5F, 0.27F, 0.0005F), new Vector3f(0.5F, 0.5F, 1));
                renderItem(entries, 1, pose, collector, light, new Vector3f(0.5F, 0.77F, 0.0005F), new Vector3f(0.5F, 0.5F, 1));
            } else {
                renderItem(entries, 0, pose, collector, light, new Vector3f(0.75F, 0.27F, 0.0005F), new Vector3f(0.5F, 0.5F, 1));
                renderItem(entries, 1, pose, collector, light, new Vector3f(0.25F, 0.27F, 0.0005F), new Vector3f(0.5F, 0.5F, 1));
                renderItem(entries, 2, pose, collector, light, new Vector3f(0.75F, 0.77F, 0.0005F), new Vector3f(0.5F, 0.5F, 1));
                renderItem(entries, 3, pose, collector, light, new Vector3f(0.25F, 0.77F, 0.0005F), new Vector3f(0.5F, 0.5F, 1));
            }
        } else if (kind == Kind.SIMPLE_COMPACTING) {
            renderItem(entries, 0, pose, collector, light, new Vector3f(0.5F, 0.27F, 0.0005F), new Vector3f(0.5F, 0.5F, 1));
            renderItem(entries, 1, pose, collector, light, new Vector3f(0.5F, 0.77F, 0.0005F), new Vector3f(0.5F, 0.5F, 1));
        } else {
            renderItem(entries, 0, pose, collector, light, new Vector3f(0.75F, 0.27F, 0.0005F), new Vector3f(0.5F, 0.5F, 1));
            renderItem(entries, 1, pose, collector, light, new Vector3f(0.25F, 0.27F, 0.0005F), new Vector3f(0.5F, 0.5F, 1));
            renderItem(entries, 2, pose, collector, light, new Vector3f(0.5F, 0.77F, 0.0005F), new Vector3f(0.5F, 0.5F, 1));
        }
    }

    private static void renderItem(List<ItemEntry> entries, int slot, PoseStack pose, SubmitNodeCollector collector, int light, Vector3f position, Vector3f scale) {
        if (slot >= entries.size()) return;
        ItemEntry entry = entries.get(slot);
        if (entry.stack().isEmpty()) return;
        pose.pushPose();
        pose.mulPose(createTransformMatrix(position, new Vector3f(), scale));
        DrawerRenderer.renderStack(pose, collector, light, entry.stack(), entry.amount(), entry.capacity(), 0.02F, entry.options(), Minecraft.getInstance().level);
        pose.popPose();
    }

    private static void renderFluids(RenderData data, PoseStack pose, SubmitNodeCollector collector, int light) {
        int slots = data.fluids().size();
        for (int slot = 0; slot < slots; slot++) {
            FluidEntry entry = data.fluids().get(slot);
            if (entry.stack().isEmpty()) continue;
            pose.pushPose();
            boolean halfText = slots == 4;
            boolean smallBar = slots != 1;
            double maxX = halfText ? 8 / 16D : 15 / 16D;
            double maxY = 1.25 / 16D + (entry.amount() / (double) Math.max(1, entry.capacity())) * ((slots == 1 ? 12.5 : 5.5) / 16D);
            if (slots >= 2 && slot >= slots / 2) pose.translate(0, 0.5, 0);
            if (slots == 4 && (slot == 0 || slot == 2)) pose.translate(0.5, 0, 0);
            AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, maxX, maxY, 15 / 16D);
            FluidDrawerRenderer.renderFluidStack(pose, collector, light, entry.stack(), entry.amount(), entry.capacity(), 0.007F, entry.options(), bounds, halfText, smallBar);
            pose.popPose();
        }
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        // The ordinary block-model layer in the composite supplies the item extents.
    }

    @Override
    public @Nullable RenderData extractArgument(ItemStack stack) {
        ControllableDrawerTile.DrawerOptions options = readOptions(stack);
        if (kind == Kind.FLUID) {
            var handler = stack.getCapability(Capabilities.Fluid.ITEM, ItemAccess.forStack(stack));
            if (!(handler instanceof FluidDrawerStackItemHandler fluids)) return null;
            List<FluidEntry> entries = new ArrayList<>(fluids.size());
            for (int slot = 0; slot < fluids.size(); slot++) {
                FluidStack fluid = fluids.getFluidInTank(slot);
                entries.add(new FluidEntry(fluid, fluid.getAmount(), fluids.getTankCapacity(slot), options));
            }
            return new RenderData(List.of(), entries);
        }

        var handler = stack.getCapability(Capabilities.Item.ITEM, ItemAccess.forStack(stack));
        List<ItemEntry> entries = new ArrayList<>();
        if (handler instanceof DrawerStackItemHandler drawer) {
            for (int slot = 0; slot < drawer.size(); slot++) {
                entries.add(new ItemEntry(drawer.getStackInSlot(slot), (int) drawer.getAmountAsLong(slot), drawer.getSlotLimit(slot), options));
            }
        } else if (handler instanceof CompactingStackItemHandler compacting) {
            int slots = kind == Kind.SIMPLE_COMPACTING ? 2 : 3;
            for (int slot = 0; slot < slots; slot++) {
                entries.add(new ItemEntry(compacting.getStackInSlot(slot), (int) compacting.getAmountAsLong(slot), compacting.getSlotLimit(slot), options));
            }
        }
        return entries.isEmpty() ? null : new RenderData(entries, List.of());
    }

    private static ControllableDrawerTile.DrawerOptions readOptions(ItemStack stack) {
        var options = new ControllableDrawerTile.DrawerOptions();
        if (stack.has(FSAttachments.TILE)) {
            var root = stack.get(FSAttachments.TILE);
            var data = root.contains("TitaniumData") ? root.getCompoundOrEmpty("TitaniumData") : root;
            options.deserializeNBT(Utils.registryAccess(), data.getCompoundOrEmpty("drawerOptions"));
        }
        return options;
    }

    public enum Kind {
        DRAWER, COMPACTING, SIMPLE_COMPACTING, FLUID
    }

    public record Unbaked(Kind kind) implements SpecialModelRenderer.Unbaked<RenderData> {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.xmap(Kind::valueOf, Kind::name).fieldOf("kind").forGetter(Unbaked::kind)
        ).apply(instance, Unbaked::new));

        @Override
        public DrawerSpecialRenderer bake(SpecialModelRenderer.BakingContext context) {
            return new DrawerSpecialRenderer(kind);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }

    public record RenderData(List<ItemEntry> items, List<FluidEntry> fluids) {}
    public record ItemEntry(ItemStack stack, int amount, int capacity, ControllableDrawerTile.DrawerOptions options) {}
    public record FluidEntry(FluidStack stack, int amount, int capacity, ControllableDrawerTile.DrawerOptions options) {}
}
