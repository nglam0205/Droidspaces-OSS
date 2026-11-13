package com.droidspaces.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

val AMOLED_BLACK = Color(0xFF141218)

/** Dark mode read off the scheme itself, so AMOLED and dynamic colour need no second hook. */
val ColorScheme.isDark: Boolean
    get() = background.luminance() < 0.5f

/* Material 3 has no warning role and tertiary follows the palette, so on a green
 * palette a warning would look like success. This is the M3 extended-colour answer:
 * a fixed amber pair at the usual container tones (90 on light, 30 on dark). */
val ColorScheme.warningContainer: Color
    get() = if (isDark) Color(0xFF5D4200) else Color(0xFFFFDF9E)

val ColorScheme.onWarningContainer: Color
    get() = if (isDark) Color(0xFFFFDF9E) else Color(0xFF261A00)

/**
 * Pre-defined accent color palettes for the app, mimicking Android's
 * "Wallpaper & style" palette picker.
 *
 * Each palette defines primary, secondary, and tertiary colors for both
 * light and dark modes. A palette taken from a real Material You scheme also
 * carries that whole scheme in [fixedLight] and [fixedDark], used as is instead
 * of the blended surfaces the others get, and its own [swatch] colours.
 */
enum class ThemePalette(
    val displayName: String,
    val primaryLight: Color,
    val secondaryLight: Color,
    val tertiaryLight: Color,
    val primaryDark: Color,
    val secondaryDark: Color,
    val tertiaryDark: Color,
    val fixedLight: ColorScheme? = null,
    val fixedDark: ColorScheme? = null,
    // Primary, secondary and tertiary for the picker swatch, the same in both modes. A
    // Material You scheme's own accents are dark tones in light mode and pale in dark, with
    // muted secondary and tertiary, so as a preview they read flat. These are the scheme's
    // hues at tone 70 with the chroma pushed up (60, 36, 48 in HCT).
    val swatch: List<Color>? = null,
) {
    // The Material You scheme One UI built from a green wallpaper, seed #8CB496 in the
    // Tonal Spot style, read off the phone's system_* colour resources
    BROCCOLI(
        displayName = "Workstation Dark",
        swatch = listOf(Color(0xFF35C278), Color(0xFF7AB98E), Color(0xFF2EBAD8)),
        fixedLight = lightColorScheme(
            primary = Color(0xFF1E4D2B), onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFA5D6A7), onPrimaryContainer = Color(0xFF00210A),
            inversePrimary = Color(0xFF81C784),
            secondary = Color(0xFF37474F), onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFCFD8DC), onSecondaryContainer = Color(0xFF102027),
            tertiary = Color(0xFF006064), onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFB2EBF2), onTertiaryContainer = Color(0xFF00191E),
            background = Color(0xFFF8F9FA), onBackground = Color(0xFF1A1C1E),
            surface = Color(0xFFF8F9FA), onSurface = Color(0xFF1A1C1E),
            surfaceVariant = Color(0xFFE0E2E5), onSurfaceVariant = Color(0xFF43474E),
            inverseSurface = Color(0xFF2F3133), inverseOnSurface = Color(0xFFF1F0F4),
            outline = Color(0xFF73777F), outlineVariant = Color(0xFFC3C7CF),
            surfaceBright = Color(0xFFF8F9FA), surfaceDim = Color(0xFFD8DAE0),
            surfaceContainerLowest = Color(0xFFFFFFFF), surfaceContainerLow = Color(0xFFF2F4F7),
            surfaceContainer = Color(0xFFECEEF2), surfaceContainerHigh = Color(0xFFE6E8EC),
            surfaceContainerHighest = Color(0xFFDFE2E6),
        ),
        fixedDark = darkColorScheme(
            primary = Color(0xFF4ADE80), onPrimary = Color(0xFF003913),
            primaryContainer = Color(0xFF143322), onPrimaryContainer = Color(0xFF86EFAC),
            inversePrimary = Color(0xFF166534),
            secondary = Color(0xFF94A3B8), onSecondary = Color(0xFF0F172A),
            secondaryContainer = Color(0xFF1E293B), onSecondaryContainer = Color(0xFFCBD5E1),
            tertiary = Color(0xFF38BDF8), onTertiary = Color(0xFF0369A1),
            tertiaryContainer = Color(0xFF0C4A6E), onTertiaryContainer = Color(0xFF7DD3FC),
            background = Color(0xFF0B0F12), onBackground = Color(0xFFE2E8F0),
            surface = Color(0xFF0B0F12), onSurface = Color(0xFFE2E8F0),
            surfaceVariant = Color(0xFF1E293B), onSurfaceVariant = Color(0xFF94A3B8),
            inverseSurface = Color(0xFFE2E8F0), inverseOnSurface = Color(0xFF0B0F12),
            outline = Color(0xFF475569), outlineVariant = Color(0xFF1E293B),
            surfaceBright = Color(0xFF1E293B), surfaceDim = Color(0xFF0B0F12),
            surfaceContainerLowest = Color(0xFF070A0D), surfaceContainerLow = Color(0xFF0F172A),
            surfaceContainer = Color(0xFF131D2E), surfaceContainerHigh = Color(0xFF1E293B),
            surfaceContainerHighest = Color(0xFF293548),
        ),
    ),
    // The Material You scheme One UI built from a gold wallpaper, seed #CFA369 in the
    // Tonal Spot style, read off the phone's system_* colour resources
    SAND(
        displayName = "Sand",
        swatch = listOf(Color(0xFFEA9A00), Color(0xFFD7A157), Color(0xFF8BB85D)),
        fixedLight = lightColorScheme(
            primary = Color(0xFF805611), onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFDDB4), onPrimaryContainer = Color(0xFF291800),
            inversePrimary = Color(0xFFF5BC6F),
            secondary = Color(0xFF705B40), onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFBDEBC), onSecondaryContainer = Color(0xFF271904),
            tertiary = Color(0xFF52643F), onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFD5EABB), onTertiaryContainer = Color(0xFF111F03),
            background = Color(0xFFFFF8F4), onBackground = Color(0xFF211B13),
            surface = Color(0xFFFFF8F4), onSurface = Color(0xFF211B13),
            surfaceVariant = Color(0xFFF0E0D0), onSurfaceVariant = Color(0xFF4F4539),
            inverseSurface = Color(0xFF362F27), inverseOnSurface = Color(0xFFFCEFE2),
            outline = Color(0xFF817567), outlineVariant = Color(0xFFD3C4B4),
            surfaceBright = Color(0xFFFFF8F4), surfaceDim = Color(0xFFE5D8CC),
            surfaceContainerLowest = Color(0xFFFFFFFF), surfaceContainerLow = Color(0xFFFFF1E5),
            surfaceContainer = Color(0xFFF9ECDF), surfaceContainerHigh = Color(0xFFF3E6DA),
            surfaceContainerHighest = Color(0xFFEDE0D4),
        ),
        fixedDark = darkColorScheme(
            primary = Color(0xFFF5BC6F), onPrimary = Color(0xFF452B00),
            primaryContainer = Color(0xFF633F00), onPrimaryContainer = Color(0xFFFFDDB4),
            inversePrimary = Color(0xFF805611),
            secondary = Color(0xFFDEC2A1), onSecondary = Color(0xFF3E2D16),
            secondaryContainer = Color(0xFF56432B), onSecondaryContainer = Color(0xFFFBDEBC),
            tertiary = Color(0xFFB9CDA0), onTertiary = Color(0xFF253515),
            tertiaryContainer = Color(0xFF3B4C29), onTertiaryContainer = Color(0xFFD5EABB),
            background = Color(0xFF18120B), onBackground = Color(0xFFEDE0D4),
            surface = Color(0xFF18120B), onSurface = Color(0xFFEDE0D4),
            surfaceVariant = Color(0xFF4F4539), onSurfaceVariant = Color(0xFFD3C4B4),
            inverseSurface = Color(0xFFEDE0D4), inverseOnSurface = Color(0xFF362F27),
            outline = Color(0xFF9C8F80), outlineVariant = Color(0xFF4F4539),
            surfaceBright = Color(0xFF3F3830), surfaceDim = Color(0xFF18120B),
            surfaceContainerLowest = Color(0xFF130D07), surfaceContainerLow = Color(0xFF211B13),
            surfaceContainer = Color(0xFF251F17), surfaceContainerHigh = Color(0xFF302921),
            surfaceContainerHighest = Color(0xFF3B342B),
        ),
    ),
    // One UI's default theme with no wallpaper palette applied, at the lowest contrast
    // level, read off the phone's system_* colour resources
    ONE_UI(
        displayName = "One UI",
        swatch = listOf(Color(0xFF86A9FF), Color(0xFF96AAE4), Color(0xFFDB92DD)),
        fixedLight = lightColorScheme(
            primary = Color(0xFF2A4174), onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFF5E73A9), onPrimaryContainer = Color(0xFFFFFFFF),
            inversePrimary = Color(0xFFB7CAFF),
            secondary = Color(0xFF3C4255), onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFF6E7488), onSecondaryContainer = Color(0xFFFFFFFF),
            tertiary = Color(0xFF553A55), onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF8A6A89), onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFFAF8FF), onBackground = Color(0xFF1A1B20),
            surface = Color(0xFFFAF8FF), onSurface = Color(0xFF1A1B20),
            surfaceVariant = Color(0xFFE1E2EC), onSurfaceVariant = Color(0xFF40434B),
            inverseSurface = Color(0xFF2F3036), inverseOnSurface = Color(0xFFF1F0F7),
            outline = Color(0xFF5D5F67), outlineVariant = Color(0xFF797A83),
            surfaceBright = Color(0xFFFAF8FF), surfaceDim = Color(0xFFDAD9E0),
            surfaceContainerLowest = Color(0xFFFFFFFF), surfaceContainerLow = Color(0xFFF4F3FA),
            surfaceContainer = Color(0xFFEEEDF4), surfaceContainerHigh = Color(0xFFE8E7EF),
            surfaceContainerHighest = Color(0xFFE2E2E9),
        ),
        fixedDark = darkColorScheme(
            primary = Color(0xFFB7CAFF), onPrimary = Color(0xFF00143B),
            primaryContainer = Color(0xFF7A90C8), onPrimaryContainer = Color(0xFF000000),
            inversePrimary = Color(0xFF2A4174),
            secondary = Color(0xFFC4CAE1), onSecondary = Color(0xFF0F1626),
            secondaryContainer = Color(0xFF8A90A5), onSecondaryContainer = Color(0xFF000000),
            tertiary = Color(0xFFE4BFE2), onTertiary = Color(0xFF240D26),
            tertiaryContainer = Color(0xFFA886A6), onTertiaryContainer = Color(0xFF000000),
            background = Color(0xFF121318), onBackground = Color(0xFFE2E2E9),
            surface = Color(0xFF121318), onSurface = Color(0xFFFCFAFF),
            surfaceVariant = Color(0xFF44464F), onSurfaceVariant = Color(0xFFC9CAD4),
            inverseSurface = Color(0xFFE2E2E9), inverseOnSurface = Color(0xFF2F3036),
            outline = Color(0xFFA1A2AC), outlineVariant = Color(0xFF81838C),
            surfaceBright = Color(0xFF38393F), surfaceDim = Color(0xFF121318),
            surfaceContainerLowest = Color(0xFF0C0E13), surfaceContainerLow = Color(0xFF1A1B20),
            surfaceContainer = Color(0xFF1E1F25), surfaceContainerHigh = Color(0xFF282A2F),
            surfaceContainerHighest = Color(0xFF33343A),
        ),
    ),
    // Generated from GNOME's accent blue #3584E4, as on a Fedora Workstation desktop, with
    // the Content style of Material color utilities, spec 2021. Content keeps the seed's
    // chroma, so the containers land on the GNOME tile blue; Tonal Spot would mute it into
    // a near copy of One UI.
    FEDORA(
        displayName = "Fedora",
        swatch = listOf(Color(0xFF75ADFF), Color(0xFF8CACE2), Color(0xFFD594E4)),
        fixedLight = lightColorScheme(
            primary = Color(0xFF005CAE), onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFF1C75D4), onPrimaryContainer = Color(0xFFFEFCFF),
            inversePrimary = Color(0xFFA7C8FF),
            secondary = Color(0xFF485F84), onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFBBD3FE), onSecondaryContainer = Color(0xFF435A7F),
            tertiary = Color(0xFF863C9C), onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFA156B7), onTertiaryContainer = Color(0xFFFFFBFF),
            background = Color(0xFFF9F9FF), onBackground = Color(0xFF191C22),
            surface = Color(0xFFF9F9FF), onSurface = Color(0xFF191C22),
            surfaceVariant = Color(0xFFDDE2F1), onSurfaceVariant = Color(0xFF414752),
            inverseSurface = Color(0xFF2D3037), inverseOnSurface = Color(0xFFEFF0F9),
            outline = Color(0xFF717784), outlineVariant = Color(0xFFC1C6D4),
            surfaceBright = Color(0xFFF9F9FF), surfaceDim = Color(0xFFD8DAE2),
            surfaceContainerLowest = Color(0xFFFFFFFF), surfaceContainerLow = Color(0xFFF2F3FC),
            surfaceContainer = Color(0xFFECEDF6), surfaceContainerHigh = Color(0xFFE6E8F0),
            surfaceContainerHighest = Color(0xFFE0E2EB),
        ),
        fixedDark = darkColorScheme(
            primary = Color(0xFFA7C8FF), onPrimary = Color(0xFF003061),
            primaryContainer = Color(0xFF4691F2), onPrimaryContainer = Color(0xFF001C3C),
            inversePrimary = Color(0xFF005EB2),
            secondary = Color(0xFFAFC8F2), onSecondary = Color(0xFF173153),
            secondaryContainer = Color(0xFF2F476B), onSecondaryContainer = Color(0xFF9EB6E0),
            tertiary = Color(0xFFF0B0FF), onTertiary = Color(0xFF54016C),
            tertiaryContainer = Color(0xFFC072D6), onTertiaryContainer = Color(0xFF340045),
            background = Color(0xFF101319), onBackground = Color(0xFFE0E2EB),
            surface = Color(0xFF101319), onSurface = Color(0xFFE0E2EB),
            surfaceVariant = Color(0xFF414752), onSurfaceVariant = Color(0xFFC1C6D4),
            inverseSurface = Color(0xFFE0E2EB), inverseOnSurface = Color(0xFF2D3037),
            outline = Color(0xFF8B919E), outlineVariant = Color(0xFF414752),
            surfaceBright = Color(0xFF363940), surfaceDim = Color(0xFF101319),
            surfaceContainerLowest = Color(0xFF0B0E14), surfaceContainerLow = Color(0xFF191C22),
            surfaceContainer = Color(0xFF1D2026), surfaceContainerHigh = Color(0xFF272A30),
            surfaceContainerHighest = Color(0xFF32353B),
        ),
    ),
    CATPPUCCIN(
        displayName = "Catppuccin",
        primaryLight = Color(0xFF8AADF4),   // Blue
        secondaryLight = Color(0xFFB7BDF8), // Lavender
        tertiaryLight = Color(0xFFA6DA95),  // Green
        primaryDark = Color(0xFF7DC4E4),    // Sky
        secondaryDark = Color(0xFFF5BDE6),  // Pink
        tertiaryDark = Color(0xFFA6DA95)    // Green
    ),
    OCEAN(
        displayName = "Ocean",
        primaryLight = Color(0xFF0277BD),   // Deep Blue
        secondaryLight = Color(0xFF00ACC1), // Cyan
        tertiaryLight = Color(0xFF26A69A),  // Teal
        primaryDark = Color(0xFF4FC3F7),    // Light Blue
        secondaryDark = Color(0xFF4DD0E1),  // Light Cyan
        tertiaryDark = Color(0xFF80CBC4)    // Light Teal
    ),
    FOREST(
        displayName = "Forest",
        primaryLight = Color(0xFF2E7D32),   // Deep Green
        secondaryLight = Color(0xFF558B2F), // Olive Green
        tertiaryLight = Color(0xFF8D6E63),  // Brown
        primaryDark = Color(0xFF81C784),    // Light Green
        secondaryDark = Color(0xFFA5D6A7),  // Pale Green
        tertiaryDark = Color(0xFFBCAAA4)    // Light Brown
    ),
    SUNSET(
        displayName = "Sunset",
        primaryLight = Color(0xFFD84315),   // Deep Orange
        secondaryLight = Color(0xFFF4511E), // Orange-Red
        tertiaryLight = Color(0xFFFFB300),  // Amber
        primaryDark = Color(0xFFFF8A65),    // Light Orange
        secondaryDark = Color(0xFFFF8A80),  // Light Coral
        tertiaryDark = Color(0xFFFFD54F)    // Light Amber
    ),
    AMETHYST(
        displayName = "Amethyst",
        primaryLight = Color(0xFF6A1B9A),   // Deep Purple
        secondaryLight = Color(0xFF8E24AA), // Purple
        tertiaryLight = Color(0xFFAD1457),  // Deep Pink
        primaryDark = Color(0xFFCE93D8),    // Light Purple
        secondaryDark = Color(0xFFBA68C8),  // Medium Purple
        tertiaryDark = Color(0xFFF48FB1)    // Light Pink
    ),
    SAKURA(
        displayName = "Sakura",
        primaryLight = Color(0xFFD81B60),   // Pink
        secondaryLight = Color(0xFFEC407A), // Rose
        tertiaryLight = Color(0xFF7E57C2),  // Violet
        primaryDark = Color(0xFFF48FB1),    // Light Pink
        secondaryDark = Color(0xFFF8BBD0),  // Pale Pink
        tertiaryDark = Color(0xFFB39DDB)    // Light Violet
    );

    /** A palette read off a real Material You scheme: its accents are the scheme's own. */
    constructor(displayName: String, swatch: List<Color>, fixedLight: ColorScheme, fixedDark: ColorScheme) : this(
        displayName,
        fixedLight.primary, fixedLight.secondary, fixedLight.tertiary,
        fixedDark.primary, fixedDark.secondary, fixedDark.tertiary,
        fixedLight, fixedDark, swatch,
    )

    companion object {
        fun fromName(name: String): ThemePalette =
            entries.find { it.name == name } ?: BROCCOLI
    }
}
