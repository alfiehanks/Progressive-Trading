package me.alfie.progressivetrading.datapack;

import com.google.gson.JsonElement;
import me.alfie.alfinolib.datapacks.DatapackKey;
import me.alfie.alfinolib.datapacks.DatapackRegistry;
import me.alfie.alfinolib.datapacks.ModDatapack;
import me.alfie.progressivetrading.ProgressiveTrading;
import me.alfie.progressivetrading.datapack.codec.ItemCost;
import me.alfie.progressivetrading.datapack.codec.LevelCost;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CostDatapack extends ModDatapack<LevelCost, CostRegistry> {

    public static final DatapackKey<CostRegistry> KEY =
            new DatapackKey<>(ProgressiveTrading.MODID, "costs");

    private CostRegistry DATA = new CostRegistry(new HashMap<>());

     public CostDatapack() {
        super(LevelCost.CODEC, KEY, CostRegistry.STREAM_CODEC);
    }

    @Override
    public CostRegistry getData() {
        return DATA;
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> map, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {

        Map<ResourceLocation, LevelCost> costMap = new HashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            ResourceLocation id = remapPath(entry.getKey());

            LevelCost data = parseOrDefault(entry.getValue(), LevelCost.EMPTY);
            costMap.put(id, data);
        }
        DATA = new CostRegistry(costMap);
    }

    /**
     * Converts the datapack file path ResourceLocation (e.g. {@code minecraft/librarian})
            * into a ResourceLocation form (e.g. {@code minecraft:librarian})
            * by replacing the first path separator with a colon.
     * <p>
     * Also preserves any additional sub-paths, allowing for hierarchical organization (e.g. {@code minecraft/group/farmer} -> {@code minecraft:group/farmer}).
            */
    private static ResourceLocation remapPath(ResourceLocation originalId) {
        String path = originalId.getPath();

        int firstSlash = path.indexOf('/');
        if (firstSlash == -1) {
            throw new IllegalArgumentException("Invalid path: " + originalId);
        }

        String namespace = path.substring(0, firstSlash);
        String subPath = path.substring(firstSlash + 1);

        return ResourceLocation.fromNamespaceAndPath(namespace, subPath);
    }

    public static void register(AddReloadListenerEvent event) {
        DatapackRegistry.register(event, CostDatapack::new);
        System.out.println(DatapackRegistry.get(KEY));
    }
}
