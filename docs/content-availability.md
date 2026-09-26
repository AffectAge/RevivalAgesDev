# Content Availability

## Contract

Revival Ages registers and enables every public content unit unconditionally.
Configuration changes balance and presentation only; it cannot disable blocks,
items, machines, feature families, acquisition, world generation, or gameplay
systems.

Every gameplay module declares a `ContentPolicy`. `CoreFeature` and
`CreativeTabFeature` declare infrastructure policies, while gameplay policies
own stable `ContentKey` definitions and classify every public item and block.
`ContentAvailability` validates the assembled catalog at startup and fails when a
key is missing, duplicated, cyclic, or when registered public content is
unclassified. Every built-in policy resolves its keys as enabled.

This classification remains useful for deterministic creative-tab ordering,
data generation, recipe-viewer ownership, probe integration, and compatibility
with existing data packs. It is not a user-facing feature switch.

## Unified configuration

All balance and client-presentation settings are assembled into one NeoForge
common config: `config/revivalages.toml`. Feature-specific config filenames are
no longer registered. Existing legacy files can be removed after copying any
still-relevant values into the matching section of the unified file.

The removed settings include family and per-content `enabled` values for Item
Size, Carried Weight, Food Spoilage, Diet, Surface Deposits, Knapping,
Construction Frame, Structural Integrity, Primitive Technology, and Hand
Grindstone. Narrow behavior and presentation options remain configurable; for
example HUD visibility, feedback sounds, automation policy, seasonal modifiers,
and camera shake do not disable their owning content.

## Legacy data conditions

The `revivalages:content_enabled` and `revivalages:any_content_enabled`
conditions, plus the older feature-specific condition codecs, remain registered
so existing built-in and third-party data packs continue to decode. Since all
catalog keys are permanently available, positive conditions resolve to true and
negative conditions resolve to false. New built-in data does not use these
conditions to expose configuration switches.

`runData` still validates conditioned recipes, block loot tables, and biome
modifiers against the central catalog and writes the deterministic
`data/revivalages/content_availability/manifest.json` review manifest. Common
setup also validates public registry classification.

## Future features

A new gameplay feature must:

1. declare its `ContentKey`, parents, and public item/block memberships in a
   mandatory `ContentPolicy`;
2. register public content and its acquisition, creative visibility, data,
   runtime behavior, and applicable integrations unconditionally;
3. put balance and presentation values in the appropriate section of
   `config/revivalages.toml` without adding feature or machine enable switches;
4. add translations and documentation.

Automated tests are added only when the user explicitly requests them.
