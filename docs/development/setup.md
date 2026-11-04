# Development setup

## Required

| Tool | Version | Why |
|---|---|---|
| JDK | 21 | Gradle and the framework plugin |
| Git | any current | version control |
| Gradle | via wrapper, once generated | builds |
| Android SDK, platform 36 | only when an extension module exists | Android library builds |

Node.js and Python are not required. The release workflow uses `gh`, which is
preinstalled on GitHub runners.

## Registry credentials

The framework Gradle plugin is published to GitHub Packages and requires an
authenticated read, even for a public repository.

Create a personal access token with `read:packages` only, then put it outside the
repository, in `%USERPROFILE%\.gradle\gradle.properties`:

```properties
gpr.user=<github-username>
gpr.key=<token>
```

Never put these in the repository, in a workflow file, or in a log. In CI they
come from the `MORPHE_REGISTRY_TOKEN` secret.

## First build

```bash
gradle wrapper --gradle-version <current>   # once, then commit the wrapper
./gradlew build
```

The wrapper is generated rather than copied, so the checksum in
`gradle/wrapper/gradle-wrapper.properties` is produced by the installed Gradle.

## Verifying against a stock build

The stock application is never committed and never uploaded. Point the build at a
local copy:

```bash
./gradlew verifyFingerprints -Papk="C:/path/to/stock-build.apkm"
```

This task is not implemented yet. It is the gate every patch must pass before it
is released: each fingerprint must match exactly the expected number of times in
the stock build.

## Deriving code points

Derivation is done with a general decompiler against the stock build. Record what
you used and what you concluded in `docs/provenance/LOG.md`. Do not consult
another patch collection while deriving.

## Useful paths

```
patches/                  bundle sources
docs/specs/<app>/         behaviour specs, one per patch
docs/provenance/LOG.md    derivation records
```
