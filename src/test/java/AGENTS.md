# Unit Test Rules

Automated unit tests are not part of normal feature work. Do not add, restore, or
modify a unit test unless the user explicitly requests test work in the current
task; bug fixes and Java changes do not imply that request.

When explicitly requested, add only deterministic coverage for the critical risk
named by the user: data loss or duplication, save migration/corruption, registry
identity, server authority or network validation, crashes, security-sensitive
permissions, or another explicit invariant. Mirror the production package, name
the class `<Subject>Test`, and test observable behavior. Do not add tests for
presentation, layout, colors, translations, routine configuration values,
ordinary calculations, or broad resource/dependency exhaustiveness unless the
user specifically requests them.
