package app.pulsefit.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import app.pulsefit.domain.MuscleGroup

@Composable
fun MuscleChart(
    levels: Map<MuscleGroup, Float>,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val background = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    Box(modifier = modifier.aspectRatio(0.7f).padding(16.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Head (not tied to a specific group, but we'll draw it)
            drawOval(
                color = background,
                topLeft = Offset(w * 0.42f, h * 0.02f),
                size = Size(w * 0.16f, h * 0.12f)
            )

            // Chest
            drawPart(
                levels[MuscleGroup.Chest] ?: 0f, primary, background,
                w * 0.35f, h * 0.16f, w * 0.3f, h * 0.14f, corner = 8f
            )

            // Shoulders
            drawPart(
                levels[MuscleGroup.Shoulders] ?: 0f, primary, background,
                w * 0.28f, h * 0.16f, w * 0.08f, h * 0.12f, corner = 20f
            )
            drawPart(
                levels[MuscleGroup.Shoulders] ?: 0f, primary, background,
                w * 0.64f, h * 0.16f, w * 0.08f, h * 0.12f, corner = 20f
            )

            // Arms (Biceps/Triceps)
            drawPart(
                levels[MuscleGroup.Arms] ?: 0f, primary, background,
                w * 0.22f, h * 0.28f, w * 0.07f, h * 0.22f, corner = 15f
            )
            drawPart(
                levels[MuscleGroup.Arms] ?: 0f, primary, background,
                w * 0.71f, h * 0.28f, w * 0.07f, h * 0.22f, corner = 15f
            )

            // Core
            drawPart(
                levels[MuscleGroup.Core] ?: 0f, primary, background,
                w * 0.4f, h * 0.32f, w * 0.2f, h * 0.18f, corner = 10f
            )

            // Back (Sides of torso in front view)
            drawPart(
                levels[MuscleGroup.Back] ?: 0f, primary, background,
                w * 0.32f, h * 0.32f, w * 0.07f, h * 0.15f, corner = 5f
            )
            drawPart(
                levels[MuscleGroup.Back] ?: 0f, primary, background,
                w * 0.61f, h * 0.32f, w * 0.07f, h * 0.15f, corner = 5f
            )

            // Legs (Quads)
            drawPart(
                levels[MuscleGroup.Legs] ?: 0f, primary, background,
                w * 0.33f, h * 0.54f, w * 0.16f, h * 0.38f, corner = 20f
            )
            drawPart(
                levels[MuscleGroup.Legs] ?: 0f, primary, background,
                w * 0.51f, h * 0.54f, w * 0.16f, h * 0.38f, corner = 20f
            )
        }
    }
}

private fun DrawScope.drawPart(
    level: Float,
    activeColor: Color,
    inactiveColor: Color,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    corner: Float = 0f
) {
    val color = if (level > 0.05f) {
        activeColor.copy(alpha = (0.2f + level * 0.8f).coerceAtMost(1f))
    } else {
        inactiveColor
    }

    drawRoundRect(
        color = color,
        topLeft = Offset(x, y),
        size = Size(width, height),
        cornerRadius = CornerRadius(corner, corner),
        style = Fill
    )
    
    drawRoundRect(
        color = activeColor.copy(alpha = 0.2f),
        topLeft = Offset(x, y),
        size = Size(width, height),
        cornerRadius = CornerRadius(corner, corner),
        style = Stroke(width = 1.dp.toPx())
    )
}
