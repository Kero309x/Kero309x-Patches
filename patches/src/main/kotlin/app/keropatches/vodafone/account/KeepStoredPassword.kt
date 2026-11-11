// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Kero309x

package app.keropatches.vodafone.account

import app.keropatches.core.PatchCategory
import app.keropatches.core.replaceStringConstant
import app.keropatches.vodafone.VodafoneApp
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.bytecodePatch

/**
 * The statement that writes the stored password of the account.
 *
 * The application keeps the password of an account in its own database, in
 * encrypted form, so that it can log the account in again without asking. The
 * write is a plain update statement that the data access layer builds. The
 * shrinker renamed the class the statement sits in, so the class and the method
 * together identify it.
 */
private object StoredPasswordStatementFingerprint : Fingerprint(
    definingClass = "Lo/includeOperationsIn\$1;",
    name = "createQuery",
    isStatic = false,
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
)

/**
 * Keeps the password the application already knows for an account.
 *
 * The application stores the password of every account, encrypted, in its own
 * database, and uses it to log the account in again when the account is
 * opened. The password reaches the stored record through a statement of the
 * data access layer that writes the password of the account that is currently
 * logged in.
 *
 * That write is what makes the application forget. A login that carries no
 * password of its own - one that was started by a link, by a token that was
 * refreshed, or by any flow that does not ask for one - writes an empty
 * password over the stored one. The encryption keeps an empty password as it
 * is, so the stored password really is emptied, and the next time the account
 * is opened the application asks for the password again.
 *
 * The patch makes that write keep the password that is already stored when it
 * is handed an empty one. A write that carries a password still writes it, so
 * the password the user types is stored as it always was, and the application
 * can still log the account in with it.
 *
 * The statement keeps its shape: it still updates the account that is logged
 * in, and it still writes a password when it is given one.
 */
@Suppress("unused")
val keepStoredPasswordPatch = bytecodePatch(
    name = "Keep the password of each account",
    description = "Stops the app forgetting the password of an account, so it stops asking for it every time.",
    default = true,
) {
    compatibleWith(VodafoneApp.compatibility)
    category(PatchCategory.ENTITLEMENT.label)

    execute {
        StoredPasswordStatementFingerprint.method.replaceStringConstant(
            "UPDATE UserInfoEntity SET password =? where isUserCurrentlyLoggedIn = 1",
            "UPDATE UserInfoEntity SET password =CASE WHEN ?1='' THEN password ELSE ?1 END " +
                "where isUserCurrentlyLoggedIn = 1",
        )
    }
}
