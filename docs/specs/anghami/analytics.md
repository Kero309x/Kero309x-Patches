# Disable analytics and crash reporting

- **Id:** `anghami.privacy.analytics`
- **Status:** draft
- **Default enabled:** yes
- **Risk:** high
- **Category:** Privacy

## Observable behaviour

The application sends usage events, campaign attribution and crash reports to
several third-party services. After patching, those calls become no-ops while the
rest of the application keeps working.

## Preconditions

None.

## Acceptance criteria

- [ ] No request reaches the analytics, attribution or crash reporting endpoints.
- [ ] The application starts normally and playback works.
- [ ] A deliberately triggered error does not crash the application because a
      reporting call returned nothing.
- [ ] No screen goes blank because a component depended on a reporting result.

## Out of scope

Advertising identifiers, which are part of the advertising behaviour, and any
network traffic the application needs for playback.

## Open questions

- Which of the reporting call sites return a value that the caller uses? Those
  cannot simply be made void, and need a neutral return value instead.
- Should this be disabled by default, given that it removes diagnostics the
  maintainer would want from bug reports?

## Verification

1. Run the patched build through a proxy and open every main screen.
2. Confirm the reporting endpoints receive nothing.
3. Trigger a handled error path and confirm no user-visible crash.
4. Confirm playback, downloads and search still work.

## Provenance

- Spec author and date: Kero309x, 2026-11-04
- Derived code points: not yet derived. See `docs/provenance/LOG.md`.
