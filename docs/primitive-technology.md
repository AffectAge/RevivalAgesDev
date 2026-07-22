# Primitive Technology

This document describes the Pyrotech-inspired primitive processing chain in
Revival Ages. The implementation uses NeoForge 1.21.1 APIs and data formats; the
reference mod supplies gameplay and presentation guidance, not obsolete Forge
1.12 code. Pyrotech and Athenaeum are treated as one reference implementation:
required Athenaeum interactions, transforms, handlers, and lifecycle contracts
are ported into Revival Ages' shared NeoForge core and used by the mechanisms.

## Progression chain

1. Dry wheat on a Crude Drying Rack to obtain straw.
2. Craft straw into thatch and tinder.
3. Hunt animals in the `revivalages:drops_raw_hide` entity-type tag to obtain raw
   hide, then use a Chopping Block with an axe to process logs and scrape it. Tool tier
   changes required chops and output; work consumes exhaustion, tool durability,
   and can produce removable wood chips.
4. Load a Pit Kiln, cover it with thatch, add three logs, validate its surrounding
   structure, and ignite it. An active kiln maintains a real fire block above it.
   A broken structure receives a 100-tick recovery window before its contents
   fail. Rain can extinguish it and recipes may define failure products.
5. Fill a Barrel with water and leaves, close it with a Barrel Lid, and wait for
   tannin. Open barrels collect rain based on continuous exposure. Breaking an
   open barrel drops its contents separately; breaking a sealed barrel produces
   one sealed barrel item that preserves its contents, fluid, and lid state.
6. Scraped hide is washed with water and then soaked in tannin in a Soaking Pot.
   Recipes may require a lit Campfire directly below the pot. Compatible input
   stacks can be added incrementally; draining required fluid ejects excess input,
   and large outputs are retained and extracted in safe stack-sized chunks.
7. Place tanned hide on a Tanning Rack under open daytime sky. Darkness pauses
   work, blocked sky resets it, and prolonged rain can produce the configured
   failure result.
8. Advance into the stone machine tier. The four two-block machines share the
   same lower fuel chamber, upper process chamber, directional interaction rules,
   synchronized process state, airflow input, and output-blocking behavior.
9. Use the in-world Anvil with a tagged hammer or pickaxe. Each recipe specifies
   the tool family and hit count; work consumes hunger and tool durability and
   eventually damages the granite anvil itself.

## Campfire

Tinder places a Campfire. Add individual logs, ignite it with flint and steel or a
fire charge, and insert one cookable item. Custom `revivalages:campfire` recipes
take priority; compatible vanilla smelting recipes are inherited, except bread and
cookies. Cooking speed scales with the visible fuel level. Rain extinguishes the
fire, ash can stop operation, forgotten results become Burned Food, and a shovel
removes accumulated ash. Empty-hand interaction recovers the cooking item first,
then the most recently added log; held-item clicks never remove stored stacks.
Removing a log from a lit fire can burn the player unless Frost Walker protects
them. A fire without fuel burns out into a dead ash state, an unsupported
campfire breaks, and an unsafe flammable floor can ignite.

At configured night hours, an unthreatened player near a lit Campfire receives
Comfort and Resting. Continued rest can grant Well Rested; eating to fullness can
grant Well Fed; satisfying both conditions grants Focused. These effects provide
the configured healing, absorption, exhaustion, food, and experience bonuses.

## Server configuration

The primitive technology configuration is written to
`config/revivalages-primitive-server.toml`. It controls automation and progress
particles, Campfire cooking, fuel, ash, rain, light, floor ignition, burn damage and all five
effects, Chopping Block tier work/output/durability/exhaustion, Pit Kiln batch and
rain behavior, Barrel capacity/rain/hot fluids, Soaking Pot batch and duration,
and Tanning Rack duration/rain failure. Raw-hide drop chance and maximum count are
also configurable. Values are server-owned and are not saved
inside recipes.

The same server file configures stone-machine fuel limits and multiplier,
airflow acceleration and drag, retained heat, Sawmill blade damage and chip
chance, Oven duration, inherited Kiln duration/failure scaling, Crucible tank
capacity, and Anvil hit, exhaustion, hunger, and durability costs. Reloaded
server configuration affects future ticks and crafts without rewriting recipes or
world saves.

## Stone machines and Anvil

The Stone Sawmill accepts a Chopping Block recipe input in its upper half and a
stone, flint, or bone saw blade. Stone blades produce one normal result in 12
seconds and four possible wood chips; flint and bone blades produce two results
in 8 seconds and two possible chips. The chip chance and active-blade contact
damage are configured. The requested content scope adds the three blades only;
board, stick, tarred-board, and later-tier material recipes are intentionally not
added.

The Stone Oven inherits Drying Rack recipes at the configured duration multiplier
and food-producing vanilla smelting recipes. The Stone Kiln inherits Pit Kiln
recipes at the configured duration and failure multipliers and adds
gravel-to-cobblestone and sand-to-glass. The Stone Crucible accepts only the
water-producing ice and snow recipes supplied by Revival Ages; it fills its
internal tank and supports normal NeoForge fluid containers.

Fuel is inserted into a machine's lower half. Inputs, blades, fluids, and outputs
are handled through the upper half. Flint and steel or a fire charge ignites a
loaded machine, and a water bucket extinguishes it. Empty-hand interaction
extracts the relevant stored stack. Breaking either half tears down the complete
machine while dropping lower-half contents exactly once. Active machines expose
flame/smoke particles and sounds, and all stored state survives save/reload.
The Stone Sawmill uses Pyrotech's licensed idle and long/short work recordings.
Its idle and recipe-completion sounds can be enabled independently and their
volumes changed in the server configuration without changing recipes or saves.
Stone fire-based machines retain the reference furnace-crackle ambience.

The Revival Ages `anvil` is a separate granite working block and does not replace
Minecraft's vanilla anvil. Place one recipe input, then strike it with the
recipe's hammer or pickaxe until the configured work completes. Insufficient
hunger blocks the action with feedback. Progress persists across reloads. Damage
advances through four visible stages; final breakage preserves the workpiece.

Drying Rack environment and seasonal balance remains in
`config/revivalages-server.toml`. Every seasonal coefficient is configurable;
`enabled=false` forces a zero seasonal bonus. Ecliptic Seasons takes precedence
when both supported season mods are installed, while all coefficients still come
from Revival Ages configuration. New seasonal coefficients must never be fixed
Java constants.

## Recipe JSON

All recipe paths use the Minecraft 1.21.1 singular `data/<namespace>/recipe`
directory. Item stack results use `{"id":"namespace:item","count":1}` and fluid
stacks use `{"id":"namespace:fluid","amount":1000}`.

- `revivalages:campfire`: `ingredient`, `result`, `cooking_time`.
- `revivalages:chopping`: `ingredient`, `result`, optional tier lists `chops` and
  `quantities`.
- `revivalages:pit_kiln`: `ingredient`, `result`, `burn_time`, optional
  `failure_chance` and `failure_results`.
- `revivalages:barrel`: one to four `items`, `input_fluid`, `result_fluid`, and
  `processing_time`.
- `revivalages:soaking_pot`: `ingredient`, `input_fluid`, `result`, optional
  `requires_campfire`, and `processing_time`.
- `revivalages:tanning_rack`: `ingredient`, `result`, optional `rain_failure`, and
  `processing_time`.
- `revivalages:stone_kiln`: `ingredient`, `result`, `processing_time`, optional
  `failure_chance` and `failure_results`.
- `revivalages:stone_crucible`: `ingredient`, fluid `result`, and
  `processing_time`.
- `revivalages:anvil`: `ingredient`, `result`, `hits`, and `tool` (`hammer` or
  `pickaxe`).

Reloading recipes changes future matching without migrating world saves. Active
machines resolve their recipe from current server data and synchronize only the
state required for rendering and overlays.

## Display integrations

Jade displays progress, inputs and predicted outputs, fuel, ash, block damage,
wood chips, Pit Kiln stage/structure/logs, Barrel seal/fluid/result, Soaking Pot
heat requirement, Tanning Rack sky/day/rain conditions, stone-machine airflow,
fuel, blade, tank and output-blocking state, and Anvil hits and damage. JEI and EMI use the
same loader-neutral recipe catalog and the Apache-2.0 functional UI textures
adapted from Pyrotech. Categories include item and fluid inputs, outputs, duration,
failure outcomes, and required environmental conditions. All three integrations
are optional and client-only; a dedicated server and the base mod load without
them. The One Probe alone is intentionally excluded from the accepted scope for
this complete primitive-device family. This exception does not remove JEI, EMI,
or any other previously accepted optional integration.

KubeJS can add or replace these codec-backed recipes through normal custom recipe
JSON. Biomes O' Plenty logs receive optional, load-conditioned Chopping recipes;
seasonal integrations apply to Drying Racks only. Curios has no direct
primitive-machine behavior.
Progressive Stages can gate recipe availability at the pack layer without being a
hard dependency. Every integration must remain removable without registry or save
corruption.

For Stone Sawmill, Stone Oven, Stone Kiln, Stone Crucible, and Anvil, KubeJS is
applicable through their codec-backed recipe inputs, JEI/EMI are applicable as
recipe viewers, Jade is applicable for synchronized machine inspection, and
Progressive Stages is applicable at the recipe/progression layer. Curios, Biomes
O' Plenty, Serene Seasons, and Ecliptic Seasons are currently not applicable
because these mechanisms have no wearable, biome-sensitive, or seasonal rule.
The One Probe remains explicitly excluded for this family.

## Athenaeum porting rule

Future Pyrotech-derived features must trace both the Pyrotech call site and every
relevant Athenaeum superclass, interaction, renderer, transform, inventory
wrapper, observer, persistence field, and synchronization callback. Required
behavior belongs in the shared Revival Ages core and must be reused by each
feature; one-off approximations inside individual blocks are not accepted.
Intentional changes required by Minecraft 1.21.1 or NeoForge must be documented
here or in the feature-specific document.
