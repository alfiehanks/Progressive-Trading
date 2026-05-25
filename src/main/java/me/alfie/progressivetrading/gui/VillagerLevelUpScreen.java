package me.alfie.progressivetrading.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.alfie.alfinosdatapacks.api.DatapackRegistry;
import me.alfie.progressivetrading.ProgressiveTrading;
import me.alfie.progressivetrading.ProgressiveTradingClient;
import me.alfie.progressivetrading.datapack.CostDatapack;
import me.alfie.progressivetrading.datapack.CostRegistry;
import me.alfie.progressivetrading.datapack.codec.ItemCost;
import me.alfie.progressivetrading.gui.common.CommonRenderUtils;
import me.alfie.progressivetrading.gui.core.HoverableItemStack;
import me.alfie.progressivetrading.gui.core.Sprite;
import me.alfie.progressivetrading.networking.LevelUpVillagerPacket;
import me.alfie.progressivetrading.networking.OpenMerchantMenuPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VillagerLevelUpScreen extends AbstractContainerScreen<@NotNull VillagerLevelUpMenu> {

    private int CLOSE_BUTTON_LEFT;
    private int CLOSE_BUTTON_TOP;

    private Button closeButton;
    private Button confirmButton;
    private List<Button> fakeTrades = new ArrayList<>();

    //private ItemStack requiredItem;
    private ItemCost validCosts = ItemCost.EMPTY;

    public VillagerLevelUpScreen(@NotNull VillagerLevelUpMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 276;
        this.inventoryLabelX = 107;


        if(ProgressiveTradingClient.lastInteractedVillager instanceof Villager villager) {
            ResourceLocation id = ProgressiveTrading.getProfessionId(villager);
            validCosts = CostRegistry.client().get(id).getLevel(villager.getVillagerData().getLevel());
        }
    }

    @Override
    protected void init() {
        super.init();
        CLOSE_BUTTON_LEFT = getGuiLeft() + VillagerLevelUpMenu.COST_SLOT_X - 32;
        CLOSE_BUTTON_TOP = getGuiTop() + VillagerLevelUpMenu.COST_SLOT_Y;

        //Create confirm button
        confirmButton = new ConfirmUpgradeButton(
                getGuiLeft() + VillagerLevelUpMenu.COST_SLOT_X - 1 + 32,
                getGuiTop() + VillagerLevelUpMenu.COST_SLOT_Y - 1,
                this);
        addRenderableWidget(confirmButton);

        //Create fake trades
        if(ProgressiveTradingClient.lastInteractedVillager instanceof Villager villager) {
            MerchantOffers offers = ProgressiveTrading.getSeededVillagerOffers(villager);

            int index = 0;
            final int x = getGuiLeft() + 5;
            final int baseY = getGuiTop() + 18;
            for(MerchantOffer offer : offers) {
                Button fakeTrade = new FakeTradeButton(x, baseY + (index * 20), offer, this);

                fakeTrades.add(fakeTrade);
                addRenderableWidget(fakeTrade);

                index ++;
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {

        RenderSystem.enableBlend();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(
                ResourceLocation.fromNamespaceAndPath(ProgressiveTrading.MODID,
                        "textures/gui/container/villager_level_up.png"),
                x, y,
                0,
                0.0F, 0.0F,
                this.imageWidth, this.imageHeight,
                512, 256);
        RenderSystem.disableBlend();

        boolean isHoveringCloseButton = isMouseOver(
                CLOSE_BUTTON_LEFT, CLOSE_BUTTON_TOP,
                Sprite.CLOSE_MENU_ACTIVE.width(), Sprite.CLOSE_MENU_ACTIVE.height(),
                mouseX, mouseY);

        Sprite closeButton = isHoveringCloseButton ? Sprite.CLOSE_MENU_ACTIVE : Sprite.CLOSE_MENU;
        confirmButton.active = validCosts.anyMatch(getMenu().getItemInSlot())
                                && getMenu().getItemInSlot().getCount() >= validCosts.count();


        graphics.blit(
                closeButton.id(),
                CLOSE_BUTTON_LEFT, CLOSE_BUTTON_TOP,
                0, 0,
                Sprite.CLOSE_MENU.width(), Sprite.CLOSE_MENU.height(),
                Sprite.CLOSE_MENU.width(), Sprite.CLOSE_MENU.height()
        );

        CommonRenderUtils.renderVillagerPortrait(this, graphics, mouseX, mouseY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        if(isMouseOver(CLOSE_BUTTON_LEFT, CLOSE_BUTTON_TOP, 14, 14, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.translatable("progressivetrading.gui.tooltip.back"), mouseX, mouseY);
        }

        super.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        this.titleLabelX = 49 + this.imageWidth / 2 - this.font.width(this.title) / 2
                + ProgressiveTrading.MERCHANT_SHIFT_X;
        this.titleLabelY = 6;
        super.renderLabels(graphics, mouseX, mouseY);

        //Draw "Unlocks Trades" above trades box
        Component unlocksTrades = Component.translatable("progressivetrading.gui.label.unlocks_trades");
        final int x = 5 - font.width(unlocksTrades) / 2 + 48;
        graphics.drawString(font, unlocksTrades, x, titleLabelY, 4210752, false);

        //Add "Old Profession -> New Profession" label below title
        if(ProgressiveTradingClient.lastInteractedVillager instanceof Villager villager) {
            int traderLevel = villager.getVillagerData().getLevel();

            Component currentLevel = CommonRenderUtils.buildLevelComponent(traderLevel)
                    .copy().withColor(CommonRenderUtils.TraderLevelColors.getColorForLevel(traderLevel));
            Component nextLevel = CommonRenderUtils.buildLevelComponent(traderLevel+1)
                    .copy().withColor(4210752);

            Component fromToProfession = Component.translatable("progressivetrading.gui.label.villager_level_up_from_to",
                    nextLevel);

            final int fromToX = 49 + this.imageWidth / 2 - this.font.width(fromToProfession) / 2
                    + ProgressiveTrading.MERCHANT_SHIFT_X;
            graphics.drawString(font, fromToProfession, fromToX, titleLabelY + font.lineHeight,
                    4210752, false);
        }

        //Add "Requires" label
        Component requiresLabel = Component.translatable("progressivetrading.gui.label.requires_item");
        final int requiresX = 49 + this.imageWidth / 2 - this.font.width(requiresLabel) / 2
                + ProgressiveTrading.MERCHANT_SHIFT_X - 8;
        final int y = titleLabelY + font.lineHeight*2 + 4;

        graphics.drawString(font, requiresLabel, requiresX, y, 4210752, false);

        final int itemX = requiresX + font.width(requiresLabel) + 2;
        final int itemY = y - 4;


        List<ItemStack> stacks = validCosts.getItems();

        final HoverableItemStack item = new HoverableItemStack(
                this, getCycledElement(stacks));

        item.setPos(itemX, itemY);
        item.render(graphics, mouseX - getGuiLeft(), mouseY - getGuiTop());
    }

    public boolean isMouseOver(double x, double y, int width, int height, double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width
                && mouseY >= y && mouseY < y + height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(isMouseOver(CLOSE_BUTTON_LEFT, CLOSE_BUTTON_TOP,
                Sprite.CLOSE_MENU.width(), Sprite.CLOSE_MENU.height(),
                mouseX, mouseY)) {

            PacketDistributor.sendToServer(new OpenMerchantMenuPacket(
                    ProgressiveTradingClient.lastInteractedVillager.getId()
            ));
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private class FakeTradeButton extends Button {

        private final VillagerLevelUpScreen screen;
        private final MerchantOffer offer;
        private final int y;

        protected FakeTradeButton(int x, int y, MerchantOffer offer, VillagerLevelUpScreen screen) {
            super(x, y, 88, 20, CommonComponents.EMPTY, (button) -> {}, DEFAULT_NARRATION);
            this.active = false;
            this.offer = offer;
            this.screen = screen;
            this.y = y + 1;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);

            ItemStack costA = offer.getCostA();
            ItemStack costB = offer.getCostB();
            ItemStack result = offer.getResult();

            HoverableItemStack itemStackA = new HoverableItemStack(this.screen, costA);
            HoverableItemStack itemStackB = new HoverableItemStack(this.screen, costB);
            HoverableItemStack itemStackResult = new HoverableItemStack(this.screen, result);

            itemStackA.setPos(screen.getGuiLeft() + 10, y);
            itemStackB.setPos(screen.getGuiLeft() +  40, y);
            itemStackResult.setPos(screen.getGuiLeft() +  73, y);

            itemStackA.render(graphics, mouseX, mouseY);
            itemStackB.render(graphics, mouseX, mouseY);
            itemStackResult.render(graphics, mouseX, mouseY);

            RenderSystem.enableBlend();
            graphics.blitSprite(ResourceLocation.withDefaultNamespace("container/villager/trade_arrow"),
                    screen.getGuiLeft() + 60, y + 3, 0, 10, 9);
            RenderSystem.disableBlend();
        }
    }

    private class ConfirmUpgradeButton extends Button {

        private final int x;
        private final int y;
        private final VillagerLevelUpScreen screen;

        protected ConfirmUpgradeButton(int x, int y, VillagerLevelUpScreen screen) {
            super(x, y, 18, 18, CommonComponents.EMPTY, button -> {
                PacketDistributor.sendToServer(new LevelUpVillagerPacket(
                        ProgressiveTradingClient.lastInteractedVillager.getId()
                ));
            }, DEFAULT_NARRATION);

            this.x = x;
            this.y = y;
            this.screen = screen;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);

            graphics.blitSprite(
                    ResourceLocation.withDefaultNamespace("container/beacon/confirm"),
                    x + 1, y - 1, 0, 18, 18);

            if(screen.isMouseOver(x, y, width, height, mouseX, mouseY)) {
                Component tooltip = active ?
                        Component.translatable("progressivetrading.gui.tooltip.confirm_level_up") :
                        Component.translatable("progressivetrading.gui.tooltip.invalid_cost");

                graphics.renderTooltip(font, tooltip, mouseX, mouseY);
            }
        }
    }

    private static <T> T getCycledElement(List<T> list) {
        if (list == null || list.isEmpty()) return null;

        final int CAROUSEL_SPEED = 700;

        long currentTime = System.currentTimeMillis();
        int index = (int) ((currentTime / CAROUSEL_SPEED) % list.size());

        return list.get(index);
    }
}
