// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Kero309x

package app.keropatches.registry

import app.keropatches.anghami.AnghamiApp
import app.keropatches.anghami.rating.disableRatingPromptPatch
import app.morphe.patcher.patch.Patch

/**
 * Every patch this bundle ships, grouped by the application it targets.
 *
 * The framework discovers patches by scanning public members of the bundle at
 * load time, so this registry is what our own tooling reads: the verification
 * task, the catalogue generator and the registry test. Keeping it explicit means
 * a patch that is written but not registered is caught by a test rather than by
 * a user who cannot find it.
 *
 * The order here is the order the manager shows. [Catalogue.entries] carries the
 * rest of the per-patch metadata.
 */
object PatchRegistry {
    /** Patches for the Anghami application. */
    val anghami: Set<Patch<*>> = setOf(
        disableRatingPromptPatch,
    )

    /** Every patch this bundle ships, across all applications. */
    val all: Set<Patch<*>> = anghami

    /** Application id a patch targets, or null when the registry does not own it. */
    fun applicationOf(patch: Patch<*>): String? = when (patch) {
        in anghami -> AnghamiApp.definition.id
        else -> null
    }
}
