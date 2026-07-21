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
- For Campfire, Chopping Block, Pit Kiln, Barrel, Soaking Pot, Tanning Rack, and
  Drying Rack, provide Jade state/progress/modifiers plus JEI and EMI recipes from
  one loader-neutral recipe view. The One Probe is intentionally outside the
  accepted scope for this device family.
- Progression checks should query a progression contract or tag/data rule. Do not
  scatter age/tier conditionals throughout blocks and items.
