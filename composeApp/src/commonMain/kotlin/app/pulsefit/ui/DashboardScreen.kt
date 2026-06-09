package app.pulsefit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.pulsefit.state.PulseStore
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    store: PulseStore,
    modifier: Modifier = Modifier
) {
    val snapshot = store.snapshotFor(store.selectedDate)
    val streak = store.streakStatus()
    val colors = MaterialTheme.colorScheme

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = colors.primary.copy(alpha = 0.10f)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Today",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Targets, training, and hydration in one clean lane.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            tint = colors.tertiary
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilledTonalButton(
                            onClick = store::markSelectedDayHit,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = colors.primary.copy(alpha = 0.16f)
                            )
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                            Text("Hit targets", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricTile(
                        label = "Calories",
                        value = snapshot.macroTotals.calories.toString(),
                        detail = "${store.macroTargets.calories} goal",
                        color = colors.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "Protein",
                        value = "${snapshot.macroTotals.proteinGrams.roundToInt()}g",
                        detail = "${store.macroTargets.proteinGrams}g goal",
                        color = colors.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricTile(
                        label = "Water",
                        value = "${snapshot.waterMl}ml",
                        detail = "${store.waterTargetMl}ml goal",
                        color = colors.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "Streak",
                        value = "${streak.count}",
                        detail = "${streak.skipBufferRemaining} skip days left",
                        color = colors.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader("Target days", action = "${store.targetHitDates().size} hits")
                TargetCalendar(
                    dates = store.calendarDates(21),
                    hitDates = store.targetHitDates(),
                    selectedDate = store.selectedDate,
                    onDateSelected = { store.selectedDate = it }
                )
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SectionHeader("Daily targets")
                    ProgressMeter(
                        label = "Calories",
                        value = snapshot.macroTotals.calories,
                        target = store.macroTargets.calories,
                        unit = "kcal",
                        color = colors.primary
                    )
                    ProgressMeter(
                        label = "Protein",
                        value = snapshot.macroTotals.proteinGrams.roundToInt(),
                        target = store.macroTargets.proteinGrams,
                        unit = "g",
                        color = colors.secondary
                    )
                    ProgressMeter(
                        label = "Hydration",
                        value = snapshot.waterMl,
                        target = store.waterTargetMl,
                        unit = "ml",
                        color = colors.tertiary
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader("Progressive overload")
                OverloadChart(points = store.progressPoints())
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader("Celebrations")
                val achievements = store.recentAchievements()
                if (achievements.isEmpty()) {
                    Text(
                        text = "No celebrations yet.",
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    AchievementFeed(achievements = achievements)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
