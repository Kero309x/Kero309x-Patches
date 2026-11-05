// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Kero309x

package app.keropatches.anghami.downloads

import app.keropatches.anghami.AnghamiApp
import app.keropatches.core.PatchCategory
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.returnEarly

/**
 * The single check the download manager runs before it accepts a download.
 *
 * It throws a request exception for each reason the stock build can refuse a
 * download: no subscription, the offline allowance already reached, and the
 * restricted collection reasons that apply to limited plans. Every download path
 * in the manager funnels through it, so answering it in one place removes the
 * client-side refusals together.
 */
private object DownloadRestrictionsFingerprint : Fingerprint(
    definingClass = "Lcom/anghami/ghost/downloads/DownloadManager;",
    name = "assertDownloadRestrictions",
    isStatic = true,
    returnType = "V",
    parameters = listOf(
        "Lcom/anghami/ghost/local/Account;",
        "I",
        "Lcom/anghami/ghost/objectbox/models/downloads/SongDownloadReason;",
    ),
)

/**
 * The predicate the download manager uses for its own database-health flag.
 *
 * The patch only needs the class and name this method is declared on, which it
 * reads from the fingerprint match at patch time, so the call it writes stays
 * correct if the helper ever moves.
 */
private object DatabaseCorruptionFingerprint : Fingerprint(
    name = "isDatabasePotentiallyCorrupted",
    returnType = "Z",
    parameters = emptyList(),
    isStatic = false,
)

/**
 * Removes the client-side refusals that stop a download, but keeps the one
 * refusal that protects the stored library.
 *
 * The check is a validator, not the download itself: it returns normally when the
 * stock build would allow the request and throws otherwise. The patch therefore
 * writes its own ending rather than making the method return immediately:
 *
 * 1. If the local database is flagged as potentially corrupted, throw the stock
 *    exception with the stock reason, exactly as the stock build does, so the
 *    caller's existing handling and repair flow are unchanged.
 * 2. Otherwise return, which drops the plan check, the collection restriction and
 *    the offline count, while leaving the actual request, storage and later
 *    playback untouched.
 *
 * A request the server refuses still fails the way the stock build fails it; the
 * patch does not make a broken download appear to work.
 */
@Suppress("unused")
val expandDownloadLimitsPatch = bytecodePatch(
    name = "Lift download limits",
    description = "Stops the client refusing downloads because of the plan check, the collection restriction or the offline count.",
    default = true,
) {
    compatibleWith(AnghamiApp.compatibility)
    category(PatchCategory.ENTITLEMENT.label)

    execute {
        val corruptionCheck = DatabaseCorruptionFingerprint.match().originalMethod.let { method ->
            "${method.definingClass}->${method.name}()Z"
        }

        DownloadRestrictionsFingerprint.method.apply {
            // The injected code uses v0/v1, so it replaces the body instead of
            // inserting ahead of it: nothing that follows can depend on registers
            // the original code expected to be initialised by the removed checks.
            returnEarly()

            addInstructions(
                0,
                """
                    invoke-static {}, Lcom/anghami/ghost/prefs/PreferenceHelper;->getInstance()Lcom/anghami/ghost/prefs/PreferenceHelper;
                    move-result-object v0
                    invoke-virtual {v0}, $corruptionCheck
                    move-result v0
                    if-eqz v0, :keropatches_database_ok
                    new-instance v0, Lcom/anghami/ghost/downloads/DownloadRequestException;
                    sget-object v1, Lcom/anghami/ghost/downloads/DownloadRequestException${'$'}Reason;->DB_POTENTIALLY_CORRUPTED:Lcom/anghami/ghost/downloads/DownloadRequestException${'$'}Reason;
                    invoke-direct {v0, v1}, Lcom/anghami/ghost/downloads/DownloadRequestException;-><init>(Lcom/anghami/ghost/downloads/DownloadRequestException${'$'}Reason;)V
                    throw v0
                    :keropatches_database_ok
                    return-void
                """.trimIndent(),
            )
        }
    }
}
