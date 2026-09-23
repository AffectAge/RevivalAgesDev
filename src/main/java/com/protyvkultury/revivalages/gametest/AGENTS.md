# GameTest Rules

This subtree is reserved for GameTests that the user explicitly requests in the
current task. Do not add, restore, or modify GameTests, templates, profiles, or
GameTest run configurations by default.

When explicitly requested, cover only the named critical in-world risk: data loss
or duplication, world/save corruption, registry identity, server-authoritative
state, network validation, crashes, or another invariant the user identifies.
Keep tests deterministic, isolated, and minimal; use the `revivalages` namespace
and a diagnostic failure message. Do not create coverage for routine gameplay,
visual presentation, recipe layouts, orientations, configuration matrices, or
enabled/disabled profiles unless the user expressly includes them.
