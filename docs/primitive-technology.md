# Primitive Technology

This document describes the primitive processing chain in Revival Ages. The
implementation uses NeoForge 1.21.1 APIs and data formats; designated reference
mods supply gameplay and presentation guidance, not obsolete platform code. Each
reference and its dependency chain are treated as one implementation: required
interactions, transforms, handlers, and lifecycle contracts are ported into
Revival Ages' shared NeoForge core and used by the mechanisms.

## Progression chain

The family and every independently usable machine or portable mechanism are
always available. Content ownership, shared materials, and compatibility data
conditions follow [content-availability.md](content-availability.md).

1. Dry wheat on a Crude Drying Rack to obtain straw.
2. Craft straw into thatch and tinder.
3. Hunt animals in the `revivalages:drops_raw_hide` entity-type tag to obtain raw
   hide, then use a Chopping Block with an axe to process logs and scrape it. Tool tier
   changes required chops and output; work consumes exhaustion, tool durability,
   and can produce removable wood chips. Completed recipe outputs drop above the
   block.
4. Load a Pit Kiln, cover it with thatch, add three logs, validate its surrounding
   structure, and ignite it. An active kiln maintains a real fire block above it.
   A broken structure receives a 100-tick recovery window before its contents
   fail. Rain can extinguish it and recipes may define failure products. Item
   Size applies a default batch capacity of four small items or one larger
   item. Inputs larger than
   the configured maximum size are rejected with an action-bar warning; existing
   inputs are preserved across reload.
5. Put counted item stacks into a Barrel's four input slots. Recipes may use a
   precise fluid amount or no fluid, may require a lid or process while open,
   and may produce an item or fluid. A finished item stays in a fifth output
   slot until collected. The original sealed water-and-leaves recipe still makes
   tannin. Open barrels collect rain based on continuous exposure. Breaking an
   open barrel drops its contents separately; breaking a sealed barrel produces
   one sealed barrel item that preserves its contents, fluid, lid state, and
   finished item.
   The separate Storage Barrel has been removed. Its 27-slot inventory cannot
   be converted losslessly into the processing Barrel; remove its contents from
   existing worlds before upgrading.
6. Scraped hide is washed with water and then soaked in tannin in a Soaking Pot.
   Manual item and fluid interaction is available only from the top face.
   Recipes may require a lit Campfire directly below the pot. Placing the pot
   above a Campfire immediately ejects its cooking input or completed result.
   The pot lowers into a dedicated combined model, and its item, fluid surface,
   shape, and interaction height move down with it. Compatible input stacks can
   be added incrementally; draining required fluid ejects excess input, and
   large outputs are retained and extracted in safe stack-sized chunks.
7. Place tanned hide on a Tanning Rack under open daytime sky. Darkness pauses
   work, blocked sky resets progress, and rain exposure accumulates for the
   current input across separate storms. Prolonged rain can produce the
   configured failure result. The built-in recipe converts one tanned hide into
   one leather. The rack is intentionally player-operated and exposes no item
   automation capability.
8. Advance into the stone machine tier. The four two-block machines share the
   same lower fuel chamber, upper process chamber, directional interaction rules,
   synchronized process state, airflow input, and output-blocking behavior.
9. Use the in-world Anvil with a tagged hammer or pickaxe. Each recipe specifies
   the tool family and hit count; work consumes hunger and tool durability and
   eventually damages the granite anvil itself. Completed recipe outputs drop
   above the block.
10. Compress nine logs into a Log Pile, completely enclose one or more connected
    piles with solid nonflammable blocks, and ignite them. Each pile becomes an
    Active Pile, produces one staged Pit Burn result at a time, and ends as an Ash
    Pile whose stored contents drop when broken. A damaged enclosure receives the
    configured recovery window before the active pile burns away.

## Ignition, torches, and primitive buckets

Flint and Tinder is a four-second held-use igniter by default. Smoke at the hit
point communicates the ongoing action; completion can light Campfires, loaded Pit
Kilns, Log Piles, Wood Torches, or a valid adjacent fire position. Its uses, use
time, and cooldown are server-configurable and its durability display reads the
same synchronized component that gameplay consumes.

Wood Torches place unlit on a floor or any horizontal wall. They can be lit,
doused with a water bucket or by rain, relit after drying conditions permit, and
burn out after a configured duration with configured random variance. Lit torches
emit light, flame and smoke, damage colliding entities, and drop either a stick or
straw when broken; an unlit torch drops itself. All three visible states use the
functional licensed textures described in the third-party notices.
Their Jade burn countdown is calculated from game time and freezes while doused;
the infrequent rain check does not delay normal burnout.

Wooden and clay buckets expose NeoForge's standard item fluid capability and hold
1,000 mB of any compatible fluid, including fluids from other mods. They interact
with world sources and machine tanks, render the contained fluid dynamically,
and retain remaining uses as item data. Filled wooden buckets wear over time;
hot fluids accelerate wear and damage their holder. Clay buckets tolerate normal
fluids indefinitely but hot fluids still wear the vessel and hurt its holder.
When a vessel breaks, its source fluid is placed at the holder when possible.
Both bucket types can milk adult cows when their material-specific milk setting
is enabled, and the resulting milk uses the standard NeoForge milk cure path when
drunk. Milking calves remains prohibited to preserve vanilla animal interaction;
this is an intentional safety correction to the designated behavior. Creative
players cannot obtain custom milk buckets by milking.

Full water buckets fill empty or partial water cauldrons to level three, while
empty buckets drain only a full cauldron. These operations preserve vessel wear,
are atomic, award the corresponding vanilla statistics, and honor block and
item-use permissions. Crafting remainders return an empty vessel with the same
wear lifecycle. Lava-tagged contents provide the configured furnace burn time.
World placement and break spills deliberately use NeoForge's transactional fluid
API rather than recreating obsolete direct level manipulation. Clay buckets are
first crafted unfired and then fired by the Pit Kiln or inherited Stone Kiln
recipe.

## Campfire

Tinder places a Campfire on sturdy ground and explains this in its item tooltip.
Add individual logs, ignite it with flint and steel or a
fire charge, and insert one cookable item. Custom `revivalages:campfire` recipes
take priority; compatible vanilla smelting recipes are inherited, except bread and
cookies. Cooking speed scales with the queued and currently burning logs. Rain extinguishes the
fire, ash can stop operation, forgotten results become Burned Food, and a shovel
removes accumulated ash. Empty-hand interaction recovers the cooking item first,
then the most recently added log; held-item clicks never remove stored stacks.
Removing a log from a lit fire can burn the player unless Frost Walker protects
them. A fire without fuel burns out into a dead ash state, an unsupported
campfire breaks, and an unsafe flammable floor can ignite.
Light also scales with queued and burning logs, reaching level 15 at full fuel
with the default configuration. Ready results remain available to collect;
after overcooking they become Burned Food once and emit heavy smoke while lit.
Normal cooking and ready results emit flame without smoke. Jade shows whole-second
remaining fuel time and locally advances cooking progress between state packets.

At configured night hours, an unthreatened player near a lit Campfire receives
Comfort and Resting. Continued rest can grant Well Rested; eating to fullness can
grant Well Fed; satisfying both conditions grants Focused. These effects provide
the configured healing, absorption, exhaustion, food, and experience bonuses.

## Server configuration

Primitive Technology settings are written under `primitiveTechnology` in
`config/revivalages.toml`. They control automation and progress
particles, Campfire cooking, fuel, ash, rain, light, floor ignition, burn damage and all five
effects, Chopping Block tier work/output/durability/exhaustion, Pit Kiln batch,
maximum input size and rain behavior, Barrel capacity, item stack size, rain and
hot fluids, Soaking Pot batch, duration,
automation, hot-fluid threshold and hot-fluid retention,
and Tanning Rack duration/rain failure. Raw-hide drop chance and maximum count are
also configurable. Values are server-owned and are not saved
inside recipes.

The same file configures Pit Burn cluster size, duration, enclosure validation
interval and failure grace; Flint and Tinder uses, use duration and cooldown;
Wood Torch light, rain, lifetime, variance and collision damage; and primitive
bucket uses, empty stack sizes, milk access, material-specific temperature
thresholds, passive wear, hot-fluid wear, holder damage, source placement on
break, and lava fuel time. These controls are read at runtime and are not
persisted as fixed balance values in world data.
The shared client setting `client.interactionOutlineColor` controls the Drying
Rack and Construction Frame selection outlines as six hexadecimal RGB digits
with an optional `#` prefix; its default is `007FBD`.

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
The Stone Sawmill uses licensed idle and long/short work recordings.
Its idle and recipe-completion sounds can be enabled independently and their
volumes changed in the server configuration without changing recipes or saves.
Stone fire-based machines retain the reference furnace-crackle ambience.

The Revival Ages `anvil` is a separate granite working block and does not replace
Minecraft's vanilla anvil. Place one recipe input, then strike it with the
recipe's hammer or pickaxe until the configured work completes. Insufficient
hunger blocks the action with feedback. Progress persists across reloads. Damage
advances through four visible stages; final breakage preserves the workpiece.

Drying Rack environment and seasonal balance remains in
`config/revivalages.toml`. Every seasonal coefficient is configurable;
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
- `revivalages:pit_burn`: `ingredient`, `result`, `stages`, `burn_time`, optional
  `failure_chance` and `failure_results`. The ingredient is the pile block item
  consumed by the state transition, normally `revivalages:log_pile`.
- `revivalages:barrel`: one to four `items`, `input_fluid`, `result_fluid`, and
  `processing_time`.
- `revivalages:soaking_pot`: `ingredient`, `input_fluid`, `result`, optional
  `process_rules`, and `processing_time`. `process_rules` is an ordered list of
  shared process conditions such as `{"type":"lit_block_below"}`. The built-in scraped-hide recipe
  requires a lit Campfire, 250 mB of water, and 2,400 ticks. The tannin recipe
  consumes 500 mB and takes 12,000 ticks without a Campfire requirement.
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
wood chips, Pit Kiln structure and firing state, Barrel seal and processing
state, Soaking Pot heat requirement, Tanning Rack sky/day/rain conditions,
stone-machine airflow, blade requirement and output items, and Anvil hits and
damage. The Pit Kiln shows its recipe arrow before ignition and while firing;
the fire block also exposes the live arrow. Barrel input icons appear as soon as
items are inserted, including before a recipe matches. Jade's fluid
bar presents tank contents without an additional text line. The Barrel capacity
defaults to 10,000 mB in new configurations. JEI and EMI use separate presentation
adapters, enumerate the same gameplay recipe types
from `RecipeManager`, and use licensed functional UI textures. Categories include
item and fluid inputs, outputs, duration,
failure outcomes, and required environmental conditions. Environmental and
machine requirements are presented in a dedicated bottom row of 16x16 icons,
ordered from left to right, with the same order and tooltip text in both viewers:
Soaking Pot heat uses the fire icon, Barrel sealing uses the lid icon, and Tanning
Rack uses open-sky and rain icons. All three integrations are optional and
client-only; a dedicated server and the base mod load without them.

Barrel recipes retain legacy single-item and fluid-only fields. A counted slot
uses `{"ingredient":{"item":"minecraft:oak_leaves"},"count":8}` in the
`items` array. `input_fluid` is optional, `result_item` and `result_fluid` are
mutually exclusive, and `requires_seal` defaults to true for old recipes. Fluid
inputs require at least the recipe amount; processing replaces or drains the
whole tank, preserving the Barrel's earlier fluid behavior. JEI and EMI display
per-slot counts and whichever result type the recipe defines. The built-in open
compost and sealed mud recipes demonstrate item outputs without and with fluid;
the original tannin recipe and a larger counted batch produce fluid.

Viewer-only chance outcomes use the same atlas row. They state whether a roll
is made for an additional output, for every input, or for every Pit Burn stage,
and list the canonical alternate results. Required tools use a real cycling
Ingredient slot rather than a generic icon; primitive recipes and Construction
Frame use the same `ToolRequirementView` contract.

Soaking Pot recipes expose the Campfire requirement through the same canonical
recipe field used by gameplay. JEI and EMI show it in the shared condition row
and keep the processing time below that row; neither viewer maintains a private
rule for deciding which recipes need heat.

Pit Burn adds its staged success/failure recipe to the same JEI/EMI catalog. Jade
shows enclosure validity, grace countdown, completed stages, progress, and the
predicted result for Active Piles; Ash Piles explain how to collect their stored
contents. Jade also reports the Wood Torch state and remaining burn time. Flint
and Tinder and primitive buckets are items rather than inspectable world devices,
so Jade is not applicable to them.
For Campfire, Chopping Block, Anvil, and Soaking Pot, the generic Jade item-storage
line is hidden in favor of the recipe arrow. Jade arrows allow progress to reset
between recipes. Timed machine arrows project their progress from synchronized
game-time snapshots between block-entity updates.
Campfire ready and burned results have separate colored statuses; legacy saves
containing Burned Food are recognized without recooking it.

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

For Pit Burn, KubeJS is applicable through codec-backed recipe JSON, JEI/EMI are
applicable for recipes, Jade is applicable to active and completed pile state,
and Progressive Stages can gate the recipe or items at pack level. Curios,
Biomes O' Plenty, Serene Seasons, and Ecliptic Seasons have no direct surface in
this mechanism. Flint and Tinder and Wood Torch use vanilla crafting recipes;
primitive buckets intentionally use the generic NeoForge fluid capability so
fluid-owning mods work without pairwise adapters.

## Reference porting rule

Future reference-derived features must trace both the primary call site and every
relevant dependency-chain superclass, interaction, renderer, transform, inventory
wrapper, observer, persistence field, and synchronization callback. Required
behavior belongs in the shared Revival Ages core and must be reused by each
feature; one-off approximations inside individual blocks are not accepted.
Intentional changes required by Minecraft 1.21.1 or NeoForge must be documented
here or in the feature-specific document.
