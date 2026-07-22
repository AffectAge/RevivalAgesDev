# Third-Party Notices

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
