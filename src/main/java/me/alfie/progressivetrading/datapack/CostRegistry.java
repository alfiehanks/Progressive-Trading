package me.alfie.progressivetrading.datapack;

import me.alfie.alfinosdatapacks.api.ClientDatapackManager;
import me.alfie.alfinosdatapacks.api.ServerDatapackManager;
import me.alfie.progressivetrading.datapack.codec.ItemCost;
import me.alfie.progressivetrading.datapack.codec.LevelCost;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CostRegistry {

    public static final StreamCodec<RegistryFriendlyByteBuf, CostRegistry> STREAM_CODEC =
            StreamCodec.of(
                    CostRegistry::encodeStream,
                    CostRegistry::decodeStream
            );

    private static CostRegistry decodeStream(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();

        Map<ResourceLocation, LevelCost> map = new HashMap<>(size);

        for (int i = 0; i < size; i++) {
            ResourceLocation id = buf.readResourceLocation();
            LevelCost levelCost = LevelCost.STREAM_CODEC.decode(buf);
            map.put(id, levelCost);
        }

        return new CostRegistry(map);
    }

    private static void encodeStream(RegistryFriendlyByteBuf buf, CostRegistry value) {
        Map<ResourceLocation, LevelCost> map = value.ID_REGISTRY;

        buf.writeVarInt(map.size());

        for (Map.Entry<ResourceLocation, LevelCost> entry : map.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            LevelCost.STREAM_CODEC.encode(buf, entry.getValue());
        }
    }

    private final Map<ResourceLocation, LevelCost> ID_REGISTRY = new HashMap<>();

    public static CostRegistry client() {
        return ClientDatapackManager.get(CostDatapack.KEY);
    }

    public static CostRegistry server() {
        return ServerDatapackManager.get(CostDatapack.KEY);
    }

    public CostRegistry(Map<ResourceLocation, LevelCost> idRegistry) {
        this.ID_REGISTRY.putAll(idRegistry);
    }

    public LevelCost get(ResourceLocation id) {
        LevelCost data = ID_REGISTRY.get(id);
        return data != null ? data : LevelCost.EMPTY;
    }
}
