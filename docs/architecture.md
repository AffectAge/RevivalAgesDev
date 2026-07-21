# Architecture

## Design goal

Revival Ages uses a modular monolith: one NeoForge mod and one distributable JAR,
split into cohesive gameplay features. This keeps registration and cross-feature
coordination simple while preventing a single global collection of blocks, event
handlers, recipes, and machine logic from becoming the architecture.

## What is retained from Pyrotech

Pyrotech separates core behavior, technology tiers, storage, tools, world
generation, and optional integrations into modules. Each module keeps related
initializers, configuration, recipes, blocks, items, tiles, client code, and
plugins close together. Revival Ages retains these principles:

- a thin composition root;
- explicit feature modules;
- feature-local registration and configuration;
- integrations outside the core implementation;
- progression mechanics expressed as cohesive systems instead of unrelated
  registry objects.

Pyrotech targets Forge 1.12, so its implementation mechanisms are not reusable.
Sided proxies, `FMLPreInitializationEvent`, `RegistryEvent`, numeric metadata,
`TileEntity`, old capabilities, and old packet patterns must not enter this code.

Pyrotech's observable behavior is produced jointly by Pyrotech and its required
Athenaeum library. Reference analysis therefore follows inheritance and delegation
across both repositories. Athenaeum supplies important block bases, interaction
dispatch, item rendering, transform order, inventory wrappers, synchronization,
and utility callbacks that may not be visible at a Pyrotech call site. Revival
Ages ports the resulting behavior to current NeoForge contracts; it does not take
a runtime dependency on Athenaeum. Required contracts are implemented once under
`com.protyvkultury.revivalages.core` and reused by every affected feature; local
feature approximations are prohibited. For example, Pyrotech interaction items are
rendered after the interaction transform with no additional item display-context
transform, which must be preserved deliberately in the modern renderer.

## NeoForge 1.21.1 adaptation

- Attach `DeferredRegister` instances to the mod event bus during mod
  construction.
- Use `BlockEntity`, data components, attachments, capabilities, and saved data
  according to the lifetime and ownership of the state.
- Register custom payloads through `RegisterPayloadHandlersEvent`; validate every
  server-bound action on the server.
- Put physical-client code in `client` and verify the mod on a dedicated server.
- Put data-generator implementations in `data`; generated output goes to
  `src/generated/resources`.
- Use the 1.21.1 singular data-pack directories such as `advancement`, `recipe`,
  `loot_table`, `structure`, and `tags/item`.
- Let modern NeoForge synthesize `pack.mcmeta` unless a documented custom pack is
  actually required.

## Package map

```text
com.protyvkultury.revivalages
|-- RevivalAges.java          composition root and identity
|-- api/                      deliberately public extension contracts
|-- client/                   physical-client-only code
|-- data/                     datagen providers
|-- feature/
|   |-- core/                 universally required foundation
|   |-- progression/          age/progression rules and unlocks
|   |-- survival/             survival interactions
|   |-- technology/           processing and machines
|   `-- worldgen/             feature-owned world generation
|-- gametest/                 in-game integration tests
|-- integration/              optional-mod adapters
`-- network/                  payloads and handler boundaries
```

Subpackages inside a feature are created when needed, commonly `block`, `item`,
`blockentity`, `menu`, `recipe`, `registry`, `event`, `config`, and `client`.
Avoid creating empty taxonomic layers without a feature need.

## Dependency rules

1. The composition root may depend on every feature entry point.
2. Feature entry points may depend on shared stable APIs and their own internals.
3. A feature may depend on another feature's public contract, never its internals.
4. `api` may depend on Minecraft/NeoForge types when the contract requires them,
   but never on feature implementations.
5. Common/server code cannot depend on `client`.
6. Integrations depend on the base API and the optional mod API; base features do
   not depend on integrations.
7. Datagen may read registry holders and feature definitions, but runtime code may
   not depend on datagen classes.

## Feature lifecycle

Every feature exposes one small registration entry point. That entry point attaches
its deferred registers and listeners to the supplied mod event bus. Runtime event
handlers subscribe to `NeoForge.EVENT_BUS` only when they need game events. Avoid
scanning or global static initialization merely to make a class load.

Registrations are always available and deterministic. Configuration changes may
control behavior, but must not conditionally omit registry entries because registry
sets must agree between server, client, saves, and data packs.

## Data ownership

- Registry singleton: stateless definition shared by all instances.
- Block entity: state owned by a placed block at a position.
- Item data component: persistent state owned by an `ItemStack`.
- Entity attachment/capability: state owned by an entity or exposed as an
  interoperability surface.
- Level saved data: world-level persistent state.
- Recipe/data map/tag: reloadable, data-driven rules.
- Network payload: transient synchronization or an explicitly validated request,
  never the source of truth.

Choose the narrowest owner with the correct lifetime. Persist only the minimum
state and define migration/default behavior before changing a serialized format.

Primitive technology is a coordinated feature family below `feature/technology`.
Each machine owns its block, block entity, and recipe type, while shared materials,
configuration, rendering helpers, tags, and synchronized read models live in
`feature/technology/primitive`. The optional Jade adapter depends only on those
read models. Probe APIs never enter the machine packages.

## Adding a feature

Use [module-template.md](module-template.md) as the checklist. Add the feature to
`ModFeatures` only after its entry point exists. If a feature is optional for
gameplay, keep its registry entries stable and gate behavior/data instead of
conditionally registering objects.
