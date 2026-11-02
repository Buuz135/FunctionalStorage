package com.buuz135.functionalstorage.test;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.testframework.conf.FrameworkConfiguration;

@Mod(FunctionalStorageTestMod.MOD_ID)
public class FunctionalStorageTestMod {
    public static final String MOD_ID = "functionalstorage_test";
    public FunctionalStorageTestMod(IEventBus modBus, ModContainer container) {
        final var framework = FrameworkConfiguration.builder(Identifier.fromNamespaceAndPath(MOD_ID, "test")) // The ID of the framework. Used by logging, primarily
                .build()
                .create();

        // Initialise this framework, using the mod event bus of the currently loading mod, and the container of the currently loading mod.
        // The container is used for collecting annotations.
        // This method will collect and register tests, structure templates, group data, and will fire init listeners.
        framework.init(modBus, container);

        // Register the commands of the framework under the `tests` top-level command.
        NeoForge.EVENT_BUS.addListener((final RegisterCommandsEvent event) -> {
            final LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal("tests");
            framework.registerCommands(node);
            event.getDispatcher().register(node);
        });
    }
}
