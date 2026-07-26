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
- Add a restart-required startup content toggle before wiring the feature's
  deferred registers. Independently usable machines and content units also need
  individual toggles.
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
- Add pure unit tests for algorithms and GameTests for in-world behavior.
- Test enabled and disabled startup configurations. Disabled content must leave
  no exclusive registry, data, creative-tab, networking, worldgen, or integration
  contribution.
- Add universal content conditions to every acquisition resource, gate shared
  recipe types with `any_content_enabled`, and add the resource to the automated
  gate-validation suite.
- Prove that disabled block entities retain inventory/fluid state, expose no
  capability, perform no tick work, reject payloads/interactions, and drop one
  restorable state-carrying block item.
- Run both `runGameTestServer` and `runGameTestServerContentDisabled`.
- Verify client, dedicated server, datagen, and build tasks.

Do not place unfinished global helpers in `util`. Prefer a private helper inside
the feature until there are multiple proven callers and a stable shared concept.
