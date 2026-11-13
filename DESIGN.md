# DESIGN.md

The visual language of the Droidspaces Android app. The C backend has no UI, so none of this
applies to `src/`.

The app was rebuilt around one idea: it is a workstation console that happens to run on a phone.
Dense, quiet, and legible at a glance, with a single signal colour reserved for state and action.
The old flat-bubble language, 20dp cards, and all-mono type are gone. This document describes
what replaced it, and the tokens that enforce it.

## How to use this file

**Components and tokens come first, values second.** Shared pieces live in:

- `ui/design/DsTokens.kt`: spacing, radii, sizes, motion, and the compact/wide breakpoints.
- `ui/design/DsComponents.kt`: `DsPageHeader`, `DsCard`, `DsSectionHeader`, `DsBadge`,
  `DsActionButton`, `DsIconAction`, `DsKeyValueRow`, `DsMetric`, `DsSearchField`,
  `DsFilterChip`, `DsEmptyState`, `DsLoadingState`, `DsErrorState`, `DsActionStrip`.
- `ui/design/DsCharts.kt`: `DsRing`.
- `ui/design/DsNav.kt`: `DsBottomDock`, `DsNavRail`, `DsDestination`.
- `ui/theme/`: the colour schemes, typography scale, and Material shapes.

If a shared component fits, use it and stop reading. If nothing fits, build from the tokens here
so the new thing belongs. Never hardcode a hex colour, a radius, or an odd spacing value.

## Identity

- Dark first. The default palette is **Graphite**: near-black neutral surfaces with a lime signal
  colour. A light scheme exists and keeps the same hierarchy.
- One accent that means something. Lime is used for running, primary actions, focus, and live
  telemetry. It is not used for decoration.
- Cool steel is the secondary, amber the tertiary and warning. Running is never a green card: the
  card stays neutral and a 2dp top accent plus a status badge carry the state.
- Square-ish panels, compact controls. Cards are 12dp, controls are 8dp, overlays are 16dp.
- Mono only for machine output. Numbers, paths, unit names, addresses, logs and pids use
  JetBrains Mono. Every label, title and paragraph uses the platform sans.

## Colour

All roles come from `MaterialTheme.colorScheme`. Only `ui/theme/Theme.kt` and
`ui/theme/Color.kt` may name a colour literally.

| Surface | Role |
| --- | --- |
| Screen | `background` |
| Panel or card | `surfaceContainer` |
| Nested control, toolbar button | `surfaceContainerHigh` |
| Field and chip track | `surfaceContainerHigh` |
| Dialog and sheet | `surfaceContainer` |

| Text | Role |
| --- | --- |
| Primary | `onSurface` |
| Supporting and metadata | `onSurfaceVariant` |
| Disabled | `onSurface` at `0.38f` |
| Disabled fill | `onSurface` at `0.08f` |
| Borders | `outlineVariant` at `0.65f` |
| Dividers | `outlineVariant` at `0.55f` |
| Focus | `primary` |

State vocabulary:

| State | Accent |
| --- | --- |
| Running, start, healthy | `primary` |
| Stopped, failed, destructive | `error` |
| Warning, caveat | `tertiary` |
| In progress, downloading | `tertiary` |
| Idle, unknown | `onSurfaceVariant` |

The init system legend in `ui/screen/InitServiceScreen.kt` keeps its hand-picked traffic light.
Dynamic colour and the accent picker move `primary` and `tertiary`, and six states sit in that
legend at once, so they cannot borrow the theme roles.

## Typography

The scale lives in `ui/theme/Type.kt`. Use the named styles, do not set `fontSize` at a call site.

| Element | Style |
| --- | --- |
| Page title | `headlineMedium`, Bold |
| Compact header title | `titleLarge`, Bold |
| Card title | `titleMedium`, SemiBold |
| Section label | `titleSmall`, uppercase, letterSpacing 1.2 |
| Eyebrow | `labelSmall`, mono, `primary`, uppercase |
| Body | `bodyMedium` |
| Supporting line | `bodySmall`, `onSurfaceVariant` |
| Button label | `labelLarge`, SemiBold |
| Status badge | `labelSmall`, mono, Bold, uppercase |
| Machine value | `bodySmall` or `headlineSmall`, JetBrains Mono |

## Spacing

The scale is **4, 8, 12, 16, 20, 24, 32** (`DsSpace`). Nothing else.

| Gap | Value |
| --- | --- |
| Screen edge | 16 |
| Between cards | 12 |
| Inside a card | 16 |
| Between actions | 8 |
| Section top | 24 |
| Header vertical | 12 |
| Tight metadata | 4 |

Screens under the floating dock add its measured height plus 16 to their bottom content padding.
The dock measures itself after layout, so three-button navigation and gesture navigation both
clear it. Never use a fixed dp for this.

## Shape and elevation

| Element | Radius |
| --- | --- |
| Panel, card | 12 |
| Control, field, chip, button | 8 |
| Dialog, sheet | 16 |
| Bottom sheet top corners | 20 |
| Status badge | 4 |
| Dock | 20 |

Every surface is flat. Depth is a surface step plus a 1dp border, never a shadow. The one
exception is a Material component that owns its own elevation, the FAB menu, and the app no longer
uses one on the main tabs.

## Page anatomy

```
DsPageHeader                     eyebrow, title, supporting line, 48dp actions
  DsSectionHeader("01")          uppercase label with mono index
  DsCard                         neutral panel, optional 2dp accent line
  DsSectionHeader("02")
  ...
```

The header sits on the background, not on a raised bar. An action is a 48dp `DsIconAction` square.
A page has at most two header actions, plus the back control on a pushed page.

A card never centres its content. Titles are left aligned. A row pairs a title with a control
using `Arrangement.SpaceBetween`.

## Buttons and actions

`DsActionButton` has four styles: `Filled` for the single primary action, `Tonal` for the common
secondary, `Outline` for a quiet peer, and `Danger` for stop and delete. Height is 48dp, radius 8,
label `labelLarge`.

A row of peers sits in `DsActionStrip`, which adds 4dp padding and an 8dp gap. The strip is for
actions on one object, not for page navigation.

Icon-only actions are `DsIconAction`, 48dp, 1dp border, 18dp glyph.

## Status

`DsBadge` is a dot plus an uppercase mono label in a 4dp chip. Colour is the state. The badge
never changes width because of a label change: the text is short and fixed ("RUNNING", "STOPPED").

While an operation runs, a 14dp spinner sits beside the badge. The badge keeps reporting the last
settled state so nothing shifts.

## Navigation

Four destinations: **Overview**, **Containers**, **Monitor**, **System**.

- Below 600dp wide: `DsBottomDock`, floating 12dp from the edges, above the navigation bar.
- At 600dp and wider: `DsNavRail`, a 96dp column that is part of the page, with the same order.
- Selection is a tinted control plus the signal colour, never a second screen or a sheet.
- Back from any tab returns to Overview. Each tab keeps its scroll and pager position.
- Settings, Requirements, boot order and terminal appearance are pushed pages from System.

The pager stays so horizontal swiping still works. The dock and rail both drive it.

## Cards and lists

`DsCard` is the only panel. It has an optional `accent` line, a 12dp radius, a 1dp border, and a
16dp inner padding. A card that needs a header row puts the title and badge on one line, a divider
below it, then the body.

Lists are `LazyColumn` with stable keys. Search is `DsSearchField`, filters are `DsFilterChip`
inside a `FlowRow`, so three filters wrap instead of clipping on a 240dp screen. Sorting is a
single header icon that toggles between name and running-first, and the current mode is announced
in the content description.

Empty, loading and error states are `DsEmptyState`, `DsLoadingState`, `DsErrorState`. A list that
is empty because of a search says so and offers no install action.

## Charts

`DsRing` is used only for a value with a known maximum: memory, storage, a CPU share. There is no
history layer, so no sparkline is drawn. A ring has a 7dp stroke, a 3dp cap, a muted track, and
animates to its target. It carries a content description with the value.

## Motion

`AnimationUtils` holds the durations: 140 fast, 220 normal, 320 slow. Pushed pages rise slightly
in the direction of travel and fade. Pop reverses that. Lists keep their entry and exit
animations. Rings interpolate on each reading. No animation decorates a control that does not
change.

## Small screens and accessibility

- Below 320dp wide, headers drop to `titleLarge`, supporting lines and subtitles give way, and
  grids become one column. `DsPageHeader` and `isShortWindow()` handle the height case.
- Everything tappable is at least 48dp. That is a floor, not a preference.
- Text takes `weight(1f)` and ellipsizes. A wrapped button label is a label that is too long.
- Content scrolls; the dock and the system bars are inset from, never overlapped.
- The dock rises with the keyboard.
- Status is always text plus colour, never colour alone.

## Improvements still open

- The setup flow (Welcome, Root Check, Installation) keeps its own hero geometry. It inherits the
  new palette and type but not `DsPageHeader`.
- The installation wizard and init service pages still use their original form structure. They
  use the shared groups, dialogs and buttons, so they match, but they have not been re-laid out.
- A dedicated Contributors page does not exist. Contributors remain in the About dialog.
