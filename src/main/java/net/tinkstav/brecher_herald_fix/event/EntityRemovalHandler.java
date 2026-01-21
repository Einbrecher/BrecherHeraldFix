package net.tinkstav.brecher_herald_fix.event;

import net.tinkstav.brecher_herald_fix.BrecherHeraldFix;
import iskallia.vault.entity.boss.ArtifactBossEntity;
import iskallia.vault.entity.boss.stage.IBossStage;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

/**
 * Handles ArtifactBossEntity removal to ensure stage cleanup.
 * Replaces the failing ArtifactBossEntityMixin which couldn't target inherited remove() method.
 */
@Mod.EventBusSubscriber(modid = BrecherHeraldFix.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityRemovalHandler {

    @SubscribeEvent
    public static void onEntityLeaveWorld(EntityLeaveWorldEvent event) {
        if (!(event.getEntity() instanceof ArtifactBossEntity boss)) {
            return;
        }

        if (boss.level.isClientSide) {
            return;
        }

        // Diagnostic logging for isRemoved() behavior verification
        BrecherHeraldFix.LOGGER.debug("Herald fix: Boss leaving world - isRemoved={}", boss.isRemoved());

        // CRITICAL: Only run when entity is actually being removed, not during chunk unload
        if (!boss.isRemoved()) {
            BrecherHeraldFix.LOGGER.debug("Herald fix: Skipping cleanup (entity not removed, likely chunk unload)");
            return;
        }

        BrecherHeraldFix.LOGGER.debug("Herald fix: Boss entity removal detected, ensuring stage cleanup");

        try {
            Optional<IBossStage> currentStage = boss.getCurrentStage();
            if (currentStage.isPresent()) {
                IBossStage stage = currentStage.get();
                BrecherHeraldFix.LOGGER.debug("Herald fix: Cleaning up stage: {}", stage.getName());

                try {
                    stage.stop();
                } catch (Exception e) {
                    BrecherHeraldFix.LOGGER.debug("Herald fix: Stage stop() exception: {}", e.getMessage());
                }

                try {
                    stage.finish();
                } catch (Exception e) {
                    BrecherHeraldFix.LOGGER.debug("Herald fix: Stage finish() exception: {}", e.getMessage());
                }

                BrecherHeraldFix.LOGGER.info("Herald fix: Stage cleanup completed for boss removal");
            }
        } catch (Exception e) {
            BrecherHeraldFix.LOGGER.warn("Herald fix: Error during boss removal cleanup: {}", e.getMessage());
        }
    }
}
