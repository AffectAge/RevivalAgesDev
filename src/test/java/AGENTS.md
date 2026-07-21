# Unit Test Rules

- Unit tests cover pure Java domain rules, codecs with available test fixtures,
  validation, math, state transitions, and deterministic algorithms.
- Mirror production packages and name classes `<Subject>Test`.
- Use Arrange/Act/Assert structure without explanatory boilerplate comments.
- Test behavior and invariants, not private implementation details.
- Tests must be deterministic, isolated, and independent of execution order,
  network access, real time, user directories, or an already-running game.
- Do not mock large parts of Minecraft. Move pure rules behind a small domain
  boundary or use a GameTest when the platform behavior is essential.
- A bug fix includes a regression test when the failure is practical to reproduce.
