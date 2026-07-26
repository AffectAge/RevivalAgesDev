# Structural Integrity

The family toggle and the independent support-beam, collapse, and landslide
toggles follow the shared [content availability contract](content-availability.md).

## Scope

Structural Integrity owns Support Beams, mining- and explosion-triggered
Collapses, and delayed Landslides. Registry identities are unconditional.
Server configuration controls acquisition, creative visibility, and simulation
without making existing worlds unreadable.

Nine Support Beam materials share one placement item per material. Placement on
the floor produces a vertical support; placement against a side produces a
horizontal span. Vertical auto-stacking and horizontal span placement validate
the entire transaction before consuming additional items. Removing the base of
a vertical post immediately invalidates its unsupported upper segments and any
dependent horizontal span through ordinary neighbor updates; automatically
removed segments apply their normal loot tables and drop their support items.

The `revivalages:support` block data map is the canonical reloadable source of
support definitions. It lets data packs and other mods assign upward, downward,
and horizontal ranges to blocks or block tags, optionally restricted by exact
block-state properties. For example:

```json
{
  "values": {
    "examplemod:reinforced_post": {
      "ingredient": {
        "properties": {
          "reinforced": "true"
        }
      },
      "support_up": 1,
      "support_down": 5,
      "support_horizontal": 4
    }
  }
}
```

Definitions are re-read with server data and tag reloads; no client class is
needed to decode or query them. Missing properties and invalid property values
do not match. The built-in beam definition is itself ordinary data at
`data/revivalages/data_maps/block/support.json`, so a higher-priority data pack
can replace it without Java changes. Only completed horizontal beam blocks carry
the built-in support definition. Vertical posts anchor spans but do not project
a support radius by themselves. Collapse and Landslide
transformations are data-driven through the `revivalages:collapse` and
`revivalages:landslide` recipe types, while tags define blocks that can trigger,
start, continue, or resist structural movement.

Structural work is server-authoritative and bounded by per-level saved data.
Each collapse stores its center, shrinking squared radius, current vertical
frontier, and next vertical frontier. A collapsed position can advance only to
the block immediately above it; collapse propagation never performs an
unbounded neighboring-block flood fill. Unloaded frontier positions are
deferred without loading chunks, and queue size and per-tick processing budget
remain configurable.

Collapse tags determine whether a block may trigger, start, or continue a
collapse. An explicit `revivalages:collapse` recipe may transform its state;
otherwise a block in `revivalages:can_collapse` falls with its original state.
Soil, sand, clay, and other unrelated terrain are not implicit collapse inputs.
Falling material destroys an obstacle only when the complete directional
fall-through contract succeeds: the impacted face is not sturdy, the falling
state has equal or greater toughness, and the obstacle is breakable and is not
structure void. Full clay, stone, and deepslate therefore stop falling
cobblestone instead of allowing it to drill a vertical shaft. Soil, sand, and
gravel use the default toughness level; stone and cobbled stone use level two.
Support Beams do not receive artificial toughness after a Collapse has already
started.

Real Collapse starts, later propagation generations, and fake-collapse warning
events send bounded camera-shake impulses to nearby players. Server settings
control radius, strength, and duration independently for warning, initial, and
propagation impulses. Client settings can disable the effect or scale its
intensity. Camera shake changes temporary view angles only and never changes
the player's authoritative rotation.

## Optional integration assessment

| Integration | Status | Notes |
| --- | --- | --- |
| KubeJS | applicable | Typed Collapse and Landslide schemas emit ordinary data-pack JSON through the canonical codecs; support data remains a normal data map. The adapter compiles against file `7143884`; an in-game script smoke test is still pending. |
| Jade | applicable | Support range and supported, unsupported, and disabled states are inspectable. Jade 15.10.5 was tested on the client and dedicated GameTest server. |
| EMI | not applicable | Structural movement does not define a workstation recipe UI; Support Beam crafting remains a normal crafting recipe. |
| JEI | not applicable | Structural movement does not define a workstation recipe UI; Support Beam crafting remains a normal crafting recipe. |
| Curios | not applicable | No wearable or accessory state exists. |
| Progressive Stages | applicable | Stable Support Beam recipe IDs can be gated without changing registry identity. |
| Biomes O' Plenty | not applicable | Third-party stone is added through tags and transformation recipes, not a direct biome adapter. |
| Serene Seasons | not applicable | Structural rules have no seasonal input. |
| Ecliptic Seasons | not applicable | Structural rules have no seasonal input. |

The base implementation and dedicated server do not load optional API classes.
The dedicated GameTest suite covers reload-visible support data, block-state
predicates, asymmetric ranges, placement limits, foundation semantics, strict
replacement rules, and immediate span invalidation.

## Configuration

`revivalages-structural-integrity-server.toml` contains independent enable
settings for Support Beams, Collapses, and Landslides plus auto-build limits,
span length, saw wear, trigger and propagation probabilities,
collapse radius, cadence, falling damage, delay, queue capacity, tick budget,
and camera-shake radius, strength, and duration. Enable settings default to
`true`.

`revivalages-structural-integrity-client.toml` contains `cameraShake.enabled`
and the local `cameraShake.intensity` multiplier.
