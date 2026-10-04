# Process Rules

`ProcessRule` is the shared runtime and presentation contract for conditions
that affect an in-progress machine operation. It deliberately does not model
inputs, output capacity, or automation permissions: those are runtime blockers
rather than permanent recipe prerequisites.

Each rule has one of three kinds:

- **Gate**: processing pauses, or resets when its configured policy is
  `reset_progress`.
- **Modifier**: processing continues and contributes a multiplicative speed
  factor.
- **Hazard**: processing retains rule-specific state and may fail or transform
  the operation according to the owning recipe.

Recipes which support rules expose an ordered `process_rules` array. For
example, a heated soaking recipe uses:

```json
"process_rules": [{ "type": "lit_block_below" }]
```

The current built-in rule types are `lit_block_below`, `open_sky`,
`weather_exposure`, `drying_environment`, `sealed_machine`, `installed_tool`,
`fuelled_and_lit`, `valid_structure`, and `required_manual_tool`.
`random_outcome` is presentation-only: it references a
recipe's existing chance and alternate results; it never participates in server
recipe validation or machine processing.

The server evaluates rules and owns persistent hazard counters. JEI and EMI read
the same ordered presentation model; Jade evaluates the same machine rule and
shows its current blocker or state. Presentation integrations do not implement
recipe validity.

The shared `process_rules.png` atlas is 256x256 pixels. Each rule occupies one
16x16 cell at the coordinates in `ProcessRulePresentation`; occupied cells are
solid, distinct placeholder colors, and unused cells remain transparent for
future icons. JEI and EMI use the same atlas dimensions and cell coordinates.

Recipe viewers render an actual cycling ingredient slot when a recipe requires a
specific manual tool. Its tooltip describes the recipe-specific interaction;
the generic `required_manual_tool` icon remains a condition rather than a
substitute for the accepted tool set.

Soaking recipes use only `process_rules`; obsolete recipe fields are rejected
during data loading.
