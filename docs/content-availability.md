# Content Availability

## Contract

Revival Ages registers every registry entry unconditionally. Server content
configuration never changes the registry set, because clients, existing saves,
datapacks, and optional integrations must continue to resolve the same IDs.

Every gameplay module declares a `ContentPolicy`. `CoreFeature` and
`CreativeTabFeature` explicitly declare infrastructure policies; they cannot be
disabled. A gameplay policy owns one or more stable `ContentKey` definitions and
classifies every public item and block. `ContentAvailability` validates the
assembled catalog at server startup and fails startup when a key is missing,
duplicated, cyclic, or when registered public content is unclassified.

An unavailable content unit is:

- registered and loadable, with all saved item and block-entity state preserved;
- absent from the Revival Ages and vanilla creative tabs;
- unavailable through enabled crafting, processing recipes, or new world
  generation;
- rejected by normal placement, item use, block interaction, menus, and
  server-bound actions with a localized message;
- inert: no processing ticks, automation, damage, effects, particles, or sounds;
- absent from JEI and EMI ingredients, recipes, catalysts, and workstations;
- shown by Jade only as a preserved block disabled by the server;
- unavailable through item and fluid capabilities.

Administrative registry operations such as `/give` remain available for recovery.
They do not make the resulting item functional. Breaking an unavailable public
block returns its block item. If it has a block entity, the dropped item carries
the complete serialized block-entity state, including inventories and fluids, so
reenabling and replacing it resumes from that state.

All content toggles default to `true` and are restart-required. A child is
effectively enabled only when its own configured value and every parent value are
enabled. Revival Ages does not rewrite conflicting configuration. It logs a
warning when a child is configured `true` below a disabled parent.

## Catalog

| Content ID | Parent | Owning server config path |
| --- | --- | --- |
| `revivalages:surface_deposits` | — | `revivalages-surface-deposits-server.toml`: `surfaceDeposits.enabled` |
| `revivalages:surface_rocks` | `surface_deposits` | `surfaceDeposits.rocksEnabled` |
| `revivalages:surface_sticks` | `surface_deposits` | `surfaceDeposits.sticksEnabled` |
| `revivalages:knapping` | — | `revivalages-knapping-server.toml`: `knapping.enabled` |
| `revivalages:construction_frame` | — | `revivalages-construction-frame-server.toml`: `constructionFrame.enabled` |
| `revivalages:structural_integrity` | — | `revivalages-structural-integrity-server.toml`: `structuralIntegrity.enabled` |
| `revivalages:support_beams` | `structural_integrity` | `structuralIntegrity.supportBeams.enabled` |
| `revivalages:collapses` | `structural_integrity` | `structuralIntegrity.collapses.enabled` |
| `revivalages:landslides` | `structural_integrity` | `structuralIntegrity.landslides.enabled` |
| `revivalages:primitive_technology` | — | `revivalages-primitive-server.toml`: `primitiveTechnology.enabled` |
| `revivalages:raw_hide_drops` | `primitive_technology` | `primitiveTechnology.rawHideDropsEnabled` |
| `revivalages:crude_drying_rack` | `primitive_technology` | `primitiveTechnology.crudeDryingRackEnabled` |
| `revivalages:drying_rack` | `primitive_technology` | `primitiveTechnology.dryingRackEnabled` |
| `revivalages:campfire` | `primitive_technology` | `primitiveTechnology.campfire.enabled` |
| `revivalages:campfire_effects` | `campfire` | `primitiveTechnology.campfireEffects.enabled` |
| `revivalages:chopping_block` | `primitive_technology` | `primitiveTechnology.choppingBlock.enabled` |
| `revivalages:pit_kiln` | `primitive_technology` | `primitiveTechnology.pitKiln.enabled` |
| `revivalages:barrel` | `primitive_technology` | `primitiveTechnology.barrel.enabled` |
| `revivalages:soaking_pot` | `primitive_technology` | `primitiveTechnology.soakingPot.enabled` |
| `revivalages:tanning_rack` | `primitive_technology` | `primitiveTechnology.tanningRack.enabled` |
| `revivalages:stone_sawmill` | `primitive_technology` | `primitiveTechnology.stoneMachines.sawmill.enabled` |
| `revivalages:stone_oven` | `primitive_technology` | `primitiveTechnology.stoneMachines.oven.enabled` |
| `revivalages:stone_kiln` | `primitive_technology` | `primitiveTechnology.stoneMachines.kiln.enabled` |
| `revivalages:stone_crucible` | `primitive_technology` | `primitiveTechnology.stoneMachines.crucible.enabled` |
| `revivalages:anvil` | `primitive_technology` | `primitiveTechnology.anvil.enabled` |
| `revivalages:pit_burn` | `primitive_technology` | `primitiveTechnology.pitBurn.enabled` |
| `revivalages:flint_and_tinder` | `primitive_technology` | `primitiveTechnology.ignition.flintAndTinderEnabled` |
| `revivalages:wood_torch` | `primitive_technology` | `primitiveTechnology.ignition.woodTorchEnabled` |
| `revivalages:wooden_bucket` | `primitive_technology` | `primitiveTechnology.primitiveBuckets.woodenBucketEnabled` |
| `revivalages:clay_bucket` | `primitive_technology` | `primitiveTechnology.primitiveBuckets.clayBucketEnabled` |
| `revivalages:animal_power` | — | `revivalages-animal-power-server.toml`: `animalPower.enabled` |
| `revivalages:hand_grindstone` | `animal_power` | `animalPower.handGrindstone.enabled` |
| `revivalages:horse_grindstone` | `animal_power` | `animalPower.horseGrindstoneEnabled` |
| `revivalages:horse_chopping_block` | `animal_power` | `animalPower.choppingBlock.enabled` |
| `revivalages:horse_press` | `animal_power` | `animalPower.press.enabled` |

Shared materials declare every producer or consumer in their policy. They remain
visible while at least one declared content unit is enabled. Examples include
straw and thatch, saw blades, hide-processing intermediates, ash, wood chips, and
unfired brick.

## Data conditions

Datapacks and other mods can gate data with the universal condition:

```json
{
  "type": "revivalages:content_enabled",
  "content": "revivalages:barrel",
  "enabled": true
}
```

Data shared by several consumers can use:

```json
{
  "type": "revivalages:any_content_enabled",
  "contents": [
    "revivalages:hand_grindstone",
    "revivalages:horse_grindstone"
  ],
  "enabled": true
}
```

The legacy `knapping_enabled`, `construction_frame_enabled`, and
`structural_enabled` conditions remain registered and delegate to the same
effective catalog. Recipe types and serializers also remain registered when all
consumers are disabled. Block loot tables use the universal condition, while the
disabled-break lifecycle returns saved blocks without consulting those normal
loot tables. Surface-deposit biome modifiers declare a `content` field and
contribute no placed feature when that key is unavailable.

Configuration-backed conditions are reevaluated through a single server resource
reload after startup when any content is disabled. Hot toggle changes during a
running world are unsupported.

`runData` validates all conditioned recipes, block loot tables, and biome
modifiers against the central catalog, then writes the deterministic
`data/revivalages/content_availability/manifest.json` review manifest. A missing
or unknown gate makes datagen fail. The normal Gradle `check`/`build` lifecycle
depends on `runData`; common setup also validates public registry
classification, so these omissions are release-blocking.

## Verification and future features

`runGameTestServer` is the all-enabled profile.
`runGameTestServerContentDisabled` starts a dedicated GameTest server with the
same registry set and forces every content key unavailable. Enabled-profile
GameTests skip only in that explicit disabled profile; availability contract
tests do the inverse.

The unit suite validates dependency graphs, effective parent state, conflicts,
all recipe and block-loot gates, all surface worldgen gates, and translations.
The disabled GameTests validate registry/classification safety, legacy and
universal conditions, recipe removal, interaction rejection, null capabilities,
and lossless block-entity drops.

A new gameplay feature is incomplete until it:

1. declares its `ContentKey`, parents, configured supplier, and public
   item/block memberships in a mandatory `ContentPolicy`;
2. adds a default-`true`, restart-required setting in the owning server config;
3. gates recipes, acquisition data, worldgen, creative tabs, runtime behavior,
   capabilities, payloads, and every applicable optional integration;
4. preserves existing serialized state and supports a lossless disabled break;
5. adds enabled and disabled tests and translations.

Catalog startup validation and resource-gate unit tests intentionally fail the
build when these requirements are omitted.
