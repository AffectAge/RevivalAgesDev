# Data Generator Rules

This subtree contains build-time data providers only. Runtime code must never
depend on it.

- Subscribe providers to the NeoForge data-gathering event on the mod event bus.
- Generate into `src/generated/resources` and use `src/main/resources` only as the
  existing-resource input.
- Prefer standard providers for recipes, loot, tags, models, block states,
  languages, advancements, data maps, and datapack registries.
- Use registry holders and shared resource keys instead of duplicating string IDs.
- Provider output must be deterministic: stable ordering, no timestamps, no
  machine-specific paths, no network calls, and no random unseeded values.
- Providers may be split by feature, but one composition class registers them so
  none silently disappear.
- Datagen must not read a local content-toggle value to change registry identity
  or make generated output machine-dependent. Generate the resources needed to
  load registered content safely, and encode supported load conditions or
  feature-owned filtering metadata for recipes, loot, worldgen, and other normal
  acquisition paths that must be inactive when content is disabled.
- Run datagen after provider changes, review additions and deletions, then run it a
  second time when investigating nondeterminism.
- Never patch generated JSON to make a test pass. Fix the provider or its source
  definition.
