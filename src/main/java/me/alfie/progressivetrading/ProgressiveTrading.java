package me.alfie.progressivetrading;

import me.alfie.progressivetrading.event.ModEvents;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;


@Mod(ProgressiveTrading.MODID)
public class ProgressiveTrading {
    public static final String MODID = "progressivetrading";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final int MERCHANT_SHIFT_AMOUNT = -24;

    public ProgressiveTrading(IEventBus modEventBus, ModContainer modContainer) {
        ModEvents.register(modEventBus);
    }

}
