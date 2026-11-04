# Block audio ads

- **Id:** `anghami.ads.audio-ads`
- **Status:** draft
- **Default enabled:** yes
- **Risk:** low
- **Category:** Ads and promotion

## Observable behaviour

Free accounts hear short audio advertisements between tracks. After patching, a
track is followed by the next track instead of an advertisement, and the player
does not display an ad state while playing.

## Preconditions

Signed in with an account that is not entitled to ad-free playback.

## Acceptance criteria

- [ ] Playing an album end to end produces no audio advertisement.
- [ ] The now-playing surface never enters an advertisement state.
- [ ] Normal playback, seeking and queue transitions are unaffected.

## Out of scope

Server-side ad decisions and ad revenue accounting. The patch changes client
behaviour only and does not make requests appear as if they came from a paying
account.

## Open questions

- Which surfaces must also be covered so that no ad placeholder remains visible
  on the player, the queue and the mini player?

## Verification

1. Install the patched build on a device with a free account.
2. Play at least five consecutive tracks from an album and from a playlist.
3. Confirm no audio advertisement and no advertisement state appears.
4. Confirm the queue still advances and the next track starts automatically.

## Provenance

- Spec author and date: Kero309x, 2026-11-04
- Derived code points: not yet derived. See `docs/provenance/LOG.md`.
