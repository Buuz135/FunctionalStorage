package com.buuz135.functionalstorage.network;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.hrznstudio.titanium.network.Message;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class DrawerPriorityMessage extends Message {

    public int x;
    public int y;
    public int z;
    public int priority;

    public DrawerPriorityMessage(BlockPos pos, int priority) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.priority = priority;
    }

    public DrawerPriorityMessage() {
    }

    @Override
    protected void handleMessage(IPayloadContext context) {
        BlockPos pos = new BlockPos(x, y, z);
        if (context.player().distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64D) {
            return;
        }
        if (context.player().level().getBlockEntity(pos) instanceof ControllableDrawerTile<?> drawer) {
            drawer.setPriority(Math.max(0, priority));
        }
    }
}
