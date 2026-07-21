# Revival Ages

Revival Ages is a NeoForge mod for Minecraft 1.21.1 by Protyv_Kultury. This
repository contains an architecture-first implementation of Pyrotech-inspired
primitive technology: Drying Racks, Campfire, Chopping Block, Pit Kiln, Barrel,
Soaking Pot, and Tanning Rack.

The project keeps Pyrotech's strongest architectural idea—small gameplay modules
with local registration, configuration, recipes, and integrations—while using
modern NeoForge 1.21.1 APIs. The main mod class is deliberately thin; feature
packages own their implementation, client-only code is isolated, and gameplay
content is designed to be data-driven. Pyrotech and its Athenaeum dependency are
reviewed together; required library behavior is ported to an internal NeoForge
layer instead of being approximated independently in each mechanism.

## Requirements

- 64-bit JDK 21
- Minecraft 1.21.1
- NeoForge 21.1.241 (the version pinned by the official 1.21.1 ModDevGradle MDK)

## Building the mod

Revival Ages requires a 64-bit JDK 21. A separate Gradle installation is not
required because the repository includes the Gradle wrapper.

1. Clone or download the repository and open a terminal in its root directory.
2. Confirm that Java 21 is active with `java -version`.
3. Run the build:

   ```powershell
   .\gradlew.bat clean build
   ```

   On Linux or macOS, run:

   ```bash
   ./gradlew clean build
   ```

4. Find the compiled mod JAR in `build/libs/`. Files ending in `-sources.jar`
   are source archives and should not be placed in a Minecraft `mods` folder.

The build compiles Java, validates resources, runs automated tests, and creates
the distributable JAR. If Gradle reports the wrong Java version, set `JAVA_HOME`
to a JDK 21 installation and open a new terminal before trying again.

## Development commands

```text
./gradlew clean build
./gradlew runClient
./gradlew runServer
./gradlew runData
./gradlew runGameTestServer
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

To test the optional Jade, JEI, and EMI integrations locally, launch with
`-PdryingRackIntegrationsRuntime=true`. Add `-PquickPlayWorld=<world-folder>` to
open a development world directly.

## Architecture at a glance

- `com.protyvkultury.revivalages.RevivalAges` is composition only.
- `core` contains shared NeoForge ports of cross-feature Athenaeum contracts.
- `feature` contains vertically sliced gameplay modules.
- `api` contains intentionally public extension contracts, not implementation.
- `client` is the physical-client boundary.
- `data` contains data generators only.
- `integration` isolates optional-mod compatibility.
- `network` owns payload definitions, registration, and handlers.
- `src/main/resources` contains hand-authored resources.
- `src/generated/resources` is datagen output and must not be hand-edited.

Read [AGENTS.md](AGENTS.md) before changing the project and
[docs/architecture.md](docs/architecture.md) before adding a new feature.
Primitive processing behavior, recipes, and configuration are documented in
[docs/primitive-technology.md](docs/primitive-technology.md).

## License

The project metadata currently uses `All Rights Reserved`. Pyrotech-derived
functional assets and Athenaeum-derived internal behavior are redistributed or
adapted under Apache-2.0; see
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) and
the license copies in [`licenses`](licenses).
