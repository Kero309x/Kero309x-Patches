# Block promotional popups

- **Id:** `anghami.ads.promotional-popups`
- **Status:** draft
- **Default enabled:** yes
- **Risk:** low
- **Category:** Ads and promotion

## Observable behaviour

The application sometimes shows a full-screen promotional overlay on launch or
while browsing. After patching, those overlays do not appear and the user lands
directly on the screen they asked for.

## Preconditions

None beyond a normal signed-in session.

## Acceptance criteria

- [ ] Cold starts do not show a promotional overlay.
- [ ] Returning to the application from the background does not show one.
- [ ] Navigating between main sections does not show one.
- [ ] First-run onboarding and permission prompts still work.

## Out of scope

Promotions rendered as ordinary cards inside a feed or a shelf. Those are covered
by the sponsored content spec.

## Open questions

- Are there promotions delivered as a full-screen route rather than as a dialog?
  If so, they need their own criterion.

## Verification

1. Start the patched application cold five times.
2. Background and foreground it five times.
3. Walk through every main section.
4. Confirm no promotional overlay appears, and onboarding still runs on a fresh
   install.

## Provenance

- Spec author and date: Kero309x, 2026-11-04
- Derived code points: not yet derived. See `docs/provenance/LOG.md`.
