# Optional Integration Rules

This subtree owns every optional-mod adapter. All rules here are mandatory.

## Required compatibility matrix

Every feature must be evaluated against these optional integrations:

| Package name | Mod | Integration responsibility when applicable |
| --- | --- | --- |
| `kubejs` | KubeJS | Script events, recipe schemas, stable IDs, and bounded domain hooks |
| `jade` | Jade | Block/entity inspection sourced from synchronized read models |
| `theoneprobe` | The One Probe | Probe information equivalent to Jade where the APIs permit it |
| `emi` | EMI | Recipe display, catalysts, workstations, and transfer support |
| `jei` | JEI | Recipe display, catalysts, workstations, and transfer support |
| `curios` | Curios | Optional equipment slots and safe item capability behavior |
| `progressivestages` | Progressive Stages | Progression gates through a stable progression contract |
| `biomesoplenty` | Biomes O' Plenty | Tag/holder-based biome and worldgen compatibility |
| `sereneseasons` | Serene Seasons | Crop, climate, temperature, and seasonal behavior |
| `eclipticseasons` | Ecliptic Seasons | Crop, climate, temperature, and seasonal behavior |

Use the actual dependency mod ID and API coordinates from the compatible 1.21.1
release; do not infer them from the package names above.

## Isolation

- One first-level package per optional mod. Do not combine multiple external APIs
  in one adapter class.
- Base features expose loader-neutral contracts/read models. Adapters depend on
  those contracts; base features never import adapter or optional-mod classes.
- Optional API classes must not appear in common entry-point signatures, static
  fields loaded unconditionally, serialized data, or base registry suppliers.
- Register an adapter only after confirming the mod is loaded and on the correct
  physical side. Prefer the external mod's documented plugin/IMC/event mechanism.
- Do not use reflection merely to avoid declaring a proper optional compile-time
  API dependency.
- Failure inside an optional adapter must be diagnosed with useful context. Do not
  silently swallow linkage or registration errors.

## Consistency

- Jade and The One Probe must present the same underlying facts even if their UI
  differs.
- Exception: the accepted primitive-device scope is Jade plus JEI/EMI and excludes
  The One Probe. Do not create a partial TOP implementation for those machines;
  revisit this exception only through an explicit scope change.
- EMI and JEI must derive from the same recipe types and validation rules. Do not
  maintain duplicate recipe semantics per viewer.
- Serene Seasons and Ecliptic Seasons adapters map external season/climate data
  into one Revival Ages contract. If both mods are present, use a documented,
  deterministic precedence policy and never apply seasonal effects twice.
- Progressive Stages augments Revival Ages progression; it cannot bypass server
  authority or make saved worlds unreadable when removed.
- Curios-backed behavior must degrade safely when Curios is absent or removed.
- KubeJS APIs are versioned public surfaces. Validate script inputs, bound work,
  and keep breaking changes out of patch releases.

## Verification

For each implemented adapter, verify:

1. Revival Ages starts and the affected feature works without the optional mod.
2. The supported 1.21.1 version starts with the optional mod installed.
3. Client-only integrations do not break a dedicated server.
4. Missing/disabled integrations do not delete items, corrupt saves, or leave
   unresolved registry references.
5. When two mods cover the same concern (Jade/TOP, EMI/JEI, or the two season
   mods), installing both produces consistent information and no duplicate effect.

If no compatible 1.21.1 release/API exists, record the adapter as blocked with the
checked version and date. Do not weaken base-mod correctness to force support.
