package me.alfie.progressivetrading.datapack.codec;

import com.mojang.serialization.Codec;
import me.alfie.alfinolib.networking.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;

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
    public static final StreamCodec<RegistryFriendlyByteBuf, LevelCost> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, LevelCost>() {
                @Override
                public void encode(RegistryFriendlyByteBuf buf, LevelCost levelCost) {
                    Map<Integer, ItemCost> map = levelCost.levelCostMap();

                    buf.writeVarInt(map.size());

                    for (Map.Entry<Integer, ItemCost> entry : map.entrySet()) {
                        buf.writeVarInt(entry.getKey());
                        ItemCost.STREAM_CODEC.encode(buf, entry.getValue());
                    }
                }

                @Override
                public LevelCost decode(RegistryFriendlyByteBuf buf) {
                    int size = buf.readVarInt();
                    Map<Integer, ItemCost> map = new HashMap<>(size);

                    for (int i = 0; i < size; i++) {
                        int level = buf.readVarInt();
                        ItemCost cost = ItemCost.STREAM_CODEC.decode(buf);
                        map.put(level, cost);
                    }

                    return new LevelCost(map);
                }
            };

    public ItemCost getLevel(int level) {
        if (!levelCostMap.containsKey(level)) return ItemCost.EMPTY;
        return levelCostMap.get(level);
    }
}
