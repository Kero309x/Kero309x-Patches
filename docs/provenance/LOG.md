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

### How the application models a subscription — mapped 2026-11-04

Every behaviour in this bundle that depends on whether the user pays reaches that
answer through the same small class, so the map is recorded once here and referred
to from the individual specs.

| Fact | Value |
|---|---|
| Account state object | `Lcom/anghami/ghost/local/Account;`, a single instance field, handed out by `getAccountInstance()` |
| The decision that matters | `isPlusUser()Z` on that instance — true when the stored plan type is `PLUS`, or `FREE_TRIAL`, or when `isGoldUser()` is true |
| The static shorthand | `Account.isPlus()Z` — wraps `isPlusUser()` of the current instance through a compiler-generated callback object |
| Gold tier | `isGoldUser()Z` — stored plan type equals `GOLD`; `Account.isGold()Z` is its static wrapper |
| Persistence | the instance is read from an on-disk account file and written back by one write helper, so the stored plan type is a local copy of what the server sent |

**Why this matters for patch design:** changing `isPlusUser()` changes every
decision built on it at once, and there are dozens of call sites across playback,
downloads, lyrics, settings and the player. That is powerful and it is broad: a
single change would silently turn the entitlement, the badge and the lyrics gate
into one behaviour, which is exactly what this bundle avoids. The shared class is
therefore treated as a place to *read* state from, not as a shortcut to rewrite.

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

**Verification run:** 2026-11-04, `verifyPatches` against the stock base APK.
Result: the fingerprint matched exactly one method (`validate = true` enforces
that a fingerprint matches at most one method), and the patch applied with no
exception.

### `anghami.playback.track-skips` — derived 2026-11-04

**Consulted:** the stock 8.0.28 build only.

**How it was found:**

1. Read the fields of the account type that describe skipping: a skip allowance,
   a skip counter, the timestamp the counter was last reset, a minimum play time
   before a skip counts, and a separate radio allowance.
2. Searched every reference to the skip allowance across the whole application,
   then for the predicate that consumes it. One queue method was found.
3. Followed that predicate's callers: the private skip-recording method of the
   queue, the public no-argument wrapper of that method, and a list adapter that
   decorates song rows.
4. Read the branches each caller takes when the predicate is false.

**Derived facts:**

| Fact | Value |
|---|---|
| The gate | `Lcom/anghami/odin/playqueue/PlayQueue;.isSkipLogicEnabled(Lcom/anghami/ghost/local/Account;)Z` |
| Stock body | true when the queue's own "skip limit disabled" flag is false, the account is not a subscriber, and the skip allowance is positive |
| Effect of a false answer in the queue | the recording method takes its early "no restriction" branch: no counter increment, no limit message, the skip is accepted |
| Effect of a false answer in the UI | the adapter treats the row as unrestricted, so the remaining-skip hint is not added |
| Separate gate | the queue also has a no-argument `checkAndRecordSkipToSong()Z` that consults the same predicate, so the same answer covers it |
| Radio queue | the radio queue has no copy of this predicate; it reuses the same method through the base class |

**Decision:** answer the queue predicate, not the entitlement and not the account's
skip allowance field. The empty-queue and "not the current queue" protections live
outside this predicate and keep working, and the entitlement stays available to
the download and lyrics patches, which are separate behaviours.

**Verification run:** see the table at the end of this section.

### `anghami.media.lyrics-access` — derived 2026-11-04

**Consulted:** the stock 8.0.28 build only.

**How it was found:**

1. Searched the whole application for the account's lyrics members. The only
   instance predicate is `lyricsEnabled()Z`; there is also a write-through
   preference helper that clears a *local* lyrics mode, which is unrelated to
   entitlement.
2. Enumerated the callers of the predicate. Only one is a gate: the static entry
   point of the lyrics screen.
3. Read the entry point around the call: when the predicate is false it reports a
   failed-attempt analytics event and asks the host screen to open the
   subscription page; when it is true it continues to start the lyrics activity.

**Derived facts:**

| Fact | Value |
|---|---|
| The gate | `Lcom/anghami/ghost/local/Account;.lyricsEnabled()Z` |
| Stock body | subscriber, or the server-provided free-lyrics flag is set |
| Gate site | `Lcom/anghami/app/lyrics/LyricsActivity$a;.a(...)` — one read, one branch |
| Paywall action | `showSubscribeActivity("lyrics")` on the host screen |
| Unrelated member with a similar name | `PreferenceHelper.clearLyricsEnabled()`, which only resets a locally remembered mode at sign-out |

**Decision:** answer the account predicate, not the entitlement. The entitlement is
shared with downloads and skips, so changing it would implement three behaviours
at once. Replacing the first instruction of the predicate also removes the failed
attempt branch at the entry point, because that branch is only reached when the
answer is false.

**Verification run:** 2026-11-04, `verifyPatches` against the stock base APK. For
both patches above, the fingerprint matched exactly one method and the patch
applied with no exception.

### `anghami.privacy.analytics` — derived 2026-11-04

**Consulted:** the stock 8.0.28 build only.

**How it was found:**

1. Located the class the application reports through and listed every method on it
   whose body reaches a third-party SDK, by scanning each method for class
   references to the Amplitude, Adjust, Branch, Firebase and Facebook packages.
2. Checked which of those methods return a value, and whether any caller reads it.
3. Followed the bootstrap: which SDKs it starts, who calls it, and what the rest of
   the app reads from the fields it fills.
4. Enumerated the callers of the one non-void method the rest of the app uses, and
   read what it does when the SDK object was never created.

**Derived facts:**

| Fact | Value |
|---|---|
| Facade | `Lcom/anghami/ghost/analytics/Analytics;` |
| Bootstrap | `init(String, boolean, FacebookAppEventLogger)` — posts one runnable to the IO pool |
| What the runnable starts | Firebase Analytics, Amplitude, Adjust (with its app secret), Branch, and the Facebook logger if provided |
| Caller of the bootstrap | `Lcom/anghami/AnghamiApplication;` — one call, result ignored |
| Latch safety | the `CountDownLatch` used to wait for Branch is awaited only inside that same runnable, so nothing else waits for a start that never happens |
| Firebase field safety | the analytics field is null-checked immediately before its only read |
| Event sinks (void) | `postEvent` ×4, `postGoogleAnalyticsEvent`, `postBranchEvent`, `logBranchPurchaseEvent`, `postAdjustEvent` ×2, `postOpenLinkAdjustEvent` |
| Event builders | dozens of `post…Event` helpers, all of which end in one of the sinks above |
| Value-returning methods, deliberately untouched | `AmplitudeExperiment.getMinArtistNumberForOnBoarding(int)` — falls back to its argument when the client is null, one caller outside the package; `shouldSendEventAndRecordTimeStamp(String, long)` — a local rate limiter that sends nothing |
| Experiment bootstrap | `AmplitudeExperiment.init(String)`, called only from the facade's bootstrap |
| Crash reporting | travels through the same facade as an ordinary event; no Firebase Crashlytics entry point is called by app code |

**Decision:** silence the bootstrap and, independently, every void sink. Silencing
only the bootstrap would still leave a sink reachable if a future build moved the
bootstrap; silencing only the sinks would leave the SDKs running and uploading
whatever they collect themselves. The value-returning methods stay as they are,
because silencing them would change a decision — which artists to show — rather
than stop a report.

**Open limitation:** BugSnag is present in the build as a library with an internal
hook, but no app code calls it, so there is nothing in the app to silence for it.
The patch does not attempt to disable a library that the app does not drive.

**Verification run:** see the table at the end of this section.

## Not consulted

No source, comment, identifier, helper or fingerprint from any other patch
collection has been used to produce anything in this repository.
