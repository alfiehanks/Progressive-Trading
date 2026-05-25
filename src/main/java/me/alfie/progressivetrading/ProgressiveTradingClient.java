package me.alfie.progressivetrading;

import net.minecraft.world.entity.npc.AbstractVillager;

public class ProgressiveTradingClient {

    public static AbstractVillager lastInteractedVillager;

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
    }
}
