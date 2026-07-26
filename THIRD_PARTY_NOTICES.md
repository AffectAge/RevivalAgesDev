# Third-Party Notices

## Inventory Weight

Carried-item formulas, inventory aggregation, armor-pocket capacity, overload
penalties, portable-container weighting, command semantics, and functional HUD
presentation were adapted from Inventory Weight for Minecraft 1.21. The source
used is the local `inventory-weight-1.21` source tree.

Inventory Weight is dedicated to the public domain under Creative Commons CC0
1.0 Universal. A copy is included at
`licenses/CarriedWeight-CC0-1.0.txt`.

The original Java paths under `com.github.saishok.inventoryweight`, including
the weight manager, item/block calculators, pocket handling, player tick logic,
commands, tooltip rendering, HUD rendering, and optional equipment integration,
were rewritten into `api/weight`,
`feature/inventory/carriedweight`, and the isolated optional-integration
packages. Unsafe registry-name heuristics and platform-specific hooks were not
carried over.

The original CC0 HUD files `empty.png`, `filled_1.png` through
`filled_12.png`, `overload.png`, `strength.png`, and the overload effect icon are
shipped under the renamed
`assets/revivalages/textures/gui/carried_weight/` and
`assets/revivalages/textures/mob_effect/overloaded.png` paths.

## TerraFirmaCraft

Support Beam geometry and placement behavior, support-range data, Collapse and
Landslide propagation rules, Knapping type and pattern data, the 5x5 interaction
flow, recipe-viewer presentation semantics, and functional audio/GUI behavior
were adapted from TerraFirmaCraft for Minecraft 1.21.x. The source used is the
official `1.21.x` branch at
<https://github.com/TerraFirmaCraft/TerraFirmaCraft>.

TerraFirmaCraft is licensed under the European Union Public Licence version 1.2.
A copy is included at `licenses/Structural-Knapping-EUPL-1.2.txt`. Under the
EUPL 1.2 compatibility appendix, the combined derivative is distributed under
Revival Ages' GNU General Public License version 3 terms while the adapted
material retains this notice and the EUPL source licence.

The original Java paths
`util/data/Support.java`, `util/tracker/Collapse.java`,
`common/recipes/CollapseRecipe.java`, `common/recipes/LandslideRecipe.java`,
`util/data/KnappingType.java`, `util/data/KnappingPattern.java`,
`common/recipes/KnappingRecipe.java`, `common/container/KnappingContainer.java`,
`compat/jei/category/KnappingRecipeCategory.java`, and
`compat/emi/recipe/EmiKnappingRecipe.java` were adapted into
`feature/world/structuralintegrity`,
`feature/technology/knapping`, and the corresponding optional-integration
packages. Generated tag, support, knapping-type, and recipe JSON was adapted
under the `data/revivalages` namespace. Functional visual behavior from
`assets/tfc/textures/gui/knapping.png`,
`assets/tfc/textures/gui/knapping/clay_ball*.png`,
`assets/tfc/textures/gui/knapping/leather.png`, and
`assets/tfc/textures/gui/knapping/goat_horn*.png` is shipped under
`assets/revivalages/textures/gui/knapping_screen.png` and
`assets/revivalages/textures/gui/knapping/`. Rock cells use Revival Ages and
vanilla material textures.

The thirteen functional source recordings
`sounds/random/rock_slide_long_{1..4}.ogg`,
`sounds/random/rock_slide_long_fake_{1..2}.ogg`,
`sounds/random/rock_slide_short_{1..2}.ogg`,
`sounds/random/dirt_slide_short_{1..2}.ogg`, and
`sounds/item/knapping/{stone,clay,leather}.ogg` are shipped under the renamed
`assets/revivalages/sounds/structural/` and
`assets/revivalages/sounds/knapping/` paths.

Item Size ordering, dynamic item/block providers, data-driven item
classification, chest and bundle insertion restrictions, and placed-item
capacity behavior were additionally adapted from the same source. The original
Java paths `common/component/size/Size.java`,
`common/component/size/IItemSize.java`,
`common/component/size/ItemSizeDefinition.java`,
`common/component/size/ItemSizeManager.java`,
`common/blockentities/TFCChestBlockEntity.java`,
`common/items/TFCBundleItem.java`, and
`common/blockentities/PlacedItemBlockEntity.java` were adapted into
`api/size`, `feature/inventory/itemsize`, the Pit Kiln integration, and the
corresponding generated data maps. Revival Ages intentionally implements only
the size half of the source inventory policy.

## YTech

Construction Frame interaction semantics, the 3x3x3 recipe format, four-way
rotation matching, functional block-model geometry, rope texture, twelve vanilla
assembly patterns, and the isometric/layered JEI and EMI presentation were
adapted from YTech by yanny7. The source used is the Minecraft 1.21.1 branch at
<https://github.com/yanny7/YTech>.

YTech is licensed under the GNU General Public License version 3. A copy is
included at `licenses/FrameAssembly-GPLv3.txt`. Revival Ages is distributed
under the same license. The implementation was modified for Revival Ages' feature
architecture and transactional NeoForge placement lifecycle, and it avoids the
source implementation's Objenesis-backed mutable virtual level.

The original Java paths
`configuration/block/CraftingWorkspaceBlock.java`,
`configuration/block_entity/CraftingWorkspaceBlockEntity.java`,
`configuration/recipe/WorkspaceCraftingRecipe.java`,
`configuration/renderer/CraftingWorkspaceRenderer.java`, and the corresponding
JEI/EMI compatibility classes were adapted into the renamed
`feature/technology/constructionframe` and `integration/jei`/`integration/emi`
paths. The generated block model
`models/block/crafting_workspace.json` is shipped as
`models/block/construction_frame.json`; `textures/block/horizontal_rope.png` is
shipped as `textures/block/construction_rope.png`; and only the functional
40x30 button region of `textures/gui/emi.png` is shipped as
`textures/gui/frame_assembly_controls.png`. The unused workspace GUI image was
not copied.

## HorsePower

The interaction flow, worker waypoint lifecycle, work-area contract, processing
rules, configuration defaults, and functional model structure for the Hand
Grindstone, Animal-Powered Grindstone, Animal-Powered Chopping Block, and
Animal-Powered Press were adapted from HorsePower by GoryMoon. The source used
for the adaptation is the local HorsePower 2.6.4 source tree for Minecraft
1.12.2.

HorsePower is licensed under the GNU Lesser General Public License version 3 or
later. A copy is included at `licenses/HorsePower-LICENSE.txt`. The implementation
was rewritten for Minecraft 1.21.1 and NeoForge, uses the Revival Ages namespace,
and intentionally corrects the asymmetric work-area edge check. No HorsePower
runtime dependency or original namespace is used by gameplay code or resources.

The adapted block model geometry is shipped under
`assets/revivalages/models/block/hand_grindstone*.json` and
`assets/revivalages/models/block/horse_*.json`. The original grinding-content
texture and functional recipe-viewer backgrounds are shipped under the renamed
paths `textures/block/grinding_contents.png`,
`textures/gui/animal_power_grinding.png`, and
`textures/gui/animal_power_pressing.png`. Model parents, vanilla texture names,
resource locations, blockstate composition, and renderer transforms were adapted
for Minecraft 1.21.1.

## Pyrotech

The Drying Rack, Barrel, Chopping Block, Pit Kiln, Soaking Pot, Tanning Rack,
Stone Sawmill, Stone Oven, Stone Kiln, Stone Crucible, granite Anvil, thatch,
Pit Burn piles, Flint and Tinder, Wood Torch, wooden and clay buckets,
primitive material item, and functional recipe-viewer textures and model geometry
were adapted from Pyrotech by codetaylor. This includes the slot
backgrounds, progress arrows, flame indicators, and fluid gauges used by the
JEI/EMI presentation layer. The source project is available at
<https://github.com/codetaylor/pyrotech>.

Pyrotech is licensed under the Apache License 2.0. A copy is included at
`licenses/Pyrotech-LICENSE.txt`. The assets were renamed, their resource paths
were updated, and their blockstate, renderer, and recipe-viewer definitions were
adapted for Minecraft 1.21.1, NeoForge, JEI, and EMI. Pyrotech GUI files
`jei11.png`, `jei2.png`, `jei8.png`, `jei6.png`, and `jei3.png` are shipped under
the renamed Revival Ages paths `stone_sawmill.png`, `stone_oven.png`,
`stone_kiln.png`, `stone_crucible.png`, and `anvil.png` respectively.

The four functional Stone Sawmill recordings from Pyrotech are shipped under the
Revival Ages namespace as `sounds/sawmill/sawmill-idle.ogg`,
`sawmill-active.ogg`, `sawmill-active-short-a.ogg`, and
`sawmill-active-short-b.ogg`. Their sound events and playback behavior were
adapted to the Minecraft 1.21.1 sound registry and server configuration.

## Athenaeum

The item interaction semantics, including its shared item-extraction sound,
oriented interaction-space rendering, and
burnable structure validation lifecycle were adapted from Athenaeum by
codetaylor, the library used by Pyrotech. The source project is available at
<https://github.com/codetaylor/athenaeum>.

Athenaeum is licensed under the Apache License 2.0. A copy is included at
`licenses/Athenaeum-LICENSE.txt`. The implementations were rewritten for the
Minecraft 1.21.1 and NeoForge APIs and live in Revival Ages' internal core layer.

## This Rocks!

The surface rock and stick blocks, splitter items, weighted block variants,
blockstates, block and item models, loot tables, recombination recipes, configured
and placed features, and biome-modifier data were adapted from This Rocks! by
Mrbysco. Model JSON files retain their original Motschen credit. The reference
source is the This Rocks! 1.8.0 branch for Minecraft 1.21.1 at
<https://github.com/Mrbysco/ThisRocks>.

This Rocks! is licensed under the MIT License. A copy is included at
`licenses/ThisRocks-LICENSE.txt`. Adapted resources were moved from the `rocks`
namespace to `revivalages`, and Java behavior was rewritten for Revival Ages'
feature-module architecture and pinned NeoForge APIs. The port intentionally
corrects the source red-sand-rock drop, missing red-sand placement count,
duplicate End/gravel placement counts, and an unreachable Nether gravel biome
modifier; details are recorded in `docs/surface-deposits.md`.
