package me.alfie.progressivetrading.networking;

import io.netty.buffer.ByteBuf;
import me.alfie.progressivetrading.ProgressiveTrading;
import me.alfie.progressivetrading.gui.VillagerLevelUpMenu;
import me.alfie.progressivetrading.gui.common.CommonRenderUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;

public record OpenMerchantMenuPacket(int villagerEntityId) implements CustomPacketPayload {

    public static final Type<OpenMerchantMenuPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ProgressiveTrading.MODID, "open_merchant_menu")
    );

    public static final StreamCodec<ByteBuf, OpenMerchantMenuPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, OpenMerchantMenuPacket::villagerEntityId,
                    OpenMerchantMenuPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void exec(Player player) {
        if(player instanceof ServerPlayer serverPlayer) {

            if(serverPlayer.level().getEntity(villagerEntityId) instanceof Villager villager) {
                player.closeContainer();
                villager.setTradingPlayer(player);
                villager.openTradingScreen(player,
                        CommonRenderUtils.buildProfessionComponent(
                                villager.getVillagerData().getProfession().name()),
                        villager.getVillagerData().getLevel());
            }
        }
    }
}
