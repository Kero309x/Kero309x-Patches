# Kero309x Patches

An independent patch collection for the Morphe patching framework, maintained by
Kero309x.

> **Status: pre-alpha.** The architecture, the behaviour specifications and the
> build scaffolding exist. No patch has been implemented or verified yet, and no
> release has been published. There is nothing to install at this time.

## What this is

A bundle of patches applied to the stock build of a supported application,
producing a repackaged build with a modified behaviour set. The bundle is used
with Morphe Manager, or with any tool that consumes a Morphe patch bundle.

## Independence

This project is not a fork, a mirror, or a rebranding of another patch
collection.

- The behaviour of each patch is specified in `docs/specs/` before any code is
  written, from the observable behaviour of the stock application.
- Every code point a patch touches is derived from the stock build, and the
  derivation is recorded in `docs/provenance/LOG.md`.
- No source, comment, identifier, helper, fingerprint or folder structure is
  taken from another patch project.
- No other patch project is a build dependency.

The framework itself is used through its published contracts only. See
`NOTICE` for its licence terms and for the naming restriction that applies to
derivative works.

## Supported applications

| Application | Package | Version | File type |
|---|---|---|---|
| Anghami | `com.anghami` | 8.0.28 (8000280) | APKM |

Behaviour specifications: [`docs/specs/anghami/`](docs/specs/anghami/README.md).

Behaviours that other collections implement but this one does not are not
carried over. Adding one is new work: a spec, a derivation, an implementation,
and a verification run.

## Repository layout

```
patches/                    patch bundle sources
  src/main/kotlin/app/keropatches/
    core/                   application model, categories, shared rules
    registry/               the list of supported applications
    <application>/          compatibility data and patches, per application
docs/
  architecture/             how the bundle is put together
  decisions/                architecture decision records
  provenance/               clean-room protocol and derivation log
  specs/                    behaviour specifications, per application
.github/workflows/          build and release
```

## Building locally

Requirements:

- JDK 21
- A GitHub personal access token with `read:packages`, because the framework
  plugin is published to GitHub Packages.

Provide the registry credentials outside the repository, for example in
`~/.gradle/gradle.properties`:

```properties
gpr.user=your-github-username
gpr.key=your-token
```

Then:

```bash
./gradlew build
```

The Gradle wrapper is generated once with `gradle wrapper --gradle-version <v>`
and committed afterwards. It is not present yet, because the toolchain is not
installed on the development machine.

## Verifying a patch

Fingerprints are validated against a stock build that must be provided locally.
The build never downloads, uploads or redistributes it:

```bash
./gradlew verifyFingerprints -Papk="path/to/stock-build.apkm"
```

This task is not implemented yet; it is the acceptance gate for every patch.

## Catalogue

The table below is generated from the bundle. Do not edit it by hand.

<!-- PATCHES_START -->
_No release yet. The catalogue is generated when the first bundle is built._
<!-- PATCHES_END -->

## Development

- [Development setup](docs/development/setup.md)
- [Architecture](docs/architecture/overview.md)
- [Clean-room protocol](docs/provenance/CLEAN_ROOM.md)
- [Decision records](docs/decisions/)
- [Contributing](CONTRIBUTING.md)

## Licence

GPL-3.0-only. See [`LICENSE`](LICENSE) and [`NOTICE`](NOTICE).

## Not affiliated

Not affiliated with, endorsed by, or sponsored by any application vendor, nor by
the Morphe project. Application names and icons belong to their owners and are
used only to describe which stock build a patch targets.
