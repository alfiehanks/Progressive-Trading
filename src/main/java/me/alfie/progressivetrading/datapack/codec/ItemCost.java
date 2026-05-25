package me.alfie.progressivetrading.datapack.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.alfie.progressivetrading.ProgressiveTrading;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import java.util.Arrays;
import java.util.List;


public record ItemCost(Ingredient ingredient, int count, DataComponentPatch components) {

    public static final ItemCost EMPTY = new ItemCost(Ingredient.EMPTY, 0, DataComponentPatch.EMPTY);

    public static final Codec<ItemCost> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Ingredient.CODEC.fieldOf("ingredient")
                            .forGetter(ItemCost::ingredient),

                    Codec.INT.fieldOf("count")
                            .forGetter(ItemCost::count),

                    DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                            .forGetter(ItemCost::components)

            ).apply(instance, ItemCost::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemCost> STREAM_CODEC =
            StreamCodec.of(
                    ItemCost::encodeStream,
                    ItemCost::decodeStream
            );

    private static ItemCost decodeStream(RegistryFriendlyByteBuf buf) {

        Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
        int count = buf.readVarInt();

        boolean hasComponents = buf.readBoolean();

        DataComponentPatch components = hasComponents
                ? DataComponentPatch.STREAM_CODEC.decode(buf)
                : DataComponentPatch.EMPTY;

        return new ItemCost(ingredient, count, components);
    }

    private static void encodeStream(RegistryFriendlyByteBuf buf, ItemCost value) {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, value.ingredient());
        buf.writeVarInt(value.count());

        boolean hasComponents = value.components() != null && !value.components().isEmpty();
        buf.writeBoolean(hasComponents);

        if (hasComponents) DataComponentPatch.STREAM_CODEC.encode(buf, value.components());
    }

    public List<ItemStack> getItems() {
        return Arrays.stream(ingredient.getItems())
                .map(ItemStack::copy)
                .peek(stack -> {
                    stack.setCount(this.count);
                    stack.applyComponents(this.components);
                })
                .toList();
    }

    public boolean anyMatch(ItemStack stack) {
        for (ItemStack candidate : ingredient.getItems()) {

            if (components.isEmpty()) {
                if (ItemStack.isSameItem(stack, candidate)) return true;
            } else {
                ItemStack copy = candidate.copy();
                copy.applyComponents(components);

                if (ItemStack.isSameItemSameComponents(stack, copy)) return true;
            }
        }

        return false;
    }
}
