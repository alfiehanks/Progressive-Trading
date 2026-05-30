package me.alfie.progressivetrading.gui.core;

import me.alfie.alfinolib.util.ResourceId;
import me.alfie.progressivetrading.ProgressiveTrading;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;

public enum Sprite {
    VILLAGER_LEVEL_UP_GUI("textures/gui/container/villager_level_up.png", 512, 256),
    CLOSE_MENU("textures/gui/sprites/close_menu.png", 14, 14),
    CLOSE_MENU_ACTIVE("textures/gui/sprites/close_menu_active.png", 14, 14)
    ;

    private final ResourceId id;
    private final int width;
    private final int height;

    Sprite(String path, int width, int height) {
        this.id = new ResourceId(ProgressiveTrading.MODID, path);
        this.width = width;
        this.height = height;
    }

    public ResourceId id() {
        return id;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }
}
