package app.pulsefit.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// Modern "Electric Performance" Palette
private val PulseLight = lightColorScheme(
    primary = Color(0xFF2563EB),     // Electric Blue
    onPrimary = Color.White,
    secondary = Color(0xFF7C3AED),   // Deep Violet
    onSecondary = Color.White,
    tertiary = Color(0xFF0D9488),    // Teal
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),  // Crisp White-Gray
    onBackground = Color(0xFF0F172A), // Slate 900
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9), // Slate 100
    onSurfaceVariant = Color(0xFF475569), // Slate 600
    outline = Color(0xFFCBD5E1),
    error = Color(0xFFE11D48)
)

private val PulseDark = darkColorScheme(
    primary = Color(0xFF60A5FA),     // Sky Blue
    onPrimary = Color(0xFF003366),
    secondary = Color(0xFFA78BFA),   // Soft Violet
    onSecondary = Color(0xFF2E1065),
    tertiary = Color(0xFF2DD4BF),    // Bright Teal
    onTertiary = Color(0xFF042F2E),
    background = Color(0xFF020617),  // Ultra Dark Navy
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF0F172A),     // Slate 900
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E293B), // Slate 800
    onSurfaceVariant = Color(0xFF94A3B8), // Slate 400
    outline = Color(0xFF334155),
    error = Color(0xFFFB7185)
)

// Ultra-Rounded Modern Shapes
private val PulseShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun PulseTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) PulseDark else PulseLight,
        typography = MaterialTheme.typography,
        shapes = PulseShapes,
        content = content
    )
}
