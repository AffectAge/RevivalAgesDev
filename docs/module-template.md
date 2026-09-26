# Feature Module Checklist

Create `feature/<feature_name>` and add only the subpackages the feature uses.

- Define one `<FeatureName>Feature` entry point implementing `FeatureModule`.
- Declare a mandatory `ContentPolicy`: define every owned `ContentKey`, its
  parents and configured supplier, and classify every public item and block.
  Declare infrastructure explicitly; never use an empty gameplay policy.
- Keep every `DeferredRegister` in a clearly named feature-local registry class.
- Register mod-bus listeners once from the feature entry point.
- Register game-bus listeners only for actual runtime events.
- Put physical-client implementation under `client`.
- Put optional-mod adapters under `integration`, not in the feature core.
- Add the feature to the permanent content catalog; do not add family, machine,
  block, item, or gameplay-system enable switches.
- Put every gameplay-significant timing, capacity, range, damage, durability,
  chance, multiplier, limit, environmental modifier, and automation policy in
  the appropriate mod configuration with validated bounds and documented defaults.
- Define tags before hard-coding item/block membership.
- Define recipes, loot, advancements, worldgen, and data maps through datagen when
  supported.
- Add `en_us`, `ru_ru` translations for every user-visible name, tooltip, config value,
  key binding, menu title, and message.
- Decide the authoritative side and persistence owner of all state.
- Define payload direction, validation, size limits, and handler thread for every
  network message.
- Do not add or modify automated tests unless the user explicitly requests test
  work.
- Keep legacy content conditions only where compatibility with existing data
  requires them; they must not expose a configuration-backed content switch.
- Run client, dedicated-server, datagen, and build verification only when the
  user requests it or the task is a release/handoff.

Do not place unfinished global helpers in `util`. Prefer a private helper inside
the feature until there are multiple proven callers and a stable shared concept.
