# Revival Ages Engineering Guide

## Scope and precedence

This file applies to the entire repository. A deeper `AGENTS.md` adds rules for
its subtree and takes precedence when rules are more specific. All repository
documentation, identifiers, comments, commit messages, and user-facing source
strings must be written in English.

## Project identity

- Author: Protyv_Kultury.
- Display name: Revival Ages.
- Mod ID and resource namespace: `revivalages`.
- Maven group: `com.protyvkultury`.
- Base Java package: `com.protyvkultury.revivalages`.
- Target: Minecraft 1.21.1, NeoForge 21.1.x, Java 21.
- Mappings: official Mojang names with the Parchment version pinned in
  `gradle.properties`.

Do not rename any identity value independently. The `@Mod` value, Gradle
properties, TOML metadata, Java packages, resource namespaces, GameTest
namespace, and datagen arguments must stay aligned.

## Source of truth

Use the official NeoForge 1.21.1 documentation and the pinned official MDK as the
API source of truth. A mod named in a task is a behavioral, visual, or
architectural reference only. Do not copy obsolete lifecycle events, sided
proxies, registry events, block-entity APIs, capabilities, networking, resource
paths, or other mechanisms from its original platform.

Treat the designated reference mod and its complete dependency chain, including
required libraries, base classes, and companion mods, as one reference
implementation. Before porting behavior, inspect every relevant superclass,
interface, helper, renderer, interaction, transform, inventory wrapper,
configuration path, synchronization path, and callback across that chain. Do not
infer final behavior from a call site in the primary mod alone. Separate the
reference's gameplay policy from its platform-specific mechanism, then reproduce
the combined observable behavior with supported NeoForge 1.21.1 APIs. Reference
mods and libraries remain reference dependencies only and must not become Revival
Ages runtime dependencies unless the task explicitly requires and justifies one.

For every reference-derived feature, port each required dependency contract into
a shared Revival Ages NeoForge layer when the contract is reusable, and consume
that layer from the feature. Do not replace dependency-provided behavior with a
one-off approximation inside a block, block entity, renderer, menu, or
integration. The required trace includes interaction ordering and mouse semantics,
oriented interaction-space transforms, item-handler ordering and limits, structure
and combustion lifecycles, dirty-state observation, persistence, client
synchronization, particles, sounds, collision shapes, and failure or ejection
behavior. Record intentional 1.21.1 deviations in feature documentation when such
documentation is part of the task.

When repository code, old tutorials, and NeoForge 1.21.1 documentation disagree,
follow the official 1.21.1 documentation. Do not silently upgrade examples from a
newer Minecraft branch; APIs and data formats may differ.

For surface rocks and sticks, use the designated reference implementation from
the task and audit its complete lifecycle instead of copying only registration
code. Reimplement obsolete or incompatible details with supported NeoForge APIs.
The reference must not become a runtime dependency. Adapt code, JSON, models, or
other resources only when its license permits it, and preserve every required
license and attribution.

## Architecture

- Keep `RevivalAges` a thin composition root. It may create/register modules but
  must not contain gameplay behavior or large registry lists.
- Organize gameplay by feature under `feature/<feature_name>`. A feature owns its
  blocks, items, block entities, menus, recipes, events, config, and tests.
- Shared code belongs in `api` only when external consumers are expected, or in a
  narrowly named internal package when at least two features genuinely need it.
- Dependencies flow from feature implementation toward stable contracts. Features
  must not reach into another feature's internal package.
- Isolate optional-mod code in `integration/<mod_id>` and load it only when the
  dependency is present. The base mod must start without optional integrations.
- Every feature must assess every entry in
  `docs/optional-integrations.md`, the single source of truth for the required
  optional-integration catalog. Implement each applicable adapter and record
  `not applicable` or `blocked` exactly as that document requires.
- All listed integrations are optional. Revival Ages must compile its base code,
  load, create worlds, and run dedicated servers when none of them are installed.
- Never claim support without testing against a Minecraft 1.21.1-compatible build.
  If a listed mod has no compatible release or stable API for the target, document
  the blocked integration instead of adding an incompatible dependency or a fake
  adapter.
- Isolate every reference to `net.minecraft.client.*` under `client`.
- Prefer data packs, tags, recipes, loot tables, data maps, and codecs over hard-
  coded content lists and ad-hoc JSON parsing.
- Mixins, access transformers, reflection, and coremods require a documented need
  and are last-resort tools.

## Configuration

- Every gameplay-significant value must be exposed through the appropriate
  Revival Ages configuration instead of being fixed in Java. This includes
  timings, capacities, ranges, damage, durability, chances, multipliers, limits,
  environmental modifiers, automation policy, and feature-specific balance.
- Every independently usable content feature or machine must have a startup
  content toggle. Content disabled at startup must not register its blocks, items,
  block entities, menus, recipes, loot, world generation, creative-tab entries,
  payloads, or optional integrations. For example, disabling the Stone Sawmill
  must leave no Stone Sawmill content in the game.
- Content toggles must be loaded before deferred registers and feature modules are
  constructed. They are restart-required and must match between the server and
  every connecting client. Reject a mismatched client with a clear diagnostic
  instead of allowing registry or network divergence.
- Group toggles may disable a complete feature family, but each public machine or
  independent content unit must also be individually controllable. Dependencies
  between toggles must be explicit, validated, and reported; never silently
  re-enable disabled content.
- Disabling previously registered content can make an existing world incompatible.
  Document the risk, warn before world load where supported, and verify both fresh
  worlds and the intended migration behavior. Never leave unresolved recipes,
  tags, loot, worldgen references, or creative-tab entries.

## Repository hygiene

- Do not edit generated files in `src/generated/resources`; change the provider
  and rerun datagen.
- Do not commit `build`, `run`, IDE state, datagen caches, crash reports, logs, or
  local dependency jars.
- Do not add a dependency without documenting why it is needed, its side, its
  license, and whether it is required or optional.
- Maintain the optional-integration status in the relevant feature documentation:
  supported version, compile/runtime dependency coordinates, side, test status,
  and known limitations.
- Copy code or assets from a reference only when its license is compatible with
  the intended use. Preserve required license files and record the source,
  original and renamed paths, license, and adaptations in
  `THIRD_PARTY_NOTICES.md`.
- Drying Rack seasonal coefficients must be server-configurable. Do not hardcode
  new seasonal balance values in Java or persist configured values in world data.
- Keep changes focused. Avoid drive-by formatting or unrelated renames.

## Reference-feature parity

- A parity audit is incomplete until the corresponding call path through the
  designated reference mod and its dependency chain has been traced. Record which
  behavior comes from the primary mod and which comes from a library or companion
  mod when that distinction affects the NeoForge adaptation. Verify camera and
  display contexts, model transform order, and whether a renderer applies an
  additional item-display transform before porting rendered items.
- When a feature intentionally follows an existing mod, audit the complete player
  experience before calling it complete. Check interaction with occupied and empty
  hands, insertion and removal, drops, all block orientations, item transforms,
  collision and selection shapes, particles and sounds, progress synchronization,
  probe and recipe-viewer output, automation, reloads, and dedicated-server safety.
- Treat small presentation and interaction details as required behavior when they
  communicate machine state or prevent item loss. A registered block and working
  recipe are not sufficient evidence of parity.
- For every reference-derived mechanism, audit the complete sound contract across
  the designated reference mod and its dependency chain: custom audio files and
  registered events, vanilla sound
  events, trigger conditions, source category, volume, pitch, random selection,
  loop/cadence, and related configuration. When licensing permits, port every
  functional original sound and record its provenance. The absence of a custom
  `.ogg` does not permit omitting vanilla interaction or state sounds. Sound
  parity is required before the mechanism may be called complete.
- Verify direction-dependent behavior in every horizontal orientation and exercise
  both successful and invalid recipes. Document intentional differences from the
  reference instead of silently omitting behavior.
- Audit the reference mod's functional UI assets whenever implementing recipe
  viewers, probes, guides, or menus. Adapt required slot backgrounds, progress
  arrows, gauges, and state indicators when licensing permits; do not replace
  meaningful reference feedback with arbitrary generic widgets. Record the source,
  license, renamed path, and adaptations in the third-party notices. Decorative
  assets are optional unless they convey state or materially aid recipe reading.
- For the primitive device family (Drying Rack, Campfire,
  Chopping Block, Pit Kiln, Barrel, Soaking Pot, Tanning Rack, Stone Sawmill,
  Stone Oven, Stone Kiln, Stone Crucible, Anvil, Pit Burn, and Wood Torch), Jade
  is the required probe integration and JEI/EMI are the required recipe viewers.
  Jade must expose
  progress and every active modifier or blocking condition, not merely inventory.
- Flint and Tinder, Wood Torch, Pit Burn, and primitive bucket ports must include
  the complete designated reference and dependency-chain interaction lifecycle:
  held-use timing,
  ignition targets, enclosure flood fill and grace failure, staged output storage,
  torch lit/unlit/doused persistence, universal fluid capability, vessel wear,
  hot-fluid damage, milk handling, particles, vanilla sound triggers, and drops.
  Do not replace these contracts with decorative blocks or fluid-specific item
  lists.
- The Revival Ages creative tab is registry-driven and progression-ordered like
  the designated reference's tab. Every new public registered item must appear
  automatically. Add
  known content to the centralized progression order; retain deterministic
  registry-ID fallback ordering so an omitted entry remains visible. Internal
  state blocks must not receive artificial BlockItems merely to expose them.
- For reference-derived surface deposits, parity includes every visual variant
  and weighted random state, placement and support rules, waterlogging, collision
  and selection shapes, creative variation cycling, drops, splitter recombination,
  biome allow/deny logic, worldgen density, model rotations, translations, and all
  functional vanilla or custom sounds. Do not copy obvious dead data or reference
  bugs; document every intentional correction and preserve the intended behavior.
- Before adding or changing any reference-derived mechanism, compare the complete
  observable behavior across the designated reference mod and every implementation
  in its dependency chain on which it relies. Port required reusable contracts
  into Revival Ages' shared core first; do not substitute a mechanism-local
  manual approximation.

## Required verification

Use the smallest relevant checks while developing, then run the full applicable
set before declaring work complete:

1. `./gradlew compileJava` for Java changes.
2. `./gradlew runData` for providers or generated-resource changes; review the
   diff after generation.
3. `./gradlew test` for pure Java logic.
4. `./gradlew runGameTestServer` when GameTests exist and gameplay behavior
   changed.
5. `./gradlew build` before release or handoff.
6. Start `runServer` for changes that could cross physical sides. A client launch
   alone is not sufficient.
7. For every affected optional integration, test both with the mod present and
   absent. For client display integrations, also verify a dedicated server without
   the client-only companion installed.
8. For every affected content toggle, test both enabled and disabled startup
   configurations. Verify matching client/server registries, a clear mismatch
   rejection, absence of all disabled content and data, and the documented
   existing-world migration behavior.

On Windows use `gradlew.bat`. Never accept a warning, missing model, missing
translation, registry error, data-pack error, or dedicated-server classloading
failure as expected behavior.

## Definition of done

A feature is complete only when registration, server-authoritative behavior,
client presentation, resources/datagen, translations, recipes/tags/loot,
configuration, content toggles, migration compatibility, and relevant tests are all
addressed. The feature's integration assessment for the required compatibility
list must also be complete. Update architecture documentation when a package
boundary or dependency direction changes.
