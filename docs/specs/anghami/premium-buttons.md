# Hide premium feature buttons

- **Id:** `anghami.interface.premium-buttons`
- **Status:** draft
- **Default enabled:** yes
- **Risk:** medium
- **Category:** Interface

## Observable behaviour

Inside playback and track menus, buttons advertise paid extras such as karaoke or
remixed playback. After patching, those buttons are hidden while the rest of the
menu is unchanged.

## Preconditions

None.

## Acceptance criteria

- [ ] The paid extra buttons are not shown in the player and in track menus.
- [ ] Menu spacing and ordering remain sensible.
- [ ] The remaining actions still work.
- [ ] The buttons do not reappear after rotation or after a configuration change.

## Out of scope

Making the paid extras usable.

## Open questions

- Are the same buttons reused on other screens, such as the now-playing sheet and
  the queue? Each occurrence must be listed before implementation.

## Verification

1. Open a track menu and the player.
2. Confirm the paid extra buttons are absent.
3. Rotate the device and reopen the menus.
4. Confirm the other actions still work.

## Provenance

- Spec author and date: Kero309x, 2026-11-04
- Derived code points: not yet derived. See `docs/provenance/LOG.md`.
