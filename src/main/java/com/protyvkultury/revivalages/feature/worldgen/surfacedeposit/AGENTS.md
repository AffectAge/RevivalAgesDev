# Surface Deposit Rules

This feature adapts rocks, sticks, splitters, and supporting world generation
from the designated reference mod to Revival Ages on NeoForge 1.21.1.

- Treat the reference as a complete behavior contract: preserve weighted visual
  variants, creative-player cycling, waterlogging, sturdy-floor survival,
  replacement behavior, collision and selection shapes, drops, recombination
  recipes, generation density, biome allow/deny semantics, rotations, and sounds.
- Keep common lifecycle behavior in `VariantSurfaceDepositBlock`; material classes
  should only define properties that genuinely differ.
- World generation is data-driven. Add or change biome coverage, counts, and
  predicates in configured features, placed features, and biome modifiers rather
  than hardcoding biome IDs in blocks.
- Do not reproduce known source-data mistakes. Document corrections in
  `docs/surface-deposits.md` and add a regression check when practical.
- The source uses vanilla stone and wood sounds for these blocks. Preserve all
  functional sound triggers even when no custom audio file exists. If the
  reference later adds custom sounds, port the licensed files and full trigger
  contract under the normal project sound rules.
- Keep every deposit and splitter in the central creative tab progression order;
  the tab's automatic registry fallback is a safety net, not a substitute for
  intentional ordering.
- Assess all integrations required by the repository. Normal shapeless recipes
  should remain automatically discoverable in JEI and EMI; do not create a custom
  category without distinct recipe semantics. Static deposits need no custom Jade
  payload unless meaningful dynamic state is added.
- Adapt code and resources only when the designated reference's license permits
  it. Retain every required attribution and bundled license, record the source in
  `THIRD_PARTY_NOTICES.md`, and never make the reference a runtime dependency
  unless the task explicitly requires and justifies it.
