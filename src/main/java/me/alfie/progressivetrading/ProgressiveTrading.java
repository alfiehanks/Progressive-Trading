package me.alfie.progressivetrading;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.alfie.progressivetrading.event.ModEvents;
import me.alfie.progressivetrading.gui.ModMenus;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Mod(ProgressiveTrading.MODID)
public class ProgressiveTrading {
    public static final String MODID = "progressivetrading";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final int MERCHANT_SHIFT_X = -24;
    public static final int MERCHANT_SHIFT_Y = 13;


    public ProgressiveTrading(IEventBus modEventBus, ModContainer modContainer) {
        ModEvents.register(modEventBus);
        ModMenus.register(modEventBus);
    }

    public static boolean canLevelUp(int traderLevel, int traderXp) {
        return VillagerData.canLevelUp(traderLevel) && traderXp >= VillagerData.getMaxXpPerLevel(traderLevel);
    }

    public static MerchantOffers getSeededVillagerOffers(Villager villager) {
        MerchantOffers offers = new MerchantOffers();

        VillagerTrades.ItemListing[] trades = ProgressiveTrading.getRandomTrades(villager);
        for(VillagerTrades.ItemListing listing : trades) {
            MerchantOffer offer = listing.getOffer(villager, ProgressiveTrading.getVillagerSeed(villager));

            if(offer != null) {
                offers.add(offer);
            }
        }
        return offers;
    }

    private static VillagerTrades.ItemListing[] getPotentialTradesForLevel(VillagerProfession profession, int level, boolean experimental) {
        Int2ObjectMap<VillagerTrades.ItemListing[]> trades;

        if (experimental) {
            Int2ObjectMap<VillagerTrades.ItemListing[]> experimentalTrades =
                    VillagerTrades.EXPERIMENTAL_TRADES.get(profession);

            trades = experimentalTrades != null ? experimentalTrades : VillagerTrades.TRADES.get(profession);
        } else {
            trades = VillagerTrades.TRADES.get(profession);
        }

        if (trades == null) return new VillagerTrades.ItemListing[0];
        return trades.getOrDefault(level, new VillagerTrades.ItemListing[0]);
    }

    private static VillagerTrades.ItemListing[] getRandomTrades(Villager villager) {
        VillagerTrades.ItemListing[] itemListings = getPotentialTradesForLevel(
                villager.getVillagerData().getProfession(),
                villager.getVillagerData().getLevel()+1,
                false);

        if (itemListings == null || itemListings.length == 0) {
            return new VillagerTrades.ItemListing[0];
        }

        List<VillagerTrades.ItemListing> pool = new ArrayList<>(Arrays.asList(itemListings));
        List<VillagerTrades.ItemListing> result = new ArrayList<>();
        RandomSource random = getVillagerSeed(villager);
        final int maxSize = 2;
        while (!pool.isEmpty() && result.size() < maxSize) {
            int index = random.nextInt(pool.size());
            VillagerTrades.ItemListing listing = pool.remove(index);

            result.add(listing);
        }

        return result.toArray(new VillagerTrades.ItemListing[0]);
    }

    public static RandomSource getVillagerSeed(Villager villager) {
        return RandomSource.create(villager.getUUID().getMostSignificantBits() ^
                villager.getUUID().getLeastSignificantBits());
    }

    public static ResourceLocation getProfessionId(Villager villager) {
        return BuiltInRegistries.VILLAGER_PROFESSION.getKey(villager.getVillagerData().getProfession());
    }

}
