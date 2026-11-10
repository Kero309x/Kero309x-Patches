# Ana Vodafone behaviour specs

Target application: `com.emeint.android.myservices`, version 2026.11.1 (version
code 1246), distributed as an APKM bundle.

| # | Behaviour | Spec | Default | Risk |
|---|---|---|---|---|
| 1 | Skip the store licence check | [license-check.md](license-check.md) | yes | high |
| 2 | Disable the forced update requirement | [forced-update.md](forced-update.md) | yes | medium |
| 3 | Disable usage tracking | [tracking.md](tracking.md) | yes | low |
| 4 | Ignore the server's remote wipe and forced logout | [remote-actions.md](remote-actions.md) | yes | medium |
| 5 | Hide the promotional card | [ad-space.md](ad-space.md) | yes | low |
| 6 | Remove the saved account limit | [account-limit.md](account-limit.md) | yes | low |
| 7 | Disable certificate pinning | [certificate-pinning.md](certificate-pinning.md) | yes | medium |
| 8 | Keep the password of each account | [stored-password.md](stored-password.md) | yes | medium |
| 9 | Allow screenshots | [screenshots.md](screenshots.md) | yes | low |

## Status

All nine specs are `derived` and implemented. The first release for this
application stopped at the two behaviours that decide whether a repackaged build
runs at all. The later ones cover what the application reports, what a push
message can make it do to the installation, the promotional card it draws, how
many accounts it keeps, the certificate it pins, the password it remembers and
the screens it hides from screenshots.

`derived` means the behaviour has a spec, a record of how it was derived and an
implementation that builds and applies to the stock APK. It does not mean it has
been checked on a device: no test device is available to this project yet, so
every spec still carries unchecked acceptance criteria.

## Scope of this release

The stock build was examined for the behaviours users usually ask about, and the
examination found nothing to patch behind them:

- no advertising SDK is bundled, so there is no ad surface to remove;
- the application hides its screens from screenshots through a security library
  it embeds, which is what the screenshot behaviour changes;
- the device checks that library carries, for root, for a developer build, for
  an emulator and for a hooking framework, are never started in this build;
- there is no Play Integrity attestation; the `IntegrityManager` in the build is
  the application's own device-integrity helper, not a store gate.

No patch is shipped for a behaviour the stock application does not have. The
promotional card is the only advertisement the application draws itself: it is
offered by the server and belongs to the operator, so it is filed as promotion
rather than as advertising.

## What a device run showed

The bundle was run on a device with the stock application installed, with the
patched build mounted over it.

- The patched build starts and runs without a crash.
- The licence check of the stock build runs at startup; the patched build does
  not run it.
- The push of the stock build works and the push of the patched build works too:
  the mount leaves the signature of the stock build in place, so the services
  that are bound to it keep working.
- The application shows a blank splash on that device, with the stock build as
  well. The tokens of the installed account had expired a few days earlier and
  the application waits for a new login that never starts, so the splash never
  ends. This is the behaviour of the stock build and not something the patches
  cause. The application needs a fresh sign in on that device.
- The window of the running application carries no secure flag, with and
  without the patches, which is what the screenshot behaviour records.

## Not included

Behaviours that exist in other collections but are absent from this project are
not listed here on purpose. If they are wanted, they are new work: a new spec,
new derivation, new implementation.
