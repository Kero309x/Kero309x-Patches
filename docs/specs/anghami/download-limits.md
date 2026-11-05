# Expand download limits

- **Id:** `anghami.downloads.limits`
- **Status:** derived
- **Default enabled:** yes
- **Risk:** medium
- **Category:** Account and entitlements

## Observable behaviour

The application restricts how many tracks can be kept offline, and refuses some
downloads for accounts on a limited plan. After patching, the client no longer
enforces those local limits, so downloads that the client itself would have
allowed proceed.

## Preconditions

A signed-in account that is allowed to download at all.

## Acceptance criteria

- [ ] The download counter does not block a new download when the previous limit
      was reached.
- [ ] A track that was refused only because of a client-side limit downloads.
- [ ] Already downloaded content remains playable offline.
- [ ] Deleting a download still works.

## Out of scope

Anything the server refuses. If the server rejects a download request, the patch
does not fabricate a successful download, and it must not be described as doing
so.

## Open questions

- Which of the limits are enforced from a locally cached plan object, and which
  come from a server response that must not be overridden? Answered for the local
  limits: the plan check, the limited-plan collection restriction and the offline
  count are all read from the locally cached account and the local object box.
  Whether the server independently refuses a download has not been observed.

## Known limitation

The stock validator also refuses every download when the local database is flagged
as potentially corrupted. That branch protects the stored library rather than
limiting the plan, so the patch re-implements it in its own instructions instead of
dropping it.

## Verification

1. Download until the previous limit is reached.
2. Attempt one more download and confirm it starts and completes.
3. Go offline and play the newly downloaded track.
4. Confirm the library still lists the download correctly.

## Provenance

- Spec author and date: Kero309x, 2026-11-04
- Derived code points: not yet derived. See `docs/provenance/LOG.md`.
