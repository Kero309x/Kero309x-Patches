# Full lyrics access

- **Id:** `anghami.media.lyrics-access`
- **Status:** draft
- **Default enabled:** yes
- **Risk:** high
- **Category:** Media and lyrics

## Observable behaviour

Time-synced lyrics are offered for some tracks and withheld for others behind a
subscription prompt. After patching, the lyrics screen opens for a track that
previously showed the prompt, and the synced lines follow playback.

## Preconditions

The track has lyrics available at all.

## Acceptance criteria

- [ ] The lyrics screen opens instead of a paywall prompt.
- [ ] Synced lines advance with playback.
- [ ] A track without lyrics shows the normal empty state rather than a crash.
- [ ] Seeking moves the highlighted line correctly.

## Out of scope

Fetching lyrics that the server does not serve. The patch does not substitute a
third-party lyrics provider.

## Open questions

- Is the refusal a client-side flag, a server response, or both? Only the client
  part may be changed.
- Does the lyrics request depend on a token that the patch would have to
  fabricate? If so this behaviour cannot be implemented independently and must be
  reported as a limitation rather than guessed at.

## Verification

1. Open a track that shows the subscription prompt on the stock build.
2. Confirm the lyrics screen opens and lines advance.
3. Seek within the track and confirm the highlighted line follows.
4. Open a track with no lyrics and confirm a clean empty state.

## Provenance

- Spec author and date: Kero309x, 2026-11-04
- Derived code points: not yet derived. See `docs/provenance/LOG.md`.
