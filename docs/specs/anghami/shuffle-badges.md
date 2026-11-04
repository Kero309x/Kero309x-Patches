# Hide shuffle badges

- **Id:** `anghami.interface.shuffle-badges`
- **Status:** draft
- **Default enabled:** yes
- **Risk:** medium
- **Category:** Interface

## Observable behaviour

Some list items carry a badge stating that playback will be shuffled. After
patching, those badges are not rendered, while the items themselves remain.

## Preconditions

A list that shows the badge in the stock build.

## Acceptance criteria

- [ ] The badge is absent on every list that showed it.
- [ ] Items remain visible and tappable.
- [ ] Row height and spacing look correct without the badge.
- [ ] No placeholder or empty icon is left behind.

## Out of scope

Changing whether playback is actually shuffled. That is a separate behaviour.

## Open questions

- Is the badge part of a shared item model used by several screens? If so, one
  change may cover all of them, and the spec must list which screens were checked.

## Verification

1. Visit every list that shows the badge on the stock build.
2. Confirm the badge is gone and rows render correctly.
3. Confirm tapping an item still starts playback.

## Provenance

- Spec author and date: Kero309x, 2026-11-04
- Derived code points: not yet derived. See `docs/provenance/LOG.md`.
