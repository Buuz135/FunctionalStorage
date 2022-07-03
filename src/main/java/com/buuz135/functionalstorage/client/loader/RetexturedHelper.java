package com.buuz135.functionalstorage.client.loader;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

/**
 * Class from Mantle {@url https://github.com/SlimeKnights/Mantle/blob/1.18.2/src/main/java/slimeknights/mantle/client/}
 *
 * This utility contains helpers to handle the NBT for retexturable blocks
 */

public final class RetexturedHelper {
    /** Tag name for texture blocks. Should not be used directly, use the utils to interact */
    private static final String TAG_TEXTURE = "texture";
    /** Property for tile entities containing a texture block */


    /* Getting */

    /**
     * Gets a block for the given name
     * @param name  Block name
     * @return  Block entry, or {@link Blocks#AIR} if no match
     */
    public static Block getBlock(String name) {
        if (!name.isEmpty()) {
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name));
            if (block != null) {
                return block;
            }
        }
        return Blocks.AIR;
    }

    /**
     * Gets the name of the texture from NBT
     * @param nbt  NBT tag
     * @return  Name of the texture, or empty if no texture
     */
    public static String getTextureName(@Nullable CompoundTag nbt) {
        if (nbt == null) {
            return "";
        }
        return nbt.getString(TAG_TEXTURE);
    }


    /* Setting */

    /**
     * Sets the texture in an NBT instance
     * @param nbt      Tag instance
     * @param texture  Texture to set
     */
    public static void setTexture(@Nullable CompoundTag nbt, String texture) {
        if (nbt != null) {
            if (texture.isEmpty()) {
                nbt.remove(TAG_TEXTURE);
            } else {
                nbt.putString(TAG_TEXTURE, texture);
            }
        }
    }
}