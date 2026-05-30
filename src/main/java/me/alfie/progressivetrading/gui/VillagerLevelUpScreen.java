package me.alfie.progressivetrading.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.alfie.alfinolib.gui.CommonAbstractContainerScreen;
import me.alfie.alfinolib.gui.GuiGraphicsX;
import me.alfie.alfinolib.gui.util.GuiGraphicsApi;
import me.alfie.alfinolib.gui.util.MousePos;
import me.alfie.alfinolib.networking.Networking;
import me.alfie.progressivetrading.ProgressiveTrading;
import me.alfie.progressivetrading.ProgressiveTradingClient;
import me.alfie.progressivetrading.datapack.CostRegistry;
import me.alfie.progressivetrading.datapack.codec.ItemCost;
import me.alfie.progressivetrading.gui.common.CommonRenderUtils;
import me.alfie.progressivetrading.gui.core.Sprite;
import me.alfie.progressivetrading.networking.LevelUpVillagerPacket;
import me.alfie.progressivetrading.networking.OpenMerchantMenuPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class VillagerLevelUpScreen extends CommonAbstractContainerScreen<@NotNull VillagerLevelUpMenu> {

    private int CLOSE_BUTTON_LEFT;
    private int CLOSE_BUTTON_TOP;

    private Button closeButton;
    private Button confirmButton;
    private List<Button> fakeTrades = new ArrayList<>();

    //private ItemStack requiredItem;
    private ItemCost validCosts = ItemCost.EMPTY;

    private final Component REQUIRES_COMPONENT = Component.translatable("progressivetrading.gui.label.requires_item");

    public VillagerLevelUpScreen(@NotNull VillagerLevelUpMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 276, 166);
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
    public void renderBackground(GuiGraphicsX gx, MousePos mousePos, float partialTick) {
        RenderSystem.enableBlend();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        //This overload not currently in AlfinoLib to blit background sprite.
        gx.graphics().blit(
                ResourceLocation.fromNamespaceAndPath(ProgressiveTrading.MODID,
                        "textures/gui/container/villager_level_up.png"),
                x, y,
                0,
                0.0F, 0.0F,
                this.imageWidth, this.imageHeight,
                512, 256);
        RenderSystem.disableBlend();

        boolean isMouseOverCloseButton = mousePos.isOver(CLOSE_BUTTON_LEFT, CLOSE_BUTTON_TOP, 14, 14);
        Sprite closeButton = isMouseOverCloseButton ? Sprite.CLOSE_MENU_ACTIVE : Sprite.CLOSE_MENU;

        confirmButton.active = validCosts.anyMatch(getMenu().getItemInSlot())
                && getMenu().getItemInSlot().getCount() >= validCosts.count();


        GuiGraphicsApi.blit(
                gx,
                closeButton.id(),
                CLOSE_BUTTON_LEFT, CLOSE_BUTTON_TOP,
                14, 14
        );

        CommonRenderUtils.renderVillagerPortrait(this, gx.graphics(), mousePos.x(), mousePos.y());
    }

    @Override
    public void render(GuiGraphicsX gx, MousePos mousePos, float partialTick) {
        super.render(gx, mousePos, partialTick);

        if(mousePos.isOver(CLOSE_BUTTON_LEFT, CLOSE_BUTTON_TOP, 14, 14)) {
            //No renderTooltip in AlfinoLib GraphicsApi
            gx.graphics().renderTooltip(font, Component.translatable("progressivetrading.gui.tooltip.back"),
                    mousePos.x(), mousePos.y());
        }

        super.renderTooltip(gx.graphics(), mousePos.x(), mousePos.y());
        Vector2i pos = calculateRequiresLabel();
        final int itemX = getGuiLeft() + pos.x() + font.width(REQUIRES_COMPONENT);
        final int itemY = getGuiTop() + pos.y() - 4;

        List<ItemStack> stacks = validCosts.getItems();
        GuiGraphicsApi.itemStackWithTooltipCycled(gx, stacks, font, itemX, itemY, mousePos, 700);
    }

    @Override
    public void renderLabels(GuiGraphicsX gx, MousePos mousePos) {
        this.titleLabelX = 49 + this.imageWidth / 2 - this.font.width(this.title) / 2 + ProgressiveTrading.MERCHANT_SHIFT_X;
        this.titleLabelY = 6;
        super.renderLabels(gx, mousePos);

        //"Unlocks Trades" above trades box
        Component unlocksTrades = Component.translatable("progressivetrading.gui.label.unlocks_trades")
                .withColor(4210752);

        final int labelX1 = 5 - font.width(unlocksTrades) / 2 + 48;
        GuiGraphicsApi.text(gx, font, unlocksTrades, labelX1, titleLabelY, false);

        //"Level-Up" -> New Profession" label
        if(ProgressiveTradingClient.lastInteractedVillager instanceof Villager villager) {
            int traderLevel = villager.getVillagerData().getLevel();

            Component nextLevel = CommonRenderUtils.buildLevelComponent(traderLevel+1)
                    .copy().withColor(4210752);

            Component fromToProfession = Component.translatable("progressivetrading.gui.label.villager_level_up_from_to", nextLevel)
                    .withColor(4210752);

            final int labelX2 = 49 + this.imageWidth / 2 - this.font.width(fromToProfession) / 2
                    + ProgressiveTrading.MERCHANT_SHIFT_X;
            final int labelY1 = titleLabelY + font.lineHeight;
            GuiGraphicsApi.text(gx, font, fromToProfession,
                    labelX2, labelY1, false);

        }

        //"Requires" label
        Component requiresLabel = REQUIRES_COMPONENT.copy().withColor(4210752);

        Vector2i pos = calculateRequiresLabel();

        GuiGraphicsApi.text(gx, font, requiresLabel,
                pos.x(), pos.y(), false);



    }

    private Vector2i calculateRequiresLabel() {
        final int x = 49 + this.imageWidth / 2 - this.font.width(REQUIRES_COMPONENT) / 2 + ProgressiveTrading.MERCHANT_SHIFT_X - 8;
        final int y = titleLabelY + font.lineHeight*2 + 4;
        return new Vector2i(x, y);
    }

    @Override
    public boolean onMouseClick(MousePos mousePos, int button) {
        if(mousePos.isOver(CLOSE_BUTTON_LEFT, CLOSE_BUTTON_TOP, 14, 14)) {
            Networking.sendToServer(new OpenMerchantMenuPacket(
                    ProgressiveTradingClient.lastInteractedVillager.getId()
            ));
            return true;

        }
        return super.onMouseClick(mousePos, button);
    }

    private static class FakeTradeButton extends Button {

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

            GuiGraphicsX gx = new GuiGraphicsX(graphics);
            MousePos mousePos = new MousePos(mouseX, mouseY);

            ItemStack costA = offer.getCostA();
            ItemStack costB = offer.getCostB();
            ItemStack result = offer.getResult();

            GuiGraphicsApi.itemStackWithTooltip(gx, costA, Minecraft.getInstance().font,
                    screen.getGuiLeft() + 10, y, mousePos);
            GuiGraphicsApi.itemStackWithTooltip(gx, costB, Minecraft.getInstance().font,
                    screen.getGuiLeft() + 40, y, mousePos);
            GuiGraphicsApi.itemStackWithTooltip(gx, result, Minecraft.getInstance().font,
                    screen.getGuiLeft() + 73, y, mousePos);

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
            MousePos mousePos = new MousePos(mouseX, mouseY);

            graphics.blitSprite(
                    ResourceLocation.withDefaultNamespace("container/beacon/confirm"),
                    x + 1, y - 1, 0, 18, 18);

            if(mousePos.isOver(x, y, width, height)) {
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
