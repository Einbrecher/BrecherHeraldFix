package net.tinkstav.brecher_herald_fix.mixin;

import net.tinkstav.brecher_herald_fix.BrecherHeraldFix;
import iskallia.vault.entity.boss.ArtifactBossEntity;
import iskallia.vault.entity.boss.stage.SummoningStage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/**
 * Fixes the Herald boss event listener leak that causes exponential mob spawning.
 *
 * The bug: SummoningStage registers a LivingDeathEvent listener but relies on AI goal
 * lifecycle to unregister. If the entity is invalidated without the goal stopping,
 * the listener remains registered and processes events for non-existent bosses.
 *
 * The fix: Check boss entity validity at handler start. If invalid, unregister and cancel.
 */
@Mixin(value = SummoningStage.class, remap = false)
public abstract class SummoningStageEventMixin {

    @Shadow
    @Final
    private ArtifactBossEntity boss;

    @Shadow
    private Consumer<LivingDeathEvent> onLivingDeath;

    @Inject(method = "onSummonedMobDeath", at = @At("HEAD"), cancellable = true)
    private void brecherHeraldFix$checkBossValidity(LivingDeathEvent event, CallbackInfo ci) {
        if (this.boss == null) {
            BrecherHeraldFix.LOGGER.debug("Herald fix: boss reference is null, cleaning up listener");
            cleanupListener();
            ci.cancel();
            return;
        }

        if (this.boss.isRemoved()) {
            BrecherHeraldFix.LOGGER.debug("Herald fix: boss is removed, cleaning up listener");
            cleanupListener();
            ci.cancel();
            return;
        }

        if (this.boss.isDeadOrDying()) {
            BrecherHeraldFix.LOGGER.debug("Herald fix: boss is dead/dying, cleaning up listener");
            cleanupListener();
            ci.cancel();
            return;
        }

        if (this.boss.level == null) {
            BrecherHeraldFix.LOGGER.debug("Herald fix: boss level is null, cleaning up listener");
            cleanupListener();
            ci.cancel();
            return;
        }
    }

    private void cleanupListener() {
        if (this.onLivingDeath != null) {
            try {
                MinecraftForge.EVENT_BUS.unregister(this.onLivingDeath);
                BrecherHeraldFix.LOGGER.info("Herald fix: Successfully unregistered leaked event listener");
            } catch (Exception e) {
                BrecherHeraldFix.LOGGER.debug("Herald fix: Listener cleanup exception (may already be unregistered): {}", e.getMessage());
            }
        }
    }
}
