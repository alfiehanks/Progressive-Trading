package me.alfie.progressivetrading.gui.core;

import me.alfie.progressivetrading.ProgressiveTrading;
import net.minecraft.resources.ResourceLocation;

public enum Sprite {
    LEVEL_UP_BUTTON("textures/gui/sprites/level_up_button.png", 22, 22);

    private final ResourceLocation id;
    private final int width;
    private final int height;

    Sprite(String path, int width, int height) {
        this.id = ResourceLocation.fromNamespaceAndPath(ProgressiveTrading.MODID, path);
        this.width = width;
        this.height = height;
    }

    public ResourceLocation id() {
        return id;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }
}
