package app.pulsefit.db

import app.pulsefit.domain.Achievement
import app.pulsefit.domain.AchievementType
import app.pulsefit.domain.Exercise
import app.pulsefit.domain.FoodHit
import app.pulsefit.domain.MacroEntry
import app.pulsefit.domain.MealSlot
import app.pulsefit.domain.MuscleGroup
import app.pulsefit.domain.NutritionSource
import app.pulsefit.domain.RoutineExercise
import app.pulsefit.domain.RoutineTemplate
import app.pulsefit.domain.SetType
import app.pulsefit.domain.WaterLog
import app.pulsefit.domain.WeightEntry
import app.pulsefit.domain.WorkoutSet
import kotlinx.datetime.LocalDate
import app.pulsefit.db.PulseDatabase
import app.cash.sqldelight.ColumnAdapter

val IntColumnAdapter = object : ColumnAdapter<Int, Long> {
    override fun decode(databaseValue: Long): Int = databaseValue.toInt()
    override fun encode(value: Int): Long = value.toLong()
}

class LocalDataSource(database: PulseDatabase) {
    private val queries = database.pulseDatabaseQueries

    fun getAllExercises(): List<Exercise> {
        return queries.getAllExercises().executeAsList().map { entity ->
            Exercise(
                id = entity.id,
                name = entity.name,
                group = MuscleGroup.valueOf(entity.muscleGroup),
                equipment = entity.equipment,
                isCustom = entity.isCustom
            )
        }
    }

    fun insertExercise(exercise: Exercise) {
        queries.insertExercise(
            id = exercise.id,
            name = exercise.name,
            muscleGroup = exercise.group.name,
            equipment = exercise.equipment,
            isCustom = exercise.isCustom
        )
    }

    fun getAllFoodEntries(): List<MacroEntry> {
        return queries.getAllFoodEntries().executeAsList().map { entity ->
            MacroEntry(
                id = entity.id,
                date = LocalDate.parse(entity.date),
                mealSlot = MealSlot.valueOf(entity.mealSlot),
                food = FoodHit(
                    id = entity.id, // Using entry id as food id for simplicity in db storage mapping
                    name = entity.foodName,
                    brand = entity.brand,
                    source = NutritionSource.valueOf(entity.source),
                    servingLabel = entity.servingLabel,
                    calories = entity.calories,
                    proteinGrams = entity.protein,
                    carbGrams = entity.carbs,
                    fatGrams = entity.fat
                ),
                grams = entity.grams
            )
        }
    }

    fun insertFoodEntry(entry: MacroEntry) {
        queries.insertFoodEntry(
            id = entry.id,
            date = entry.date.toString(),
            mealSlot = entry.mealSlot.name,
            foodName = entry.food.name,
            brand = entry.food.brand,
            source = entry.food.source.name,
            servingLabel = entry.food.servingLabel,
            calories = entry.food.calories,
            protein = entry.food.proteinGrams,
            carbs = entry.food.carbGrams,
            fat = entry.food.fatGrams,
            grams = entry.grams
        )
    }

    fun getAllWaterLogs(): List<WaterLog> {
        return queries.getAllWaterLogs().executeAsList().map { entity ->
            WaterLog(
                id = entity.id,
                date = LocalDate.parse(entity.date),
                milliliters = entity.milliliters
            )
        }
    }

    fun insertWaterLog(log: WaterLog) {
        queries.insertWaterLog(
            id = log.id,
            date = log.date.toString(),
            milliliters = log.milliliters
        )
    }

    fun getAllWorkoutSets(): List<WorkoutSet> {
        return queries.getAllWorkoutSets().executeAsList().map { entity ->
            WorkoutSet(
                id = entity.id,
                exerciseId = entity.exerciseId,
                date = LocalDate.parse(entity.date),
                weightKg = entity.weightKg,
                reps = entity.reps,
                restSeconds = entity.restSeconds,
                workoutId = entity.workoutId,
                rpe = entity.rpe,
                type = SetType.valueOf(entity.setType)
            )
        }
    }

    fun insertWorkoutSet(set: WorkoutSet) {
        queries.insertWorkoutSet(
            id = set.id,
            exerciseId = set.exerciseId,
            date = set.date.toString(),
            weightKg = set.weightKg,
            reps = set.reps,
            restSeconds = set.restSeconds,
            workoutId = set.workoutId,
            rpe = set.rpe,
            setType = set.type.name
        )
    }

    fun getAllRoutines(): List<RoutineTemplate> {
        val routineEntities = queries.getAllRoutines().executeAsList()
        val exerciseEntities = queries.getAllRoutineExercises().executeAsList()
        
        return routineEntities.map { routine ->
            RoutineTemplate(
                id = routine.id,
                name = routine.name,
                exercises = exerciseEntities
                    .filter { it.routineId == routine.id }
                    .map {
                        RoutineExercise(
                            exerciseId = it.exerciseId,
                            targetSets = it.targetSets,
                            targetReps = it.targetReps,
                            restSeconds = it.restSeconds
                        )
                    }
            )
        }
    }

    fun insertRoutine(routine: RoutineTemplate) {
        queries.insertRoutine(routine.id, routine.name)
        routine.exercises.forEach {
            queries.insertRoutineExercise(
                routineId = routine.id,
                exerciseId = it.exerciseId,
                targetSets = it.targetSets,
                targetReps = it.targetReps,
                restSeconds = it.restSeconds
            )
        }
    }

    fun getAllAchievements(): List<Achievement> {
        return queries.getAllAchievements().executeAsList().map { entity ->
            Achievement(
                id = entity.id,
                date = LocalDate.parse(entity.date),
                title = entity.title,
                detail = entity.detail,
                type = AchievementType.valueOf(entity.type)
            )
        }
    }

    fun insertAchievement(achievement: Achievement) {
        queries.insertAchievement(
            id = achievement.id,
            date = achievement.date.toString(),
            title = achievement.title,
            detail = achievement.detail,
            type = achievement.type.name
        )
    }

    fun getAllWeightEntries(): List<WeightEntry> {
        return queries.getAllWeightEntries().executeAsList().map { entity ->
            WeightEntry(
                id = entity.id,
                date = LocalDate.parse(entity.date),
                weightKg = entity.weightKg
            )
        }
    }

    fun insertWeightEntry(entry: WeightEntry) {
        queries.insertWeightEntry(
            id = entry.id,
            date = entry.date.toString(),
            weightKg = entry.weightKg
        )
    }

    fun getSetting(key: String): String? {
        return queries.getSetting(key).executeAsOneOrNull()
    }

    fun insertSetting(key: String, value: String) {
        queries.insertSetting(key, value)
    }

    fun getAllTargetHitDays(): List<LocalDate> {
        return queries.getAllTargetHitDays().executeAsList().map { LocalDate.parse(it) }
    }

    fun insertTargetHitDay(date: LocalDate) {
        queries.insertTargetHitDay(date.toString())
    }
}
