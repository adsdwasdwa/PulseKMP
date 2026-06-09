package app.pulsefit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.pulsefit.domain.FoodHit
import app.pulsefit.domain.MealSlot
import app.pulsefit.domain.NutritionSource
import app.pulsefit.state.PulseStore
import kotlin.math.roundToInt

@Composable
fun MacroScreen(
    store: PulseStore,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var grams by remember { mutableStateOf("100") }
    var mealSlot by remember { mutableStateOf(MealSlot.Breakfast) }
    var selectedSources by remember { mutableStateOf(NutritionSource.entries.toSet()) }

    val snapshot = store.snapshotFor(store.selectedDate)

    androidx.compose.runtime.LaunchedEffect(query, selectedSources) {
        if (query.isNotBlank()) {
            kotlinx.coroutines.delay(500)
        }
        store.searchFoods(query, selectedSources)
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader("Macros", action = monthDay(store.selectedDate))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Search food") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        suffix = {
                            if (store.isSearching) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    )
                    OutlinedTextField(
                        value = grams,
                        onValueChange = { grams = it.filter { char -> char.isDigit() }.take(4) },
                        label = { Text("g") },
                        singleLine = true,
                        modifier = Modifier.width(92.dp)
                    )
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(NutritionSource.entries) { source ->
                        FilterChip(
                            selected = source in selectedSources,
                            onClick = {
                                selectedSources = if (source in selectedSources) {
                                    selectedSources - source
                                } else {
                                    selectedSources + source
                                }
                            },
                            label = { Text(source.shortLabel) }
                        )
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(MealSlot.entries) { slot ->
                        FilterChip(
                            selected = slot == mealSlot,
                            onClick = { mealSlot = slot },
                            label = { Text(slot.label) }
                        )
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SectionHeader("Today")
                    ProgressMeter(
                        label = "Calories",
                        value = snapshot.macroTotals.calories,
                        target = store.macroTargets.calories,
                        unit = "kcal",
                        color = MaterialTheme.colorScheme.primary
                    )
                    ProgressMeter(
                        label = "Protein",
                        value = snapshot.macroTotals.proteinGrams.roundToInt(),
                        target = store.macroTargets.proteinGrams,
                        unit = "g",
                        color = MaterialTheme.colorScheme.secondary
                    )
                    ProgressMeter(
                        label = "Carbs",
                        value = snapshot.macroTotals.carbGrams.roundToInt(),
                        target = store.macroTargets.carbGrams,
                        unit = "g",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    ProgressMeter(
                        label = "Fat",
                        value = snapshot.macroTotals.fatGrams.roundToInt(),
                        target = store.macroTargets.fatGrams,
                        unit = "g",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader("Food sources", action = "${store.searchResults.size} results")
                store.searchResults.forEach { food ->
                    FoodResultRow(
                        food = food,
                        onAdd = {
                            store.addFood(
                                food = food,
                                grams = grams.toIntOrNull() ?: 100,
                                mealSlot = mealSlot
                            )
                        }
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader("Log")
                store.foodsFor().forEach { entry ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(entry.food.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "${entry.mealSlot.label} - ${entry.grams}g - ${entry.food.source.shortLabel}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${(entry.food.calories * entry.grams / 100.0).roundToInt()} kcal",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodResultRow(
    food: FoodHit,
    onAdd: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = listOfNotNull(food.brand, food.servingLabel).joinToString(" - "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AssistChip(onClick = {}, label = { Text(food.source.shortLabel) })
                    Text(
                        text = "${food.calories} kcal  P ${food.proteinGrams.roundToInt()}  C ${food.carbGrams.roundToInt()}  F ${food.fatGrams.roundToInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 9.dp)
                    )
                }
            }
            Button(onClick = onAdd) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Add")
            }
        }
    }
}
