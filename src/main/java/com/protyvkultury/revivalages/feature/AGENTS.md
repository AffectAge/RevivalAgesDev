# Feature Module Rules

This directory contains vertical gameplay slices, following the useful modular
boundary demonstrated by Pyrotech.

- Pyrotech feature behavior must be evaluated together with the Athenaeum code it
  delegates to or inherits from. Trace the complete Pyrotech-to-Athenaeum call
  path for interactions, transforms, rendering, inventories, block bases,
  networking, synchronization, and utility callbacks before implementing a port.
  Reproduce the combined behavior with NeoForge 1.21.1 APIs; never add Athenaeum
  as a Revival Ages dependency or copy its obsolete Forge 1.12 mechanisms.

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
- Do not conditionally register an entire feature. Registry identity must remain
  stable; disable behavior through config/data and keep save compatibility.
- A new feature must include its resource/datagen plan and tests in the same
  change. A Java-only content feature is incomplete.
- Every new or changed feature must complete the following optional-integration
  assessment and implement all applicable adapters:
  - KubeJS for scripts, recipes, registries, or configurable gameplay hooks.
  - Jade and The One Probe for block/entity inspection and machine state.
  - EMI and JEI for recipe categories, catalysts, transfer, and usage displays.
  - Curios for wearable or accessory-like items.
  - Progressive Stages for gated recipes, content, or progression milestones.
  - Biomes O' Plenty for biome-sensitive content and world generation.
  - Serene Seasons and Ecliptic Seasons for crops, temperature, weather, climate,
    or season-sensitive behavior.
- Mark an integration `not applicable` only when the feature has no corresponding
  domain surface. Convenience is not a reason to skip an applicable integration.
- Viewer/probe integrations must consume the feature's public read model. Script,
  equipment, progression, biome, and season integrations must consume explicit
  feature contracts rather than internal fields.
- Primitive processing machines are audited as one interaction chain. Each block
  must cover placement in every facing, exact rendered item/fluid positions,
  insertion and extraction priority, teardown drops, particles and sounds,
  synchronized progress, blocking conditions, failure outcomes, and automation.
  Do not close a porting task after only its recipe completes successfully.
- A primitive mechanism's sound audit must trace both Pyrotech and Athenaeum.
  Implement custom and vanilla sounds at their original lifecycle points,
  including shared extraction feedback, work/idle loops, completion, breakage,
  fuel, ignition, extinguishing, and material placement/removal where applicable.
  Preserve configured enable/volume controls and reference randomization instead
  of replacing them with mechanism-local approximations.
- For Campfire, Chopping Block, Pit Kiln, Barrel, Soaking Pot, Tanning Rack,
  Drying Rack, Stone Sawmill, Stone Oven, Stone Kiln, Stone Crucible, and Anvil,
  provide Jade state/progress/modifiers plus JEI and EMI recipes from one
  loader-neutral recipe view. The One Probe is intentionally outside the accepted
  scope for this device family. This exclusion does not remove or narrow any
  other optional integration requirement.
- Pit Burn and Wood Torch follow the same Jade-only probe rule; do not add The
  One Probe. Pit Burn recipes also use the shared JEI/EMI read model. Igniters and
  portable buckets do not need a block probe, but their crafting and firing
  recipes must remain discoverable through normal recipe viewers.
- Register public content normally and rely on the central registry-driven,
  progression-ordered Revival Ages creative tab. Add established content to its
  one centralized progression list; do not maintain a second list in a feature.
  Its registry-ID fallback must continue to expose unlisted public items.
- Surface rocks and sticks follow This Rocks! 1.8.0 parity rules. Audit variants,
  waterlogging, support loss, replacement, shapes, creative cycling, loot,
  splitter recipes, models, rotations, biome filtering, generation density, and
  sounds as one lifecycle. Reuse feature-level shared code rather than cloning
  behavior for each material.
- Progression checks should query a progression contract or tag/data rule. Do not
  scatter age/tier conditionals throughout blocks and items.
