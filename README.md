# BrecherHeraldFix

A server-side Forge mod that fixes the Herald boss event listener leak bug in Vault Hunters.

## The Bug

When a Herald boss entity is invalidated (via chunk unload, `/kill` command, or server restart) without proper cleanup, its `SummoningStage` event listeners remain registered on the global event bus. These "zombie" listeners continue processing `LivingDeathEvent` for all entity deaths, causing exponential mob spawning in subsequent Herald fights.

## The Fix

This mod adds two mixins:

1. **SummoningStageEventMixin**: Injects at the start of the death event handler to check if the boss entity is still valid. If the boss is null, removed, dead, or has no level, the listener is unregistered and the handler is cancelled.

2. **ArtifactBossEntityMixin**: Hooks into the entity removal process to explicitly call `stop()` and `finish()` on the current boss stage, ensuring cleanup happens even when normal AI goal lifecycle is bypassed.

## Installation

1. Copy `brecher_herald_fix-1.0.0.jar` to your server's `mods/` folder
2. Restart the server
3. Verify in logs: look for "BrecherHeraldFix loaded"

## Compatibility

- Minecraft: 1.18.2
- Forge: 40.2.x
- Vault Hunters: 3.18.x and later (tested with 3.20.3.6055)

## Verification

After installation, check server logs for these messages during Herald fights:
- `Herald fix: Successfully unregistered leaked event listener` - Fix is actively cleaning up leaked listeners
- `Herald fix: Stage cleanup completed for boss removal` - Defense-in-depth cleanup triggered

## License

MIT
