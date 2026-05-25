package me.alfie.progressivetrading.gui;

import me.alfie.progressivetrading.ProgressiveTrading;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ProgressiveTrading.MODID);
    public static final Supplier<MenuType<VillagerLevelUpMenu>> VILLAGER_LEVEL_UP_MENU = MENUS.register(
            "villager_level_up_menu", () -> new MenuType<>(
                    (containerId, playerInventory) ->
                            new VillagerLevelUpMenu(containerId, playerInventory, null, null),
                    FeatureFlags.DEFAULT_FLAGS
            )
    );

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(VILLAGER_LEVEL_UP_MENU.get(), VillagerLevelUpScreen::new);
    }
}
