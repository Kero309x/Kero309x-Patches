// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Kero309x

package app.keropatches.registry

import app.keropatches.core.AppDefinition
import app.morphe.patcher.patch.Patch

/**
 * Metadata for one patch, as our tooling needs it.
 *
 * The framework exposes a patch's name, description and default on the patch
 * itself, but it has no idea which behaviour spec a patch implements. That link
 * is project knowledge, so it is declared once per patch in [Catalogue] and is
 * checked against the behaviour index and the source tree by the tests.
 */
data class PatchEntry(
    /** The patch object the framework will load. */
    val patch: Patch<*>,
    /** Application id, matching the key in the behaviour index. */
    val application: String,
    /** Behaviour id, for example `anghami.system.rating-prompt`. */
    val behaviourId: String,
    /** Repository-relative path of the behaviour spec. */
    val specPath: String,
    /** Path of the source file, relative to this module, that declares the patch. */
    val sourceFile: String,
    /** Other patch entries this patch needs to be selected with. */
    val dependsOn: List<String> = emptyList(),
)

/**
 * The explicit, ordered list of patches this bundle ships.
 *
 * This is the single description of "what the bundle contains" that the build,
 * the catalogue generator and the tests all read, so the three cannot disagree.
 */
object Catalogue {
    val entries: List<PatchEntry> = listOf(
        PatchEntry(
            patch = app.keropatches.anghami.skips.unlimitedSkipsPatch,
            application = "anghami",
            behaviourId = "anghami.playback.track-skips",
            specPath = "docs/specs/anghami/track-skips.md",
            sourceFile = "src/main/kotlin/app/keropatches/anghami/skips/UnlimitedSkips.kt",
        ),
        PatchEntry(
            patch = app.keropatches.anghami.lyrics.fullLyricsAccessPatch,
            application = "anghami",
            behaviourId = "anghami.media.lyrics-access",
            specPath = "docs/specs/anghami/lyrics-access.md",
            sourceFile = "src/main/kotlin/app/keropatches/anghami/lyrics/UnlockLyrics.kt",
        ),
        PatchEntry(
            patch = app.keropatches.anghami.downloads.expandDownloadLimitsPatch,
            application = "anghami",
            behaviourId = "anghami.downloads.limits",
            specPath = "docs/specs/anghami/download-limits.md",
            sourceFile = "src/main/kotlin/app/keropatches/anghami/downloads/ExpandDownloadLimits.kt",
        ),
        PatchEntry(
            patch = app.keropatches.anghami.analytics.disableAnalyticsPatch,
            application = "anghami",
            behaviourId = "anghami.privacy.analytics",
            specPath = "docs/specs/anghami/analytics.md",
            sourceFile = "src/main/kotlin/app/keropatches/anghami/analytics/DisableAnalytics.kt",
        ),
        PatchEntry(
            patch = app.keropatches.anghami.rating.disableRatingPromptPatch,
            application = "anghami",
            behaviourId = "anghami.system.rating-prompt",
            specPath = "docs/specs/anghami/rating-prompt.md",
            sourceFile = "src/main/kotlin/app/keropatches/anghami/rating/DisableRatingPrompt.kt",
        ),
    )

    /** Applications this bundle announces, in registry order. */
    val applications: List<AppDefinition> = AppRegistry.applications

    fun forApplication(application: String): List<PatchEntry> =
        entries.filter { it.application == application }
}
