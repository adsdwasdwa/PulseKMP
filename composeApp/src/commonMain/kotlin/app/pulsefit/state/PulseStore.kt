package app.pulsefit.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.pulsefit.data.seedExerciseLibrary
import app.pulsefit.data.seedFoods
import app.pulsefit.data.seedRoutines
import app.pulsefit.domain.Achievement
import app.pulsefit.domain.AchievementType
import app.pulsefit.domain.DailySnapshot
import app.pulsefit.domain.Exercise
import app.pulsefit.domain.FoodHit
import app.pulsefit.domain.MacroEntry
import app.pulsefit.domain.MacroTargets
import app.pulsefit.domain.MealSlot
import app.pulsefit.domain.MuscleGroup
import app.pulsefit.domain.NutritionSource
import app.pulsefit.domain.ProgressPoint
import app.pulsefit.domain.RoutineExercise
import app.pulsefit.domain.RoutineTemplate
import app.pulsefit.domain.TrackerEngine
import app.pulsefit.domain.WaterLog
import app.pulsefit.domain.WorkoutSet
import app.pulsefit.db.LocalDataSource
import app.pulsefit.db.PulseDatabase
import app.pulsefit.db.FoodEntryEntity
import app.pulsefit.db.RoutineExerciseEntity
import app.pulsefit.db.WaterLogEntity
import app.pulsefit.db.WorkoutSetEntity
import app.pulsefit.db.IntColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt

class PulseStore(driver: SqlDriver) {
    private val db = LocalDataSource(
        PulseDatabase(
            driver = driver,
            FoodEntryEntityAdapter = FoodEntryEntity.Adapter(
                caloriesAdapter = IntColumnAdapter,
                gramsAdapter = IntColumnAdapter
            ),
            RoutineExerciseEntityAdapter = RoutineExerciseEntity.Adapter(
                targetSetsAdapter = IntColumnAdapter,
                restSecondsAdapter = IntColumnAdapter
            ),
            WaterLogEntityAdapter = WaterLogEntity.Adapter(
                millilitersAdapter = IntColumnAdapter
            ),
            WorkoutSetEntityAdapter = WorkoutSetEntity.Adapter(
                repsAdapter = IntColumnAdapter,
                restSecondsAdapter = IntColumnAdapter,
                rpeAdapter = IntColumnAdapter
            )
        )
    )

    val today: LocalDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

    var selectedDate by mutableStateOf(today)

    private var _macroTargets by mutableStateOf(MacroTargets())
    var macroTargets: MacroTargets
        get() = _macroTargets
        set(value) {
            _macroTargets = value
            db.insertSetting("target_calories", value.calories.toString())
            db.insertSetting("target_protein", value.proteinGrams.toString())
            db.insertSetting("target_carbs", value.carbGrams.toString())
            db.insertSetting("target_fat", value.fatGrams.toString())
        }

    private var _waterTargetMl by mutableStateOf(3200)
    var waterTargetMl: Int
        get() = _waterTargetMl
        set(value) {
            _waterTargetMl = value
            db.insertSetting("target_water", value.toString())
        }

    val foodEntries = mutableStateListOf<MacroEntry>()
    val waterLogs = mutableStateListOf<WaterLog>()
    val workoutSets = mutableStateListOf<WorkoutSet>()
    val exercises = mutableStateListOf<Exercise>()
    val routines = mutableStateListOf<RoutineTemplate>()
    val achievements = mutableStateListOf<Achievement>()
    var pendingAchievement by mutableStateOf<Achievement?>(null)
    val weightEntries = mutableStateListOf<app.pulsefit.domain.WeightEntry>()
    private val manuallyHitTargetDays = mutableStateListOf<LocalDate>()

    private var _isDarkMode by mutableStateOf(true)
    var isDarkMode: Boolean
        get() = _isDarkMode
        set(value) {
            _isDarkMode = value
            db.insertSetting("is_dark_mode", value.toString())
        }

    private var _restTimerSeconds by mutableStateOf(90)
    var restTimerSeconds: Int
        get() = _restTimerSeconds
        set(value) {
            _restTimerSeconds = value
            db.insertSetting("rest_timer", value.toString())
        }

    var activeTimerSeconds by mutableStateOf(0)
    var isTimerRunning by mutableStateOf(false)

    private val httpClient = app.pulsefit.data.nutritionHttpClient()
    private val searchHub = app.pulsefit.data.NutritionSearchHub(
        listOf(
            app.pulsefit.data.OpenFoodFactsProvider(httpClient),
            app.pulsefit.data.FoodDataCentralProvider(httpClient),
            app.pulsefit.data.CalorieNinjasProvider(httpClient, null), // API keys omitted for demo
            app.pulsefit.data.FatSecretProvider(httpClient, null)
        )
    )

    var searchResults = mutableStateListOf<FoodHit>()
    var isSearching by mutableStateOf(false)

    var activeWorkout by mutableStateOf<app.pulsefit.domain.ActiveWorkout?>(null)

    init {
        loadFromDb()
        if (exercises.isEmpty()) {
            exercises.addAll(seedExerciseLibrary)
            exercises.forEach { db.insertExercise(it) }
        }
        if (routines.isEmpty()) {
            routines.addAll(seedRoutines)
            routines.forEach { db.insertRoutine(it) }
        }
        searchResults.addAll(seedFoods)
    }

    private fun loadFromDb() {
        exercises.addAll(db.getAllExercises())
        routines.addAll(db.getAllRoutines())
        foodEntries.addAll(db.getAllFoodEntries())
        waterLogs.addAll(db.getAllWaterLogs())
        workoutSets.addAll(db.getAllWorkoutSets())
        achievements.addAll(db.getAllAchievements())
        weightEntries.addAll(db.getAllWeightEntries())

        db.getSetting("is_dark_mode")?.let { _isDarkMode = it.toBoolean() }
        db.getSetting("rest_timer")?.let { _restTimerSeconds = it.toInt() }
        db.getSetting("target_water")?.let { _waterTargetMl = it.toInt() }

        val tCal = db.getSetting("target_calories")?.toInt()
        val tProt = db.getSetting("target_protein")?.toInt()
        val tCarb = db.getSetting("target_carbs")?.toInt()
        val tFat = db.getSetting("target_fat")?.toInt()

        if (tCal != null && tProt != null && tCarb != null && tFat != null) {
            _macroTargets = app.pulsefit.domain.MacroTargets(tCal, tProt, tCarb, tFat)
        }

        manuallyHitTargetDays.addAll(db.getAllTargetHitDays())
    }

    suspend fun searchFoods(
        query: String,
        sources: Set<NutritionSource>
    ) {
        if (query.isBlank()) {
            searchResults.clear()
            searchResults.addAll(seedFoods)
            return
        }

        isSearching = true
        try {
            val results = searchHub.search(query, sources)
            searchResults.clear()
            searchResults.addAll(results)
        } finally {
            isSearching = false
        }
    }

    fun addFood(food: FoodHit, grams: Int, mealSlot: MealSlot) {
        val entry = MacroEntry(
            id = next("food"),
            date = selectedDate,
            mealSlot = mealSlot,
            food = food,
            grams = grams.coerceIn(1, 2000)
        )
        foodEntries += entry
        db.insertFoodEntry(entry)
        syncTargetHitFor(selectedDate)
    }

    fun addWater(amountMl: Int) {
        val log = WaterLog(
            id = next("water"),
            date = selectedDate,
            milliliters = amountMl.coerceIn(1, 5000)
        )
        waterLogs += log
        db.insertWaterLog(log)
        syncTargetHitFor(selectedDate)
    }

    fun logSet(exercise: Exercise, weightKg: Double, reps: Int, restSeconds: Int) {
        val previousBest = bestEstimatedOneRepMax(exercise.id)
        val set = WorkoutSet(
            id = next("set"),
            exerciseId = exercise.id,
            date = selectedDate,
            weightKg = weightKg.coerceAtLeast(0.0),
            reps = reps.coerceAtLeast(1),
            restSeconds = restSeconds.coerceAtLeast(0)
        )
        workoutSets += set
        db.insertWorkoutSet(set)

        val newBest = TrackerEngine.estimatedOneRepMax(set.weightKg, set.reps)
        if (newBest > previousBest + 0.5) {
            emitAchievement(Achievement(
                id = next("achievement"),
                date = selectedDate,
                title = "New PR",
                detail = "${exercise.name}: ${newBest.roundToInt()} kg estimated 1RM",
                type = AchievementType.Pr
            ))
        }
        syncTargetHitFor(selectedDate)
    }

    fun addCustomExercise(name: String, group: MuscleGroup, equipment: String): Exercise? {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return null

        val exercise = Exercise(
            id = "custom-${cleanName.lowercase().replace(Regex("[^a-z0-9]+"), "-")}-${Clock.System.now().toEpochMilliseconds()}",
            name = cleanName,
            group = group,
            equipment = equipment.ifBlank { "Custom" },
            isCustom = true
        )
        exercises += exercise
        db.insertExercise(exercise)
        return exercise
    }

    fun createRoutine(name: String, exerciseIds: List<String>): RoutineTemplate? {
        val cleanName = name.trim()
        val uniqueIds = exerciseIds.distinct().take(12)
        if (cleanName.isBlank() || uniqueIds.isEmpty()) return null

        val routine = RoutineTemplate(
            id = next("routine"),
            name = cleanName,
            exercises = uniqueIds.map {
                RoutineExercise(
                    exerciseId = it,
                    targetSets = 3,
                    targetReps = "8-12",
                    restSeconds = 90
                )
            }
        )
        routines += routine
        db.insertRoutine(routine)
        return routine
    }

    fun startWorkout(routine: RoutineTemplate?) {
        activeWorkout = app.pulsefit.domain.ActiveWorkout(
            id = next("workout"),
            startTime = Clock.System.now().toEpochMilliseconds(),
            routineId = routine?.id,
            name = routine?.name ?: "Custom Workout",
            exercises = routine?.exercises?.map { re ->
                val history = lastSessionSets(re.exerciseId)
                app.pulsefit.domain.ActiveExercise(
                    exerciseId = re.exerciseId,
                    sets = List(re.targetSets) { index ->
                        val hSet = history.getOrNull(index) ?: history.lastOrNull()
                        app.pulsefit.domain.ActiveSet(
                            id = next("aset"),
                            weightKg = hSet?.weightKg ?: 0.0,
                            reps = hSet?.reps ?: 0,
                            isCompleted = false
                        )
                    }
                )
            } ?: emptyList()
        )
    }

    fun addExerciseToActiveWorkout(exercise: Exercise) {
        val current = activeWorkout ?: return
        val history = lastSessionSets(exercise.id)
        val hSet = history.firstOrNull()
        
        activeWorkout = current.copy(
            exercises = current.exercises + app.pulsefit.domain.ActiveExercise(
                exerciseId = exercise.id,
                sets = listOf(
                    app.pulsefit.domain.ActiveSet(
                        id = next("aset"),
                        weightKg = hSet?.weightKg ?: 0.0,
                        reps = hSet?.reps ?: 0,
                        isCompleted = false
                    )
                )
            )
        )
    }

    fun updateActiveSet(
        exerciseId: String,
        setId: String,
        weight: Double,
        reps: Int,
        completed: Boolean,
        rpe: Int? = null,
        type: app.pulsefit.domain.SetType = app.pulsefit.domain.SetType.Normal
    ) {
        val current = activeWorkout ?: return
        activeWorkout = current.copy(
            exercises = current.exercises.map { ex ->
                if (ex.exerciseId == exerciseId) {
                    ex.copy(sets = ex.sets.map { s ->
                        if (s.id == setId) {
                            if (!s.isCompleted && completed) {
                                startRestTimer()
                            }
                            s.copy(
                                weightKg = weight,
                                reps = reps,
                                isCompleted = completed,
                                rpe = rpe,
                                type = type
                            )
                        } else s
                    })
                } else ex
            }
        )
    }

    private fun startRestTimer() {
        activeTimerSeconds = restTimerSeconds
        isTimerRunning = true
    }

    fun tickTimer() {
        if (isTimerRunning && activeTimerSeconds > 0) {
            activeTimerSeconds--
        } else {
            isTimerRunning = false
        }
    }

    fun addWeightEntry(kg: Double) {
        val entry = app.pulsefit.domain.WeightEntry(
            id = next("weight"),
            date = today,
            weightKg = kg
        )
        weightEntries += entry
        db.insertWeightEntry(entry)
    }

    fun addSetToActiveExercise(exerciseId: String) {
        val current = activeWorkout ?: return
        activeWorkout = current.copy(
            exercises = current.exercises.map { ex ->
                if (ex.exerciseId == exerciseId) {
                    val lastSet = ex.sets.lastOrNull()
                    ex.copy(sets = ex.sets + app.pulsefit.domain.ActiveSet(
                        id = next("aset"),
                        weightKg = lastSet?.weightKg ?: 0.0,
                        reps = lastSet?.reps ?: 0,
                        isCompleted = false
                    ))
                } else ex
            }
        )
    }

    fun finishWorkout(saveAsRoutine: Boolean) {
        val current = activeWorkout ?: return
        val date = today

        current.exercises.forEach { ex ->
            val exercise = exercises.firstOrNull { it.id == ex.exerciseId } ?: return@forEach
            ex.sets.filter { it.isCompleted }.forEach { s ->
                logSet(exercise, s.weightKg, s.reps, 90) // Using default rest for now
            }
        }

        if (saveAsRoutine) {
            createRoutine(current.name, current.exercises.map { it.exerciseId })
        }

        activeWorkout = null
    }

    fun discardWorkout() {
        activeWorkout = null
    }

    fun markSelectedDayHit() {
        if (selectedDate !in manuallyHitTargetDays) {
            manuallyHitTargetDays += selectedDate
            db.insertTargetHitDay(selectedDate)
            emitAchievement(Achievement(
                id = next("achievement"),
                date = selectedDate,
                title = "Targets locked",
                detail = "All daily targets hit",
                type = AchievementType.Targets
            ))
        }

        val streak = streakStatus()
        if (streak.count in listOf(3, 7, 14, 30, 60, 100)) {
            emitAchievement(Achievement(
                id = next("achievement"),
                date = selectedDate,
                title = "${streak.count}-day streak",
                detail = "Two skip days are allowed before reset",
                type = AchievementType.Streak
            ))
        }
    }

    fun snapshotFor(date: LocalDate = selectedDate): DailySnapshot {
        val totals = TrackerEngine.macroTotals(foodsFor(date))
        val water = waterFor(date)
        val sets = setsFor(date).size
        val targetHit = date in targetHitDates() ||
            TrackerEngine.hitsAllTargets(totals, macroTargets, water, waterTargetMl, sets)

        return DailySnapshot(
            date = date,
            macroTotals = totals,
            waterMl = water,
            workoutSets = sets,
            targetHit = targetHit,
            streak = streakStatus()
        )
    }

    fun targetHitDates(): Set<LocalDate> {
        val calculated = calendarDates(45).filter { date ->
            val totals = TrackerEngine.macroTotals(foodsFor(date))
            TrackerEngine.hitsAllTargets(
                totals = totals,
                targets = macroTargets,
                waterMl = waterFor(date),
                waterTargetMl = waterTargetMl,
                workoutSets = setsFor(date).size
            )
        }
        return (manuallyHitTargetDays + calculated).toSet()
    }

    fun calendarDates(days: Int): List<LocalDate> {
        return (days - 1 downTo 0).map { today.minus(DatePeriod(days = it)) }
    }

    fun streakStatus() = TrackerEngine.streakFor(
        hitDates = targetHitDates(),
        today = today,
        maxSkips = 2
    )

    fun progressPoints(): List<ProgressPoint> {
        return TrackerEngine.progressiveOverloadPoints(
            sets = workoutSets,
            exercises = exercises,
            limit = 14
        )
    }

    fun foodsFor(date: LocalDate = selectedDate) = foodEntries.filter { it.date == date }
    fun waterFor(date: LocalDate = selectedDate) = waterLogs.filter { it.date == date }.sumOf { it.milliliters }
    fun setsFor(date: LocalDate = selectedDate) = workoutSets.filter { it.date == date }

    fun lastSetFor(exerciseId: String): app.pulsefit.domain.WorkoutSet? {
        return workoutSets
            .filter { it.exerciseId == exerciseId }
            .sortedByDescending { it.date }
            .firstOrNull()
    }

    fun lastSessionSets(exerciseId: String): List<app.pulsefit.domain.WorkoutSet> {
        val lastDate = workoutSets
            .filter { it.exerciseId == exerciseId }
            .maxOfOrNull { it.date } ?: return emptyList()

        return workoutSets.filter { it.exerciseId == exerciseId && it.date == lastDate }
    }

    fun exerciseName(id: String): String {
        return exercises.firstOrNull { it.id == id }?.name ?: "Exercise"
    }

    fun recentAchievements(limit: Int = 6): List<Achievement> {
        return achievements.sortedWith(
            compareByDescending<Achievement> { it.date }
                .thenByDescending { it.id }
        ).take(limit)
    }

    private fun emitAchievement(achievement: Achievement) {
        achievements += achievement
        db.insertAchievement(achievement)
        pendingAchievement = achievement
    }

    private fun syncTargetHitFor(date: LocalDate) {
        val snapshot = snapshotFor(date)
        if (snapshot.targetHit && date !in manuallyHitTargetDays) {
            manuallyHitTargetDays += date
            db.insertTargetHitDay(date)
        }
    }

    private fun bestEstimatedOneRepMax(exerciseId: String): Double {
        return workoutSets
            .filter { it.exerciseId == exerciseId }
            .maxOfOrNull { TrackerEngine.estimatedOneRepMax(it.weightKg, it.reps) }
            ?: 0.0
    }

    private fun seedHistory() {
        val d0 = today
        val d1 = today.minus(DatePeriod(days = 1))
        val d2 = today.minus(DatePeriod(days = 2))
        val d3 = today.minus(DatePeriod(days = 3))
        val d5 = today.minus(DatePeriod(days = 5))
        val d6 = today.minus(DatePeriod(days = 6))
        val d8 = today.minus(DatePeriod(days = 8))
        val d9 = today.minus(DatePeriod(days = 9))

        foodEntries += listOf(
            MacroEntry(next("food"), d0, MealSlot.Breakfast, seedFoods[2], 80),
            MacroEntry(next("food"), d0, MealSlot.Lunch, seedFoods[1], 180),
            MacroEntry(next("food"), d0, MealSlot.Snack, seedFoods[5], 120),
            MacroEntry(next("food"), d0, MealSlot.Dinner, seedFoods[3], 160),
            MacroEntry(next("food"), d0, MealSlot.Dinner, seedFoods[4], 250),
            MacroEntry(next("food"), d1, MealSlot.Breakfast, seedFoods[0], 220),
            MacroEntry(next("food"), d1, MealSlot.Lunch, seedFoods[1], 220),
            MacroEntry(next("food"), d1, MealSlot.Dinner, seedFoods[3], 180),
            MacroEntry(next("food"), d1, MealSlot.Dinner, seedFoods[4], 320),
            MacroEntry(next("food"), d3, MealSlot.Lunch, seedFoods[1], 240),
            MacroEntry(next("food"), d3, MealSlot.Dinner, seedFoods[4], 340)
        )

        waterLogs += listOf(
            WaterLog(next("water"), d0, 750),
            WaterLog(next("water"), d0, 750),
            WaterLog(next("water"), d0, 500),
            WaterLog(next("water"), d1, 3400),
            WaterLog(next("water"), d3, 3300)
        )

        workoutSets += listOf(
            WorkoutSet(next("set"), "barbell-bench-press", d9, 92.5, 5, 150),
            WorkoutSet(next("set"), "barbell-bench-press", d6, 95.0, 5, 150),
            WorkoutSet(next("set"), "barbell-bench-press", d3, 97.5, 4, 150),
            WorkoutSet(next("set"), "barbell-bench-press", d0, 100.0, 4, 150),
            WorkoutSet(next("set"), "back-squat", d8, 125.0, 5, 180),
            WorkoutSet(next("set"), "back-squat", d5, 130.0, 5, 180),
            WorkoutSet(next("set"), "back-squat", d2, 132.5, 4, 180),
            WorkoutSet(next("set"), "lat-pulldown", d1, 72.5, 10, 90),
            WorkoutSet(next("set"), "seated-cable-row", d1, 78.0, 9, 90)
        )

        manuallyHitTargetDays += listOf(d9, d8, d6, d5, d3, d1)

        achievements += listOf(
            Achievement(
                id = next("achievement"),
                date = d1,
                title = "Targets locked",
                detail = "Macros, water, and training targets completed",
                type = AchievementType.Targets
            ),
            Achievement(
                id = next("achievement"),
                date = d3,
                title = "Bench PR",
                detail = "97.5 kg x 4 moved your estimated max up",
                type = AchievementType.Pr
            ),
            Achievement(
                id = next("achievement"),
                date = d5,
                title = "3-day streak",
                detail = "Skip buffer preserved the run",
                type = AchievementType.Streak
            )
        )
    }

    private fun next(prefix: String): String {
        return "$prefix-${Clock.System.now().toEpochMilliseconds()}-${(100..999).random()}"
    }
}
