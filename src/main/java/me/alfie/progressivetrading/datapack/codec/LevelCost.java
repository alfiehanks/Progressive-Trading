package me.alfie.progressivetrading.datapack.codec;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public record LevelCost(Map<Integer, ItemCost> levelCostMap) {

    public static final LevelCost EMPTY = new LevelCost(new HashMap<>());

    public static final Codec<LevelCost> CODEC =
            Codec.unboundedMap(Codec.STRING, ItemCost.CODEC)
                    .xmap(
                            stringMap -> {
                                Map<Integer, ItemCost> intMap = new HashMap<>();
                                stringMap.forEach((key, value) ->
                                        intMap.put(Integer.parseInt(key), value)
                                );
                                return new LevelCost(intMap);
                            },
                            levelCost -> {
                                Map<String, ItemCost> stringMap = new HashMap<>();
                                levelCost.levelCostMap().forEach((key, value) ->
                                        stringMap.put(String.valueOf(key), value)
                                );
                                return stringMap;
                            }
                    );

    public static final StreamCodec<RegistryFriendlyByteBuf, LevelCost> STREAM_CODEC =
            StreamCodec.of(
                    LevelCost::encodeStream,
                    LevelCost::decodeStream
            );

    private static LevelCost decodeStream(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<Integer, ItemCost> map = new HashMap<>(size);

        for (int i = 0; i < size; i++) {
            int level = buf.readVarInt();
            ItemCost cost = ItemCost.STREAM_CODEC.decode(buf);
            map.put(level, cost);
        }

        return new LevelCost(map);
    }

    private static void encodeStream(RegistryFriendlyByteBuf buf, LevelCost value) {
        Map<Integer, ItemCost> map = value.levelCostMap();

        buf.writeVarInt(map.size());

        for (Map.Entry<Integer, ItemCost> entry : map.entrySet()) {
            buf.writeVarInt(entry.getKey());
            ItemCost.STREAM_CODEC.encode(buf, entry.getValue());
        }
    }

    public ItemCost getLevel(int level) {
        if (!levelCostMap.containsKey(level)) return ItemCost.EMPTY;
        return levelCostMap.get(level);
    }
}
