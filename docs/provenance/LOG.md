# Provenance log

Every derived fact about a target application is recorded here, together with how
it was obtained. The point is that a reviewer can retrace the work without
assuming anything was carried over from another project.

## Environment record

| Date | Item | Value |
|---|---|---|
| 2026-11-04 | Stock build used | `com.anghami` 8.0.28, version code 8000280 |
| 2026-11-04 | Build container | APKM bundle, universal variant, seven DEX files in the base APK |
| 2026-11-04 | Minimum SDK declared by the build | 23 |
| 2026-11-04 | Signing certificate subject | `O=Anghami, C=LB` |
| 2026-11-04 | Signing certificate SHA-256 | `5e5d1b3609403e1db445d142ef36433a2ad2008030281d437d9e632c01c45659` |

## Derivation records

| Date | Behaviour | Consulted | Derived | Verified |
|---|---|---|---|---|
| 2026-11-04 | platform metadata | stock APKM `info.json` and `META-INF/BNDLTOOL.RSA` | package name, version name, version code, minimum SDK, signing certificate digest | certificate digest recomputed from the DER certificate |

> The certificate digest above was read from the v1 JAR signature of the base
> APK. Before release it must be confirmed against the v2/v3 signing block with
> `apksigner verify --print-certs`, so that the value enforced at install time is
> the one recorded here.

## Behaviour derivations

### `anghami.system.rating-prompt` — derived 2026-11-04

**Consulted:** the stock 8.0.28 build only, decompiled locally with apktool
3.0.3 and `--no-res`. No other patch collection was opened.

**How it was found:**

1. Listed the libraries bundled in the build and noticed the Play in-app review
   library is present.
2. Searched, across the whole application, for code that references that
   library. Two synthetic classes matched.
3. One of them logs the tag `AppRater`, which led to the package the
   application uses for rating.
4. Enumerated that package and inspected its members.

**Derived facts:**

| Fact | Value |
|---|---|
| Rating controller | `Lcom/anghami/ghost/rating/AppRater;`, a singleton |
| Decision predicate | `getShouldShowDialog()Z` — true only when online, past a reset window, past a minimum time since first launch, past a minimum launch count, not already rated for this version, and past a minimum event count |
| Call sites of the predicate | exactly one outside the class: the rating-event handler in the main activity |
| Store-presence gate | `Lcom/anghami/util/c;->g(Landroidx/fragment/app/n;)Z`, true when the store package is installed |
| Two presentation paths | a custom dialog when the store is absent, the Play in-app review sheet when it is present |

**Decision:** change the predicate, not the dialog and not the review call. One
change then covers both presentation paths, and the dialog code and the review
library stay untouched. This also avoids depending on the two synthetic classes,
which are compiler-generated and unstable.

**Verification run:** not yet. Pending a working build.

## Not consulted

No source, comment, identifier, helper or fingerprint from any other patch
collection has been used to produce anything in this repository.
