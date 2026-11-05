// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Kero309x

package app.keropatches.core

import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

/**
 * Makes this method see a boolean field as false without changing the field.
 *
 * Some flags are filled in reflectively from a server response, which means the
 * bytecode contains no store instruction to rewrite. What it does contain is the
 * read at each place that acts on the flag. Replacing a read with a constant
 * reuses the register the read wrote to, so the surrounding branch keeps testing
 * the same register and takes its "flag is off" path with no other edit.
 *
 * Only the reads in this method change. The object keeps its real value, so code
 * that persists it, compares it or sends it back is unaffected.
 *
 * @param field field reference in smali form, for example
 *   `Lcom/example/Thing;->isEnabled:Z`.
 */
internal fun MutableMethod.replaceBooleanFieldReadsWithFalse(field: String) {
    val indices = instructions.indices.filter { index ->
        val instruction = instructions[index]
        instruction.opcode == Opcode.IGET_BOOLEAN &&
            (instruction as? ReferenceInstruction)?.reference?.toString() == field
    }

    check(indices.isNotEmpty()) {
        "No read of $field in $definingClass->$name$returnType; the code changed."
    }

    indices.forEach { index ->
        val register = (instructions[index] as OneRegisterInstruction).registerA
        replaceInstruction(index, "const/4 v$register, 0x0")
    }
}
