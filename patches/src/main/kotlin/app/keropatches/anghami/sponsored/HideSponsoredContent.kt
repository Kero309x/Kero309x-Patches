// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Kero309x

package app.keropatches.anghami.sponsored

import app.keropatches.anghami.AnghamiApp
import app.keropatches.core.PatchCategory
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.morphe.patcher.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

/**
 * The field a song carries to say the server supplied it as sponsored content.
 *
 * It is only ever read to decorate a song: the three list layouts use it to show
 * a sponsor label, and nothing in the client filters on it or opens a different
 * screen for it. That is why the patch edits the readers instead of the field:
 * the value arrives through a reflective JSON mapper, so there is no assignment
 * in the bytecode to rewrite.
 */
private const val SPONSORED_FIELD = "Lcom/anghami/ghost/pojo/Song;->isSponsored:Z"

/**
 * The song layouts, one fingerprint per shape.
 *
 * Each fingerprint names both the class and the concrete view holder type it
 * binds, because every layout also has two bridge methods with the same name and
 * a generic parameter. The parameter list is what tells them apart.
 */
private object SongCardFingerprint : Fingerprint(
    definingClass = "Lcom/anghami/model/adapter/SongCardModel;",
    name = "_bind",
    isStatic = false,
    returnType = "V",
    parameters = listOf("Lcom/anghami/model/adapter/base/CardModel\$SquareViewHolder;"),
)

private object SongRowFingerprint : Fingerprint(
    definingClass = "Lcom/anghami/model/adapter/SongRowModel;",
    name = "_bind",
    isStatic = false,
    returnType = "V",
    parameters = listOf("Lcom/anghami/model/adapter/base/RowModel\$RowViewHolder;"),
)

private object SongWideFingerprint : Fingerprint(
    definingClass = "Lcom/anghami/model/adapter/SongWideModel;",
    name = "_bind",
    isStatic = false,
    returnType = "V",
    parameters = listOf("Lcom/anghami/model/adapter/base/WideModel\$WideViewHolder;"),
)

/**
 * The two preferences that hold the sponsor artwork and caption of the in-car
 * screen.
 *
 * They are written by the server payload and read once, when the screen binds
 * them; an empty value is the documented "nothing to show" state, so answering
 * empty is enough and the screen hides the views itself.
 */
private object CarSponsorImageFingerprint : Fingerprint(
    definingClass = "Lcom/anghami/ghost/prefs/PreferenceHelper;",
    name = "getACRSponsoredImage",
    isStatic = false,
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
)

private object CarSponsorTextFingerprint : Fingerprint(
    definingClass = "Lcom/anghami/ghost/prefs/PreferenceHelper;",
    name = "getACRSponsoredText",
    isStatic = false,
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
)

/**
 * Hides sponsored content from every surface that draws it.
 *
 * Sponsored songs are ordinary songs that the server flags, so the client cannot
 * remove them from a feed it does not assemble; what it can do is stop drawing
 * the sponsorship. This patch does that at the exact reads that produce the
 * marker:
 *
 * - the compact row, which shows a "sponsored" caption next to the title;
 * - the square card, which passes the flag into the shared badge helper;
 * - the wide row, which shows the same caption;
 * - the in-car screen, which shows a sponsor image and caption supplied by the
 *   server.
 *
 * Each read is replaced with a constant false. Nothing else about the row
 * changes: the song still plays, still shows its title, artist and artwork, and
 * still appears wherever the server placed it.
 */
@Suppress("unused")
val hideSponsoredContentPatch = bytecodePatch(
    name = "Hide sponsored content",
    description = "Removes the sponsored label from song rows and cards, and the sponsor banner from the in-car screen.",
    default = true,
) {
    compatibleWith(AnghamiApp.compatibility)
    category(PatchCategory.ADS.label)

    execute {
        SongCardFingerprint.method.apply { ignoreSponsorship() }
        SongRowFingerprint.method.apply { ignoreSponsorship() }
        SongWideFingerprint.method.apply { ignoreSponsorship() }

        CarSponsorImageFingerprint.method.returnEarly("")
        CarSponsorTextFingerprint.method.returnEarly("")
    }
}

/**
 * Replaces every read of the sponsored field in this method with a constant false.
 *
 * The register the original read wrote to is reused, so the surrounding branch
 * still tests the same register and takes its "not sponsored" path without any
 * other edit. Reading the field is the only thing changed; the song object itself
 * keeps the flag, so nothing that inspects the model later behaves differently
 * from what the row now shows.
 */
private fun MutableMethod.ignoreSponsorship() {
    val indices = instructions.indices.filter { index ->
        val instruction = instructions[index]
        instruction.opcode == Opcode.IGET_BOOLEAN &&
            (instruction as? ReferenceInstruction)?.reference?.toString() == SPONSORED_FIELD
    }

    check(indices.isNotEmpty()) {
        "No sponsored read in $definingClass->$name$returnType; the layout changed."
    }

    indices.forEach { index ->
        val register = (instructions[index] as OneRegisterInstruction).registerA
        replaceInstruction(index, "const/4 v$register, 0x0")
    }
}
