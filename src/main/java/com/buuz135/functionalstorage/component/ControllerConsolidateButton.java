package com.buuz135.functionalstorage.component;

import com.buuz135.functionalstorage.client.gui.ControllerConsolidateGuiAddon;
import com.buuz135.functionalstorage.util.DrawerConsolidation;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.component.button.ButtonComponent;
import com.hrznstudio.titanium.container.addon.IContainerAddon;
import net.minecraft.world.inventory.ContainerData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class ControllerConsolidateButton extends ButtonComponent implements IContainerAddon, ContainerData {
    public static final int IDLE = 0, RUNNING = 1, DONE = 2, STOPPED = 3, FAILED = 4;
    // Menu data travels in 16-bit words. Keep full counters, including long totals.
    private final int[] status = new int[15];

    public ControllerConsolidateButton() {
        super(10, 30, 98, 16);
    }

    public void update(int state, DrawerConsolidation.Progress progress, DrawerConsolidation.Result result) {
        put(0, progress.checked(), 2);
        put(2, progress.drawers(), 2);
        put(4, progress.processedTypes(), 2);
        put(6, progress.types(), 2);
        put(8, result.moved(), 4);
        put(12, result.freed(), 2);
        status[14] = state;
    }

    public int state() { return status[14]; }
    public DrawerConsolidation.Progress progress() {
        return new DrawerConsolidation.Progress((int) read(0, 2), (int) read(2, 2), (int) read(4, 2), (int) read(6, 2));
    }
    public DrawerConsolidation.Result result() {
        return new DrawerConsolidation.Result(read(8, 4), (int) read(12, 2));
    }

    private void put(int index, long value, int words) {
        for (int word = 0; word < words; word++) status[index + word] = (int) (value >>> (word * 16)) & 0xffff;
    }

    private long read(int index, int words) {
        long value = 0;
        for (int word = 0; word < words; word++) value |= (long) status[index + word] << (word * 16);
        return value;
    }

    @Override
    public int get(int index) { return status[index]; }
    @Override
    public void set(int index, int value) { status[index] = value & 0xffff; }
    @Override
    public int getCount() { return status.length; }
    @Override
    public List<ContainerData> getIntArrayReferenceHolders() { return List.of(this); }

    @OnlyIn(Dist.CLIENT)
    @Override
    public List<IFactory<?>> getScreenAddons() {
        return List.of(() -> new ControllerConsolidateGuiAddon(this));
    }
}
