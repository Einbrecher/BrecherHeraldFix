# BrecherHeraldFix

A Forge mod that fixes Herald boss bugs in Vault Hunters, including event listener leaks and grayscale rendering crashes.

## Bugs Fixed

### 1. Event Listener Leak (Exponential Mob Spawning)

When a Herald boss entity is invalidated (via chunk unload, `/kill` command, or server restart) without proper cleanup, its `SummoningStage` event listeners remain registered on the global event bus. These "zombie" listeners continue processing `LivingDeathEvent` for all entity deaths, causing exponential mob spawning in subsequent Herald fights.

### 2. Grayscale Rendering Crash

Cursed mobs spawned by the Herald have a grayscale shader applied. This shader expects vertex formats with UV2 (lightmap) data, but font/text rendering (entity name labels) uses formats without UV2. This mismatch causes a "Not filled all elements of the vertex" crash when rendering cursed entity names.

## The Fixes

### SummoningStageEventMixin (Server-side)

Injects at the start of the death event handler to check if the boss entity is still valid. If the boss is null, removed, dead, or has no level, the listener is unregistered and the handler is cancelled.

### SummoningStageGrayscaleMixin (Server-side)

Prevents the grayscale flag from being set on cursed entities. This avoids the client-side crash without requiring clients to install any mods. Trade-off: cursed mobs won't appear grayscale visually, but all curse mechanics (damage bonuses, special effects) still work normally.

### GrayscaleBufferSourceMixin (Client-side)

Detects incompatible vertex formats by checking for UV2 (lightmap) presence and passes them through unchanged to the delegate buffer source. This allows grayscale rendering to work correctly while preventing crashes from font/glyph rendering. Compatible with Embeddium/Sodium optimized font rendering and Emojiful.

## Installation

**Server-side (required):**
1. Copy `brecher_herald_fix-<version>.jar` to your server's `mods/` folder
2. Restart the server

**Client-side (optional):**
- Installing on clients enables grayscale visuals for cursed mobs while preventing the rendering crash
- Without the client mod, cursed mobs appear normal but curse mechanics still work

## Compatibility

- Minecraft: 1.18.2
- Forge: 40.2.x
- Vault Hunters: 3.18.x and later (tested with 3.20.3.6055)

## Verification

Check logs for these messages during Herald fights:
- `Herald fix: Successfully unregistered leaked event listener` - Cleaning up leaked listeners
- `Herald fix: Suppressed grayscale on cursed entity to prevent client crash` - Grayscale suppression active

## License

MIT
