package app.pulsefit.domain

import kotlinx.datetime.LocalDate

enum class NutritionSource(
    val label: String,
    val shortLabel: String,
    val needsSecret: Boolean
) {
    OpenFoodFacts("Open Food Facts", "OFF", false),
    FoodDataCentral("USDA FoodData Central", "FDA", true),
    CalorieNinjas("CalorieNinjas", "Ninja", true),
    FatSecret("FatSecret Platform", "FatSecret", true)
}

enum class MealSlot(val label: String) {
    Breakfast("Breakfast"),
    Lunch("Lunch"),
    Dinner("Dinner"),
    Snack("Snack")
}

data class MacroTargets(
    val calories: Int = 2400,
    val proteinGrams: Int = 180,
    val carbGrams: Int = 260,
    val fatGrams: Int = 75
)

data class MacroTotals(
    val calories: Int = 0,
    val proteinGrams: Double = 0.0,
    val carbGrams: Double = 0.0,
    val fatGrams: Double = 0.0
)

data class FoodHit(
    val id: String,
    val name: String,
    val brand: String?,
    val source: NutritionSource,
    val servingLabel: String,
    val calories: Int,
    val proteinGrams: Double,
    val carbGrams: Double,
    val fatGrams: Double
)

data class MacroEntry(
    val id: String,
    val date: LocalDate,
    val mealSlot: MealSlot,
    val food: FoodHit,
    val grams: Int
)

enum class MuscleGroup(val label: String) {
    Chest("Chest"),
    Back("Back"),
    Legs("Legs"),
    Shoulders("Shoulders"),
    Arms("Arms"),
    Core("Core"),
    FullBody("Full body"),
    Cardio("Cardio")
}

data class Exercise(
    val id: String,
    val name: String,
    val group: MuscleGroup,
    val equipment: String,
    val isCustom: Boolean = false
)

data class WorkoutSet(
    val id: String,
    val exerciseId: String,
    val date: LocalDate,
    val weightKg: Double,
    val reps: Int,
    val restSeconds: Int,
    val workoutId: String? = null,
    val rpe: Int? = null,
    val type: SetType = SetType.Normal
)

data class ActiveWorkout(
    val id: String,
    val startTime: Long,
    val routineId: String?,
    val name: String,
    val exercises: List<ActiveExercise>
)

data class ActiveExercise(
    val exerciseId: String,
    val sets: List<ActiveSet>
)

enum class SetType {
    Normal, WarmUp, Failure, DropSet
}

data class ActiveSet(
    val id: String,
    val weightKg: Double,
    val reps: Int,
    val isCompleted: Boolean = false,
    val rpe: Int? = null,
    val type: SetType = SetType.Normal
)

data class RoutineExercise(
    val exerciseId: String,
    val targetSets: Int,
    val targetReps: String,
    val restSeconds: Int
)

data class RoutineTemplate(
    val id: String,
    val name: String,
    val exercises: List<RoutineExercise>
)

data class WaterLog(
    val id: String,
    val date: LocalDate,
    val milliliters: Int
)

data class Achievement(
    val id: String,
    val date: LocalDate,
    val title: String,
    val detail: String,
    val type: AchievementType
)

enum class AchievementType {
    Pr,
    Streak,
    Targets
}

data class StreakStatus(
    val count: Int,
    val skipBufferRemaining: Int,
    val lastHitDate: LocalDate?,
    val isActive: Boolean
)

data class ProgressPoint(
    val date: LocalDate,
    val exerciseName: String,
    val estimatedOneRepMaxKg: Double
)

data class WeightEntry(
    val id: String,
    val date: LocalDate,
    val weightKg: Double
)

data class DailySnapshot(
    val date: LocalDate,
    val macroTotals: MacroTotals,
    val waterMl: Int,
    val workoutSets: Int,
    val targetHit: Boolean,
    val streak: StreakStatus
)
