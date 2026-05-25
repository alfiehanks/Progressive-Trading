package me.alfie.progressivetrading.mixin;

import me.alfie.progressivetrading.ProgressiveTrading;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(MerchantScreen.class)
public class MerchantScreenLayoutMixin {

    @Redirect(
            method = "renderBg",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIIII)V",
                    ordinal = 0
            )
    )
    public void progressivetrading$shiftOutOfStockSprite(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int offset, int width, int height) {
        graphics.blitSprite(sprite, x + ProgressiveTrading.MERCHANT_SHIFT_X, y + 13, offset, width, height);
    }

    @ModifyArgs(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/MerchantScreen;isHovering(IIIIDD)Z"
            )
    )
    private void progressivetrading$shiftOutOfStockHoverBox(Args args) {
        int x = args.get(0);
        int y = args.get(1);

        x += ProgressiveTrading.MERCHANT_SHIFT_X;
        y += ProgressiveTrading.MERCHANT_SHIFT_Y;

        args.set(0, x);
        args.set(1, y);
    }

    @ModifyVariable(
            method = "renderProgressBar",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private int progressivetrading$shiftProgressBarX(int posX) {
        return posX + ProgressiveTrading.MERCHANT_SHIFT_X;
    }
}
