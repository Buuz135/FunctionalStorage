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
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;

public record MoveItemsBehavior(boolean drawerIsSource, int itemsPerOperation) implements FunctionalUpgradeBehavior {
    public static final MapCodec<MoveItemsBehavior> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            Codec.BOOL.fieldOf("drawer_is_source").forGetter(MoveItemsBehavior::drawerIsSource),
            Codec.INT.fieldOf("items_per_operation").forGetter(MoveItemsBehavior::itemsPerOperation)
    ).apply(in, MoveItemsBehavior::new));

    @Override
    public void work(Level level, BlockPos pos, ControllableDrawerTile<?> dr, ItemStack upgradeStack, int upgradeSlot) {
        if (!(dr instanceof ItemControllableDrawerTile<?> drawer)) return;

        Direction direction = UpgradeItem.getDirection(upgradeStack);
        var otherResourceHandler = level.getCapability(Capabilities.Item.BLOCK, pos.relative(direction), direction.getOpposite());

        if (otherResourceHandler != null) {
            ResourceHandler<ItemResource> source = drawerIsSource ? drawer.getStorage() : otherResourceHandler;
            ResourceHandler<ItemResource> destination = drawerIsSource ? otherResourceHandler : drawer.getStorage();
            ResourceHandlerUtil.moveStacking(source, destination, resource -> true, itemsPerOperation, null);
        }
    }

    @Override
    public MapCodec<? extends FunctionalUpgradeBehavior> codec() {
        return CODEC;
    }
}
