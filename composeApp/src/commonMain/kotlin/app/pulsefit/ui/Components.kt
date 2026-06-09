package app.pulsefit.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.pulsefit.domain.Achievement
import app.pulsefit.domain.AchievementType
import app.pulsefit.domain.ProgressPoint
import kotlinx.datetime.LocalDate
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun AchievementPopup(
    achievement: Achievement,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = when (achievement.type) {
        AchievementType.Pr -> MaterialTheme.colorScheme.secondary
        AchievementType.Streak -> MaterialTheme.colorScheme.tertiary
        AchievementType.Targets -> MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = modifier
            .fillMaxWidth(0.85f)
            .padding(16.dp),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        border = BorderStroke(2.dp, color.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (achievement.type) {
                    AchievementType.Pr -> "🏆"
                    AchievementType.Streak -> "🔥"
                    AchievementType.Targets -> "🎯"
                }
                Text(text = icon, fontSize = 40.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = achievement.detail,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            androidx.compose.material3.Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = color)
            ) {
                Text("Awesome!")
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    action: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (action != null) {
            Text(
                text = action,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun MetricTile(
    label: String,
    value: String,
    detail: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ProgressMeter(
    label: String,
    value: Number,
    target: Number,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val targetDouble = target.toDouble().coerceAtLeast(1.0)
    val progress = (value.toDouble() / targetDouble).toFloat().coerceIn(0f, 1.15f)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(
                text = "${value.smart()} / ${target.smart()} $unit",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { progress.coerceAtMost(1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun TargetCalendar(
    dates: List<LocalDate>,
    hitDates: Set<LocalDate>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(dates) { date ->
            val hit = date in hitDates
            val selected = date == selectedDate
            val fill = when {
                selected -> MaterialTheme.colorScheme.primary
                hit -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            val textColor = when {
                selected -> MaterialTheme.colorScheme.onPrimary
                hit -> MaterialTheme.colorScheme.onTertiary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onDateSelected(date) }
                    .padding(2.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = fill,
                    border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Box(
                        modifier = Modifier.size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            color = textColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Text(
                    text = monthDay(date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AchievementFeed(
    achievements: List<Achievement>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        achievements.forEach { achievement ->
            val color = when (achievement.type) {
                AchievementType.Pr -> MaterialTheme.colorScheme.secondary
                AchievementType.Streak -> MaterialTheme.colorScheme.tertiary
                AchievementType.Targets -> MaterialTheme.colorScheme.primary
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = color.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, color.copy(alpha = 0.32f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = achievement.title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = achievement.detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = monthDay(achievement.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun OverloadChart(
    points: List<ProgressPoint>,
    modifier: Modifier = Modifier
) {
    val line = MaterialTheme.colorScheme.primary
    val accent = MaterialTheme.colorScheme.secondary
    val grid = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp)
                .border(
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                    RoundedCornerShape(8.dp)
                )
                .padding(8.dp)
        ) {
            if (points.isEmpty()) {
                return@Canvas
            }

            val minValue = points.minOf { it.estimatedOneRepMaxKg }.toFloat()
            val maxValue = points.maxOf { it.estimatedOneRepMaxKg }.toFloat()
            val range = max(1f, maxValue - minValue)
            val left = 18f
            val right = size.width - 18f
            val top = 20f
            val bottom = size.height - 20f
            val step = if (points.size == 1) 0f else (right - left) / (points.size - 1)

            repeat(4) { index ->
                val y = top + (bottom - top) * index / 3f
                drawLine(
                    color = grid,
                    start = Offset(left, y),
                    end = Offset(right, y),
                    strokeWidth = 1f
                )
            }

            val offsets = points.mapIndexed { index, point ->
                val x = left + step * index
                val normalized = ((point.estimatedOneRepMaxKg.toFloat() - minValue) / range)
                val y = bottom - normalized * (bottom - top)
                Offset(x, y)
            }

            offsets.zipWithNext().forEach { (start, end) ->
                drawLine(
                    color = line,
                    start = start,
                    end = end,
                    strokeWidth = 5f,
                    cap = StrokeCap.Round
                )
            }

            offsets.forEachIndexed { index, offset ->
                drawCircle(
                    color = if (index == offsets.lastIndex) accent else line,
                    radius = if (index == offsets.lastIndex) 7f else 5f,
                    center = offset,
                    style = Stroke(width = 3f)
                )
                drawCircle(
                    color = if (index == offsets.lastIndex) accent else line,
                    radius = 3f,
                    center = offset
                )
            }
        }
        if (points.isNotEmpty()) {
            Text(
                text = "Latest: ${points.last().exerciseName} ${points.last().estimatedOneRepMaxKg.roundToInt()} kg e1RM",
                style = MaterialTheme.typography.bodySmall,
                color = labelColor
            )
        }
    }
}

fun monthDay(date: LocalDate): String = "${date.monthNumber}/${date.dayOfMonth}"

private fun Number.smart(): String {
    val value = toDouble()
    return if (value % 1.0 == 0.0) value.roundToInt().toString() else "${(value * 10).roundToInt() / 10.0}"
}
