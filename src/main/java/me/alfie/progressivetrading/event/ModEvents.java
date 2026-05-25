package me.alfie.progressivetrading.event;

import me.alfie.progressivetrading.ProgressiveTradingClient;
import me.alfie.progressivetrading.datapack.CostDatapack;
import me.alfie.progressivetrading.gui.ModMenus;
import me.alfie.progressivetrading.networking.LevelUpVillagerPacket;
import me.alfie.progressivetrading.networking.OpenLevelUpMenuPacket;
import me.alfie.progressivetrading.networking.OpenMerchantMenuPacket;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModEvents {

    public static void register(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(ModEvents::onMerchantInteract);

        modEventBus.addListener(ModMenus::registerScreens);
        modEventBus.addListener(ModEvents::registerPackets);

        NeoForge.EVENT_BUS.addListener(CostDatapack::register);
    }

    public static void registerPackets(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(OpenLevelUpMenuPacket.TYPE, OpenLevelUpMenuPacket.STREAM_CODEC,
                (packet, context) -> packet.exec(context.player()));

        registrar.playToServer(OpenMerchantMenuPacket.TYPE, OpenMerchantMenuPacket.STREAM_CODEC,
                (packet, context) -> packet.exec(context.player()));

        registrar.playToServer(LevelUpVillagerPacket.TYPE, LevelUpVillagerPacket.STREAM_CODEC,
                (packet, context) -> packet.exec(context.player()));
    }

    public static void onMerchantInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getSide().isServer()) return;

        if(event.getTarget() instanceof AbstractVillager villager) {
            ProgressiveTradingClient.lastInteractedVillager = villager;
        }
    }
}
