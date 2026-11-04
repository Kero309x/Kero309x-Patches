# Allow screenshots

- **Id:** `anghami.system.screenshots`
- **Status:** draft
- **Default enabled:** yes
- **Risk:** medium
- **Category:** Interface

## Observable behaviour

Some screens mark their window as secure, which makes screenshots and screen
recording produce a black image. After patching, those screens can be captured.

## Preconditions

A screen that is marked secure in the stock build.

## Acceptance criteria

- [ ] A screenshot of the previously blocked screen contains the expected content.
- [ ] Screen recording shows the same.
- [ ] The application does not crash when it is captured.
- [ ] Screens that are not marked secure are unchanged.

## Out of scope

Protecting content in any other way, and any change to what the application
displays.

## Open questions

- Which screens are marked secure, and is the flag set once for the whole
  application or per screen? This determines whether one change is enough.

## Verification

1. Open each secure screen.
2. Take a screenshot and a short recording.
3. Confirm the content appears instead of a black frame.
4. Confirm app switching and rotation still work.

## Provenance

- Spec author and date: Kero309x, 2026-11-04
- Derived code points: not yet derived. See `docs/provenance/LOG.md`.
