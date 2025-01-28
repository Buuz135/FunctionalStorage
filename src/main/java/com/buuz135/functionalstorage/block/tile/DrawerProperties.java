package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.item.component.SizeProvider;
import net.minecraft.core.component.DataComponentType;

import java.util.function.Supplier;

public record DrawerProperties(int baseSize, Supplier<DataComponentType<SizeProvider>> upgradeComponent) {
}
