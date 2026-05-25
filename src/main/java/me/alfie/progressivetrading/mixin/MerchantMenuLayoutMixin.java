package me.alfie.progressivetrading.mixin;

import me.alfie.progressivetrading.ProgressiveTrading;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.trading.Merchant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Mixin to move slot positions to new texture positions
 */
@Mixin(MerchantMenu.class)
public class MerchantMenuLayoutMixin {

    @Redirect(
            method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/trading/Merchant;)V",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/world/inventory/Slot"
            )
    )
    private Slot alfinos$shiftTradeInputSlots(Container container, int index, int x, int y) {
        if (container instanceof MerchantContainer && index <= 1)
            return new Slot(container, index, x + ProgressiveTrading.MERCHANT_SHIFT_X, y + ProgressiveTrading.MERCHANT_SHIFT_Y);

        return new Slot(container, index, x, y);
    }

    @Redirect(
            method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/trading/Merchant;)V",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/world/inventory/MerchantResultSlot"
            )
    )
    private MerchantResultSlot alfinos$shiftTradeResultSlot(Player player, Merchant trader, MerchantContainer container, int index, int x, int y) {
        return new MerchantResultSlot(
                player,
                trader,
                container,
                index,
                x + ProgressiveTrading.MERCHANT_SHIFT_X,
                y + ProgressiveTrading.MERCHANT_SHIFT_Y + 1
        );
    }
}
