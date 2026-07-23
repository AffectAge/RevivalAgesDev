# GameTest Rules

- Use GameTests for behavior requiring a level, blocks, entities, inventories,
  ticking, redstone, recipes, or client/server synchronization boundaries.
- Keep templates minimal and store them under
  `data/revivalages/structure/gametest`.
- Use the `revivalages` namespace explicitly and group tests by feature.
- Every test must reach success or fail with a diagnostic message. Avoid timing
  assumptions broader than the mechanic actually requires.
- Reset or contain state so tests are order-independent and repeatable.
- Prefer an empty structure for logic that does not need a prepared scene.
- Pure algorithms and codecs belong in fast unit tests instead.
- For configurable content, test an enabled launch and a disabled launch. The
  disabled case must prove that the feature's blocks, items, recipes, creative-tab
  entries, payloads, and worldgen contributions are absent.
