# Surface Deposits

Revival Ages provides a complete rock-and-stick surface-deposit system for
Minecraft 1.21.1. The implementation uses Revival Ages registration and shared
block lifecycle code while preserving its designated reference's observable
behavior without requiring that reference at runtime.

## Content and behavior

Ten rock deposits are registered for cobblestone, granite, diorite, andesite,
sandstone, red sandstone, gravel, End stone, netherrack, and soul soil. Each uses
four visual sizes with generation weights `10 / 7 / 5 / 1` for tiny, small,
medium, and large. Eleven fallen-stick deposits cover the vanilla Overworld,
Nether, cherry, mangrove, and bamboo wood families. Their three visual sizes use
weights `7 / 5 / 1` for small, medium, and large.

All deposits are waterloggable and require a sturdy supporting surface. Losing
support removes the block through the normal block lifecycle. They have no
collision, retain the reference selection footprint, and use vanilla stone or
wood interaction sounds. There are no omitted custom audio files because the
reference does not define any for these blocks. Creative players can right-click
a placed deposit to cycle through its visual sizes; survival interaction does not
mutate the variant.

Rocks drop their material splitter, except gravel rocks, which drop flint. Sticks
drop a vanilla stick, while bamboo sticks drop bamboo. Four matching splitters
recombine shapelessly into cobblestone, granite, diorite, andesite, sandstone,
red sandstone, End stone, netherrack, or soul soil. These ordinary recipes are
automatically visible in JEI and EMI without a custom category.

## World generation

Configured random patches, placed features, and NeoForge biome modifiers own all
generation policy. Existing Minecraft biome tags provide the broad allowlist and
blacklist semantics used by the reference; vanilla cherry grove, dark forest, and
mangrove swamp use explicit IDs because the reference's equivalent common tags do
not exist in the pinned NeoForge environment. This keeps density and most biome
coverage reloadable and permits compatible modded biomes to participate where
they expose the corresponding existing tags. Existing worlds remain valid;
deposits appear in newly generated chunks.

The adaptation fixes four evident reference-data defects instead of preserving
them:

- red-sand rocks drop red-sandstone splitters rather than themselves;
- red-sand rock placement has the intended count of seven;
- End-stone and gravel rock placement have one count modifier rather than two;
- the unreachable Nether gravel modifier with the Nether simultaneously allowed
  and denied is omitted, while the functional Nether gravel modifier is retained.

## Optional integration assessment

- KubeJS: no dedicated adapter. Recipes and worldgen are normal reloadable data,
  so scripts and datapacks can replace them through their standard mechanisms.
- Jade: no custom payload. Deposits have no progress, inventory,
  modifiers, or other dynamic state beyond the visible model variant.
- JEI and EMI: supported through vanilla shapeless recipe discovery; no distinct
  recipe semantics justify a custom category.
- Curios: not applicable because deposits and splitters are not wearable.
- Progressive Stages: no direct adapter; packs can gate the ordinary recipes and
  content using the progression mod's normal datapack/script facilities.
- Biomes O' Plenty: biome-tag-compatible data is provided. Runtime support is not
  claimed until tested against a Minecraft 1.21.1-compatible build.
- Serene Seasons and Ecliptic Seasons: not applicable because generation and
  deposit behavior are not season-sensitive.

Source and license details are in `THIRD_PARTY_NOTICES.md` and the `licenses`
directory.
