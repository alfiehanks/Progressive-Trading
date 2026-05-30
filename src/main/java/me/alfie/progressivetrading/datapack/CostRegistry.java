package me.alfie.progressivetrading.datapack;

import me.alfie.alfinolib.datapacks.ClientDatapackManager;
import me.alfie.alfinolib.datapacks.ServerDatapackManager;
import me.alfie.alfinolib.networking.codec.StreamCodec;
import me.alfie.alfinolib.networking.codec.StreamCodecBuilder;
import me.alfie.progressivetrading.datapack.codec.ItemCost;
import me.alfie.progressivetrading.datapack.codec.LevelCost;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CostRegistry {

    public static final StreamCodec<RegistryFriendlyByteBuf, CostRegistry> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, CostRegistry>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, CostRegistry costRegistry) {
            Map<ResourceLocation, LevelCost> map = costRegistry.ID_REGISTRY;

            buf.writeVarInt(map.size());

            for (Map.Entry<ResourceLocation, LevelCost> entry : map.entrySet()) {
                buf.writeResourceLocation(entry.getKey());
                LevelCost.STREAM_CODEC.encode(buf, entry.getValue());
            }
        }

        @Override
        public CostRegistry decode(RegistryFriendlyByteBuf buf) {
            int size = buf.readVarInt();

            Map<ResourceLocation, LevelCost> map = new HashMap<>(size);

            for (int i = 0; i < size; i++) {
                ResourceLocation id = buf.readResourceLocation();
                LevelCost levelCost = LevelCost.STREAM_CODEC.decode(buf);
                map.put(id, levelCost);
            }

            return new CostRegistry(map);
        }
    };

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
