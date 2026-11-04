# Remove sponsored content

- **Id:** `anghami.ads.sponsored-content`
- **Status:** draft
- **Default enabled:** yes
- **Risk:** low
- **Category:** Ads and promotion

## Observable behaviour

Browse surfaces can contain sponsored shelves, sponsored items inside a shelf,
and sponsored entries in the in-car and discovery views. After patching those
entries are absent, and the surrounding lists render normally without gaps.

## Preconditions

None.

## Acceptance criteria

- [ ] The home feed contains no sponsored shelf.
- [ ] A list that would have contained a sponsored item renders the remaining
      items without a blank row or a placeholder.
- [ ] The in-car view contains no sponsored entry.
- [ ] Ordinary recommendations still appear.

## Out of scope

Advertising identifiers and telemetry. Those are covered separately.

## Open questions

- Does the application paginate around a removed sponsored item, or does it
  expect a fixed count? This decides whether removal or filtering is correct.

## Verification

1. Open the home feed and scroll to the end.
2. Open a genre and a playlist.
3. Open the in-car view if the device supports it.
4. Confirm no sponsored entry and no layout gap.

## Provenance

- Spec author and date: Kero309x, 2026-11-04
- Derived code points: not yet derived. See `docs/provenance/LOG.md`.
