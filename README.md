# PulseKMP

PulseKMP is a Kotlin Multiplatform Compose tracker with three core areas:

- Macro tracker with provider boundaries for Open Food Facts, USDA FoodData Central, CalorieNinjas, and FatSecret Platform API.
- Gym tracker with a large seed exercise library, custom exercises, routine creation, set logging, PR celebrations, rest timer, and progressive overload chart.
- Water tracker with daily target progress, quick-add controls, and hydration rhythm.

It also includes a target-day calendar, streak logic with a two-day skip buffer, PR/streak/target celebrations, and a light/dark theme toggle.

## Project Shape

```text
composeApp/
  src/commonMain/     Shared Compose UI, state, domain, API clients
  src/androidMain/    Android entry point
  src/desktopMain/    Desktop entry point for local preview
  src/iosMain/        iOS ComposeUIViewController entry point
```

## Run

Use Android Studio or IntelliJ with JDK 17 or newer.

```bash
./gradlew :composeApp:run
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:allTests
```

On Windows PowerShell, use `.\gradlew.bat` if you add/generate the Gradle wrapper from Android Studio.

## API Wiring

The app currently uses rich seed data in the UI so it is usable immediately. Live nutrition APIs are isolated in:

```text
composeApp/src/commonMain/kotlin/app/pulsefit/data/NutritionClients.kt
```

Example composition:

```kotlin
val client = nutritionHttpClient()
val hub = NutritionSearchHub(
    providers = listOf(
        OpenFoodFactsProvider(client),
        FoodDataCentralProvider(client, apiKey = "YOUR_FDC_KEY"),
        CalorieNinjasProvider(client, apiKey = "YOUR_CALORIE_NINJAS_KEY"),
        FatSecretProvider(client, oauthBearerToken = "YOUR_FATSECRET_OAUTH_TOKEN")
    )
)
```

For production, proxy keyed providers through your backend. Do not ship FoodData Central, CalorieNinjas, or FatSecret secrets inside a mobile app binary.

Notes:

- Open Food Facts docs currently recommend `/api/v2/search` for structured search and note that legacy keyword search exists at `/cgi/search.pl`.
- FoodData Central requires a data.gov API key; `DEMO_KEY` is useful only for low-rate exploration.
- CalorieNinjas requires the `X-Api-Key` header.
- FatSecret supports OAuth 2.0 and the current search endpoint is path based at `/rest/foods/search/v5`.

## Implemented Screens

- `Today`: combined macro, water, workout, streak, calendar, celebrations, and overload chart.
- `Macros`: source chips, meal slots, gram input, food result cards, and daily macro totals.
- `Gym`: exercise filters/search, custom exercises, routines, rest timer, set logging, and PR detection.
- `Water`: quick-add hydration, custom amount, daily target progress, and rhythm checklist.
- `Progress`: target-day calendar, streak status, overload chart, and celebration feed.

## Key Business Rules

- A target day is hit when macros, water, and at least three workout sets meet the daily goals.
- Streaks count target-hit days and allow up to two skipped days between hits.
- A streak resets after the third missed day.
- PRs are detected with the Epley estimated 1RM formula: `weight * (1 + reps / 30)`.
