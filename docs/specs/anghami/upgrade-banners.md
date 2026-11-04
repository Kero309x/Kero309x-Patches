# Hide upgrade banners

- **Id:** `anghami.store.upgrade-banners`
- **Status:** draft
- **Default enabled:** yes
- **Risk:** medium
- **Category:** Store and upsells

## Observable behaviour

Upgrade prompts appear as a tab, as banners inside the feed, and as cards in some
sections. After patching, those surfaces are not shown, while the rest of the
navigation is unchanged.

## Preconditions

Signed in with an account that is not subscribed.

## Acceptance criteria

- [ ] The upgrade tab or entry is not present.
- [ ] Feed banners advertising an upgrade are not rendered.
- [ ] Navigation still works and no empty section remains where a banner was.
- [ ] Deep links into the removed surfaces fail gracefully instead of crashing.

## Out of scope

The subscription screen itself, which the user may still reach deliberately.

## Open questions

- Deep links are matched against obfuscated identifiers; is suppressing the entry
  point enough, or must the destination also be guarded?

## Verification

1. Walk through every navigation entry.
2. Scroll the feed to the end.
3. Open every deep link the stock build exposes.
4. Confirm no upgrade surface appears and nothing crashes.

## Provenance

- Spec author and date: Kero309x, 2026-11-04
- Derived code points: not yet derived. See `docs/provenance/LOG.md`.
