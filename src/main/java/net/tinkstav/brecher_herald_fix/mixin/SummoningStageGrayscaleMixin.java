package net.tinkstav.brecher_herald_fix.mixin;

import iskallia.vault.entity.IGrayscale;
import iskallia.vault.entity.boss.stage.SummoningStage;
import net.tinkstav.brecher_herald_fix.BrecherHeraldFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Server-side fix: Prevents grayscale flag from being set on cursed entities.
 * This avoids the client-side "Not filled all elements of the vertex" crash
 * without requiring clients to install any mods.
 *
 * Trade-off: Cursed mobs will not appear grayscale, but curse mechanics
 * (damage bonuses, special effects) still work normally.
 */
@Mixin(value = SummoningStage.class, remap = false)
public class SummoningStageGrayscaleMixin {

    /**
     * Intercept setGrayscale(true) calls and suppress them.
     * The curse tag and other curse data are still applied normally.
     *
     * Note: remap = false on @At because IGrayscale is a Vault Hunters interface,
     * not vanilla Minecraft, so it has no SRG mappings.
     */
    @Redirect(
        method = "applyCurse(Lnet/minecraft/world/entity/Entity;Liskallia/vault/entity/boss/stage/SummoningStage$CurseType;)V",
        at = @At(
            value = "INVOKE",
            target = "Liskallia/vault/entity/IGrayscale;setGrayscale(Z)V",
            remap = false
        ),
        remap = false
    )
    private void brecherHeraldFix$suppressGrayscale(IGrayscale instance, boolean value) {
        // Don't set grayscale - this prevents the client rendering crash
        // The curse tag and "velara_cursed" tag are still applied by applyCurse
        BrecherHeraldFix.LOGGER.debug("Herald fix: Suppressed grayscale on cursed entity to prevent client crash");
        // Intentionally do nothing - not calling instance.setGrayscale()
    }
}
