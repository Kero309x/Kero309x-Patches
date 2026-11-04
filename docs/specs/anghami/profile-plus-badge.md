# Show profile Plus badge

- **Id:** `anghami.entitlement.profile-badge`
- **Status:** draft
- **Default enabled:** yes
- **Risk:** medium
- **Category:** Account and entitlements

## Observable behaviour

The profile screen and the account rows show a subscription badge. After
patching, the badge is shown even for an account without an active subscription.

## Preconditions

Signed in.

## Acceptance criteria

- [ ] The profile header shows the badge for a free account.
- [ ] The account settings row shows the badge consistently with the header.
- [ ] No other subscription-dependent feature becomes available.

## Out of scope

Feature entitlements. This behaviour is cosmetic and must not be relied on to
unlock anything; a separate spec covers entitlement state.

## Open questions

- Does the badge drive any downstream logic, such as hiding upgrade prompts that
  would otherwise be hidden for paying users? If so it must not be enabled by
  default together with the entitlement patch.

## Verification

1. Sign in with a free account.
2. Open the profile and the account settings.
3. Confirm the badge appears in both places.
4. Confirm downloads, lyrics and playback behave as they did before patching.

## Provenance

- Spec author and date: Kero309x, 2026-11-04
- Derived code points: not yet derived. See `docs/provenance/LOG.md`.
