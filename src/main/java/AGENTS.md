# Java and NeoForge Rules

These rules apply to all production Java source under `src/main/java`.

## Language and formatting

- Target Java 21. Use language features only when they make ownership or intent
  clearer; records are appropriate for immutable value objects and payloads.
- Use four spaces, UTF-8, one top-level type per file, and a final newline.
- Prefer lines at or below 120 characters. Break fluent calls and argument lists
  by semantic unit.
- Use braces for every control-flow body. Do not use wildcard imports.
- Order members as constants, static fields, instance fields, constructors,
  public methods, protected/package methods, private methods, nested types.
- Make classes `final` when not designed for inheritance. Make variables `final`
  when it materially clarifies ownership; do not add noise mechanically.
- Prefer immutable collections and defensive copies at API boundaries.

## Naming

- Packages, registry paths, translation keys, and resource paths use lowercase
  ASCII and underscores where a separator is required.
- Classes and records use UpperCamelCase; methods and variables use lowerCamelCase;
  constants use UPPER_SNAKE_CASE.
- Name Minecraft types with conventional suffixes: `Block`, `Item`, `BlockEntity`,
  `Menu`, `Screen`, `Renderer`, `Recipe`, `Serializer`, `Payload`, and `Provider`.
- Registry holder fields name the content, not the implementation detail. Prefer
  `STONE_ANVIL`, not `STONE_ANVIL_REGISTRY_OBJECT`.
- Event handlers describe the event or outcome (`onPlayerTick`, `addCreativeTabItems`).

## Design

- Keep methods small and single-purpose. Extract concepts, not arbitrary line
  ranges. Prefer composition over inheritance.
- Do not create catch-all `Manager`, `Helper`, `Common`, or `Util` classes. Give
  services and operations domain names.
- Avoid global mutable state. Registry holders and immutable codecs/specifications
  may be static; per-world, per-player, per-stack, and per-block state may not.
- Represent absence with an appropriate `Optional` at return boundaries, not as a
  field or parameter everywhere. Reject invalid required inputs early.
- Do not catch `Exception` or suppress failures without recovery. Add useful
  context and preserve the cause when translating an exception.
- Log through SLF4J. Use placeholders, never string concatenation. Do not log every
  tick or every packet. Never log secrets or full untrusted payloads.
- Comments explain invariants, side/thread requirements, format decisions, and
  non-obvious tradeoffs. Do not narrate syntax. Public APIs require useful Javadoc.

## Registration and lifecycle

- Use one consistent mechanism: feature-local `DeferredRegister` instances
  attached to the mod event bus during construction.
- Store registry references as the strongest appropriate deferred holder type.
  Never instantiate registry singleton types outside their registration supplier.
- Never gate registry creation on configuration or optional runtime state.
- Use the mod event bus for registration/lifecycle events and
  `NeoForge.EVENT_BUS` for gameplay events. Register each listener exactly once.
- Keep the main `@Mod` class free of gameplay logic.
- Use `ResourceLocation.fromNamespaceAndPath` (or `RevivalAges.id`) and validate
  externally supplied identifiers. Never concatenate resource locations by hand.

## Sides, threads, and networking

- Common code must load on a dedicated server. It cannot import or mention
  `net.minecraft.client.*`, even behind a runtime condition.
- Treat the logical server as authoritative for inventory, progression, recipes,
  machines, world changes, damage, and unlocks.
- A server-bound payload is a request, not proof. Validate sender, permissions,
  distance, dimension, menu/container, ownership, cooldown, input bounds, and
  current state before applying it.
- Payloads are small immutable records with an explicit `Type` and `StreamCodec`.
  Register them through `RegisterPayloadHandlersEvent` with a versioned registrar.
- Know the handler thread. Schedule world mutations on the correct main thread and
  never block it with file, network, or expensive unbounded work.
- Do not send a packet when vanilla synchronization, block updates, data slots,
  entity data, menu synchronization, or data components already solve the need.

## Data and persistence

- Use Mojang `Codec`/`MapCodec` and NeoForge stream codecs where the platform
  expects them; avoid parallel bespoke serializers.
- Persist stable semantic state, not cached/render-derived values.
- Every serialized field needs a default or an intentional failure mode. Changes
  to saved formats require a migration or backward-compatible read path.
- Mark dirty state and send synchronization only when values actually change.
- Use tags for categories and interoperability. Do not compare long hard-coded
  lists of blocks or items.
- Recipe matching and result creation run on the server. Do not mutate inputs in a
  matcher and do not return a shared mutable stack.

## Performance

- Never scan all blocks, entities, recipes, or registry entries every tick.
- Avoid allocation, registry lookups by string, and repeated capability queries in
  hot paths. Cache only with a clear invalidation rule.
- Bound queues, searches, recursion, packet sizes, and work per tick.
- Profile before adding complex caching or asynchronous behavior.

## Compatibility

- Prefer tags in the `c` namespace for cross-loader material conventions when a
  suitable convention exists.
- Optional integrations stay under `integration` and must not cause classloading
  of absent mod APIs.
- The required optional-integration review list is: KubeJS, Jade, The One Probe,
  EMI, JEI, Curios, Progressive Stages, Biomes O' Plenty, Serene Seasons, and
  Ecliptic Seasons. Implement an adapter whenever the feature exposes information,
  recipes, equipment, progression, biomes, climate, or seasons relevant to one of
  these mods.
- Detect optional mods through NeoForge loading APIs or supported integration
  events. Do not use broad exception catching, reflection-based class probing, or
  unconditional static references to optional API classes.
- Put optional API dependencies on the narrowest correct Gradle configuration.
  Client display APIs must not leak into common/server signatures. Never bundle an
  optional mod or publish it as a required transitive dependency by accident.
- Keep common domain data independent from a viewer/probe API. Jade and The One
  Probe adapters translate synchronized state; EMI and JEI adapters translate the
  same canonical recipe definitions. One integration must not become the source
  of truth for another.
- KubeJS hooks must expose bounded, validated domain operations and stable IDs;
  they must not grant arbitrary access to feature internals or bypass server-side
  progression validation.
- Curios integration must preserve items and player state when Curios is removed.
  Progressive Stages integration must use a progression boundary rather than
  scattering stage checks across content classes.
- Biomes O' Plenty integration uses biome tags/holders and data-driven worldgen.
  Serene Seasons and Ecliptic Seasons share a mod-owned seasonal/climate contract
  so their adapters cannot produce conflicting gameplay state.
- Do not overwrite vanilla data broadly when an additive tag, data map, modifier,
  or event provides a narrower extension point.
