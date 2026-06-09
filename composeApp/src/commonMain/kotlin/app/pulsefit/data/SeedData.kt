package app.pulsefit.data

import app.pulsefit.domain.Exercise
import app.pulsefit.domain.FoodHit
import app.pulsefit.domain.MuscleGroup
import app.pulsefit.domain.NutritionSource
import app.pulsefit.domain.RoutineExercise
import app.pulsefit.domain.RoutineTemplate

private fun exercise(
    id: String,
    name: String,
    group: MuscleGroup,
    equipment: String
) = Exercise(id, name, group, equipment)

val seedExerciseLibrary = listOf(
    exercise("barbell-bench-press", "Barbell bench press", MuscleGroup.Chest, "Barbell"),
    exercise("incline-dumbbell-press", "Incline dumbbell press", MuscleGroup.Chest, "Dumbbells"),
    exercise("flat-dumbbell-press", "Flat dumbbell press", MuscleGroup.Chest, "Dumbbells"),
    exercise("decline-bench-press", "Decline bench press", MuscleGroup.Chest, "Barbell"),
    exercise("machine-chest-press", "Machine chest press", MuscleGroup.Chest, "Machine"),
    exercise("push-up", "Push-up", MuscleGroup.Chest, "Bodyweight"),
    exercise("weighted-push-up", "Weighted push-up", MuscleGroup.Chest, "Plate"),
    exercise("chest-dip", "Chest dip", MuscleGroup.Chest, "Bars"),
    exercise("cable-fly", "Cable fly", MuscleGroup.Chest, "Cable"),
    exercise("pec-deck", "Pec deck", MuscleGroup.Chest, "Machine"),
    exercise("landmine-press", "Landmine press", MuscleGroup.Chest, "Barbell"),
    exercise("pull-up", "Pull-up", MuscleGroup.Back, "Bodyweight"),
    exercise("chin-up", "Chin-up", MuscleGroup.Back, "Bodyweight"),
    exercise("lat-pulldown", "Lat pulldown", MuscleGroup.Back, "Cable"),
    exercise("wide-grip-pulldown", "Wide-grip pulldown", MuscleGroup.Back, "Cable"),
    exercise("seated-cable-row", "Seated cable row", MuscleGroup.Back, "Cable"),
    exercise("barbell-row", "Barbell row", MuscleGroup.Back, "Barbell"),
    exercise("pendlay-row", "Pendlay row", MuscleGroup.Back, "Barbell"),
    exercise("single-arm-dumbbell-row", "Single-arm dumbbell row", MuscleGroup.Back, "Dumbbell"),
    exercise("chest-supported-row", "Chest-supported row", MuscleGroup.Back, "Machine"),
    exercise("t-bar-row", "T-bar row", MuscleGroup.Back, "Machine"),
    exercise("face-pull", "Face pull", MuscleGroup.Back, "Cable"),
    exercise("straight-arm-pulldown", "Straight-arm pulldown", MuscleGroup.Back, "Cable"),
    exercise("deadlift", "Deadlift", MuscleGroup.Back, "Barbell"),
    exercise("romanian-deadlift", "Romanian deadlift", MuscleGroup.Legs, "Barbell"),
    exercise("sumo-deadlift", "Sumo deadlift", MuscleGroup.Legs, "Barbell"),
    exercise("back-squat", "Back squat", MuscleGroup.Legs, "Barbell"),
    exercise("front-squat", "Front squat", MuscleGroup.Legs, "Barbell"),
    exercise("hack-squat", "Hack squat", MuscleGroup.Legs, "Machine"),
    exercise("leg-press", "Leg press", MuscleGroup.Legs, "Machine"),
    exercise("walking-lunge", "Walking lunge", MuscleGroup.Legs, "Dumbbells"),
    exercise("reverse-lunge", "Reverse lunge", MuscleGroup.Legs, "Dumbbells"),
    exercise("bulgarian-split-squat", "Bulgarian split squat", MuscleGroup.Legs, "Dumbbells"),
    exercise("step-up", "Step-up", MuscleGroup.Legs, "Dumbbells"),
    exercise("leg-extension", "Leg extension", MuscleGroup.Legs, "Machine"),
    exercise("lying-leg-curl", "Lying leg curl", MuscleGroup.Legs, "Machine"),
    exercise("seated-leg-curl", "Seated leg curl", MuscleGroup.Legs, "Machine"),
    exercise("hip-thrust", "Hip thrust", MuscleGroup.Legs, "Barbell"),
    exercise("glute-bridge", "Glute bridge", MuscleGroup.Legs, "Bodyweight"),
    exercise("standing-calf-raise", "Standing calf raise", MuscleGroup.Legs, "Machine"),
    exercise("seated-calf-raise", "Seated calf raise", MuscleGroup.Legs, "Machine"),
    exercise("overhead-press", "Overhead press", MuscleGroup.Shoulders, "Barbell"),
    exercise("seated-dumbbell-press", "Seated dumbbell press", MuscleGroup.Shoulders, "Dumbbells"),
    exercise("arnold-press", "Arnold press", MuscleGroup.Shoulders, "Dumbbells"),
    exercise("machine-shoulder-press", "Machine shoulder press", MuscleGroup.Shoulders, "Machine"),
    exercise("lateral-raise", "Lateral raise", MuscleGroup.Shoulders, "Dumbbells"),
    exercise("cable-lateral-raise", "Cable lateral raise", MuscleGroup.Shoulders, "Cable"),
    exercise("rear-delt-fly", "Rear delt fly", MuscleGroup.Shoulders, "Dumbbells"),
    exercise("reverse-pec-deck", "Reverse pec deck", MuscleGroup.Shoulders, "Machine"),
    exercise("upright-row", "Upright row", MuscleGroup.Shoulders, "Barbell"),
    exercise("barbell-shrug", "Barbell shrug", MuscleGroup.Shoulders, "Barbell"),
    exercise("dumbbell-shrug", "Dumbbell shrug", MuscleGroup.Shoulders, "Dumbbells"),
    exercise("barbell-curl", "Barbell curl", MuscleGroup.Arms, "Barbell"),
    exercise("ez-bar-curl", "EZ-bar curl", MuscleGroup.Arms, "EZ bar"),
    exercise("dumbbell-curl", "Dumbbell curl", MuscleGroup.Arms, "Dumbbells"),
    exercise("hammer-curl", "Hammer curl", MuscleGroup.Arms, "Dumbbells"),
    exercise("incline-curl", "Incline curl", MuscleGroup.Arms, "Dumbbells"),
    exercise("preacher-curl", "Preacher curl", MuscleGroup.Arms, "Bench"),
    exercise("cable-curl", "Cable curl", MuscleGroup.Arms, "Cable"),
    exercise("skull-crusher", "Skull crusher", MuscleGroup.Arms, "EZ bar"),
    exercise("triceps-pushdown", "Triceps pushdown", MuscleGroup.Arms, "Cable"),
    exercise("overhead-triceps-extension", "Overhead triceps extension", MuscleGroup.Arms, "Cable"),
    exercise("close-grip-bench", "Close-grip bench press", MuscleGroup.Arms, "Barbell"),
    exercise("bench-dip", "Bench dip", MuscleGroup.Arms, "Bodyweight"),
    exercise("plank", "Plank", MuscleGroup.Core, "Bodyweight"),
    exercise("side-plank", "Side plank", MuscleGroup.Core, "Bodyweight"),
    exercise("hanging-leg-raise", "Hanging leg raise", MuscleGroup.Core, "Bars"),
    exercise("captains-chair-raise", "Captain's chair raise", MuscleGroup.Core, "Machine"),
    exercise("cable-crunch", "Cable crunch", MuscleGroup.Core, "Cable"),
    exercise("ab-wheel-rollout", "Ab wheel rollout", MuscleGroup.Core, "Wheel"),
    exercise("russian-twist", "Russian twist", MuscleGroup.Core, "Medicine ball"),
    exercise("pallof-press", "Pallof press", MuscleGroup.Core, "Cable"),
    exercise("farmer-carry", "Farmer carry", MuscleGroup.FullBody, "Dumbbells"),
    exercise("sled-push", "Sled push", MuscleGroup.FullBody, "Sled"),
    exercise("kettlebell-swing", "Kettlebell swing", MuscleGroup.FullBody, "Kettlebell"),
    exercise("clean-and-press", "Clean and press", MuscleGroup.FullBody, "Barbell"),
    exercise("thruster", "Thruster", MuscleGroup.FullBody, "Barbell"),
    exercise("burpee", "Burpee", MuscleGroup.FullBody, "Bodyweight"),
    exercise("rower", "Rower", MuscleGroup.Cardio, "Machine"),
    exercise("assault-bike", "Assault bike", MuscleGroup.Cardio, "Bike"),
    exercise("incline-treadmill-walk", "Incline treadmill walk", MuscleGroup.Cardio, "Treadmill"),
    exercise("stair-climber", "Stair climber", MuscleGroup.Cardio, "Machine"),
    exercise("jump-rope", "Jump rope", MuscleGroup.Cardio, "Rope"),
    exercise("battle-ropes", "Battle ropes", MuscleGroup.Cardio, "Ropes")
)

val seedFoods = listOf(
    FoodHit(
        id = "off-greek-yogurt",
        name = "Greek yogurt, plain",
        brand = "Open product",
        source = NutritionSource.OpenFoodFacts,
        servingLabel = "100 g",
        calories = 97,
        proteinGrams = 9.0,
        carbGrams = 3.9,
        fatGrams = 5.0
    ),
    FoodHit(
        id = "fdc-chicken-breast",
        name = "Chicken breast, cooked",
        brand = "USDA reference",
        source = NutritionSource.FoodDataCentral,
        servingLabel = "100 g",
        calories = 165,
        proteinGrams = 31.0,
        carbGrams = 0.0,
        fatGrams = 3.6
    ),
    FoodHit(
        id = "ninja-oats",
        name = "Rolled oats",
        brand = "Parsed meal",
        source = NutritionSource.CalorieNinjas,
        servingLabel = "100 g",
        calories = 389,
        proteinGrams = 16.9,
        carbGrams = 66.3,
        fatGrams = 6.9
    ),
    FoodHit(
        id = "fatsecret-salmon",
        name = "Atlantic salmon",
        brand = "FatSecret verified",
        source = NutritionSource.FatSecret,
        servingLabel = "100 g",
        calories = 208,
        proteinGrams = 20.4,
        carbGrams = 0.0,
        fatGrams = 13.4
    ),
    FoodHit(
        id = "fdc-rice",
        name = "White rice, cooked",
        brand = "USDA reference",
        source = NutritionSource.FoodDataCentral,
        servingLabel = "100 g",
        calories = 130,
        proteinGrams = 2.7,
        carbGrams = 28.0,
        fatGrams = 0.3
    ),
    FoodHit(
        id = "off-banana",
        name = "Banana",
        brand = "Open product",
        source = NutritionSource.OpenFoodFacts,
        servingLabel = "100 g",
        calories = 89,
        proteinGrams = 1.1,
        carbGrams = 22.8,
        fatGrams = 0.3
    )
)

val seedRoutines = listOf(
    RoutineTemplate(
        id = "push-strength",
        name = "Push strength",
        exercises = listOf(
            RoutineExercise("barbell-bench-press", targetSets = 4, targetReps = "4-6", restSeconds = 150),
            RoutineExercise("overhead-press", targetSets = 3, targetReps = "5-8", restSeconds = 120),
            RoutineExercise("incline-dumbbell-press", targetSets = 3, targetReps = "8-10", restSeconds = 90),
            RoutineExercise("triceps-pushdown", targetSets = 3, targetReps = "10-12", restSeconds = 75)
        )
    ),
    RoutineTemplate(
        id = "pull-hypertrophy",
        name = "Pull hypertrophy",
        exercises = listOf(
            RoutineExercise("pull-up", targetSets = 4, targetReps = "AMRAP", restSeconds = 120),
            RoutineExercise("barbell-row", targetSets = 4, targetReps = "6-8", restSeconds = 120),
            RoutineExercise("lat-pulldown", targetSets = 3, targetReps = "10-12", restSeconds = 90),
            RoutineExercise("hammer-curl", targetSets = 3, targetReps = "10-12", restSeconds = 75)
        )
    ),
    RoutineTemplate(
        id = "leg-day",
        name = "Leg day",
        exercises = listOf(
            RoutineExercise("back-squat", targetSets = 5, targetReps = "3-5", restSeconds = 180),
            RoutineExercise("romanian-deadlift", targetSets = 4, targetReps = "6-8", restSeconds = 150),
            RoutineExercise("leg-press", targetSets = 3, targetReps = "10-12", restSeconds = 120),
            RoutineExercise("standing-calf-raise", targetSets = 4, targetReps = "12-15", restSeconds = 60)
        )
    )
)
