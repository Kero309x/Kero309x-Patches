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

## Status

All five specs are `derived` and implemented. The first release for this
application stopped at the two behaviours that decide whether a repackaged build
runs at all. The later three cover what the application reports, what a push
message can make it do to the installation, and the promotional card it draws.

`derived` means the behaviour has a spec, a record of how it was derived and an
implementation that builds and applies to the stock APK. It does not mean it has
been checked on a device: no test device is available to this project yet, so
every spec still carries unchecked acceptance criteria.

## Scope of this release

The stock build was examined for the behaviours users usually ask about, and the
examination found nothing to patch behind them:

- no advertising SDK is bundled, so there is no ad surface to remove;
- the application does not set `FLAG_SECURE`, so screenshots already work;
- there is no root or debugger detection beyond crash-reporter heuristics;
- there is no Play Integrity attestation; the `IntegrityManager` in the build is
  the application's own device-integrity helper, not a store gate.

No patch is shipped for a behaviour the stock application does not have. The
promotional card is the only advertisement the application draws itself: it is
offered by the server and belongs to the operator, so it is filed as promotion
rather than as advertising.

## Not included

Behaviours that exist in other collections but are absent from this project are
not listed here on purpose. If they are wanted, they are new work: a new spec,
new derivation, new implementation.
