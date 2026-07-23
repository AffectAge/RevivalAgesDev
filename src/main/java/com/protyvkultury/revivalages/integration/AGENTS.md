# Optional Integration Rules

This subtree owns every optional-mod adapter. All rules here are mandatory.

## Required compatibility catalog

`docs/optional-integrations.md` is the single source of truth for the mods every
feature must assess, their package hints, and applicability criteria. Do not
duplicate that catalog here. Use the actual dependency mod ID and API coordinates
from the compatible 1.21.1 release; do not infer them from package hints.

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

- Jade must present synchronized feature facts from loader-neutral read models.
- EMI and JEI use separate viewer adapters and may have independent category,
  widget, tooltip, and layout classes. Both adapters must enumerate the same
  gameplay recipe types from Minecraft's `RecipeManager`; recipe codecs and
  feature-owned gameplay/query logic remain the only source of validation,
  filtering, inheritance, and matching semantics. Never reimplement those
  semantics inside either viewer.
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
5. When two mods cover the same concern (EMI/JEI or the two season mods),
   installing both produces consistent information and no duplicate effect.

If no compatible 1.21.1 release/API exists, record the adapter as blocked with the
checked version and date. Do not weaken base-mod correctness to force support.
