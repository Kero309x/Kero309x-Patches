# Remove the saved account limit

- **Id:** `vodafone.account.account-limit`
- **Status:** verified
- **Default enabled:** yes
- **Risk:** low
- **Category:** Account and entitlements

## Observable behaviour

The stock build keeps at most five accounts, or six when the first of them is a
seamless one. Once the list is that long the application refuses to switch to
another account and shows a screen that says too many accounts are saved.
After patching, the list has no limit: every account that is saved can be
opened, and new accounts can be added.

## Preconditions

None. The limit is checked in the application itself, against the list of
accounts it has saved.

## Acceptance criteria

- [x] The screen that says too many accounts are saved is not shown.
- [x] A new account can be added when five accounts are saved: the button
      that adds another account opens the screen that asks for the number.
- [ ] A sixth account can be added and used. Not checked on a device: the
      accounts need a number of their own and a sign in of their own.
- [ ] Switching to any saved account works, however many accounts are saved.
- [ ] The accounts behave as before: they are logged in, switched and removed
      the same way.
- [ ] Turning the patch off restores the limit.

## Out of scope

How many accounts the server accepts and what each account may do. The patch
removes the check in the application; it does not change what the server
answers.

## Limitations

- The accounts are stored the way they always were. The patch only removes the
  check, so nothing about how an account is remembered, logged in or removed
  changes.
- The list of accounts is still what the application has saved. A very long
  list is shown in the same list as before.
- The text that says how many accounts can be saved is a text of the resources
  and still says five. The patch removes the check, not the text.

## Open questions

- Is the limit enforced anywhere else? Answered for this build: the limit is
  counted in three places, the overlay that lists the accounts, the overlay
  that asks which account to switch to, and the screen that shows the accounts
  with the button that adds another account. All three are changed. A count of
  every comparison against the number five in the whole application was made
  afterwards and found no fourth one that belongs to the account list; the
  other comparisons belong to the strength of a password and to the order of
  an enum.
- Does the operator refuse the sixth account? Not answered. The message that
  says too many accounts are saved is shown in two ways. When the application
  finds the list full it builds the error itself and shows the message. When
  the operator refuses a sign-in, the application shows the operator's own
  answer. The wording of the message is the operator's in both cases: the
  error code is `-1234566` and the text for that code comes from the content
  the operator sent. The check that decides whether the message appears is the
  application's; the wording is not.

## Verification

1. Install the patched build and sign in.
2. Add accounts until there are more than five, and confirm that none of the
   steps refuses and that the "too many accounts" screen never appears.
3. Switch between the accounts and confirm that each one opens.
4. Disable the patch, rebuild and install, and confirm the limit returns.

Verified on a device on 2026-11-11 with five accounts saved: before the third
of the three checks was changed, the button that adds another account showed
the "Maximum Accounts Reached" message; with it, the button opens the screen
that asks for the number.

## Provenance

- Spec author and date: Kero309x, 2026-11-10
- Derived: 2026-11-10 from the stock build. Derivation record:
  `docs/provenance/LOG.md`, section `vodafone.account.account-limit`.
