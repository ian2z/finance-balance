---
name: Modern Financial Precision
colors:
  surface: '#131315'
  surface-dim: '#131315'
  surface-bright: '#39393b'
  surface-container-lowest: '#0e0e10'
  surface-container-low: '#1b1b1d'
  surface-container: '#201f21'
  surface-container-high: '#2a2a2c'
  surface-container-highest: '#353437'
  on-surface: '#e5e1e4'
  on-surface-variant: '#e0c0b1'
  inverse-surface: '#e5e1e4'
  inverse-on-surface: '#303032'
  outline: '#a78b7d'
  outline-variant: '#584237'
  surface-tint: '#ffb690'
  primary: '#ffb690'
  on-primary: '#552100'
  primary-container: '#f97316'
  on-primary-container: '#582200'
  inverse-primary: '#9d4300'
  secondary: '#ffb95f'
  on-secondary: '#472a00'
  secondary-container: '#ee9800'
  on-secondary-container: '#5b3800'
  tertiary: '#d1c79e'
  on-tertiary: '#363113'
  tertiary-container: '#b5ac84'
  on-tertiary-container: '#464021'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#ffdbca'
  primary-fixed-dim: '#ffb690'
  on-primary-fixed: '#341100'
  on-primary-fixed-variant: '#783200'
  secondary-fixed: '#ffddb8'
  secondary-fixed-dim: '#ffb95f'
  on-secondary-fixed: '#2a1700'
  on-secondary-fixed-variant: '#653e00'
  tertiary-fixed: '#ede3b8'
  tertiary-fixed-dim: '#d1c79d'
  on-tertiary-fixed: '#201c02'
  on-tertiary-fixed-variant: '#4d4727'
  background: '#131315'
  on-background: '#e5e1e4'
  surface-variant: '#353437'
typography:
  display:
    fontFamily: Plus Jakarta Sans
    fontSize: 48px
    fontWeight: '700'
    lineHeight: 56px
    letterSpacing: -0.03em
  display-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  title-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 11px
    fontWeight: '700'
    lineHeight: 14px
    letterSpacing: 0.04em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1.5rem
  gutter-sm: 1rem
  margin: 2rem
  margin-sm: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2.5rem
---

## Brand & Style

The design system establishes a high-performance, dark-mode-first aesthetic for personal wealth management and enterprise financial intelligence. The identity balances precision, confidence, and warmth: the aggressive clarity of deep graphite environments is tempered by the energy of vivid orange and the luxury of warm amber and ivory highlights.

The visual style embraces modern structural minimalism with crisp, solid surfaces, deliberately avoiding soft gradients or ornamental blur. The tone is decisive and institutional yet accessible, transforming dense balance sheets, asset distributions, and cash-flow data into high-contrast, effortless visual hierarchies.

## Colors

The palette relies entirely on deep, solid graphite foundations accented by high-energy citrus and rich amber tones, using refined warm ivories for secondary readability and delicate contrast.

- **Primary (`#f97316`)**: Radiant safety-orange for key conversion funnels, active states, active tab highlights, and primary actions. Step-down hover state: `#ea580c`; active/border accent: `#fb923c`.
- **Secondary (`#f59e0b`)**: Warm gold and amber used strictly for yield indicators, pending transactions, wealth progression bars, and tertiary accents. Supporting highlight: `#fbbf24`.
- **Tertiary / Cream (`#fef3c7`)**: Pale warm ivory used for secondary typography, quiet badges, soft keylines, and neutral high-contrast labels against dark backdrops. Lighter variants include `#fef9c3` and `#fffbeb` for high-emphasis metric captions.
- **Neutral Foundations**:
  - Canvas Base: `#121214` (pure deep graphite foundation)
  - Card & Container Level 1: `#18181b` (solid raised surface)
  - Container Level 2 / Inputs: `#27272a` (interactive surfaces and nested modules)
  - Text Primary: `#fafafa`
  - Text Muted: `#a1a1aa`
  - Structural Borders: `#27272a` and `#3f3f46` (no fuzzy glow; crisp 1px hairpins)

## Typography

The type scale leverages **Plus Jakarta Sans** across all hierarchies to ensure rhythmic geometric clarity and readability for monetary balances, tabular figures, and dense financial summaries. 

Headings feature tight negative letter-spacing for structural authority. Monetary readouts use tabular lining numerals where applicable to maintain alignment across fluctuating values. Ivory tones (`#fef3c7`) are deployed selectively on small-caps titles and section labels for enhanced sophistication without distracting from primary numeric outputs.

## Layout & Spacing

Layout follows an adaptable 12-column grid on desktop screens (1024px and above) with a 24px (`1.5rem`) gutter and generous padding to maintain breathing space between complex analytical modules. Tablet viewports (640px to 1023px) compress to an 8-column system, while mobile devices (under 640px) transition into a single-column stacked layout with 16px (`1rem`) gutters and horizontal margins.

Component density is compact yet legible, pairing high-density metric bars with deliberate spacing gaps between autonomous financial cards.

## Elevation & Depth

Depth in this system avoids blur-heavy drop shadows and skeuomorphic gradients in favor of **solid tonal layering** and **low-contrast precision hairpins**:

- **Ground (Canvas)**: `#121214` - Root application background.
- **Layer 1 (Cards, Tables, Panels)**: `#18181b` with a crisp border (`1px solid #27272a`).
- **Layer 2 (Inputs, Sub-modules, Inner Metrics)**: `#27272a` with an optional subtle boundary (`1px solid #3f3f46`).
- **Layer 3 (Modals, Overlays, Dropdowns)**: `#18181b` surrounded by a distinct accent border (`1px solid #3f3f46`) and a deep, crisp grounding shadow (`0 20px 25px -5px rgba(0, 0, 0, 0.6), 0 8px 10px -6px rgba(0, 0, 0, 0.6)`).
- **Interactive Focus/Active Elevation**: Focused elements display a solid, razor-sharp outline in orange (`#f97316`) without radiant glow rings.

## Shapes

The interface systematically adopts generous curvature with consistent **rounded-xl (1.5rem / 24px)** on exterior containers, cards, and dialogue frames. 

Interior child elements—such as inputs, action triggers, buttons, and segmented pickers—adopt proportional inner radii (**rounded-lg, 1rem / 16px** or **rounded-md, 0.5rem / 8px**) to establish optical harmony and avoid awkward nested corners. Pills are reserved strictly for status badges, tickers, and monetary trend indicators.

## Components

- **Buttons**:
  - *Primary*: Solid `#f97316` fill, `#121214` bold text, rounded-xl corner curvature. Hover state transitions to `#ea580c`.
  - *Secondary*: Solid `#27272a` background with 1px border `#3f3f46` and text in soft ivory `#fef3c7`. Hover shifts background to `#3f3f46`.
  - *Tertiary / Ghost*: Transparent background, `#f97316` text, no border; background tint `#27272a` on hover.

- **Cards & Data Modules**:
  - Built with `#18181b` solid background, surrounded by a 1px border `#27272a`, with `1.5rem` (`rounded-xl`) perimeter radius. No gradients or background blur.

- **Input Fields & Form Selectors**:
  - Surface color `#27272a` with 1px border `#3f3f46`. Placeholder text in `#71717a`, active text in `#fafafa`. On focus: 1.5px solid `#f97316` border.

- **Chips & Status Badges**:
  - *Positive/Yield*: Solid dark base with `#f59e0b` text and subtle `#f59e0b` 1px border (pill radius).
  - *Informational*: Solid `#27272a` base with `#fef3c7` ivory text.
  - *Critical/Alert*: Solid `#27272a` base with bright `#ea580c` text.

- **Checkboxes & Radios**:
  - Square with soft corners (`rounded-md`) for checkboxes, fully circular for radios. Inactive state: `#27272a` with `#3f3f46` border. Selected state: solid `#f97316` fill with an off-black `#121214` check icon.

- **Financial Metric Readouts & Balance Cards**:
  - Featured balance figures set in bold `display` size with `#fafafa`, accompanied by secondary currency abbreviations in `#fef3c7`.
  - Sparklines and trend indicators use crisp solid paths in `#f97316` and `#f59e0b` without area gradient fills beneath the curve.