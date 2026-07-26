# Diet

Diet is a server-authoritative, data-driven player system. It tracks independent
values for every entry in the synchronized `revivalages:diet_group` datapack
registry. The built-in groups are dairy, fruit, grain, protein, and vegetable,
but no Java enum limits the registry.

Items contribute through the synchronized `revivalages:item_diet` item data map.
Each value maps diet-group IDs to item multipliers. The gain for one group is:

`vanilla nutrition * nutritionMultiplier * item multiplier *
max(0, 1 - multiGroupReduction * (group count - 1))`

Only decreases in vanilla food level decay Diet values. Saturation-only changes
do not decay them. Death applies the configured penalty and floor. Milk and each
successfully consumed Cake slice use their own server-configured nutrition
values because neither follows the ordinary edible ItemStack lifecycle.

Effect rules live in `revivalages:diet_effect_rule`. The `any`, `average`,
`all`, and `cumulative` detectors all evaluate the same persisted group values.
The built-in cumulative high-diet rule grants one Diet Toughness level for each
qualifying group.

The screen is opened with the configurable Diet key or the inventory button. It
sorts arbitrary group IDs deterministically and supports scrolling. Food
tooltips remain separate from Item Size, Carried Weight, and Food Spoilage.

## Optional integration assessment

| Integration | Status | Reason |
| --- | --- | --- |
| KubeJS | applicable | Read-only access is exposed through `DietApi`; datapack JSON remains the write path. |
| Jade | not applicable | Diet has no inspectable placed block or entity surface. |
| JEI | not applicable | Diet does not define recipe semantics. |
| EMI | not applicable | Diet does not define recipe semantics. |
| Curios | not applicable | Diet does not add equipment slots or wearable behavior. |
| Progressive Stages | not applicable | Diet does not gate recipes or progression. |
| Biomes O' Plenty | not applicable | Diet is not biome-sensitive. |
| Serene Seasons | not applicable | This task intentionally has no calendar or seasonal diet behavior. |
| Ecliptic Seasons | not applicable | This task intentionally has no calendar or seasonal diet behavior. |
