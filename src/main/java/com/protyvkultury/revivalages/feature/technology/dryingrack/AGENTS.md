# Drying Rack Feature Rules

- Keep processing server-authoritative, bounded, reload-safe, and independent for
  every visible slot.
- Preserve the no-menu interaction model: stored items and progress are presented
  in the world, while optional probes consume the immutable feature read model.
- Normal-rack recipes override and then inherit crude-rack recipes. Never duplicate
  inherited JSON merely to make a viewer integration work.
- Read biome behavior through tags. Never hardcode Biomes O' Plenty biome IDs.
- Serene Seasons and Ecliptic Seasons adapters must map into `SeasonProvider`.
  Ecliptic Seasons takes precedence when both are installed, and only one seasonal
  bonus may be applied.
- Every seasonal coefficient must be configurable in the server configuration.
  New seasonal balance values must never be introduced as immutable Java gameplay
  constants. Code constants may only describe serialization or validation limits.
- Seasonal configuration is read at calculation time. Do not persist configured
  bonuses into recipes, block entities, chunks, or world data.
- KubeJS, Jade, EMI, JEI, Progressive Stages, Biomes O' Plenty,
  Serene Seasons, and Ecliptic Seasons are applicable integration targets. Keep
  every adapter optional and isolated from this package's implementation classes.
- Curios is not applicable because a placed drying rack has no wearable or player
  equipment behavior. Reassess this only if that domain surface changes.
- Item-handler automation remains disabled by default and must not change manual
  interaction behavior.
- Visual assets derived from the designated reference require a compatible source
  license, every license file required for redistribution, and an entry in
  `THIRD_PARTY_NOTICES.md`.
- Drying Rack viewer work must inspect the designated reference's functional
  recipe-viewer artwork. Preserve or deliberately adapt its slot background,
  progress arrow, timing feedback, and other state-bearing UI elements for JEI
  and EMI when they remain useful. Keep both viewers visually consistent, isolate
  their APIs, and update attribution for every copied or adapted asset. Pure
  decoration is not a parity requirement.
- Parity reviews for this feature must include removal with occupied and empty
  hands, drops on break/support loss, slot selection, item transforms in all four
  horizontal directions, normal-rack top transforms, active-only progress
  particles, progress and speed-modifier synchronization, Jade presentation,
  JEI/EMI categories and catalysts, collision, climbing, recipe reloads, and
  optional-mod presence/absence. Jade is the selected probe integration for this
  feature. Do not defer these as cosmetic polish.
