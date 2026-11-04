# Hide Gold upsell

- **Id:** `anghami.store.gold-upsell`
- **Status:** draft
- **Default enabled:** yes
- **Risk:** medium
- **Category:** Store and upsells

## Observable behaviour

The application advertises a higher tier across several surfaces and marks
accounts with a tier state. After patching, the tier is not advertised and the
account is not treated as being on that tier.

## Preconditions

Signed in.

## Acceptance criteria

- [ ] No surface advertises the higher tier.
- [ ] The account is never reported as being on that tier locally.
- [ ] The ordinary subscription screen is unaffected.
- [ ] Nothing else changes colour, layout or navigation because the tier is
      absent.

## Out of scope

Billing. The patch does not change prices or purchase flows.

## Open questions

- Should this be merged with the upgrade banner behaviour, or kept separate so a
  user can hide one and keep the other?

## Verification

1. Open every store and account surface.
2. Confirm no higher-tier advertisement.
3. Open the ordinary subscription screen and confirm it still renders.
4. Confirm profile and playback are unchanged.

## Provenance

- Spec author and date: Kero309x, 2026-11-04
- Derived code points: not yet derived. See `docs/provenance/LOG.md`.
