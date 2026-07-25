package com.buuz135.functionalstorage.item.component;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

public record MoveItemsBehavior(boolean drawerIsSource, int itemsPerOperation) implements FunctionalUpgradeBehavior {
    public static final MapCodec<MoveItemsBehavior> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            Codec.BOOL.fieldOf("drawer_is_source").forGetter(MoveItemsBehavior::drawerIsSource),
            Codec.INT.fieldOf("items_per_operation").forGetter(MoveItemsBehavior::itemsPerOperation)
    ).apply(in, MoveItemsBehavior::new));

    @Override
    public void work(Level level, BlockPos pos, ControllableDrawerTile<?> dr, ItemStack upgradeStack, int upgradeSlot) {
        if (!(dr instanceof ItemControllableDrawerTile<?> drawer)) return;

        Direction direction = UpgradeItem.getDirection(upgradeStack);
        var otherHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.relative(direction), direction.getOpposite());

        if (otherHandler != null) {
            IItemHandler source = drawerIsSource ? drawer.getStorage() : otherHandler;
            IItemHandler destination = drawerIsSource ? otherHandler : drawer.getStorage();

            for (int sourceSlot = 0; sourceSlot < source.getSlots(); sourceSlot++) {
                ItemStack pulledStack = source.extractItem(sourceSlot, itemsPerOperation, true);
                if (pulledStack.isEmpty()) continue;

                for (int destinationSlot = 0; destinationSlot < destination.getSlots(); destinationSlot++) {
                    if (destination.getStackInSlot(destinationSlot).getCount() >= destination.getSlotLimit(destinationSlot))
                        continue;
                    ItemStack remainder = destination.insertItem(destinationSlot, pulledStack, true);
                    if (remainder.getCount() < pulledStack.getCount()) {
                        destination.insertItem(destinationSlot, source.extractItem(sourceSlot, pulledStack.getCount() - remainder.getCount(), false), false);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public MapCodec<? extends FunctionalUpgradeBehavior> codec() {
        return CODEC;
    }
}
