# Keep the password of each account

- **Id:** `vodafone.account.stored-password`
- **Status:** verified
- **Default enabled:** yes
- **Risk:** medium
- **Category:** Account and entitlements

## Observable behaviour

The stock build stores the password of every account, encrypted, in its own
database, and forgets it as soon as the account is signed out of or the
application is started again: a write that carries no password empties the
stored one, so the next time the account is opened the application asks for
the password again and the one the user typed is gone.

After patching, that write keeps the password that is already stored when it is
handed an empty one. A write that carries a password still writes it, so the
password the user types is stored as it always was, and it survives a sign out
and a restart of the application.

## Preconditions

None. The password is part of the account record the application keeps for
itself.

## Acceptance criteria

- [x] The password of an account is still in the database after the account is
      signed out of.
- [x] The password of an account is still in the database after the
      application is restarted.
- [x] The stored password is still encrypted; nothing about the storage
      changes.
- [x] A password the user types is still used for the login it belongs to, and
      is still stored for the next time.
- [ ] The application opens the account without asking for the password.
      Not met: the application only logs an account in without a prompt when
      the account is a seamless one, which is a rule of the application itself
      and not of the password that is stored.

## Out of scope

The rule of the application that decides whether an account is opened with a
prompt. The patch changes what is kept; it does not change the rule that
decides what the application does with it.

## Limitations

- The account is still shown as signed out after a sign out, and the
  application still asks for the password of an account that is not a seamless
  one. Keeping the password is what the patch does; making the application use
  it for those accounts is a different rule.
- The password is kept in the application's own encrypted database, with the
  application's own encryption. This patch does not change where that is or how
  it is encrypted.

## Open questions

- Where does the empty password come from? Answered for this build: the
  statement that writes the password of the account that is logged in is run
  with the password of the login that is being performed, and that password is
  empty for every login that does not ask for one, such as the silent login
  that follows a token refresh. The encryption keeps an empty password as it
  is, so the stored password really is emptied.
- Does the application read the stored password back? Answered for this build:
  the credential login reads the password of the account it is asked to open
  and uses it when it is there, and the account list is built from the stored
  records, so the password reaches the point where it is used. The application
  only takes that path for a seamless account.

## Verification

1. Install the patched build and sign in with a password.
2. Confirm the password is in the database, in encrypted form.
3. Sign out of the account and confirm the password is still there.
4. Restart the application and confirm the password is still there.
5. Disable the patch, rebuild and install, and confirm the stock behaviour
   returns: the password is gone after the sign out and after the restart.

Verified on a device on 2026-11-11: the stored password was 161 characters
after the sign in, 164 characters after the sign out and 164 characters after
the restart. Without the patch it was 0 characters after both.

## Provenance

- Spec author and date: Kero309x, 2026-11-10
- Derived: 2026-11-11 from the stock build. Derivation record:
  `docs/provenance/LOG.md`, section `vodafone.account.stored-password`.
