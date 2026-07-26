# Food Spoilage

Food Spoilage uses an independent world clock, not Minecraft day time. The
Overworld `SavedData` value advances once after every processed server tick,
including ticks on a dedicated server with zero connected players. It stops when
the server is stopped or the feature is disabled, so real-world downtime never
ages food. `/time set` cannot modify this clock.

When `ageThroughSleep` is enabled, one successful Overworld sleep skip adds the
skipped vanilla ticks once. It is not multiplied by the number of sleepers.

Perishable items are declared by the synchronized
`revivalages:food_spoilage` item data map:

```json
{
  "decay_modifier": 2.0,
  "result": {
    "id": "minecraft:rotten_flesh"
  }
}
```

The result is optional and defaults to rotten flesh. Lifetime is
`baseLifetimeTicks / (decay modifier * global multiplier * trait multipliers)`.
Every stack carries `revivalages:food_state`, containing its creation tick and
trait IDs. Old perishable stacks without the component become fresh when first
observed by the authoritative server.

Traits are entries in the synchronized `revivalages:food_trait` registry.
Adding or removing one preserves the current freshness fraction with
`newCreation = (1 - p) * now + p * oldCreation`. Drying Rack output inherits the
oldest input and gains `dried`. Food in a sealed Barrel gains `preserved`; the
temporary trait is removed when the Barrel is opened or the food is extracted.
Repeated sealing cannot refresh a stack.

Output inheritance is data-driven through the synchronized
`revivalages:food_output_policy` registry. An entry uses the recipe ID as its
registry ID and selects `copy_first`, `copy_oldest`, `reset`, `add_trait`, or
`remove_trait`. Crafting, furnace processing, and Revival Ages food-processing
machines pass their canonical recipe ID through this policy layer; the default
when no entry exists is `copy_oldest`.

Expired stacks are rejected as food before use and are physically transformed
during server sweeps of player inventories, carried menu stacks, open container
slots, item entities, portable `DataComponents.CONTAINER` contents, and Revival
Ages machine inventories reached through those paths. No unloaded chunk is
loaded to find food.

The client receives the current counter and effective lifetime settings only for
display. Tooltips show remaining game time and traits, never a calendar date.

## Optional integration assessment

| Integration | Status | Reason |
| --- | --- | --- |
| KubeJS | applicable | Read-only calculations are exposed by `FoodFreshnessApi`; data maps and registries remain codec-validated JSON. |
| Jade | applicable | Sealed Barrel preservation and nearest expiry are inspectable block state. |
| JEI | applicable | Viewer stacks must remain non-decaying display copies without changing recipe matching. |
| EMI | applicable | Viewer stacks must remain non-decaying display copies without changing recipe matching. |
| Curios | not applicable | Spoilage does not add wearable storage or equipment behavior. |
| Progressive Stages | not applicable | Spoilage does not gate recipes or progression IDs. |
| Biomes O' Plenty | not applicable | Decay has no biome modifier in this task. |
| Serene Seasons | not applicable | Calendar and seasonal decay are explicitly out of scope. |
| Ecliptic Seasons | not applicable | Calendar and seasonal decay are explicitly out of scope. |
