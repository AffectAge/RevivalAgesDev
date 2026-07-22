# Third-Party Notices

## Pyrotech

The Drying Rack, Barrel, Chopping Block, Pit Kiln, Soaking Pot, Tanning Rack,
Stone Sawmill, Stone Oven, Stone Kiln, Stone Crucible, granite Anvil, thatch,
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
