package me.alfie.progressivetrading.networking;

import io.netty.buffer.ByteBuf;
import me.alfie.alfinolib.networking.NetworkPacket;
import me.alfie.alfinolib.networking.codec.CommonCodecs;
import me.alfie.alfinolib.networking.codec.StreamCodec;
import me.alfie.alfinolib.networking.codec.StreamCodecBuilder;
import me.alfie.alfinolib.util.ResourceId;
import me.alfie.progressivetrading.ProgressiveTrading;
import me.alfie.progressivetrading.datapack.CostRegistry;
import me.alfie.progressivetrading.datapack.codec.ItemCost;
import me.alfie.progressivetrading.gui.VillagerLevelUpMenu;
import me.alfie.progressivetrading.gui.common.CommonRenderUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record LevelUpVillagerPacket(int villagerEntityId) implements NetworkPacket<LevelUpVillagerPacket> {

    public static Type<LevelUpVillagerPacket> TYPE = new Type<>(
            new ResourceId(ProgressiveTrading.MODID, "level_up_villager").mc()
    );
    @Override public Type<? extends CustomPacketPayload> type() {return TYPE;}

    public static StreamCodec<RegistryFriendlyByteBuf, LevelUpVillagerPacket> STREAM_CODEC =
            StreamCodecBuilder.<RegistryFriendlyByteBuf, LevelUpVillagerPacket>create()
                    .add(CommonCodecs.VAR_INT, LevelUpVillagerPacket::villagerEntityId)
                    .build(LevelUpVillagerPacket::new);

    @Override
    public void exec(IPayloadContext context) {
        Player player = context.player();
        Level level = player.level();
        if(level.isClientSide()) return;

        Entity entity = level.getEntity(villagerEntityId());
        if(entity instanceof Villager villager) {
            boolean playerVerified = villager.getTradingPlayer() != null &&
                    villager.getTradingPlayer() == player;


            if(player.containerMenu instanceof VillagerLevelUpMenu menu) {

                ItemCost validCost = CostRegistry.server()
                        .get(ProgressiveTrading.getProfessionId(villager))
                        .getLevel(villager.getVillagerData().getLevel());

                if(playerVerified && menu.tryConsume(validCost, menu.getItemInSlot())) {
                    int villagerLevel = villager.getVillagerData().getLevel();

                    MerchantOffers offers = villager.getOffers();
                    MerchantOffers newOffers = ProgressiveTrading.getSeededVillagerOffers(villager);
                    offers.addAll(newOffers);
                    villager.setOffers(offers);

                    villager.setVillagerData(
                            villager.getVillagerData()
                                    .setLevel(villagerLevel + 1));

                    villager.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
                    villager.makeSound(SoundEvents.VILLAGER_YES);

                    //Send back to trade screen
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
}
