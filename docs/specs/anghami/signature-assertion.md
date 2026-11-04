# Stock application signature assertion

- **Id:** `anghami.integrity.signature-assertion`
- **Status:** draft
- **Default enabled:** yes
- **Risk:** high
- **Category:** Account and entitlements

## Observable behaviour

Requests carry a value derived from the signature of the installed application,
so a repackaged build can be distinguished from the store build. After patching,
the derived value matches the stock application even though the installed
application is repackaged.

## Preconditions

The patched application is installed under a different signing key than the
stock build.

## Acceptance criteria

- [ ] The value sent matches the stock build's value for the same request.
- [ ] The value is produced locally, without a network round trip.
- [ ] No other request field is altered.

## Out of scope

Server-side verification and anything that requires knowing a server secret.
This behaviour affects a server-facing identity claim and must be documented as
such; it must never be described as harmless local UI work.

## Open questions

- Should this be enabled by default at all? It is the only behaviour in the
  bundle that asserts an identity claim to a remote service. Current proposal:
  disabled by default.
- Which account-level restrictions depend on the same identity claim, so that
  enabling it does not produce a partially trusted session?

## Verification

1. Capture the request from the stock build and from the patched build.
2. Compare the value, and confirm every other header is unchanged.
3. Confirm the session works normally for at least one full app restart.

## Provenance

- Spec author and date: Kero309x, 2026-11-04
- Derived code points: not yet derived. See `docs/provenance/LOG.md`.
