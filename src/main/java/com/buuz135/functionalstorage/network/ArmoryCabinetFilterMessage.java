package com.buuz135.functionalstorage.network;

import com.buuz135.functionalstorage.inventory.ArmoryCabinetMenu;
import com.hrznstudio.titanium.network.Message;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.BitSet;

public class ArmoryCabinetFilterMessage extends Message {

    public int containerId;
    public long[] slots;

    public ArmoryCabinetFilterMessage(int containerId, BitSet slots) {
        this.containerId = containerId;
        this.slots = slots.toLongArray();
    }

    public ArmoryCabinetFilterMessage() {
    }

    @Override
    protected void handleMessage(IPayloadContext context) {
        if (context.player().containerMenu instanceof ArmoryCabinetMenu menu && menu.containerId == containerId) {
            menu.setFilteredSlots(BitSet.valueOf(slots));
        }
    }
}
