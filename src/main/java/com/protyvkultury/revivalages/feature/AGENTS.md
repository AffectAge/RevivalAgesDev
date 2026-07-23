# Feature Module Rules

This directory contains vertical gameplay slices. A reference mod named in a task
may demonstrate a useful boundary, but Revival Ages owns the resulting
architecture.

- Reference-derived feature behavior must be evaluated together with all code in
  the designated reference mod's dependency chain that it delegates to or
  inherits from. Trace the complete call path for interactions, transforms,
  rendering, inventories, block bases, configuration, networking,
  synchronization, and utility callbacks before implementing a port. Reproduce
  the combined behavior with NeoForge 1.21.1 APIs; never add a reference mod or
  library as a Revival Ages runtime dependency unless the task explicitly
  requires it, and never copy obsolete platform mechanisms.

- Each first-level feature has one entry point implementing `FeatureModule`.
- The entry point only wires deferred registers, configs, payloads, and listeners.
  Gameplay behavior stays in domain types.
- A feature owns its registry classes and related `block`, `item`, `blockentity`,
  `menu`, `recipe`, `event`, `config`, and feature-local `client` packages.
- Create subpackages only when they contain real code. Do not mirror every possible
  Minecraft registry preemptively.
- Feature internals are package-private where practical. Expose a small contract
  from the feature root or the top-level `api` package when another feature needs
  it.
- Cross-feature access to another feature's `internal`, registry implementation,
  block entity fields, or event handlers is prohibited.
- Give every feature family and every independently usable public machine or
  content unit a server configuration toggle that defaults to enabled.
- Feature modules always attach all of their deferred registers, payload
  registrations, and stable prerequisites. Never conditionally register content
  from a toggle. Apply disabled state to creative-tab visibility, recipes, loot,
  worldgen, integrations, automation, and server-authoritative gameplay behavior.
- Validate dependencies between enabled behaviors explicitly. Disabled placed
  content remains loadable and inert, preserves state, reports that it is
  disabled when used, and never deletes stored items or fluids.
- A new feature must include its resource/datagen plan and tests in the same
  change. A Java-only content feature is incomplete.
- Every new or changed feature must assess the complete catalog in
  `docs/optional-integrations.md` and implement all applicable adapters. That
  document owns the list and applicability criteria; do not duplicate them here.
- Record `applicable`, `not applicable`, or `blocked` outcomes in the feature
  documentation according to the catalog rules.
- Viewer/probe integrations must consume the feature's public read model. Script,
  equipment, progression, biome, and season integrations must consume explicit
  feature contracts rather than internal fields.
- Primitive processing machines are audited as one interaction chain. Each block
  must cover placement in every facing, exact rendered item/fluid positions,
  insertion and extraction priority, teardown drops, particles and sounds,
  synchronized progress, blocking conditions, failure outcomes, and automation.
  Do not close a porting task after only its recipe completes successfully.
- A primitive mechanism's sound audit must trace the designated reference mod and
  its complete dependency chain.
  Implement custom and vanilla sounds at their original lifecycle points,
  including shared extraction feedback, work/idle loops, completion, breakage,
  fuel, ignition, extinguishing, and material placement/removal where applicable.
  Preserve configured enable/volume controls and reference randomization instead
  of replacing them with mechanism-local approximations.
- For Campfire, Chopping Block, Pit Kiln, Barrel, Soaking Pot, Tanning Rack,
  Drying Rack, Stone Sawmill, Stone Oven, Stone Kiln, Stone Crucible, and Anvil,
  provide Jade state/progress/modifiers plus JEI and EMI recipes. Each viewer may
  have its own presentation adapter, but both must enumerate the same gameplay
  recipe types through `RecipeManager` and reuse feature-owned query semantics.
- Pit Burn and Wood Torch use Jade for probe output. Pit Burn recipes also use
  the shared JEI/EMI read model. Igniters and
  portable buckets do not need a block probe, but their crafting and firing
  recipes must remain discoverable through normal recipe viewers.
- Register public content normally and rely on the central registry-driven,
  progression-ordered Revival Ages creative tab. Add established content to its
  one centralized progression list; do not maintain a second list in a feature.
  Its registry-ID fallback must continue to expose unlisted enabled public items
  while content disabled by server configuration remains hidden.
- Surface rocks and sticks follow the designated reference's parity rules. Audit
  variants, waterlogging, support loss, replacement, shapes, creative cycling,
  loot, splitter recipes, models, rotations, biome filtering, generation density,
  and sounds as one lifecycle. Reuse feature-level shared code rather than
  cloning behavior for each material.
- Progression checks should query a progression contract or tag/data rule. Do not
  scatter age/tier conditionals throughout blocks and items.
