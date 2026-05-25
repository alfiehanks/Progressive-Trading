package me.alfie.progressivetrading.mixin;

import me.alfie.progressivetrading.ProgressiveTrading;
import me.alfie.progressivetrading.ProgressiveTradingClient;
import me.alfie.progressivetrading.gui.common.CommonRenderUtils;
import me.alfie.progressivetrading.networking.OpenLevelUpMenuPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Mixin to render villager portrait, level up button and experience label.
 */
@Mixin(MerchantScreen.class)
public class MerchantScreenRenderMixin {

    private MerchantScreen screen = (MerchantScreen) (Object) this;
    private Button levelUpButton;

    @Inject(method = "init", at = @At("TAIL"))
    private void progressivetrading$addButton(CallbackInfo ci) {
        Component buttonLabel = Component.translatable("progressivetrading.gui.button.level_up");

        levelUpButton = Button.builder(
                        buttonLabel,
                        b -> {

                            if(ProgressiveTradingClient.lastInteractedVillager instanceof Villager villager) {
                                PacketDistributor.sendToServer(new OpenLevelUpMenuPacket(
                                        villager.getId()
                                ));
                            }


                        }
                )
                .bounds(
                        screen.getGuiLeft() + 112,
                        screen.getGuiTop() + 23,
                        102,
                        20
                )
                .build();

        ((AddWidgetAccessor<?>) this).progressivetrading$addRenderableWidget(levelUpButton);
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void progressivetrading$renderPortraitAndButton(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        CommonRenderUtils.renderVillagerPortrait(screen, graphics, mouseX, mouseY);

        if(this.levelUpButton != null) {
            int level = screen.getMenu().getTraderLevel();
            int xp = screen.getMenu().getTraderXp();
            this.levelUpButton.visible = ProgressiveTrading.canLevelUp(level, xp);
            this.levelUpButton.active = ProgressiveTrading.canLevelUp(level, xp);
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
    private int progressivetrading$replaceMerchantTitle(GuiGraphics graphics, Font font, Component text, int x, int y, int color, boolean shadow) {
        if(text.getContents() instanceof TranslatableContents translatable) {
            String key = translatable.getKey();

            if("merchant.title".equals(key)) { //Replace title
                if(VillagerData.canLevelUp(screen.getMenu().getTraderLevel())) {
                    Component experienceLabel = Component.translatable("progressivetrading.gui.label.xp_level");

                    final int left = 112;
                    final int right = 213;
                    int centredX = left + (right - left) / 2 - font.width(experienceLabel) / 2;
                    return graphics.drawString(font, experienceLabel, centredX, y, color, shadow);
                } else {
                    return 0;
                }
            }
        }

        return graphics.drawString(font, text, x, y, color, shadow);
    }
}
