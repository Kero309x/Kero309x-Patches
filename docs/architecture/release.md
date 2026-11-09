# Release process

Status: `v0.1.0` was published from a local build; `v0.1.1` is the first release
produced by the automated workflow. The sections below distinguish what has been
exercised from what is still intended.

## Published so far

| Version | Date | Status | Asset |
|---|---|---|---|
| 0.1.0 | 2026-11-09 | pre-release, published manually | `Kero309x-Patches-0.1.0.mpp` |
| 0.1.1 | 2026-11-09 | pre-release, published by the release workflow | `Kero309x-Patches-0.1.1.mpp` |

Each bundle is built from the tagged commit.

- `0.1.0` — `BD74E5B95BADD671DB0C4678D874EE7456750A688FCB0849FEFACFD4F102B644`
- `0.1.1` — `849971EF03A28F5767DCCE5FCBC6428D5D64626C6309615D486554377CED9FE4`

The same hash is repeated in the release notes.

## Versioning

The bundle version lives in `gradle.properties` as `version`. A release is a tag
of the form `v<version>` on `main`. The version is bumped in its own commit
before the release commit, so the tag and the manifest always agree.

## What a release publishes

- `Kero309x-Patches-<version>.mpp` — the patch bundle, built by `buildAndroid`
  at `patches/build/libs/patches-<version>.mpp` and renamed when it is attached to
  the release. `buildAndroid` adds `classes.dex`, which Morphe Manager needs; the
  plain `build` output has no DEX and only serves Morphe Desktop.
- Release notes on the tag page. They summarise the change and the verification
  status; they do not replace the behaviour specs.

The framework plugin writes `META-INF/MANIFEST.MF` inside the bundle from the
`patches` block in `patches/build.gradle.kts`. The `.mpp` is a build artefact and
is never edited by hand.

`patches-bundle.json` at the repository root is the patch source metadata that
Morphe Manager reads from `main`. The release workflow rewrites it after each
release with the version, the release time, the published notes, the asset URL
and an empty signature URL. `public_key` is added only when the build was signed.
It is generated, not edited by hand.

## Signing

Signing lets the manager refuse a release that was altered after publication.

The bundle is signed with an Ed25519 key. The signature is appended inside the
`.mpp` itself, in the `MorpheSig1` trailer after the zip end record, so a release
publishes no separate signature file. `patches-bundle.json` carries the matching
`public_key` instead.

Current state:

- The key pair was generated on 2026-11-09. `morphe-signing.pub` is committed;
  `morphe-signing.key` is ignored by `.gitignore` and stays on the maintainer's
  machine.
- `MORPHE_SIGNING_KEY` is not configured yet, so `0.1.1` is unsigned. The release
  workflow only writes `public_key` into the metadata when that secret is present,
  so an unsigned build is never described as signed.
- Releases `0.1.0` and `0.1.1` were built by `build` and carry no `classes.dex`.
  They work with Morphe Desktop, not with Morphe Manager.
- The private key must be backed up offline before it is used for a release.
  Losing it means every future release is rejected by clients that already trust
  the old key; generating a new pair means every client must re-add the source.

The steps, for the record:

1. `./gradlew :patches:generateSigningKey --no-configuration-cache` produces
   `morphe-signing.key` and `morphe-signing.pub`. The task captures the project,
   so it fails when the configuration cache is on.
2. `morphe-signing.pub` is committed. The private key never is.
3. The Base64 contents of `morphe-signing.key` are stored as the repository
   secret `MORPHE_SIGNING_KEY`. No Dependabot secret is needed; only the release
   workflow signs.
4. `buildAndroid` reads the secret and appends the signature. Without the secret
   the build still succeeds and the bundle is unsigned.

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
