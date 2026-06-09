package app.pulsefit.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.pulsefit.domain.ActiveExercise
import app.pulsefit.domain.ActiveWorkout
import app.pulsefit.domain.Exercise
import app.pulsefit.domain.MuscleGroup
import app.pulsefit.domain.RoutineTemplate
import app.pulsefit.domain.SetType
import app.pulsefit.domain.WorkoutSet
import app.pulsefit.state.PulseStore
import kotlinx.coroutines.delay

@Composable
fun GymScreen(
    store: PulseStore,
    modifier: Modifier = Modifier
) {
    val activeWorkout = store.activeWorkout

    Box(modifier = modifier.fillMaxSize()) {
        if (activeWorkout == null) {
            RoutineLibraryView(store)
        } else {
            ActiveWorkoutView(store, activeWorkout)
        }
    }
}

@Composable
fun RoutineLibraryView(store: PulseStore) {
    var query by remember { mutableStateOf("") }
    var showCreateRoutine by remember { mutableStateOf(false) }
    var showCreateExercise by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Workout",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Let's get those gains today.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "New Workout",
                    subtitle = "Empty session",
                    icon = Icons.Rounded.Add,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = { store.startWorkout(null) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    title = "Routines",
                    subtitle = "Custom templates",
                    icon = Icons.Rounded.Save,
                    color = MaterialTheme.colorScheme.secondary,
                    onClick = { showCreateRoutine = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            SectionHeader("My Routines", action = "${store.routines.size} saved")
        }

        if (store.routines.isEmpty()) {
            item {
                Text(
                    "No routines yet. Create one to save time!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        items(store.routines) { routine ->
            RoutineCard(
                routine = routine,
                onStart = { store.startWorkout(routine) }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader("Exercise Library")
                TextButton(onClick = { showCreateExercise = true }) {
                    Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("New")
                }
            }
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search exercises...") },
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                )
            )
        }

        val grouped = store.exercises
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
            .groupBy { it.group }

        grouped.forEach { (group, exercises) ->
            item {
                Text(
                    group.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(exercises) { exercise ->
                ModernExerciseCard(
                    exercise = exercise,
                    onClick = { /* View history */ }
                )
            }
        }
    }

    if (showCreateRoutine) {
        CreateRoutineDialog(store, onDismiss = { showCreateRoutine = false })
    }
    if (showCreateExercise) {
        CreateExerciseDialog(store, onDismiss = { showCreateExercise = false })
    }
}

@Composable
fun ActiveWorkoutView(store: PulseStore, workout: ActiveWorkout) {
    var showFinishDialog by remember { mutableStateOf(false) }
    var saveAsRoutine by remember { mutableStateOf(false) }
    var showAddExercise by remember { mutableStateOf(false) }
    var showPlateCalc by remember { mutableStateOf(false) }
    var calcWeight by remember { mutableStateOf(0.0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            store.tickTimer()
        }
    }

    val isBlurred = showFinishDialog || showAddExercise || showPlateCalc

    Column(
        modifier = Modifier
            .fillMaxSize()
            .blur(if (isBlurred) 12.dp else 0.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            tonalElevation = 8.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            workout.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Timer, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                if (store.isTimerRunning) "Rest: ${formatTimer(store.activeTimerSeconds)}" else "Active session",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (store.isTimerRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { store.discardWorkout() },
                            modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error)
                        }
                        Button(
                            onClick = { showFinishDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Finish")
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(workout.exercises) { activeEx ->
                val exercise = store.exercises.firstOrNull { it.id == activeEx.exerciseId }
                if (exercise != null) {
                    ActiveExerciseCard(
                        store = store,
                        exercise = exercise,
                        activeEx = activeEx,
                        onShowPlateCalc = {
                            calcWeight = it
                            showPlateCalc = true
                        }
                    )
                }
            }

            item {
                ModernOutlinedButton(
                    onClick = { showAddExercise = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Rounded.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Exercise")
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text("Workout Complete?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Great job! All completed sets will be saved to your history.")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { saveAsRoutine = !saveAsRoutine }
                    ) {
                        Checkbox(
                            checked = saveAsRoutine,
                            onCheckedChange = { saveAsRoutine = it },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Text("Save as routine for next time", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    store.finishWorkout(saveAsRoutine)
                    showFinishDialog = false
                }, shape = RoundedCornerShape(16.dp)) { Text("Finish & Save") }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) { Text("Keep going") }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showAddExercise) {
        ExercisePicker(
            store = store,
            onDismiss = { showAddExercise = false },
            onSelect = { exercise ->
                store.addExerciseToActiveWorkout(exercise)
                showAddExercise = false
            }
        )
    }

    if (showPlateCalc) {
        PlateCalculatorDialog(weight = calcWeight, onDismiss = { showPlateCalc = false })
    }
}

@Composable
fun ActiveExerciseCard(
    store: PulseStore,
    exercise: Exercise,
    activeEx: ActiveExercise,
    onShowPlateCalc: (Double) -> Unit
) {
    val lastPerformance = store.lastSetFor(exercise.id)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        exercise.group.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = { store.addSetToActiveExercise(exercise.id) },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Rounded.Add, null, tint = MaterialTheme.colorScheme.primary)
                }
            }

            if (lastPerformance != null) {
                Surface(
                    modifier = Modifier.padding(top = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Last: ${lastPerformance.weightKg}kg x ${lastPerformance.reps}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("SET", modifier = Modifier.width(32.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text("TYPE", modifier = Modifier.width(60.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text("KG", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text("REPS", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text("RPE", modifier = Modifier.width(36.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(32.dp))
            }

            activeEx.sets.forEachIndexed { index, set ->
                SetRow(
                    index = index + 1,
                    weight = set.weightKg,
                    reps = set.reps,
                    rpe = set.rpe,
                    type = set.type,
                    isCompleted = set.isCompleted,
                    lastWeight = lastPerformance?.weightKg,
                    lastReps = lastPerformance?.reps,
                    onUpdate = { w, r, c, rpe, t ->
                        store.updateActiveSet(exercise.id, set.id, w, r, c, rpe, t)
                    },
                    onShowCalc = { onShowPlateCalc(set.weightKg) }
                )
            }
        }
    }
}

@Composable
fun SetRow(
    index: Int,
    weight: Double,
    reps: Int,
    rpe: Int?,
    type: SetType,
    isCompleted: Boolean,
    lastWeight: Double?,
    lastReps: Int?,
    onUpdate: (Double, Int, Boolean, Int?, SetType) -> Unit,
    onShowCalc: () -> Unit
) {
    var weightText by remember { mutableStateOf(if (weight > 0) weight.toString() else "") }
    var repsText by remember { mutableStateOf(if (reps > 0) reps.toString() else "") }
    var rpeText by remember { mutableStateOf(rpe?.toString() ?: "") }
    var showTypeMenu by remember { mutableStateOf(false) }

    val backgroundColor by animateColorAsState(
        if (isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    val scale by animateFloatAsState(
        if (isCompleted) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "$index",
            modifier = Modifier.width(32.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(modifier = Modifier.width(60.dp)) {
            Text(
                text = when(type) {
                    SetType.Normal -> "N"
                    SetType.WarmUp -> "W"
                    SetType.Failure -> "F"
                    SetType.DropSet -> "D"
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { showTypeMenu = true }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            DropdownMenu(expanded = showTypeMenu, onDismissRequest = { showTypeMenu = false }) {
                SetType.entries.forEach { entry ->
                    DropdownMenuItem(
                        text = { Text(entry.name) },
                        onClick = {
                            onUpdate(weight, reps, isCompleted, rpe, entry)
                            showTypeMenu = false
                        }
                    )
                }
            }
        }

        SetInputField(
            value = weightText,
            placeholder = lastWeight?.toString() ?: "0",
            onValueChange = {
                weightText = it
                onUpdate(it.toDoubleOrNull() ?: 0.0, reps, isCompleted, rpe, type)
            },
            modifier = Modifier.weight(1f),
            onAction = onShowCalc
        )

        SetInputField(
            value = repsText,
            placeholder = lastReps?.toString() ?: "0",
            onValueChange = {
                repsText = it
                onUpdate(weight, it.toIntOrNull() ?: 0, isCompleted, rpe, type)
            },
            modifier = Modifier.weight(1f)
        )

        SetInputField(
            value = rpeText,
            placeholder = "-",
            onValueChange = {
                if (it.length <= 2) {
                    rpeText = it
                    onUpdate(weight, reps, isCompleted, it.toIntOrNull(), type)
                }
            },
            modifier = Modifier.width(36.dp)
        )

        IconButton(
            onClick = { onUpdate(weight, reps, !isCompleted, rpe, type) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                if (isCompleted) Icons.Rounded.Check else Icons.Rounded.Close,
                contentDescription = null,
                tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SetInputField(
    value: String,
    placeholder: String = "",
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onAction: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .clickable(enabled = onAction != null) { onAction?.invoke() },
        contentAlignment = Alignment.Center
    ) {
        if (value.isEmpty() && placeholder.isNotEmpty()) {
            Text(
                placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                fontWeight = FontWeight.Normal
            )
        }
        BasicTextField(
            value = value,
            onValueChange = { if (it.length <= 5) onValueChange(it) },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PlateCalculatorDialog(weight: Double, onDismiss: () -> Unit) {
    val barWeight = 20.0
    val targetOnEachSide = (weight - barWeight) / 2.0
    
    val plates = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)
    val result = mutableListOf<Double>()
    var remaining = targetOnEachSide
    
    if (remaining > 0) {
        plates.forEach { plate ->
            while (remaining >= plate) {
                result.add(plate)
                remaining -= plate
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Plate Calculator") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "${weight}kg Total",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("20kg Bar + ${targetOnEachSide}kg per side", style = MaterialTheme.typography.bodySmall)
                
                Spacer(Modifier.height(20.dp))
                
                if (targetOnEachSide <= 0) {
                    Text("Just the bar!")
                } else {
                    Text("Plates per side:", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                        result.forEach { plate ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    plate.toString(),
                                    modifier = Modifier.padding(8.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(16.dp)) { Text("Got it") }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun RoutineCard(routine: RoutineTemplate, onStart: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onStart() },
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.History, null, tint = MaterialTheme.colorScheme.secondary)
            }
            Column(Modifier.weight(1f)) {
                Text(routine.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    routine.exercises.size.toString() + " exercises",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Rounded.PlayArrow, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun ModernExerciseCard(exercise: Exercise, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(exercise.name.first().uppercase(), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            }
            Column(Modifier.weight(1f)) {
                Text(exercise.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(exercise.equipment, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun CreateExerciseDialog(store: PulseStore, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var equipment by remember { mutableStateOf("") }
    var group by remember { mutableStateOf(MuscleGroup.FullBody) }
    var showGroupMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Exercise") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, shape = RoundedCornerShape(24.dp))
                OutlinedTextField(value = equipment, onValueChange = { equipment = it }, label = { Text("Equipment (e.g. Barbell)") }, shape = RoundedCornerShape(24.dp))
                Box {
                    ModernOutlinedButton(onClick = { showGroupMenu = true }, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                        Text("Group: ${group.label}")
                    }
                    DropdownMenu(expanded = showGroupMenu, onDismissRequest = { showGroupMenu = false }) {
                        MuscleGroup.entries.forEach { entry ->
                            DropdownMenuItem(text = { Text(entry.label) }, onClick = { group = entry; showGroupMenu = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { store.addCustomExercise(name, group, equipment); onDismiss() }, shape = RoundedCornerShape(16.dp)) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        shape = RoundedCornerShape(28.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineDialog(store: PulseStore, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    val selectedIds: SnapshotStateList<String> = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Routine") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Routine Name") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Select Exercises", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(store.exercises) { ex ->
                        val isSelected = ex.id in selectedIds
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { if (isSelected) selectedIds.remove(ex.id) else selectedIds.add(ex.id) }
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = isSelected, onCheckedChange = null)
                                Spacer(Modifier.width(8.dp))
                                Text(ex.name, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                store.createRoutine(name, selectedIds.toList())
                onDismiss()
            }, shape = RoundedCornerShape(16.dp)) { Text("Save Routine") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun ExercisePicker(
    store: PulseStore,
    onDismiss: () -> Unit,
    onSelect: (Exercise) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf<MuscleGroup?>(null) }
    var showCreateExercise by remember { mutableStateOf(false) }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selectedGroup != null || query.isNotEmpty()) {
                            IconButton(onClick = { 
                                selectedGroup = null
                                query = ""
                            }) {
                                Icon(Icons.Rounded.ArrowBackIosNew, null, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            text = when {
                                query.isNotEmpty() -> "Search Results"
                                selectedGroup != null -> selectedGroup?.label ?: ""
                                else -> "Add Exercise"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { showCreateExercise = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Rounded.Add, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Create New", fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Rounded.Close, null)
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search 100+ exercises...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(Modifier.height(16.dp))

                AnimatedContent(
                    targetState = (selectedGroup == null && query.isEmpty()),
                    transitionSpec = {
                        if (targetState) {
                            slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                        } else {
                            slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { isCategoryView ->
                    if (isCategoryView) {
                        // Categories Grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(MuscleGroup.entries) { group ->
                                CategoryCard(
                                    group = group,
                                    count = store.exercises.count { it.group == group },
                                    onClick = { selectedGroup = group }
                                )
                            }
                        }
                    } else {
                        // Exercises List
                        val filtered = store.exercises.filter { ex ->
                            (selectedGroup == null || ex.group == selectedGroup) &&
                            (query.isEmpty() || ex.name.contains(query, ignoreCase = true))
                        }.sortedBy { it.name }

                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtered, key = { it.id }) { ex ->
                                ModernExerciseCard(
                                    exercise = ex,
                                    onClick = { onSelect(ex) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateExercise) {
        CreateExerciseDialog(
            store = store,
            onDismiss = { showCreateExercise = false }
        )
    }
}

@Composable
fun CategoryCard(
    group: MuscleGroup,
    count: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = group.label.first().toString(),
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column {
                Text(
                    text = group.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$count exercises",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ModernOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = ButtonDefaults.outlinedShape,
    border: androidx.compose.foundation.BorderStroke? = null,
    content: @Composable RowScope.() -> Unit
) {
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        border = border ?: BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        content = content
    )
}

fun formatTimer(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "$mins:${secs.toString().padStart(2, '0')}"
}
