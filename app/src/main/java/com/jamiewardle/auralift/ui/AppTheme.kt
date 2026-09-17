package com.jamiewardle.auralift.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.*
import com.jamiewardle.auralift.model.Accent
import kotlin.math.*

/** Shared by Compose screens, previews and the native floating window. */
internal data class AppThemeSpec(
    val accent: Accent,
    val colors: ColorScheme,
    val backdrop: Color,
    val corner: Dp
)

internal fun appTheme(accent: Accent, light: Boolean): AppThemeSpec {
    fun c(value: Long) = Color(value)
    val background: Color; val surface: Color; val raised: Color
    val text: Color; val muted: Color; val primary: Color; val secondary: Color
    if (light) {
        when (accent) {
            Accent.MINT -> { background = c(0xFFF0F7F3); surface = c(0xFFFFFFFF); raised = c(0xFFE0EFE7); text = c(0xFF142E26); muted = c(0xFF4B6459); primary = c(0xFF176A58); secondary = c(0xFF365F75) }
            Accent.OCEAN -> { background = c(0xFFDCEBFF); surface = c(0xFFF1F7FF); raised = c(0xFFCDDEF4); text = c(0xFF0B2441); muted = c(0xFF405D7D); primary = c(0xFF005E9F); secondary = c(0xFF005E6D) }
            Accent.AMBER -> { background = c(0xFFF5E6CD); surface = c(0xFFFFF4E2); raised = c(0xFFEBD7B4); text = c(0xFF37210C); muted = c(0xFF735337); primary = c(0xFF815000); secondary = c(0xFF984C2C) }
            Accent.ORCHID -> { background = c(0xFFF2DFFC); surface = c(0xFFFFF0FE); raised = c(0xFFE9CCEE); text = c(0xFF3D154A); muted = c(0xFF75537E); primary = c(0xFF853999); secondary = c(0xFFA32765) }
            Accent.ROSE -> { background = c(0xFFF8E4EB); surface = c(0xFFFFF5F8); raised = c(0xFFEED2DE); text = c(0xFF401B2B); muted = c(0xFF775063); primary = c(0xFFA22E60); secondary = c(0xFF7C477B) }
            Accent.CORAL -> { background = c(0xFFFFE9DF); surface = c(0xFFFFF7F0); raised = c(0xFFF7D8C8); text = c(0xFF422019); muted = c(0xFF785448); primary = c(0xFFA13E2D); secondary = c(0xFF82610A) }
            Accent.FOREST -> { background = c(0xFFE7EFDA); surface = c(0xFFF8FBEF); raised = c(0xFFD5E3C1); text = c(0xFF24311B); muted = c(0xFF526146); primary = c(0xFF406622); secondary = c(0xFF686011) }
            Accent.SUNSHINE -> { background = c(0xFFFFF1BE); surface = c(0xFFFFFBE5); raised = c(0xFFF3E19E); text = c(0xFF362E09); muted = c(0xFF6B602D); primary = c(0xFF786000); secondary = c(0xFF876200) }
            Accent.MIDNIGHT -> { background = c(0xFFE8E7FF); surface = c(0xFFF7F6FF); raised = c(0xFFD9D5F8); text = c(0xFF232044); muted = c(0xFF5B5680); primary = c(0xFF5142A5); secondary = c(0xFF8B4378) }
            Accent.SLATE -> { background = c(0xFFE9EDF0); surface = c(0xFFFAFBFC); raised = c(0xFFD7DFE5); text = c(0xFF222E35); muted = c(0xFF536570); primary = c(0xFF365A70); secondary = c(0xFF4C6262) }
        }
    } else {
        when (accent) {
            Accent.MINT -> { background = c(0xFF0D1615); surface = c(0xFF172521); raised = c(0xFF293C35); text = c(0xFFEFF7F2); muted = c(0xFFADBCB5); primary = c(0xFF8BE2CD); secondary = c(0xFFB0C7DF) }
            Accent.OCEAN -> { background = c(0xFF06162C); surface = c(0xFF102C49); raised = c(0xFF204563); text = c(0xFFEAF4FF); muted = c(0xFFA9C9E6); primary = c(0xFF7BD5FF); secondary = c(0xFF76E5D9) }
            Accent.AMBER -> { background = c(0xFF21170E); surface = c(0xFF39291A); raised = c(0xFF53402A); text = c(0xFFFFEFDA); muted = c(0xFFD0B99A); primary = c(0xFFFFCE80); secondary = c(0xFFFFAB82) }
            Accent.ORCHID -> { background = c(0xFF200C32); surface = c(0xFF39204D); raised = c(0xFF553465); text = c(0xFFFFEDFF); muted = c(0xFFD7B7E5); primary = c(0xFFE0B0FF); secondary = c(0xFFFFACE0) }
            Accent.ROSE -> { background = c(0xFF230E19); surface = c(0xFF3A1C2B); raised = c(0xFF543143); text = c(0xFFFFEDF4); muted = c(0xFFDAB5C7); primary = c(0xFFFFA6C9); secondary = c(0xFFD6B2FA) }
            Accent.CORAL -> { background = c(0xFF271412); surface = c(0xFF40241E); raised = c(0xFF5C3830); text = c(0xFFFFF0E8); muted = c(0xFFE1BBAA); primary = c(0xFFFFAE92); secondary = c(0xFFF3D37D) }
            Accent.FOREST -> { background = c(0xFF101A0D); surface = c(0xFF1E2E17); raised = c(0xFF33452A); text = c(0xFFF0F7E6); muted = c(0xFFBBCAA9); primary = c(0xFFB1DB8B); secondary = c(0xFFE1D48D) }
            Accent.SUNSHINE -> { background = c(0xFF1E1B0A); surface = c(0xFF322D13); raised = c(0xFF4A4220); text = c(0xFFFFF7D3); muted = c(0xFFD0C594); primary = c(0xFFFFDF68); secondary = c(0xFFFBB78A) }
            Accent.MIDNIGHT -> { background = c(0xFF0E0E20); surface = c(0xFF1B1A36); raised = c(0xFF303051); text = c(0xFFF1EDFF); muted = c(0xFFBEBCDE); primary = c(0xFFBEB4FF); secondary = c(0xFFF1AEDB) }
            Accent.SLATE -> { background = c(0xFF12171B); surface = c(0xFF212A31); raised = c(0xFF364149); text = c(0xFFF1F5F7); muted = c(0xFFB7C4CD); primary = c(0xFFD0E1ED); secondary = c(0xFFACD3CE) }
        }
    }
    val base = if (light) lightColorScheme() else darkColorScheme()
    val container = primary.copy(alpha = if (light) .14f else .18f).compositeOver(surface)
    val colors = base.copy(
        primary = primary, onPrimary = if (light) Color.White else background,
        primaryContainer = container, onPrimaryContainer = primary,
        secondary = secondary, onSecondary = if (light) Color.White else background,
        secondaryContainer = container, onSecondaryContainer = text,
        tertiary = secondary, onTertiary = if (light) Color.White else background,
        tertiaryContainer = raised, onTertiaryContainer = text,
        background = background, onBackground = text,
        surface = surface, onSurface = text, surfaceVariant = raised, onSurfaceVariant = muted,
        surfaceDim = background, surfaceBright = raised, surfaceContainerLowest = background,
        surfaceContainerLow = surface, surfaceContainer = surface, surfaceContainerHigh = raised,
        surfaceContainerHighest = raised, surfaceTint = primary,
        outline = muted.copy(alpha = .65f).compositeOver(surface), outlineVariant = raised
    )
    return AppThemeSpec(accent, colors, secondary.copy(alpha = if (light) .08f else .09f).compositeOver(background),
        when (accent) { Accent.MINT -> 26.dp; Accent.OCEAN -> 18.dp; Accent.AMBER -> 10.dp; Accent.ORCHID, Accent.ROSE -> 32.dp; Accent.CORAL -> 24.dp; Accent.FOREST -> 20.dp; Accent.SUNSHINE -> 16.dp; Accent.MIDNIGHT -> 22.dp; Accent.SLATE -> 12.dp })
}

internal val LocalAppTheme = staticCompositionLocalOf { appTheme(Accent.MINT, false) }

@Composable internal fun ThemeBackdrop(modifier: Modifier = Modifier) {
    val spec = LocalAppTheme.current
    Canvas(modifier.clearAndSetSemantics {}) {
        drawRect(Brush.verticalGradient(listOf(spec.backdrop, spec.colors.background)))
        when (spec.accent) {
            Accent.OCEAN -> repeat(5) { i ->
                drawCircle(spec.colors.secondary.copy(alpha = .07f), size.width * (.35f + i * .13f),
                    Offset(size.width * 1.03f, size.height * .14f), style = Stroke(1.dp.toPx()))
            }
            Accent.AMBER -> {
                val spacing = 28.dp.toPx()
                var x = 0f
                while (x < size.width) { drawLine(spec.colors.primary.copy(alpha = .025f), Offset(x, 0f), Offset(x, size.height), 1.dp.toPx()); x += spacing }
            }
            Accent.ORCHID -> drawCircle(Brush.radialGradient(listOf(spec.colors.secondary.copy(alpha = .16f), Color.Transparent),
                center = Offset(size.width, 0f), radius = size.width), size.width, Offset(size.width, 0f))
            Accent.MINT, Accent.ROSE, Accent.CORAL, Accent.FOREST, Accent.SUNSHINE, Accent.MIDNIGHT, Accent.SLATE -> Unit
        }
    }
}

internal fun DrawScope.drawThemeDial(spec: AppThemeSpec, fraction: Float, active: Boolean) {
    val ink = spec.colors.primary.copy(alpha = if (active) 1f else .7f)
    val track = spec.colors.surfaceVariant
    val pad = size.width * .092f
    val arc = Size(size.width - pad * 2, size.height - pad * 2)
    val weight = size.width * (if (spec.accent == Accent.AMBER) .025f else .046f)
    if (spec.accent == Accent.ORCHID) {
        drawCircle(Brush.radialGradient(listOf(spec.colors.secondary.copy(alpha = .14f), spec.colors.surface)), size.width * .38f)
        drawCircle(spec.colors.secondary.copy(alpha = .25f), size.width * .34f, style = Stroke(size.width * .005f))
    }
    if (spec.accent == Accent.OCEAN) drawArc(spec.colors.secondary.copy(alpha = .55f), 135f, 270f, false,
        Offset(pad * 1.65f, pad * 1.65f), Size(size.width - pad * 3.3f, size.height - pad * 3.3f), style = Stroke(size.width * .007f))
    drawArc(track, 135f, 270f, false, Offset(pad, pad), arc, style = Stroke(weight, cap = StrokeCap.Round))
    if (fraction > 0f) drawArc(ink, 135f, 270f * fraction.coerceIn(0f, 1f), false, Offset(pad, pad), arc,
        style = Stroke(weight, cap = StrokeCap.Round))
    for (i in 0..35) {
        val angle = Math.toRadians((135f + i * 270f / 35).toDouble())
        val radius = size.width * .484f
        val length = size.width * (if (spec.accent == Accent.AMBER) .037f else if (i % 5 == 0) .026f else .009f)
        drawLine(if (i / 35f <= fraction) ink else track,
            center + Offset((cos(angle) * (radius - length)).toFloat(), (sin(angle) * (radius - length)).toFloat()),
            center + Offset((cos(angle) * radius).toFloat(), (sin(angle) * radius).toFloat()), size.width * .007f,
            cap = if (spec.accent == Accent.AMBER) StrokeCap.Butt else StrokeCap.Round)
    }
}

/** An illustration of the actual palette and dial; no fabricated live audio visualization. */
@Composable internal fun ThemePreview(accent: Accent, light: Boolean) {
    val spec = remember(accent, light) { appTheme(accent, light) }
    Canvas(Modifier.fillMaxWidth().height(164.dp).clearAndSetSemantics {}) {
        drawRect(Brush.verticalGradient(listOf(spec.backdrop, spec.colors.background)))
        val inset = size.width * .08f
        val radius = spec.corner.toPx() * .35f
        drawRoundRect(spec.colors.primary, Offset(inset, 13.dp.toPx()), Size(size.width * .43f, 4.dp.toPx()), CornerRadius(2.dp.toPx()))
        drawRoundRect(spec.colors.surface, Offset(inset, 27.dp.toPx()), Size(size.width - 2 * inset, size.height * .65f), CornerRadius(radius))
        val dialSize = minOf(size.width * .56f, size.height * .51f)
        inset(left = (size.width - dialSize) / 2, top = 32.dp.toPx(),
            right = (size.width - dialSize) / 2, bottom = size.height - 32.dp.toPx() - dialSize) {
            drawThemeDial(spec, .57f, true)
        }
        drawRoundRect(spec.colors.primary, Offset(inset * 1.8f, size.height * .66f), Size(size.width - inset * 3.6f, 9.dp.toPx()), CornerRadius(radius))
        repeat(3) { i -> drawRoundRect(if (i == 0) spec.colors.primaryContainer else spec.colors.surface,
            Offset(inset + i * (size.width - inset * 2) / 3, size.height * .87f), Size(size.width * .2f, 6.dp.toPx()), CornerRadius(radius)) }
    }
}
