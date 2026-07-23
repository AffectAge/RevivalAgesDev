# Animal Power

## Content

The animal-power feature owns four always-registered devices:

- `revivalages:hand_grindstone`;
- `revivalages:horse_grindstone`;
- `revivalages:horse_chopping_block`;
- `revivalages:horse_press`.

The feature has no content enable flags. Its server configuration contains only
balance, navigation, capacity, and automation values. Changing configuration
never changes registry identity.

## Worker lifecycle

An eligible mob already leashed to a player can be attached by interacting with
an animal-powered device. Sneak-interacting without an item detaches the worker
and returns the lead. Eligible entity types are data-driven through
`revivalages:animal_power_workers`; the built-in tag contains horses, donkeys,
mules, llamas, and camels.

Each device validates a solid 7 by 7 floor centered below it and two blocks of
clearance above that floor. The eight waypoints form a symmetric circuit around
the perimeter. Invalid areas pause navigation and processing without consuming
input. Worker UUID, waypoint, machine progress, inventory, press fluid, and
chopping-block wood variant are persisted.

The symmetric area check intentionally corrects an asymmetric edge check in the
designated reference. Navigation, processing, inventory mutation, and completion
remain server-authoritative.

## Recipes and automation

`revivalages:grinding` is the canonical recipe type for both grindstones. It
defines an ingredient, input count, primary result, optional secondary result and
chance, positive work-point requirement, and supported machine variants.

`revivalages:pressing` defines an ingredient, input count, and exactly one item or
fluid result. The press exposes its output-only fluid tank from the bottom.
Animal-machine item automation inserts into the input from non-bottom sides and
extracts result slots. The automation policy is server-configurable.

The animal chopping block consumes the existing `revivalages:chopping` recipes.
Its configured tier defaults to tier 2, which gives standard log recipes four
cycles and four planks. The crafting recipe records the selected log in the
`revivalages:wood_variant` item component and preserves it through placement and
drops.

## Presentation

Block-entity renderers display the active input or output, the selected chopping
block wood, both rotating grindstones, chopping-blade and press-platen movement,
press fluid level, the visual worker tether, and a red invalid-area boundary.
The machine geometry and functional recipe-viewer backgrounds follow the
designated reference assets permitted by its license and are adapted to current
model and texture paths. Functional sounds use vanilla placement, leash,
grindstone, and wood-breaking events at their corresponding lifecycle points.
All normal source identifiers, translation keys, package names, and resource
paths use only the Revival Ages namespace.

Jade presents worker attachment, area validity, the active blocking state,
progress, item result, and tank contents. JEI and EMI have independent
presentation adapters, but both enumerate `grinding`, `pressing`, and the
existing `chopping` type through `RecipeManager` and the shared
`AnimalPowerRecipeCatalog`. Recipe codecs remain the only validation source.

## Optional integration assessment

| Integration | Outcome | Notes |
| --- | --- | --- |
| KubeJS | applicable | Recipes use normal reloadable JSON recipe types and stable IDs; no private machine access is exposed. |
| Jade | applicable | Client adapter compiled against Jade 15.10.5 for Minecraft 1.21.1. |
| EMI | applicable | Client adapter compiled against EMI 1.1.24 for Minecraft 1.21.1. |
| JEI | applicable | Client adapter compiled against JEI 19.39.0.369 for Minecraft 1.21.1. |
| Curios | not applicable | The feature has no wearable or accessory state. |
| Progressive Stages | applicable | Canonical recipe IDs and `RecipeManager` lookup allow recipe-level gating without machine-internal stage checks. |
| Biomes O' Plenty | not applicable | Worker operation and recipes do not depend on biomes or world generation. |
| Serene Seasons | not applicable | No seasonal or climate modifiers are applied. |
| Ecliptic Seasons | not applicable | No seasonal or climate modifiers are applied. |

Jade, EMI, and JEI are compile-time optional and are not required by the base mod
or a dedicated server.
