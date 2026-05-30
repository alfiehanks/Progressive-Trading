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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenMerchantMenuPacket(int villagerEntityId) implements NetworkPacket<OpenMerchantMenuPacket> {

    public static final Type<OpenMerchantMenuPacket> TYPE = new Type<>(
            new ResourceId(ProgressiveTrading.MODID, "open_merchant_menu").mc()
    );
    @Override public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenMerchantMenuPacket> STREAM_CODEC =
            StreamCodecBuilder.<RegistryFriendlyByteBuf, OpenMerchantMenuPacket >create()
                    .add(CommonCodecs.VAR_INT, OpenMerchantMenuPacket::villagerEntityId)
                    .build(OpenMerchantMenuPacket::new);

    public void exec(IPayloadContext context) {
        Player player = context.player();
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
