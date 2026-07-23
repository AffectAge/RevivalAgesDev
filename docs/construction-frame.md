# Construction Frame

## Overview

The Construction Frame is a physical 3x3x3 assembly grid. Each of its 27 cells
holds one item. Using an item on a selected empty cell inserts one item; using an
empty hand on an occupied cell removes it. A matching tool completes the
assembly and replaces the frame with the recipe result.

`revivalages:frame_assembly` recipes use exact three-row `bottom`, `middle`, and
`top` layers, a strict symbol key, a tool ingredient, and a unit BlockItem
result. Matching permits all four rotations around the Y axis and never mirrors
the pattern. A recipe may provide `wood_variant_source` with zero-based `x`,
`y`, and `z` coordinates to copy a log variant into a compatible result.

Assembly is server-authoritative. Result placement uses the normal BlockItem
placement lifecycle, including facing, item components, `setPlacedBy`, and
multi-block placement. A failed placement restores the frame and all inputs.
Successful assembly consumes the frame and ingredients and damages the tool by
the configured amount. Breaking a frame drops the frame and every stored item
exactly once.

## Configuration and recipes

The server config contains:

- `constructionFrame.enabled`, which requires a restart and controls normal
  acquisition, interaction, viewer display, and recipe selection without
  changing registry identity;
- `constructionFrame.toolDurabilityCost`, the durability consumed after a
  successful assembly.

When enabled, Frame Assembly replaces the ordinary recipes for twelve vanilla
workstations and the supported Revival Ages machines. When disabled, conditional
data restores their two-dimensional fallback recipes. The replacement of vanilla
recipe IDs is intentional and may conflict with another data pack that replaces
the same IDs; normal pack priority determines the winner.

The frame exposes no item capability. Existing disabled frames remain loadable,
preserve their synchronized contents, can be broken safely, and perform no
assembly behavior.

## Recipe viewers

JEI and EMI enumerate the same `FrameAssemblyRecipe` holders through
`FrameAssemblyRecipeCatalog`. Mode 0 renders all 27 ingredients in an isometric
projection. Modes 1 through 3 render one standard-slot layer at a time. Both
adapters provide layer controls, ingredient interaction, tool and output slots,
and safe rendering for completely empty layers.

## Optional-integration assessment

| Integration | Outcome | Notes |
| --- | --- | --- |
| KubeJS | applicable | Provides typed full-pattern and three-layer constructors; emitted JSON is decoded by the canonical gameplay codec. |
| Jade | applicable | Displays occupancy, matching result, required tool, blocked placement, and disabled state. |
| EMI | applicable | Dedicated isometric and layered presentation over the canonical recipe catalog. |
| JEI | applicable | Dedicated isometric and layered presentation over the canonical recipe catalog. |
| Curios | not applicable | The mechanic has no wearable or equipment behavior. |
| Progressive Stages | applicable | Stable recipe IDs allow normal recipe-level gating without a feature-internal stage system. |
| Biomes O' Plenty | not applicable | Assembly has no biome-sensitive behavior. |
| Serene Seasons | not applicable | Assembly has no seasonal or climate behavior. |
| Ecliptic Seasons | not applicable | Assembly has no seasonal or climate behavior. |

KubeJS 2101.7.2, Jade 15.10.5, JEI 19.39.0.369, and EMI 1.1.24 are
compile-checked optional integrations. The base implementation has no runtime
dependency on any of them.
