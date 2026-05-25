package me.alfie.progressivetrading.networking;

import io.netty.buffer.ByteBuf;
import me.alfie.progressivetrading.ProgressiveTrading;
import me.alfie.progressivetrading.datapack.CostRegistry;
import me.alfie.progressivetrading.datapack.codec.ItemCost;
import me.alfie.progressivetrading.gui.VillagerLevelUpMenu;
import me.alfie.progressivetrading.gui.common.CommonRenderUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

public record LevelUpVillagerPacket(int villagerEntityId) implements CustomPacketPayload {
    public static Type<LevelUpVillagerPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ProgressiveTrading.MODID, "level_up_villager")
    );

    public static StreamCodec<ByteBuf, LevelUpVillagerPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, LevelUpVillagerPacket::villagerEntityId,
                    LevelUpVillagerPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void exec(Player player) {
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
