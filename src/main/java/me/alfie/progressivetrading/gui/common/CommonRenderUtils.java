package me.alfie.progressivetrading.gui.common;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import me.alfie.progressivetrading.ProgressiveTradingClient;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommonRenderUtils {

    public enum TraderLevelColors {
        NOVICE(0x6B6865),
        APPRENTICE(0xA2816A),
        JOURNEYMAN(0xD7D964),
        EXPERT(0x35C56B),
        MASTER(0x86CFC4);

        private final int color;
        TraderLevelColors(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }

        public static int getColorForLevel(int level) {
            int index = level - 1;

            if (index < 0 || index >= values().length) return NOVICE.getColor();
            return values()[index].getColor();
        }
    }

    public static void renderVillagerPortrait(AbstractContainerScreen<?> screen, GuiGraphics graphics, int mouseX, int mouseY) {
        int x = screen.getGuiLeft();
        int y = screen.getGuiTop();

        final int left = x + 227;
        final int top = y + 10;
        final int right = left + 36;
        final int bottom = top + 62;

        if(ProgressiveTradingClient.lastInteractedVillager != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    graphics,
                    left, top,
                    right, bottom,
                    22,
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
        if(hoveringPortrait && ProgressiveTradingClient.lastInteractedVillager instanceof Villager villager) {
            Component traderLevel = buildLevelComponent(villager.getVillagerData().getLevel());
            String professionTranslationKey = villager.getVillagerData().getProfession().name();
            Component traderProfession = buildProfessionComponent(professionTranslationKey);

            Component merchantName = traderProfession.copy()
                    .withStyle(ChatFormatting.UNDERLINE);

            Component merchantLevel = traderLevel.copy()
                    .withColor(TraderLevelColors.getColorForLevel(villager.getVillagerData().getLevel()));

            List<Component> tooltip = new ArrayList<>();
            tooltip.add(merchantName);
            tooltip.add(merchantLevel);

            graphics.renderTooltip(Minecraft.getInstance().font, tooltip, Optional.empty(),mouseX, mouseY);
        }
    }

    public static Component buildProfessionComponent(String professionTranslationKey) {
        return Component.translatable("entity.minecraft.villager."+professionTranslationKey);
    }

    public static Component buildLevelComponent(int level) {
        return Component.translatable("merchant.level." + level);
    }
}
