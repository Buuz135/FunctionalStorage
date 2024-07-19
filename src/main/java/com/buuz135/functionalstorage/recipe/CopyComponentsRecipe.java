package com.buuz135.functionalstorage.recipe;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class CopyComponentsRecipe extends ShapedRecipe {
    public static final MapCodec<CopyComponentsRecipe> CODEC = new MapCodec<>() {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.concat(Serializer.CODEC.keys(ops), Stream.of(ops.createString("components"), ops.createString("copyIndex")));
        }

        @Override
        public <T> DataResult<CopyComponentsRecipe> decode(DynamicOps<T> ops, MapLike<T> input) {
            return Serializer.CODEC.decode(ops, input)
                    .flatMap(recipe -> ops.getNumberValue(input.get("copyIndex"))
                            .flatMap(ci -> ops.getList(input.get("components"))
                                    .map(comp -> {
                                        var components = new ArrayList<DataComponentType<?>>();
                                        comp.accept(t -> components.add(BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.parse(ops.getStringValue(t).getOrThrow()))));
                                        return new CopyComponentsRecipe(recipe, ci.intValue(), components);
                                    })));
        }

        @Override
        public <T> RecordBuilder<T> encode(CopyComponentsRecipe input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            var builder = Serializer.CODEC.encode(input.wrapped, ops, prefix);
            builder.add("copyIndex", ops.createInt(input.copyIndex));
            builder.add("components", ops.createList(input.components.stream()
                    .map(c -> ops.createString(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(c).toString()))));
            return builder;
        }
    };
    public static final StreamCodec<RegistryFriendlyByteBuf, CopyComponentsRecipe> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public CopyComponentsRecipe decode(RegistryFriendlyByteBuf buffer) {
            var recipe = Serializer.STREAM_CODEC.decode(buffer);
            return new CopyComponentsRecipe(
                    recipe, buffer.readVarInt(), buffer.readList(ByteBufCodecs.idMapper(BuiltInRegistries.DATA_COMPONENT_TYPE))
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, CopyComponentsRecipe value) {
            Serializer.STREAM_CODEC.encode(buffer, value.wrapped);
            buffer.writeVarInt(value.copyIndex);
            buffer.writeCollection(value.components, ByteBufCodecs.idMapper(BuiltInRegistries.DATA_COMPONENT_TYPE));
        }
    };

    private final ShapedRecipe wrapped;
    private final int copyIndex;
    private final List<DataComponentType<?>> components;

    public CopyComponentsRecipe(ShapedRecipe other, int copyIndex, List<DataComponentType<?>> components) {
        super(other.getGroup(), other.category(), other.pattern, other.getResultItem(RegistryAccess.EMPTY));
        this.wrapped = other;
        this.copyIndex = copyIndex;
        this.components = components;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        var result = wrapped.assemble(input, registries);
        var base = input.getItem(copyIndex);
        for (DataComponentType type : components) {
            var component = base.get(type);
            if (component != null) result.set(type, component);
        }
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return FunctionalStorage.COPY_COMPONENTS_SERIALIZER.value();
    }

    @SafeVarargs
    public static RecipeOutput output(RecipeOutput output, int copyIndex, DataComponentType<?>... components) {
        return new RecipeOutput() {
            @Override
            public Advancement.Builder advancement() {
                return output.advancement();
            }

            @Override
            public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
                output.accept(id, new CopyComponentsRecipe((ShapedRecipe) recipe, copyIndex, Arrays.asList(components)), advancement, conditions);
            }
        };
    }
}
