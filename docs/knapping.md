# Knapping

## Scope

Knapping is a server-authoritative 5×5 item interaction for material splitters,
flint, clay, leather, and horn materials. Its type definitions are supplied by the synchronized
`revivalages:knapping_type` data-pack registry; recipes use the
`revivalages:knapping` recipe type.

Using a matching main-hand stack opens a fully filled grid. Every click or drag
sends only the requested cell index and menu ID. The server verifies the active
menu, source item, count, type, cell bounds, and current cell state before
changing the grid and resolving recipes from `RecipeManager`.

Patterns may be one to five cells wide and high, declare the required state
outside their bounds, shift horizontally and vertically, and mirror
horizontally. Any non-space pattern character denotes an enabled cell. Type data
owns consume timing, click sound, visual flags, particles, and viewer icon.
Horn results use the instrument component declared by the matched recipe.

The built-in stone Knapping type accepts the `revivalages:knapping_splitters`
item tag and flint. Its 5×5 material grid uses the actual material texture of
each built-in splitter; an externally added tag member safely uses the stone
fallback. The menu background, grid, output slot, and player inventory share
one coordinate contract.

The server synchronizes the complete 25-bit grid through a dedicated payload;
vanilla 16-bit menu data is not used. After the player takes a result, every
cell is disabled. Starting another attempt requires reopening the screen.
Closing a completed screen returns the result and performs any completion-time
material consumption exactly once.

When Knapping is disabled, its registry, menu, payload, and recipe serializer
remain registered. Opening is blocked, the category is hidden, and the affected
two-dimensional fallback recipes load instead.
The complete lifecycle follows the shared
[content availability contract](content-availability.md).

## Optional integration assessment

| Integration | Status | Notes |
| --- | --- | --- |
| KubeJS | applicable | The typed schema writes normal `revivalages:knapping` JSON through the canonical codec. It compiles against file `7143884`; an in-game script smoke test is still pending. |
| Jade | not applicable | Knapping is item-based and has no inspectable world block or entity. |
| EMI | applicable | Compact pattern bounds and material texture cycling present recipes from `RecipeManager`. EMI 1.1.24 was client-smoke-tested with Jade and JEI. |
| JEI | applicable | A fixed centered 5×5 presentation presents the same recipe IDs from `RecipeManager`. JEI 19.39.0.369 was client-smoke-tested with EMI and Jade. |
| Curios | not applicable | Knapping does not equip or persist accessories. |
| Progressive Stages | applicable | Stable recipe IDs provide the progression boundary. |
| Biomes O' Plenty | not applicable | Input interoperability is item/tag driven. |
| Serene Seasons | not applicable | No seasonal or climate state participates. |
| Ecliptic Seasons | not applicable | No seasonal or climate state participates. |

Viewer adapters may differ in layout, but recipe enumeration, validation,
matching, consume timing, and outputs remain feature-owned.
The same client run completed resource reload with both viewers present, and the
base dedicated GameTest run passed all 15 tests with every optional JAR absent.

## Configuration

`revivalages-knapping-server.toml` contains `knapping.enabled=true`.
`revivalages-knapping-client.toml` contains `knapping.screenParticles=true`.
The server enable setting is restart-required because it selects knapping or
fallback recipe data.
