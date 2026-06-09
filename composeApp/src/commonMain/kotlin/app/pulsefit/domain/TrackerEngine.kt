package app.pulsefit.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlin.math.max
import kotlin.math.roundToInt

object TrackerEngine {
    fun macroTotals(entries: List<MacroEntry>): MacroTotals {
        return entries.fold(MacroTotals()) { acc, entry ->
            val scale = entry.grams / 100.0
            acc.copy(
                calories = acc.calories + (entry.food.calories * scale).roundToInt(),
                proteinGrams = acc.proteinGrams + entry.food.proteinGrams * scale,
                carbGrams = acc.carbGrams + entry.food.carbGrams * scale,
                fatGrams = acc.fatGrams + entry.food.fatGrams * scale
            )
        }
    }

    fun hitsMacroTargets(totals: MacroTotals, targets: MacroTargets): Boolean {
        val calorieFloor = targets.calories * 0.92
        val calorieCeiling = targets.calories * 1.08
        return totals.calories in calorieFloor.roundToInt()..calorieCeiling.roundToInt() &&
            totals.proteinGrams >= targets.proteinGrams &&
            totals.carbGrams >= targets.carbGrams * 0.85 &&
            totals.fatGrams >= targets.fatGrams * 0.85
    }

    fun hitsAllTargets(
        totals: MacroTotals,
        targets: MacroTargets,
        waterMl: Int,
        waterTargetMl: Int,
        workoutSets: Int
    ): Boolean {
        return hitsMacroTargets(totals, targets) &&
            waterMl >= waterTargetMl &&
            workoutSets >= 3
    }

    fun streakFor(
        hitDates: Collection<LocalDate>,
        today: LocalDate,
        maxSkips: Int = 2
    ): StreakStatus {
        val ordered = hitDates.distinct().sorted()
        if (ordered.isEmpty()) {
            return StreakStatus(
                count = 0,
                skipBufferRemaining = maxSkips,
                lastHitDate = null,
                isActive = false
            )
        }

        val lastHit = ordered.last()
        val daysSinceLastHit = lastHit.daysUntil(today).coerceAtLeast(0)
        if (daysSinceLastHit > maxSkips + 1) {
            return StreakStatus(
                count = 0,
                skipBufferRemaining = 0,
                lastHitDate = lastHit,
                isActive = false
            )
        }

        var streak = 1
        for (index in ordered.lastIndex downTo 1) {
            val current = ordered[index]
            val previous = ordered[index - 1]
            val skippedDays = previous.daysUntil(current) - 1
            if (skippedDays <= maxSkips) {
                streak += 1
            } else {
                break
            }
        }

        return StreakStatus(
            count = streak,
            skipBufferRemaining = max(0, maxSkips - daysSinceLastHit),
            lastHitDate = lastHit,
            isActive = true
        )
    }

    fun estimatedOneRepMax(weightKg: Double, reps: Int): Double {
        if (weightKg <= 0.0 || reps <= 0) return 0.0
        return weightKg * (1.0 + reps / 30.0)
    }

    fun progressiveOverloadPoints(
        sets: List<WorkoutSet>,
        exercises: List<Exercise>,
        limit: Int = 12
    ): List<ProgressPoint> {
        val exerciseById = exercises.associateBy { it.id }
        return sets
            .sortedBy { it.date }
            .mapNotNull { set ->
                val exercise = exerciseById[set.exerciseId] ?: return@mapNotNull null
                ProgressPoint(
                    date = set.date,
                    exerciseName = exercise.name,
                    estimatedOneRepMaxKg = estimatedOneRepMax(set.weightKg, set.reps)
                )
            }
            .takeLast(limit)
    }

    fun muscleGroupLevels(
        sets: List<WorkoutSet>,
        exercises: List<Exercise>
    ): Map<MuscleGroup, Float> {
        val exerciseMap = exercises.associateBy { it.id }
        val maxVolumeByGroup = mutableMapOf<MuscleGroup, Double>()
        
        sets.forEach { set ->
            val group = exerciseMap[set.exerciseId]?.group ?: return@forEach
            val volume = set.weightKg * set.reps
            maxVolumeByGroup[group] = (maxVolumeByGroup[group] ?: 0.0) + volume
        }

        if (maxVolumeByGroup.isEmpty()) return emptyMap()

        val maxEver = maxVolumeByGroup.values.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
        return MuscleGroup.entries.associateWith { group ->
            ((maxVolumeByGroup[group] ?: 0.0) / maxEver).toFloat()
        }
    }
}
