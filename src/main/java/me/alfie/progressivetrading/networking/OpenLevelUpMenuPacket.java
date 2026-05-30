package me.alfie.progressivetrading.networking;

import io.netty.buffer.ByteBuf;
import me.alfie.alfinolib.networking.NetworkPacket;
import me.alfie.alfinolib.networking.codec.CommonCodecs;
import me.alfie.alfinolib.networking.codec.StreamCodec;
import me.alfie.alfinolib.networking.codec.StreamCodecBuilder;
import me.alfie.alfinolib.util.ResourceId;
import me.alfie.progressivetrading.ProgressiveTrading;
import me.alfie.progressivetrading.gui.VillagerLevelUpMenu;
import me.alfie.progressivetrading.gui.common.CommonRenderUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenLevelUpMenuPacket(int villagerEntityId) implements NetworkPacket<OpenLevelUpMenuPacket> {

    public static final Type<OpenLevelUpMenuPacket> TYPE = new Type<>(
            new ResourceId(ProgressiveTrading.MODID, "open_level_up_menu").mc()
    );
    @Override public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenLevelUpMenuPacket> STREAM_CODEC =
            StreamCodecBuilder.<RegistryFriendlyByteBuf, OpenLevelUpMenuPacket>create()
                    .add(CommonCodecs.VAR_INT, OpenLevelUpMenuPacket::villagerEntityId)
                    .build(OpenLevelUpMenuPacket::new);

    @Override
    public void exec(IPayloadContext context) {
        Player player = context.player();
        if(player instanceof ServerPlayer serverPlayer) {
            if(player.level().getEntity(villagerEntityId) instanceof Villager villager) {

                serverPlayer.openMenu(
                        new SimpleMenuProvider(
                                (containerId, playerInventory, p) ->
                                        new VillagerLevelUpMenu(containerId, playerInventory, p.level(), villager),
                                Component.translatable("progressivetrading.gui.villager_level_up",
                                        CommonRenderUtils.buildProfessionComponent(
                                                villager.getVillagerData().getProfession().name()
                                        ))
                        )
                );

                villager.setTradingPlayer(player);
            }
        }
    }
}
