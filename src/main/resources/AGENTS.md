# Hand-Authored Resource Rules

This subtree contains source resources that humans intentionally maintain.

## Namespaces and paths

- Mod-owned assets use `assets/revivalages`; mod-owned data uses
  `data/revivalages`.
- Use lowercase ASCII file names and resource paths. Registry/resource paths use
  underscores, never spaces or uppercase characters.
- Minecraft 1.21.1 uses singular data directories: `advancement`, `recipe`,
  `loot_table`, `structure`, `function`, and tag registry folders such as
  `tags/item` and `tags/block`.
- Do not add `pack.mcmeta`; NeoForge synthesizes it for normal mod resources.

## Assets

- Every user-visible string starts in `assets/revivalages/lang/en_us.json`.
  Translation keys are stable API and follow Minecraft conventions.
- Item models live in `models/item`; block models in `models/block`; block state
  definitions in `blockstates`.
- Textures are original or properly licensed, power-of-two where appropriate, and
  stored in the narrowest matching texture folder. Keep source project files out
  of shipped resources unless the build explicitly excludes them.
- Define every referenced sound in `sounds.json`, use lowercase sound IDs, and
  document the license/source outside the shipped asset.
- Pyrotech-derived primitive recipe categories must reuse or adapt the licensed
  functional backgrounds, slots, arrows, flame indicators, and fluid gauges when
  they improve recipe readability. JEI and EMI must use the same visual semantics
  and recipe facts. Record every imported asset in `THIRD_PARTY_NOTICES.md`.

## Data

- Prefer generation for repetitive recipes, tags, loot tables, advancements,
  models, and worldgen. Hand-author only data that is clearer or unsupported in a
  provider.
- Use tags rather than enumerating compatible content in recipes or Java code.
  Use the `c` namespace for established common material tags.
- Optional-integration data for KubeJS, Jade, The One Probe, EMI, JEI, Curios,
  ProgressiveStages, Biomes O' Plenty, Serene Seasons, or Ecliptic Seasons must
  load safely when the target mod is absent. Use supported NeoForge load conditions
  or the target API's documented data mechanism where applicable.
- Do not override resources in an optional mod's namespace unless that mod's
  documented extension mechanism requires it. Prefer Revival Ages-owned data,
  common tags, data maps, and explicit adapters.
- Gameplay recipes and reloadable rules must remain server-authoritative.
- JSON must be strict, UTF-8, deterministic, and free of comments or trailing
  commas. Resource locations must be namespaced.
- Do not replace Minecraft namespace data unless the feature explicitly requires
  an override and documents compatibility consequences.
- Validate worldgen and loot changes in fresh and existing worlds as applicable.

Before handoff, run datagen (if providers exist), launch the client to catch models
and translations, and launch a dedicated server to catch data-pack errors.
