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
API source of truth. Pyrotech 1.12 is an architectural reference only. Do not
copy its obsolete Forge lifecycle events, sided proxies, registry events, tile
entity APIs, capabilities, networking, or resource paths.

Treat Pyrotech and its required Athenaeum library as one reference implementation.
Before porting Pyrotech behavior, inspect every relevant superclass, interface,
helper, renderer, interaction, transform, inventory wrapper, synchronization path,
and callback implemented by Athenaeum. Do not infer final behavior from a Pyrotech
call site alone. Separate Pyrotech's gameplay policy from Athenaeum's mechanism,
then reproduce the combined observable behavior with supported NeoForge 1.21.1
APIs. Athenaeum remains a reference dependency only and must not be added as a
runtime dependency of Revival Ages.

For every current and future Pyrotech-derived feature, port every required
Athenaeum contract into a shared Revival Ages NeoForge layer and use that layer
from the feature. Do not replace Athenaeum behavior with a one-off approximation
inside a block, block entity, renderer, menu, or integration. The required trace
includes interaction ordering and mouse semantics, oriented interaction-space
transforms, item-handler ordering and limits, structure and combustion lifecycles,
dirty-state observation, persistence, client synchronization, particles, sounds,
collision shapes, and failure or ejection behavior. Record intentional 1.21.1
deviations in feature documentation.

When repository code, old tutorials, and NeoForge 1.21.1 documentation disagree,
follow the official 1.21.1 documentation. Do not silently upgrade examples from a
newer Minecraft branch; APIs and data formats may differ.

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
- Every feature must assess and, where relevant, provide optional integration for
  KubeJS, Jade, The One Probe, EMI, JEI, Curios, Progressive Stages, Biomes O'
  Plenty, Serene Seasons, and Ecliptic Seasons. Record `not applicable` with a
  short reason when a listed mod has no meaningful interaction with the feature.
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
- You can copy Pyrotech textures, sounds, or other assets.
- Drying Rack seasonal coefficients must be server-configurable. Do not hardcode
  new seasonal balance values in Java or persist configured values in world data.
- Keep changes focused. Avoid drive-by formatting or unrelated renames.

## Reference-feature parity

- A Pyrotech parity audit is incomplete until the corresponding Athenaeum call
  path has been traced. Record which behavior comes from Pyrotech and which comes
  from Athenaeum when that distinction affects the NeoForge adaptation. In
  particular, verify Athenaeum camera/display contexts and transform order before
  porting rendered items; its interaction renderer uses the baked model without
  an additional item-display transform.
- When a feature intentionally follows an existing mod, audit the complete player
  experience before calling it complete. Check interaction with occupied and empty
  hands, insertion and removal, drops, all block orientations, item transforms,
  collision and selection shapes, particles and sounds, progress synchronization,
  probe and recipe-viewer output, automation, reloads, and dedicated-server safety.
- Treat small presentation and interaction details as required behavior when they
  communicate machine state or prevent item loss. A registered block and working
  recipe are not sufficient evidence of parity.
- Verify direction-dependent behavior in every horizontal orientation and exercise
  both successful and invalid recipes. Document intentional differences from the
  reference instead of silently omitting behavior.
- Audit the reference mod's functional UI assets whenever implementing recipe
  viewers, probes, guides, or menus. Adapt required slot backgrounds, progress
  arrows, gauges, and state indicators when licensing permits; do not replace
  meaningful reference feedback with arbitrary generic widgets. Record the source,
  license, renamed path, and adaptations in the third-party notices. Decorative
  assets are optional unless they convey state or materially aid recipe reading.
- For the Pyrotech-inspired primitive device family, Jade is the required probe
  integration and JEI/EMI are the required recipe viewers. Do not add The One
  Probe to this family unless its scope is explicitly reopened. Jade must expose
  progress and every active modifier or blocking condition, not merely inventory.

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

On Windows use `gradlew.bat`. Never accept a warning, missing model, missing
translation, registry error, data-pack error, or dedicated-server classloading
failure as expected behavior.

## Definition of done

A feature is complete only when registration, server-authoritative behavior,
client presentation, resources/datagen, translations, recipes/tags/loot,
configuration (if needed), migration compatibility, and relevant tests are all
addressed. The feature's integration assessment for the required compatibility
list must also be complete. Update architecture documentation when a package
boundary or dependency direction changes.
