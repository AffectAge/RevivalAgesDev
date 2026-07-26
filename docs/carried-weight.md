# Carried Weight

## Scope

Carried Weight limits how much a player can comfortably carry. It is independent
from Item Size: Item Size controls whether an item fits in a chest, Bundle, or
Pit Kiln, while Carried Weight reads the player's inventory and applies
player-only penalties. Chests never reject items because of weight, and this
module never changes stack limits.

The feature is controlled by `carriedWeight.enabled` in
`revivalages-carried-weight-server.toml`. Registry identities remain registered
when it is disabled; current penalties are removed and the HUD and weight
tooltips are hidden. The setting is restart-required for content-policy
consistency.

## Weight lookup

`WeightApi` exposes per-stack weight, current carried weight, capacity, pockets,
and overload state. External code registers prioritized item providers,
player-inventory sources, capacity providers, and pocket providers during
`RegisterCarriedWeightProvidersEvent`. Registration closes after common setup so
runtime ordering cannot change unexpectedly.

The canonical item lookup order is:

1. an `ItemWeightProvider` implemented by the item;
2. an `ItemWeightProvider` implemented by the block of a `BlockItem`;
3. registered item providers, ordered by priority:
   explicit `revivalages:item_weight`, supported portable-container contents,
   the block formula, then the generic item formula.

Invalid, negative, NaN, or infinite provider results are normalized before they
reach inventory totals.

The synchronized item data maps use standard NeoForge data-map files:

```json
{
  "values": {
    "example:heavy_part": 1250.0,
    "#example:pocketed_armor": 2
  }
}
```

Place fixed weights in
`data/<namespace>/data_maps/item/item_weight.json` and pocket counts in
`data/<namespace>/data_maps/item/pockets.json`. Values represent grams for one
item and a non-negative integer pocket count respectively. A data-map value
takes precedence over formulas.

The extendable `revivalages:technical_weight_items` item tag marks blocks and
items that use the configurable technical-item base weight. Classification uses
item types and common tags rather than registry-name substring matching.

## Formulas and containers

Defaults use a 90,000 g base capacity and 9,000 g per pocket. Vanilla armor
pockets are:

```text
max(1, 7 - floor(protection / 1.2) - toughness)
```

The configured category bases are 120 g for buckets, 60 g for bottles, 240 g
for blocks, 90 g for ingots/gems/shards, 10 g for nuggets, 50 g for generic
items, and 30,000 g for technical items. Formula settings also cover maximum
stack size, rarity, food, fire resistance, durability, armor, block hardness,
blast resistance, transparency, block entities, slabs, and stairs.

Bundle contents, `DataComponents.CONTAINER`, and item-handler capability
contents are read through one adapter per stack. Contents contribute 50% by
default and nest to a configurable maximum depth of eight. No arbitrary NBT or
third-party backpack-name heuristics are used.

Player weight includes the main inventory, hotbar, offhand, armor, and any
registered optional equipment source exactly once. The server recalculates at
the configured cadence and synchronizes only changed derived state. Current
weight is not saved. The persistent
`revivalages:carry_capacity_bonus` player attribute stores permanent capacity
bonuses.

## Overload and client presentation

At or above capacity the server applies the hidden `revivalages:overloaded`
effect and transient movement-speed, attack-speed, and attack-damage modifiers.
Each additional configured percentage step raises the level up to the configured
maximum. Strength and Haste reduce that level. Creative and Spectator players
receive no penalty.

Jump height is adjusted through NeoForge's player jump event after vanilla jump
velocity is established. This keeps the change player-only and removes it from
the call path when the feature is disabled. Optional realistic mode starts
gradual penalties at the configured fraction of capacity; it is disabled by
default.

Client configuration controls weight tooltips, compact or precise Shift
formatting, two HUD styles, sprite/bar dimensions and position, offsets, text,
color, and shadow. Item Size and Carried Weight always use separate tooltip
lines.

Administrators can inspect and diagnose the system with
`/revivalages weight`, including player totals, base/bonus capacity, held-item
weight, and armor pockets.

## Optional integration assessment

| Catalog entry | Status | Notes |
| --- | --- | --- |
| KubeJS | applicable | Read-only bindings expose item weight, carried weight, capacity, and pockets. Overrides remain codec-validated data-map JSON. |
| Jade | not applicable | The feature has no inspectable placed-block or entity surface. |
| EMI | not applicable | No recipes or recipe-viewer categories are added. |
| JEI | not applicable | No recipes or recipe-viewer categories are added. |
| Curios | applicable | The optional 1.21.1 API contributes equipped stacks as one additional player weight source. It does not grant pockets automatically. |
| Progressive Stages | not applicable | No recipes, content gates, or progression milestones are introduced. |
| Biomes O' Plenty | not applicable | Weight does not depend on biome data or world generation. |
| Serene Seasons | not applicable | Weight has no seasonal or climate modifiers. |
| Ecliptic Seasons | not applicable | Weight has no seasonal or climate modifiers. |

Curios support targets `9.5.1+1.21.1`, is compile-only, and is loaded only when
the mod is present. The base mod and dedicated server do not require it.

A moving-structure physics mass system is intentionally not used as an
ItemStack weight source: installed `BlockState` mass and player-carried item
weight have different ownership and balance contracts.
