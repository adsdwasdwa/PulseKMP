package app.pulsefit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.pulsefit.state.PulseStore

@Composable
fun WaterScreen(
    store: PulseStore,
    modifier: Modifier = Modifier
) {
    var customAmount by remember { mutableStateOf("400") }
    val current = store.waterFor()
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
                color = colors.tertiary.copy(alpha = 0.12f)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.WaterDrop,
                            contentDescription = null,
                            tint = colors.tertiary
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "$current ml",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    ProgressMeter(
                        label = "Hydration",
                        value = current,
                        target = store.waterTargetMl,
                        unit = "ml",
                        color = colors.tertiary
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader("Quick add")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(250, 500, 750).forEach { amount ->
                        FilledTonalButton(
                            onClick = { store.addWater(amount) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("${amount}ml")
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = customAmount,
                        onValueChange = { customAmount = it.filter { char -> char.isDigit() }.take(4) },
                        label = { Text("Custom ml") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { store.addWater(customAmount.toIntOrNull() ?: 0) },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add")
                    }
                }
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SectionHeader("Hydration rhythm")
                    HydrationRow("Morning", "750 ml", current >= 750)
                    HydrationRow("Training window", "1000 ml", current >= 1750)
                    HydrationRow("Evening", "${store.waterTargetMl - 1750} ml", current >= store.waterTargetMl)
                }
            }
        }
    }
}

@Composable
private fun HydrationRow(
    label: String,
    target: String,
    complete: Boolean
) {
    val color = if (complete) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = if (complete) 0.14f else 0.55f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontWeight = FontWeight.SemiBold)
            Text(
                target,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
