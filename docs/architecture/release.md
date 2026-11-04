# Release process

Status: designed, not yet exercised. Nothing here has been run.

## Versioning

The bundle version lives in `gradle.properties` as `version`. A release is a tag
of the form `v<version>` on `main`. The version is bumped in its own commit
before the release commit, so the tag and the manifest always agree.

## What a release publishes

- `patches-<version>.mpp` — the patch bundle.
- `patches-bundle.json` — manifest read by the manager, including the public
  signing key when signing is enabled.
- `patches-list.json` — generated catalogue.

Generated files are never edited by hand. The build writes them, and CI fails if
a committed copy differs from a freshly generated one.

## Signing

Signing lets the manager refuse a release that was altered after publication.

1. `./gradlew generateSigningKey` produces `morphe-signing.key` and
   `morphe-signing.pub`.
2. `morphe-signing.pub` is committed. The private key never is; it is ignored by
   `.gitignore`.
3. The private key is stored as the repository secret `MORPHE_SIGNING_KEY`.
4. A backup is kept offline. Losing the key means every future release is
   rejected by clients that already trust the old one.

Signing is not enabled yet. It requires the toolchain, and the key must be
generated on a machine the maintainer controls.

## Workflow rules

- The release workflow runs on `main` only, plus manual dispatch.
- It does nothing when the tag already exists, so a documentation push cannot
  produce a duplicate release.
- `permissions` is `contents: write` and nothing else.
- `concurrency` prevents two releases at once.
- No scheduled runs, no polling, no workflow chaining.

## Registry access

The framework Gradle plugin is published to GitHub Packages and needs an
authenticated read. Repository secrets are not shared with other repositories, so
a personal access token with `read:packages` is stored as
`MORPHE_REGISTRY_TOKEN`. It is read-only and scoped as narrowly as GitHub allows.

## Rollback

A bad release is marked as a draft or pre-release rather than deleted, so that
clients which already downloaded it can be told what happened. Deleting a tag is
a last resort and requires a decision recorded in `docs/decisions/`.
