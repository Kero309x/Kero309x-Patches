# Release process

Status: `v0.1.0` was published as a pre-release from a local build. The automated
release workflow has run but has not produced a release yet. The sections below
distinguish what has been exercised from what is still intended.

## Published so far

| Version | Date | Status | Asset |
|---|---|---|---|
| 0.1.0 | 2026-11-09 | pre-release, published manually | `Kero309x-Patches-0.1.0.mpp` |

The attached bundle was built from the tagged commit and its SHA-256 is recorded
in the release notes.

## Versioning

The bundle version lives in `gradle.properties` as `version`. A release is a tag
of the form `v<version>` on `main`. The version is bumped in its own commit
before the release commit, so the tag and the manifest always agree.

## What a release publishes

- `Kero309x-Patches-<version>.mpp` — the patch bundle, built at
  `patches/build/libs/patches-<version>.mpp` and renamed when it is attached to
  the release.
- Release notes on the tag page. They summarise the change and the verification
  status; they do not replace the behaviour specs.

The framework plugin writes `META-INF/MANIFEST.MF` inside the bundle from the
`patches` block in `patches/build.gradle.kts`. The `.mpp` is a build artefact and
is never edited by hand. There are no separate JSON catalogue or manifest files.

## Signing

Signing lets the manager refuse a release that was altered after publication.

It is not enabled yet: no key has been generated, no public key is committed, and
`MORPHE_SIGNING_KEY` is not set. The steps below are the intended process.

1. `./gradlew generateSigningKey` produces `morphe-signing.key` and
   `morphe-signing.pub`.
2. `morphe-signing.pub` is committed. The private key never is; it is ignored by
   `.gitignore`.
3. The private key is stored as the repository secret `MORPHE_SIGNING_KEY`.
4. A backup is kept offline. Losing the key means every future release is
   rejected by clients that already trust the old one.

Signing must be enabled on a machine the maintainer controls before it can be
used for a release.

## Workflow rules

- The release workflow runs on `main` only, plus manual dispatch.
- It does nothing when the tag already exists, so a documentation push cannot
  produce a duplicate release.
- `permissions` is `contents: write` and nothing else.
- `concurrency` prevents two releases at once.
- Versions starting with `0.` are published as pre-releases.
- No scheduled runs, no polling, no workflow chaining.

## Registry access

The framework Gradle plugin is published to GitHub Packages and needs an
authenticated read. Repository secrets are not shared with other repositories, so
a personal access token with `read:packages` must be stored as
`MORPHE_REGISTRY_TOKEN`. It must be read-only and scoped as narrowly as GitHub
allows. The same name must also exist in the Dependabot secret store, because
Dependabot-triggered runs cannot read repository secrets.

Both workflows check the secret before they configure Gradle, and fail with an
explicit message when it is empty instead of the plugin-resolution error a missing
credential otherwise produces. A local build uses the credentials in the
maintainer's Gradle properties instead.

## Rollback

A bad release is marked as a draft or pre-release rather than deleted, so that
clients which already downloaded it can be told what happened. Deleting a tag is
a last resort and requires a decision recorded in `docs/decisions/`.
