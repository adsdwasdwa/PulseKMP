package app.pulsefit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.pulsefit.state.PulseStore
import app.pulsefit.ui.DashboardScreen
import app.pulsefit.ui.GymScreen
import app.pulsefit.ui.MacroScreen
import app.pulsefit.ui.ProgressScreen
import app.pulsefit.ui.PulseTheme
import app.pulsefit.ui.WaterScreen
import app.pulsefit.ui.AchievementPopup
import app.cash.sqldelight.db.SqlDriver

private enum class PulseScreen(
    val title: String,
    val icon: ImageVector
) {
    Today("Today", Icons.Rounded.Dashboard),
    Macros("Macros", Icons.Rounded.Restaurant),
    Gym("Gym", Icons.Rounded.FitnessCenter),
    Water("Water", Icons.Rounded.WaterDrop),
    Progress("Progress", Icons.Rounded.Insights),
    Settings("Settings", Icons.Rounded.Settings)
}

@Composable
fun PulseApp(driver: SqlDriver) {
    val store = remember { PulseStore(driver) }
    var selectedScreen by remember { mutableStateOf(PulseScreen.Today) }

    PulseTheme(darkTheme = store.isDarkMode) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                PulseTopBar(
                    darkTheme = store.isDarkMode,
                    streakCount = store.streakStatus().count,
                    onToggleTheme = { store.isDarkMode = !store.isDarkMode }
                )
            },
            bottomBar = {
                NavigationBar {
                    PulseScreen.entries.forEach { screen ->
                        NavigationBarItem(
                            selected = selectedScreen == screen,
                            onClick = { selectedScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) }
                        )
                    }
                }
            }
        ) { padding ->
            val contentModifier = Modifier
                .fillMaxSize()
                .padding(padding)

            when (selectedScreen) {
                PulseScreen.Today -> DashboardScreen(store, contentModifier)
                PulseScreen.Macros -> MacroScreen(store, contentModifier)
                PulseScreen.Gym -> GymScreen(store, contentModifier)
                PulseScreen.Water -> WaterScreen(store, contentModifier)
                PulseScreen.Progress -> ProgressScreen(store, contentModifier)
                PulseScreen.Settings -> app.pulsefit.ui.SettingsScreen(store, contentModifier)
            }

            store.pendingAchievement?.let { achievement ->
                androidx.compose.ui.window.Dialog(
                    onDismissRequest = { store.pendingAchievement = null }
                ) {
                    AchievementPopup(
                        achievement = achievement,
                        onDismiss = { store.pendingAchievement = null }
                    )
                }
            }
        }
    }
}

@Composable
private fun PulseTopBar(
    darkTheme: Boolean,
    streakCount: Int,
    onToggleTheme: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.small
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocalFireDepartment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "PulseKMP",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${streakCount}d",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = onToggleTheme) {
                    Icon(
                        imageVector = if (darkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                        contentDescription = if (darkTheme) "Use light mode" else "Use dark mode"
                    )
                }
            }
        }
    }
}
