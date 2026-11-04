# Plus entitlement state

- **Id:** `anghami.entitlement.plus`
- **Status:** draft
- **Default enabled:** yes
- **Risk:** high
- **Category:** Account and entitlements

## Observable behaviour

Several screens and features check whether the account is a paying subscriber.
After patching, the client-side check reports a subscribed account, so features
that are gated only on that local check become available.

## Preconditions

Signed in. A feature that is gated on the server is out of reach regardless of
this patch.

## Acceptance criteria

- [ ] The client reports an active subscription wherever it derives that state
      locally.
- [ ] Features gated only by that local state become usable.
- [ ] Anything the server refuses still fails, with a normal error rather than a
      crash.

## Out of scope

Server-side entitlement, billing, and anything that would require forging a
purchase. This patch does not grant paid content that the server withholds.

## Open questions

- Which local checks must change, and which must be left alone so that the
  application does not enter an inconsistent state such as showing an active
  subscription next to a payment prompt?
- Should this remain disabled by default? Current decision: yes.

## Verification

1. Sign in with a free account.
2. Exercise every feature the spec lists as locally gated.
3. Confirm no crash and no inconsistent purchase prompt.
4. Confirm server-refused features still show their normal refusal.

## Provenance

- Spec author and date: Kero309x, 2026-11-04
- Derived code points: not yet derived. See `docs/provenance/LOG.md`.
