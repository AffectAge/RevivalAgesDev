# Interaction and Parity Checklist

Use this checklist before changing any reference-derived interaction. Trace the
entire call path before implementing a local fix.

## Player interaction routing

- Check occupied main hand, occupied off hand, and both empty-hand paths.
- Check `useItemOn`, default block interaction, `useWithoutItem`, held-use start,
  held-use ticks, release, completion, cooldown, and item consumption.
- Decide explicitly whether a supported target consumes a completed use when its
  state change is a no-op.
- Store and revalidate the original target for timed interactions. Looking at a
  different block must cancel the action.
- Keep item insertion, fluid transfer, extraction, ignition, and GUI opening in a
  documented order so one path cannot accidentally invoke another.

## Stateful blocks

- Verify open, sealed, disabled, active, complete, invalid, rain-exposed, and
  unloaded/reloaded states.
- Block both manual and cached capability access when a state forbids access.
- Preserve contents during reload and configuration changes.
- Drop every item exactly once when broken in each state.

## Presentation and feedback

- Verify all orientations, item transforms, selection shapes, particles, light,
  sounds, action-bar messages, tooltips, models, and state textures.
- Treat model voids and functional visual layers as observable behavior.
- Test client rendering and dedicated-server class loading separately.

## Reusable contracts

Shared behavior belongs in a common contract before a second local copy is
introduced. Held igniters use `api/ignition/HeldIgniter` and
`api/ignition/HeldIgnitableBlock`; target blocks must route the igniter away from
their normal inventory interaction.

Future reference-derived work must review this checklist. If the required shared
contract does not exist, propose its scope first. After the user approves it,
implement the contract and update this checklist in the same change.
