# Drying Rack

Crude and normal racks have independent restart-required content toggles under
the Primitive Technology family and follow the shared
[content availability contract](content-availability.md).

The Drying Rack is a server-authoritative, no-menu processing feature. A Crude
Drying Rack has one wall-mounted slot. A normal
Drying Rack has four independent slots, inherits crude recipes, processes at a
1.35 default multiplier, and can act as a ladder when vertically stacked.

## Interaction and feedback

- Right-click an empty slot to insert one held item. An item without a matching
  recipe may still be displayed and recovered.
- Right-click an occupied slot with an empty hand to retrieve its item. A held
  item never removes stored contents. If the inventory is full, the retrieved
  stack drops at the rack instead of being deleted.
- While sneaking, scroll up over a valid interaction area to insert one
  main-hand item and scroll down to extract one item. The server validates the
  targeted block, face, slot, distance, availability, and current contents.
- A normal rack accepts manual interaction only through the top face of its
  selected quarter. The crude rack retains its single wall-mounted interaction
  area on every face.
- Breaking the rack or removing a crude rack's supporting block drops all stored
  items. Progress belongs to its slot and is synchronized by the block entity.
- Displayed items use direction-aware transforms for every horizontal facing.
  Crude-rack items lie on the wall-facing mesh; normal-rack items lie on the top
  grill.
- The selected interaction area receives a green outline. A held item is
  previewed translucently in an empty selected slot. Both forms of feedback have
  independent client toggles.
- A subtle progress particle appears only while at least one valid recipe is
  advancing. It can be disabled in `revivalages-client.toml` with
  `dryingRack.showProgressParticles=false`.

The crude rack deliberately requires a sturdy wall and breaks when that support
is lost. This is an intentional safety improvement over the designated
behavioral source. Normal-rack climbing deliberately applies to every
`LivingEntity`, not only players. Optional item-handler automation is also a
Revival Ages extension and remains disabled by default. A disabled rack refuses
new manual and automated insertion while preserving existing contents.

## Recipe duration and environment

The built-in crude recipe converts one wheat into one straw in 14,400 ticks.
Wet Sponge keeps its 9,600-tick recipe. No Rotten Flesh to Leather recipe is
provided. Recipes for materials not present in Revival Ages are intentionally
absent, while the normal-rack crafting recipe continues to consume a crude rack.

Each rack has its own base recipe-duration modifier. A crude recipe inherited by
the normal rack first receives the inherited-crude modifier and then the normal
base modifier. Existing environmental speed remains a separate value, so
duration and changing weather are not folded into one persisted number.

Datapacks can override the exact base speed for individual biomes or biome tags:

```text
data/<namespace>/data_maps/worldgen/biome/crude_drying_speed.json
data/<namespace>/data_maps/worldgen/biome/drying_speed.json
```

An explicit biome value replaces the rain, dimension, and biome-tag base-speed
selection. Nearby directional fire sources, daylight, the selected season
provider, and the final rack multiplier are still applied. Heat detection uses
the shared directional fire-source contract rather than a fixed list of lava or
campfire blocks.

## Seasonal configuration

All seasonal balance is read from `revivalages-server.toml`:

```toml
[dryingRack.seasons]
enabled = true
springBonus = 0.1
summerBonus = 0.3
autumnBonus = -0.1
winterBonus = -0.3
```

The values are additive and may be positive, zero, or negative. They are read at
environment-calculation time and are not stored in recipes, chunks, block entity
NBT, or world data. Disabling the section makes the seasonal contribution zero.

Ecliptic Seasons is selected when both supported season mods are installed.
Otherwise Serene Seasons is used. Exactly one configured bonus is applied:

```text
(environment speed + configured season bonus) * rack multiplier
```

## Optional integration status

| Mod | Status | Version/API | Side | Verification and notes |
| --- | --- | --- | --- | --- |
| Serene Seasons | Implemented | 1.21.1-10.1.0.3, `SeasonHelper` | Both | Compile-only CurseMaven dependency; absent-safe. |
| Ecliptic Seasons | Implemented | 1.21.1-0.13.7, `EclipticSeasonsApi` | Both | Compile-only CurseMaven dependency; preferred when both season mods are loaded. |
| Biomes O' Plenty | Data-compatible | NeoForge biome tags | Server | No BOP API dependency or hardcoded biome IDs. |
| Progressive Stages | Data-compatible | Stable block/item/recipe IDs | Server | Generic registry and recipe locking can target the feature. |
| KubeJS | Data-compatible | Custom recipe JSON | Server | Scripts can add/remove both stable recipe types. A typed schema remains future work. |
| Jade | Implemented | 15.10.5 for NeoForge | Client | Present/absent client launches verified. Shows speed, modifiers, multiplier, and per-slot recipe progress. |
| EMI | Implemented | 1.1.24 for Minecraft 1.21.1 | Client | Present/absent launches verified. Native categories, workstations, animated time, and inheritance. |
| JEI | Implemented | 19.39.0.369 | Client | Present/absent launches verified. Categories, catalysts, duration, and inheritance. |
| Curios | Not applicable | N/A | N/A | Placed racks expose no wearable/equipment behavior. |

JEI and EMI use separate presentation adapters but enumerate the same Drying Rack
recipe types from `RecipeManager`. Normal-rack recipes override matching crude
alternatives through shared feature-owned query logic; all remaining crude
alternatives are shown as inherited without duplicating recipe JSON or viewer-side
matching rules. Jade, JEI, and EMI remain optional: their API classes are isolated
under `integration`, compiled without being bundled, and never loaded when the
corresponding mod is absent.

Both recipe viewers use licensed slot-background and animated progress-arrow
artwork adapted to the modern viewer APIs. Its attribution is recorded in
`THIRD_PARTY_NOTICES.md`.

JEI deliberately retains its fixed 200-tick progress animation, while EMI uses
the effective configured recipe duration. Both show the same effective time and
enumerate recipes through the shared catalog.

Development coordinates are `mezz.jei:jei-1.21.1-*-api:19.39.0.369`,
`maven.modrinth:fRiHVvU7:5sIPA1To` for EMI, and
`maven.modrinth:nvQzSEkH:yd8FKCmx` for Jade. All are `compileOnly`; the full mods
are added to `localRuntime` only by the explicit compatibility-test property.

For a local compatibility launch, use:

```text
gradlew.bat runClient -PdryingRackIntegrationsRuntime=true
```

## Third-party assets

The rack models and crude rack texture are adapted from licensed third-party
assets. See the repository-level third-party notices and bundled licenses.
