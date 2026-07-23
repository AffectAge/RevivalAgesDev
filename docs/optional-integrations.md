# Optional Integration Catalog

This document is the single source of truth for optional mods that every new or
changed Revival Ages mechanic must assess. Add, remove, or rename catalog entries
only in the table below; repository instructions must link here instead of
duplicating the global list.

An integration is required only when the mechanic exposes the corresponding
domain surface. Record one of these outcomes in the mechanic's documentation:

- `applicable`: implement and test the adapter;
- `not applicable`: give a short domain-specific reason;
- `blocked`: record the checked Minecraft 1.21.1 version, API, date, and blocker.

Convenience, implementation effort, or an absent local development dependency is
not a valid reason to mark an integration `not applicable`. Never claim support
without testing a compatible Minecraft 1.21.1 build.

## Catalog

| Package | Mod | Applicable when the mechanic provides |
| --- | --- | --- |
| `kubejs` | KubeJS | Scripts, custom recipes, registries, configuration hooks, or bounded gameplay operations |
| `jade` | Jade | Inspectable block/entity state, progress, modifiers, inventory, fluids, or blocking conditions |
| `emi` | EMI | Recipes, usages, catalysts, workstations, transfer, or state-bearing recipe UI |
| `jei` | JEI | Recipes, usages, catalysts, workstations, transfer, or state-bearing recipe UI |
| `curios` | Curios | Wearable, accessory, equipment-slot, or player-bound item behavior |
| `progressivestages` | Progressive Stages | Gated recipes, content, interactions, or progression milestones |
| `biomesoplenty` | Biomes O' Plenty | Biome-sensitive behavior, tags, placement, spawning, or world generation |
| `sereneseasons` | Serene Seasons | Crops, temperature, weather, climate, seasons, or seasonal modifiers |
| `eclipticseasons` | Ecliptic Seasons | Crops, temperature, weather, climate, seasons, or seasonal modifiers |

Package names are repository organization hints, not dependency coordinates.
Resolve the actual mod ID, supported version, API coordinates, side, and license
from the compatible 1.21.1 release when implementing an adapter.

## Shared requirements

- All integrations are optional. The base mod must compile, start, create worlds,
  and run on a dedicated server when none are installed.
- Keep adapters under `integration/<mod_id>` and load them only when the target
  mod is present on the correct physical side.
- Integrations consume loader-neutral feature contracts or read models and never
  become the source of gameplay truth.
- When multiple catalog entries cover the same domain, they must expose the same
  underlying facts and must not apply gameplay effects more than once.
- Test every implemented adapter with the target mod present and absent. For a
  client display adapter, also test a dedicated server without the client mod.
- Maintain supported version, dependency coordinates, side, test status, and
  known limitations in the relevant mechanic documentation.

## Feature assessments

- [Animal Power](animal-power.md#optional-integration-assessment)
