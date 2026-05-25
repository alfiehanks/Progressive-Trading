package me.alfie.progressivetrading.gui.core;

import me.alfie.progressivetrading.gui.VillagerLevelUpScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class HoverableItemStack {

    private final VillagerLevelUpScreen screen;
    private ItemStack stack;
    private float x;
    private float y;

    public HoverableItemStack(VillagerLevelUpScreen screen, ItemStack stack) {
        this.screen = screen;
        this.stack = stack;
    }

    public void render(GuiGraphics graphics, double mouseX, double mouseY) {
        if(stack == null) {
            stack = ItemStack.EMPTY;
        }

        graphics.renderItem(stack, (int) x, (int) y);
        graphics.renderItemDecorations(Minecraft.getInstance().font, stack, (int) x, (int) y);

        if (screen.isMouseOver(x, y, 16, 16, mouseX, mouseY)) {
            if (stack.is(Items.AIR)) return;
            graphics.renderTooltip(Minecraft.getInstance().font, stack,
                    (int) mouseX, (int) mouseY);
        }
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }
}