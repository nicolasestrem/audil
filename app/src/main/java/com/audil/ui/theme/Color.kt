package com.audil.ui.theme

import androidx.compose.ui.graphics.Color

// ── Linear-inspired Dark-First Palette ───────────────────────
// Near-black canvas, luminance-stepped surfaces, single indigo accent.

// Brand Accent (the ONLY chromatic color)
val BrandIndigo = Color(0xFF5E6AD2)      // CTA backgrounds, active states
val BrandViolet = Color(0xFF7170FF)      // Interactive accent, links
val BrandVioletHover = Color(0xFF828FFF) // Hover/lighter variant

// ── Dark Theme (native medium) ───────────────────────────────
val LinBlack = Color(0xFF08090A)         // Marketing/canvas black
val LinPanelDark = Color(0xFF0F1011)     // Sidebars, panels
val LinSurface = Color(0xFF191A1B)       // Cards, elevated surfaces
val LinSurfaceHover = Color(0xFF28282C)  // Hover states
val LinTextPrimary = Color(0xFFF7F8F8)   // Near-white, not pure
val LinTextSecondary = Color(0xFFD0D6E0) // Silver-gray body
val LinTextTertiary = Color(0xFF8A8F98)  // Muted gray
val LinTextQuaternary = Color(0xFF62666D) // Most subdued
val LinBorderSubtle = Color(0x0DFFFFFF)  // rgba(255,255,255,0.05)
val LinBorder = Color(0x14FFFFFF)        // rgba(255,255,255,0.08)
val LinBorderStrong = Color(0xFF23252A)  // Solid prominent border

// Status
val StatusGreen = Color(0xFF27A644)
val StatusEmerald = Color(0xFF10B981)
val StatusRed = Color(0xFFEF4444)
val StatusAmber = Color(0xFFF59E0B)

// ── Light Theme (secondary) ──────────────────────────────────
val LinLightBg = Color(0xFFF7F8F8)
val LinLightSurface = Color(0xFFFFFFFF)
val LinLightSurfaceAlt = Color(0xFFF3F4F5)
val LinLightBorder = Color(0xFFE6E6E6)
val LinLightBorderAlt = Color(0xFFD0D6E0)
val LinLightTextPrimary = Color(0xFF1C1C1C)
val LinLightTextSecondary = Color(0xFF62666D)

// Meeting type accents (used sparingly as small dots/indicators)
val AccentStandup = Color(0xFF8B5CF6)
val AccentTeam = Color(0xFF5E6AD2)
val AccentInterview = Color(0xFF10B981)
val AccentLecture = Color(0xFFF59E0B)
val AccentPersonal = Color(0xFFEC4899)
val AccentCustom = Color(0xFF8A8F98)
