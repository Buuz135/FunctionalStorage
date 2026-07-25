package com.buuz135.functionalstorage.compat.ftb;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import dev.ftb.mods.ftbchunks.api.Protection;
import dev.ftb.mods.ftbchunks.data.ClaimedChunkManagerImpl;

public class FTBChunksManager {

    public static boolean preventInteraction(BlockPos pos, Player player) {
        return ClaimedChunkManagerImpl.getInstance().shouldPreventInteraction(player, InteractionHand.MAIN_HAND, pos, Protection.INTERACT_BLOCK, null) ||
                ClaimedChunkManagerImpl.getInstance().shouldPreventInteraction(player, InteractionHand.OFF_HAND, pos, Protection.INTERACT_BLOCK, null);
    }
}
