# Disable forced shuffle

- **Id:** `anghami.playback.forced-shuffle`
- **Status:** draft
- **Default enabled:** yes
- **Risk:** medium
- **Category:** Playback

## Observable behaviour

For some sources, such as free on-demand playback, the application turns shuffle
on and refuses to turn it off. After patching, shuffle starts in the state the
user chose and stays there.

## Preconditions

A source that the stock build forces into shuffle.

## Acceptance criteria

- [ ] Playback starts in order when the user has shuffle off.
- [ ] Turning shuffle on and off again works repeatedly.
- [ ] The queue advances in the expected order.
- [ ] Manually selecting a track still plays that track.

## Out of scope

Recommendations. The patch does not change what the queue contains, only whether
it is shuffled.

## Open questions

- Are there sources where shuffle is a licensing condition rather than a client
  restriction? Those must be listed explicitly if the answer is yes.

## Verification

1. Start an album with shuffle off.
2. Confirm tracks play in album order.
3. Toggle shuffle several times and confirm the toggle sticks.
4. Select a track from the middle of the queue and confirm it plays.

## Provenance

- Spec author and date: Kero309x, 2026-11-04
- Derived code points: not yet derived. See `docs/provenance/LOG.md`.
