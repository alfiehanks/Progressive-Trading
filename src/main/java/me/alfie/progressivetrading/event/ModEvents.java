package me.alfie.progressivetrading.event;

import me.alfie.progressivetrading.ProgressiveTradingClient;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class ModEvents {

    public static void register(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(ModEvents::onMerchantInteract);
    }

    public static void onMerchantInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getSide().isServer()) return;

        if(event.getTarget() instanceof AbstractVillager villager) {
            ProgressiveTradingClient.lastInteractedVillager = villager;
        }
    }
}
