package com.audil.ui.theme

import androidx.compose.ui.graphics.Color

// ── Warm Journal Palette ──────────────────────────────────────
// Human-first: paper, leather, terracotta, warm shadows.
// Inspired by Moleskine, Notion's warmth, and the tactile feel of a real notebook.

// Accent — the recording pulse
val WarmTerracotta = Color(0xFFC26743)     // Primary: warm amber-terracotta
val WarmTerracottaLight = Color(0xFFDA7B55) // Lighter for dark mode
val WarmTerracottaMuted = Color(0xFFFFF0EB) // Very subtle warm highlight

// Secondary — quiet green for completion, success, calm
val SageGreen = Color(0xFF5B8C5A)
val SageGreenLight = Color(0xFF6AA86A)

// ── Light Theme — warm paper ──────────────────────────────────
val PaperCream = Color(0xFFFAF8F5)         // Background: warm paper, not sterile white
val PaperWhite = Color(0xFFFFFFFF)         // Cards: bright but on cream it feels warm
val PaperWarmGray = Color(0xFFF3F0EB)      // Subtle surface variant
val PaperInk = Color(0xFF2D2824)           // Text: warm brown-black, never pure #000
val PaperInkSecondary = Color(0xFF6E6862)  // Secondary: warm gray
val PaperInkTertiary = Color(0xFF9E9790)   // Muted: warm light gray
val PaperBorder = Color(0x142D2824)        // rgba(45,40,36,0.08)
val PaperShadow = Color(0x0A000000)        // Ultra-subtle warm shadow

// ── Dark Theme — warm leather ─────────────────────────────────
val WarmDarkBg = Color(0xFF1C1A18)         // Background: warm dark, like a leather notebook
val WarmDarkSurface = Color(0xFF262320)    // Cards: slightly lifted warm dark
val WarmDarkSurfaceAlt = Color(0xFF302C28) // Hover/alt surface
val WarmDarkInk = Color(0xFFEBE4DC)        // Text: warm off-white, like candlelight
val WarmDarkInkSecondary = Color(0xFFA39B92) // Secondary warm gray
val WarmDarkInkTertiary = Color(0xFF787168)  // Muted
val WarmDarkBorder = Color(0x14EBE4DC)     // rgba(235,228,220,0.08)
val WarmDarkShadow = Color(0x1A000000)

// ── Status ────────────────────────────────────────────────────
val StatusRed = Color(0xFFD94A4A)
val StatusAmber = Color(0xFFE8A838)

// ── Meeting type accents (for small indicator dots) ──────────
val AccentStandup = Color(0xFF9B72E8)
val AccentTeam = Color(0xFF5E9BD2)
val AccentInterview = Color(0xFF5BBD8A)
val AccentLecture = Color(0xFFE8A838)
val AccentPersonal = Color(0xFFE8749B)
val AccentCustom = Color(0xFFA39B92)
