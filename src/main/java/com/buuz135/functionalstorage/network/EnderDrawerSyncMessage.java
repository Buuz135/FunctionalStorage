package com.buuz135.functionalstorage.network;

import com.buuz135.functionalstorage.inventory.EnderInventoryHandler;
import com.buuz135.functionalstorage.world.EnderSavedData;
import com.hrznstudio.titanium.network.CompoundSerializableDataHandler;
import com.hrznstudio.titanium.network.Message;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class EnderDrawerSyncMessage extends Message {

    static {
        CompoundSerializableDataHandler.map(EnderInventoryHandler.class, buf -> {
            EnderInventoryHandler handler = new EnderInventoryHandler(buf.readUtf(), EnderSavedData.CLIENT);
            handler.deserializeNBT(buf.registryAccess(), buf.readNbt());
            return handler;
        }, (buf, handler1) -> {
            buf.writeUtf(handler1.getFrequency());
            buf.writeNbt(handler1.serializeNBT(buf.registryAccess()));
        });
    }


    public String frequency;
    public EnderInventoryHandler handler;

    public EnderDrawerSyncMessage(String frequency, EnderInventoryHandler handler) {
        this.frequency = frequency;
        this.handler = handler;
    }

    public EnderDrawerSyncMessage() {
    }

    @Override
    protected void handleMessage(IPayloadContext context) {
        EnderSavedData.getInstance(context.player().level()).setFrenquency(frequency, handler);
    }
}
