package me.alfie.progressivetrading.mixin;

import me.alfie.progressivetrading.ProgressiveTrading;
import me.alfie.progressivetrading.ProgressiveTradingClient;
import me.alfie.progressivetrading.gui.core.Sprite;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.ClientSideMerchant;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.trading.Merchant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(MerchantScreen.class)
public class MerchantScreenMixin {

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void progressivetrading$renderCustomBg(GuiGraphics graphics, float partialTick,
                                                   int mouseX, int mouseY, CallbackInfo ci) {
        MerchantScreen screen = (MerchantScreen) (Object) this;

        int x = screen.getGuiLeft();
        int y = screen.getGuiTop();

        final int left = x + 227;
        final int top = y + 10;
        final int right = left + 36;
        final int bottom = top + 49;

        if(ProgressiveTradingClient.lastInteractedVillager != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    graphics,
                    left, top,
                    right, bottom,
                    20,
                    0,
                    mouseX, mouseY,
                    ProgressiveTradingClient.lastInteractedVillager
            );
        }

        boolean hoveringPortrait =
                mouseX >= left &&
                mouseX <= right &&
                mouseY >= top &&
                mouseY <= bottom;
        if(hoveringPortrait) {

            int traderLevel = screen.getMenu().getTraderLevel();

            Component merchantName = screen.getTitle().copy()
                    .withStyle(ChatFormatting.UNDERLINE);
            Component merchantLevel = Component.translatable("merchant.level." + traderLevel)
                    .withColor(ProgressiveTradingClient.TraderLevelColors.values()[traderLevel-1].getColor());
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(merchantName);
            tooltip.add(merchantLevel);

            graphics.renderTooltip(Minecraft.getInstance().font, tooltip, Optional.empty(),mouseX, mouseY);
        }
    }

    @Redirect(method = "renderLabels", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString("
                    + "Lnet/minecraft/client/gui/Font;"
                    + "Lnet/minecraft/network/chat/Component;"
                    + "III"
                    + "Z)I"
    ))
    private int progressivetrading$shiftMerchantTitle(
            GuiGraphics graphics,
            Font font,
            Component text,
            int x,
            int y,
            int color,
            boolean shadow
    ) {
        if (text.getContents() instanceof TranslatableContents translatable
                && "merchant.title".equals(translatable.getKey())) x += ProgressiveTrading.MERCHANT_SHIFT_AMOUNT;

        return graphics.drawString(font, text, x, y, color, shadow);
    }

    @ModifyVariable(
            method = "renderProgressBar",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private int alfinos$shiftProgressBarX(int posX) {
        return posX + ProgressiveTrading.MERCHANT_SHIFT_AMOUNT;
    }
}
