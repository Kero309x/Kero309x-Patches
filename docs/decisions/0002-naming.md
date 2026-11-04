# 0002 — Names, coordinates and repository layout

- **Status:** accepted
- **Date:** 2026-11-04

## Context

The bundle needs a name that does not imply authorship by the framework project,
and coordinates that do not collide with the previous repository.

## Decision

| Item | Value |
|---|---|
| Display name | Kero309x Patches |
| Repository | `Kero309x/KeroPatches` |
| Maven group | `app.keropatches` |
| Kotlin package root | `app.keropatches` |
| Extension namespace | `app.keropatches.extension.<app>` |

Gradle modules: one `patches` module, plus an extension module per application
that needs runtime code.

## Rationale

- The display name is derived from the maintainer, which the framework's notice
  explicitly recommends when a neutral name would be ambiguous.
- `Kero309x-Patches` cannot be used: repository names are compared
  case-insensitively and the previous repository already occupies that name.
- `app.keropatches` contains no framework or upstream identifier, so no part of
  the artefact suggests it originates elsewhere.

## Consequences

- The bundle can be installed alongside other bundles without coordinate
  collisions.
- Renaming the repository or the group later requires a coordinated change to
  the manifest, the README and any published links.
