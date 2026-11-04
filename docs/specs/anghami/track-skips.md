# Unlimited track skips

- **Id:** `anghami.playback.track-skips`
- **Status:** draft
- **Default enabled:** yes
- **Risk:** medium
- **Category:** Playback

## Observable behaviour

Free accounts can skip a limited number of tracks per period, after which the
skip control is refused. After patching, the client no longer stops the user at
that limit.

## Preconditions

A signed-in account that is subject to the skip limit.

## Acceptance criteria

- [ ] Skipping past the previous limit continues to work.
- [ ] The skip control stays enabled and does not show a limit message.
- [ ] Previous and next still behave normally after many skips.
- [ ] No crash when the queue ends.

## Out of scope

Server-side enforcement, if any. The patch only removes the client-side gate.

## Open questions

- Is the count reported to the server, and does the server act on it? This must be
  answered before the patch is described as unlimited rather than as a removed
  client gate.

## Verification

1. Skip more tracks than the stock build allows.
2. Confirm each skip starts the next track.
3. Confirm the limit message never appears.
4. Confirm behaviour is still correct at the end of the queue.

## Provenance

- Spec author and date: Kero309x, 2026-11-04
- Derived code points: not yet derived. See `docs/provenance/LOG.md`.
