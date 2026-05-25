package me.alfie.progressivetrading.mixin;

import me.alfie.progressivetrading.ProgressiveTrading;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public class VillagerMixin {

    /**
     * Stop vanilla from automatically levelling up villagers when XP treshold reached.
     * @param ci
     */
    @Inject(
            method = "customServerAiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/npc/Villager;increaseMerchantCareer()V"
            ),
            cancellable = true
    )
    private void progressivetrading$blockVanillaLeveling(CallbackInfo ci) {
        ci.cancel(); // prevents increaseMerchantCareer()
    }
}
