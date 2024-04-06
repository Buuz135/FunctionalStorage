package com.buuz135.functionalstorage.world;

import com.buuz135.functionalstorage.inventory.EnderInventoryHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;

public class EnderSavedData extends SavedData {

    public static EnderSavedData CLIENT = new EnderSavedData(null);

    public static final String NAME = "FunctionalStorageEnder";

    private HashMap<String, EnderInventoryHandler> itemHandlers;

    private Level level;

    public EnderSavedData(Level level) {
        this.itemHandlers = new HashMap<>();
        this.level = level;
    }

    public static EnderSavedData getInstance(LevelAccessor accessor){
        if (accessor instanceof ServerLevel){
            ServerLevel serverWorld = ((ServerLevel) accessor).getServer().getLevel(Level.OVERWORLD);
            EnderSavedData data = serverWorld.getDataStorage().computeIfAbsent(
                    new Factory<EnderSavedData>(() -> new EnderSavedData((ServerLevel)accessor), tag -> EnderSavedData.load(tag, (ServerLevel) accessor)), NAME);
            return data;
        } else if (accessor instanceof ClientLevel){
            return CLIENT;
        }
        return null;
    }

    private static EnderSavedData load(CompoundTag compoundTag, Level level) {
        EnderSavedData manager = new EnderSavedData(level);
        manager.itemHandlers = new HashMap<>();
        CompoundTag backpacks = compoundTag.getCompound("Ender");
        for (String s : backpacks.getAllKeys()) {
            EnderInventoryHandler hander = new EnderInventoryHandler(s, manager);
            hander.deserializeNBT(backpacks.getCompound(s));
            manager.itemHandlers.put(s, hander);
        }

        return manager;
    }

    public Level getLevel() {
        return level;
    }

    public void setFrenquency(String frequency, EnderInventoryHandler handler){
        itemHandlers.put(frequency, handler);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag nbt = new CompoundTag();
        itemHandlers.forEach((s, iItemHandler) -> nbt.put(s, iItemHandler.serializeNBT()));
        tag.put("Ender", nbt);
        return tag;
    }

    public EnderInventoryHandler getFrequency(String string){
        return itemHandlers.computeIfAbsent(string, s -> new EnderInventoryHandler(s, this));
    }

    public HashMap<String, EnderInventoryHandler> getItemHandlers() {
        return itemHandlers;
    }
}
