package net.tinkstav.brecher_herald_fix.mixin.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import iskallia.vault.client.render.buffer.GrayscaleBufferSource;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fixes "Not filled all elements of the vertex" crash when rendering entity
 * name labels with grayscale effect enabled.
 *
 * The grayscale shader expects vertex formats with UV2 (lightmap) data.
 * Font/glyph rendering uses formats without UV2, causing a vertex mismatch.
 * This mixin detects incompatible formats by checking for UV2 presence and
 * passes them through unchanged to the delegate buffer source.
 *
 * Compatible with Embeddium/Sodium optimized font rendering and Emojiful.
 */
@Mixin(value = GrayscaleBufferSource.class, remap = false)
public abstract class GrayscaleBufferSourceMixin {

    @Shadow
    @Final
    private MultiBufferSource delegate;

    @Inject(method = "getBuffer", at = @At("HEAD"), cancellable = true)
    private void brecherHeraldFix$passthroughIncompatibleFormats(RenderType type, CallbackInfoReturnable<VertexConsumer> cir) {
        // Check if the RenderType's format is compatible with the grayscale shader.
        // The grayscale shader expects UV2 (lightmap) data. If the format doesn't have it,
        // pass through to the original buffer to avoid "Not filled all elements" crash.
        if (type != null && !hasLightmapElement(type.format())) {
            cir.setReturnValue(this.delegate.getBuffer(type));
        }
    }

    /**
     * Check if the vertex format contains the lightmap element (UV2).
     * Entity rendering formats have UV2, font/text formats do not.
     *
     * @param format The vertex format to check (may be null)
     * @return true if format contains UV2 element, false otherwise
     */
    private static boolean hasLightmapElement(VertexFormat format) {
        if (format == null) {
            return false;
        }
        for (VertexFormatElement element : format.getElements()) {
            // ELEMENT_UV2 is the lightmap element (usage: UV, index: 2)
            if (element.getUsage() == VertexFormatElement.Usage.UV && element.getIndex() == 2) {
                return true;
            }
        }
        return false;
    }
}
