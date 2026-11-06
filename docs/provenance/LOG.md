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

### `anghami.ads.sponsored-content` — derived 2026-11-04

**Consulted:** the stock 8.0.28 build only.

**How it was found:**

1. Listed every field whose name contains "sponsored" and every method that reads
   one, across the whole application.
2. Separated the model that carries the flag from the views that draw it, and read
   each view.
3. Checked whether anything filters on the flag, or reacts to it on a click, before
   deciding that removing the marker is the whole of the client behaviour.

**Derived facts:**

| Fact | Value |
|---|---|
| The flag | `Lcom/anghami/ghost/pojo/Song;->isSponsored:Z`, serialised from the JSON key `sponsored` |
| How it is populated | reflective JSON mapping — there is no assignment to the field anywhere in the bytecode, and no mapper calls the protobuf getter either |
| Readers | `SongCardModel._bind(SquareViewHolder)` — passed to the shared badge helper; `SongRowModel._bind(RowViewHolder)` — caption visibility; `SongWideModel._bind(WideViewHolder)` — caption visibility |
| Non-readers | no click handler, no filter, no sort, no playback decision |
| Section flag | `Section;->sponsored:Z` is copied from the response and written to a parcel, but no view reads it |
| In-car sponsor | `PreferenceHelper.getACRSponsoredImage()/getACRSponsoredText()`, read once by `com.anghami.acr.ACRActivity`; an empty value is its documented "nothing to show" state |

**Decision:** replace the four reads that draw a marker with a constant false, and
answer the two in-car getters with an empty string. Editing assignments was not an
option: the flag arrives through a reflective mapper, so the bytecode has no store
to change. Removing the songs is not attempted, because the client never does that
itself and pretending otherwise would describe a behaviour the patch cannot keep.

**Verification run:** see the table at the end of this section.

### `anghami.ads.promotional-popups` — derived 2026-11-04

**Consulted:** the stock 8.0.28 build only.

**How it was found:**

1. Found the two classes whose stock source names are the promotion surfaces: the
   popup ad component and the ad popup handler.
2. Read the one method on each that starts a draw, and confirmed both take the
   server-provided ad model rather than a generic UI model.
3. Enumerated every caller of both methods, to be sure that patching them does not
   suppress anything that is not an ad.
4. Checked what reads the handler's visibility query, to be sure nothing waits for
   a dialog to be dismissed.

**Derived facts:**

| Fact | Value |
|---|---|
| Popup window surface | `Lcom/anghami/ui/popupwindow/x;`, source `PopupAdShower.kt`, created once in `AnghamiApplication` |
| Popup start method | `x.i(Lcom/anghami/ui/popupwindow/a;)V`, called from the main screen and from two base-screen helpers |
| Full-screen surface | `Lp4/d;`, source `AdsPopupHandler.java`, one instance per main screen |
| Flyer start method | `p4.d.a(Lcom/anghami/odin/ads/e;)V`, called on an ad event and on resume |
| Visibility query | `p4.d.b()Z` — reads the dialog, returns false when there is none; no caller waits on it |
| Non-ad users of the popup component | none: its only model parameter is the popup ad model |

**Decision:** answer both start methods immediately. They are the only two places
that put a promotion on top of the user, and neither is shared with a non-ad
surface. The ad pipeline itself is untouched, so the patch cannot leave the client
in a state where an ad is half-built.

**Verification run:** see the table at the end of this section.

### `anghami.ads.audio-ads` — derived 2026-11-05

**Consulted:** the stock 8.0.28 build only.

**How it was found:**

1. Located the playback core by tracing which class constructs the audio ad
   player, then listed every method in that class whose name mentions ads.
2. Read the branch that runs when a track ends, and separated "is an ad allowed
   here" from "play the ad that was chosen".
3. Confirmed the ad is only ever started from that branch, by listing every
   caller of the start method, and confirmed the second caller is the
   back-to-back step that runs after an ad has already started.

**Derived facts:**

| Fact | Value |
|---|---|
| Playback core | `Lcom/anghami/odin/core/S;`, source `OdinPlayer.java` |
| Ad decision | `S.L0()Z` — the single "does an ad belong after this track" answer |
| What it reads | the queue's `getDisableAds()`, the server's `AdSettings.getNoAd(currentSong)`, `adFrequency` or `adVideoFrequency` depending on video mode, and the player's own offline flag |
| Where it is used | once when a track ends, and once when the reported item duration is calculated |
| Start method | `S.w1(Lcom/anghami/odin/ads/A;)V` — the only place an ad is handed to a player |
| Callers of the start method | the track-end branch, gated by `L0()`; and `S.b1()`, the back-to-back step, which only runs while an ad is already playing |
| Ad pipeline | separate from the decision: loaders and players are built regardless, so skipping the decision leaves no partial state |

**Decision:** answer `L0()` with false. The stock code already has a branch for
"no ad here" — it is the branch taken when the server sends no ad, when the queue
disables ads or when the song is exempt — and it advances the queue normally.
Returning false therefore reuses existing behaviour instead of suppressing a call
and hoping the caller recovers. The ad pipeline is left intact on purpose: it
still fetches and caches its models, and a player handed an ad from elsewhere
would still work.

**Verification run:** see the table at the end of this section.

### `anghami.playback.forced-shuffle` — derived 2026-11-05

**Consulted:** the stock 8.0.28 build only.

**How it was found:**

1. Started from the player, not from the list: read how a queue is obtained when a
   list is started, and found that the presenter asks one private predicate
   before it decides what kind of queue to build.
2. Listed every caller of that predicate across all seven DEX files. There are
   exactly two, both in the same presenter: the page play button and the
   play-one-song action.
3. Read both branches. The true branch builds a radio queue; the false branch
   builds a queue over the list it was given. Followed the radio path into the
   queue constructors to see where the queue's own play mode comes from.
4. Checked whether the server marks anything else that would make the false
   branch behave like the true one, by looking at how a section's play mode is
   consumed when a queue is filled.

**Derived facts:**

| Fact | Value |
|---|---|
| The predicate | `Lcom/anghami/app/base/list_fragment/c;->shouldPlayRadio(Lcom/anghami/ghost/pojo/Song;Lcom/anghami/ghost/pojo/section/Section;)Z` |
| Callers | two, both in `c`: the page play button path and `getPlayQueueFromSection` |
| Stock inputs | `Account.isPlus()Z`; the first song's `playMode`; the section's `playMode` |
| Stock answer | for non-Plus: true when either play mode is `shuffle`; for Plus: true only when the section's play mode is `shuffle` |
| True branch | `buildRadioQueue(...)`, which builds a `Radio` of type `SONGLIST` over the joined song ids and a queue whose play mode is `shuffle` |
| False branch | `buildRelatedPlayQueue(...)` when the page allows it, otherwise a plain queue over the same list, starting at the chosen song |
| Why shuffle cannot be turned off | the queue is a radio whose own play mode is `shuffle`; the player's toggle does not describe the list the user opened |
| Effect of a plain queue | the section play mode `shuffle` is not mapped onto the queue flag; only `infinite` is, so the queue is genuinely in list order |

**Decision:** inject a guard at the start of the predicate that answers false for
accounts without an active plan and otherwise falls through to the untouched
stock body. The Plus case is preserved deliberately: for Plus accounts the same
play mode marks content that is meant to be a radio, and removing it would take
away a feature rather than a restriction. The patch also does not touch the
queue's shuffle control, so the user keeps a working toggle over the plain queue.

**Known limitation:** the false branch does not expand the list into a radio, so a
free account hears the songs the list contains and not the extra recommendations
the radio queue would have fetched. This is the client-side part of the change.

**Verification run:** see the table at the end of this section.

### `anghami.interface.shuffle-badges` — derived 2026-11-05

**Consulted:** the stock 8.0.28 build only.

**How it was found:**

1. Located the model field whose name describes shuffle state, then listed every
   write and every read of it across all seven DEX files.
2. Confirmed the field is filled in reflectively: the only writes are a copy
   constructor, a remote-update copy and a parcel write, so there is no mapper
   assignment to rewrite and the value has to be handled at the read sites.
3. Grouped the reads by the method that contains them, and read each method. Three
   groups draw something: a subtitle leading drawable on card layouts, an image
   span inside row subtitle text, and a dedicated badge view in headers and one
   carousel layout.
4. Checked the remaining reads to make sure they are not drawing decisions:
   persistence, remote update, diff flags, section flattening and objectbox
   cursors all only carry or compare the value.

**Derived facts:**

| Fact | Value |
|---|---|
| The flag | `Lcom/anghami/ghost/pojo/PossiblyGenericModel;->isShuffleMode:Z` |
| How it is populated | reflective JSON mapping; no assignment in the bytecode |
| Card surfaces | `AlbumCardModel`, `PlaylistCardModel` and `LinkCardModel`, each in `getSubtitleStartingDrawable()`, returning the shuffle glyph resource or `-1` |
| Row surfaces | `AlbumRowModel` and `PlaylistRowModel` in `getSubtitleText()`, and both `LinkModel.setSubtitleView` overloads, which prepend an image span |
| Dedicated badge views | `LinkNewCardModel._bind(HeaderLinkHolder)` and `StoreCarouselSubModel._bind(StoreSubViewHolder)`, both toggling a view's visibility |
| Header predicate | `getHasShuffleBadge()Z` declared on `BaseHeaderModel` and overridden by `AlbumHeaderModel` and `PlaylistHeaderModel` |
| Non-visual reads left alone | `PossiblyGenericModel.updateFromRemote`, `writeToParcel`, `BaseModel.fillChangeFlags`, `t4.f` and `P5.h` section flattening, `StoredPlaylist.updateMetadataFromRemote`, objectbox cursors |

**Decision:** replace only the drawing reads with a constant false, and answer the
three header predicates with false. The field keeps its value, so persistence,
diffing and queue building are untouched. Every read site that produces a badge
was enumerated, so the patch asserts that at least one read exists in each method
it edits and fails loudly if the layout changes.

**Verification run:** see the table at the end of this section.

### `anghami.system.screenshots` — derived 2026-11-06

**Consulted:** the stock 8.0.28 build only.

**How it was found:**

1. Searched the application packages for the numeric window flag that blocks
   capture, rather than for a method name. The only place that adds it to a window
   is one method on the shared activity base, and the other hits are unrelated
   bits with the same value.
2. Read that method. It asks a single boolean, applies the flag when the answer is
   true, and clears it when the answer is false; the flag is stored on the window
   itself, not on the activity.
3. Listed every declaration of that boolean across all seven DEX files. There are
   four: the base, which answers false, and three overrides, which answer true.
4. Checked that no other code path adds the flag, so the four declarations are the
   whole of the behaviour.

**Derived facts:**

| Fact | Value |
|---|---|
| The question | `shouldUseSecureMode()Z` |
| Base declaration | `Lcom/anghami/app/base/o;`, answers false |
| Overrides that answer true | `com/anghami/app/main/MainActivity`, `com/anghami/app/stories/live_radio/LiveRadioFullscreenVideoActivity`, `com/anghami/player/video/VideoPlayerActivity` |
| The consequence | `refreshSecureMode()V` on the same base adds `0x2000` (`FLAG_SECURE`) to the window when the answer is true, and clears it otherwise |
| Extra condition on the flag | the flag is applied only while a playback service object exists and reports a state the base treats as requiring protection; the clear path runs regardless |
| Other users of `0x2000` in the app packages | none for window flags; the remaining hits are text and system-UI flags |

**Decision:** answer false on all four declarations. The three overrides are the
real change; the base is included so the patch does not depend on the base
happening to answer false in this build. The method that applies the flag is left
alone so an already-marked window is still cleared.

**Verification run:** see the table at the end of this section.

### `anghami.interface.premium-buttons` — derived 2026-11-06

**Consulted:** the stock 8.0.28 build only.

**How it was found:**

1. Listed the account's boolean answers whose names mention karaoke, then read
   each one. Three describe the feature itself; one is a server-sent attribute
   that only says whether to advertise it.
2. Listed every reader of that attribute across the application. There are two.
3. Read both. One sets the visibility of a view in the full player screen, guarded
   by the track's own karaoke marker; the other passes the answer as a flag into a
   row holder in the player's queue list.
4. Checked that no other surface reads the track's karaoke marker in the player,
   model or UI packages, and searched for a separately advertised remix button.
   There is none in this build.

**Derived facts:**

| Fact | Value |
|---|---|
| The answer | `Lcom/anghami/ghost/local/Account;->isShowKaraokeUpsellButton()Z` |
| What it reads | a boolean attribute sent with the account, not a computed plan check |
| Reader one | `com/anghami/player/ui/l`, which shows a view when the track's `hasKaraoke` is true and this answer is true |
| Reader two | `y8/a`, the player's queue adapter, which passes the answer into `F8/X` when it creates a row holder |
| Feature answers left alone | `isShowKaraoke()Z` and `isCanUseKaraoke()Z` |
| Remix teaser | none in the player or queue of this build |

**Decision:** answer false on the getter. Both surfaces already have a path for
"do not draw the button", and neither needs its layout touched. The feature
answers stay as they are, so an account that legitimately has karaoke keeps its
entry point and only loses the advertisement.

**Verification run:** see the table at the end of this section.

## Not consulted

No source, comment, identifier, helper or fingerprint from any other patch
collection has been used to produce anything in this repository.
