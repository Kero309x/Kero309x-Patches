// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Kero309x

package app.keropatches.anghami.entitlement

import app.keropatches.anghami.AnghamiApp
import app.keropatches.core.PatchCategory
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.returnEarly

/**
 * The account's own derivation of "is this a paying subscriber?".
 *
 * The static `isPlus()` is a cached wrapper around this method, so answering here
 * covers both entry points. Every other entitlement answer either calls this or
 * compares the raw plan type, which keeps the change in one place.
 */
private object AccountPlanFingerprint : Fingerprint(
    definingClass = "Lcom/anghami/ghost/local/Account;",
    name = "isPlusUser",
    isStatic = false,
    returnType = "Z",
    parameters = emptyList(),
)

/**
 * Makes the client treat the signed-in account as a subscriber.
 *
 * The answer is derived locally: a plus plan, a free trial, or a gold plan. The
 * patch replaces the derivation with a constant, so every feature that asks the
 * client whether the account pays gets yes: download allowances, lyrics, queue
 * behaviour, upsell suppression and the rest.
 *
 * The plan type itself is not changed, and neither is the gold answer. That is
 * deliberate. The plan type is what the server sent and what the account screen
 * displays, and the gold answer drives a different tier's checks; rewriting
 * either would make the client claim a purchase that never happened. Features
 * that the server still refuses keep their normal refusal path.
 *
 * This patch overlaps several single-purpose patches on purpose. It is the broad
 * switch, and the narrow patches exist so a user can take one behaviour without
 * the rest.
 */
@Suppress("unused")
val plusEntitlementPatch = bytecodePatch(
    name = "Plus entitlement state",
    description = "Reports the account as a paying subscriber to every local check that derives that state.",
    default = true,
) {
    compatibleWith(AnghamiApp.compatibility)
    category(PatchCategory.ENTITLEMENT.label)

    execute {
        AccountPlanFingerprint.method.returnEarly(true)
    }
}
