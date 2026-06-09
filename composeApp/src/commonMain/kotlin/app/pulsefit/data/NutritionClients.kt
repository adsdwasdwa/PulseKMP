package app.pulsefit.data

import app.pulsefit.domain.FoodHit
import app.pulsefit.domain.NutritionSource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.roundToInt

interface NutritionProvider {
    val source: NutritionSource
    suspend fun search(query: String, maxResults: Int = 20): List<FoodHit>
}

fun nutritionHttpClient() = HttpClient {
    install(ContentNegotiation) {
        json(nutritionJson)
    }
}

val nutritionJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

class NutritionSearchHub(
    private val providers: List<NutritionProvider>
) {
    suspend fun search(
        query: String,
        sources: Set<NutritionSource>,
        maxResultsPerSource: Int = 10
    ): List<FoodHit> {
        return providers
            .filter { it.source in sources }
            .flatMap { provider -> provider.search(query, maxResultsPerSource) }
            .sortedWith(compareBy<FoodHit> { it.source.ordinal }.thenBy { it.name })
    }
}

class OpenFoodFactsProvider(
    private val client: HttpClient
) : NutritionProvider {
    override val source = NutritionSource.OpenFoodFacts

    override suspend fun search(query: String, maxResults: Int): List<FoodHit> {
        if (query.isBlank()) return emptyList()

        val response: OffSearchResponse = client.get("https://world.openfoodfacts.org/cgi/search.pl") {
            parameter("search_terms", query)
            parameter("search_simple", "1")
            parameter("action", "process")
            parameter("json", "1")
            parameter("page_size", maxResults.coerceIn(1, 50))
            parameter("fields", "code,product_name,brands,nutriments")
        }.body()

        return response.products.mapNotNull { product ->
            val name = product.productName?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val nutrients = product.nutriments
            FoodHit(
                id = "off-${product.code ?: name}",
                name = name,
                brand = product.brands,
                source = source,
                servingLabel = "100 g",
                calories = (nutrients.energyKcal100g ?: nutrients.energyKcalServing ?: 0.0).roundToInt(),
                proteinGrams = nutrients.proteins100g ?: nutrients.proteinsServing ?: 0.0,
                carbGrams = nutrients.carbohydrates100g ?: nutrients.carbohydratesServing ?: 0.0,
                fatGrams = nutrients.fat100g ?: nutrients.fatServing ?: 0.0
            )
        }
    }
}

class FoodDataCentralProvider(
    private val client: HttpClient,
    private val apiKey: String = "DEMO_KEY"
) : NutritionProvider {
    override val source = NutritionSource.FoodDataCentral

    override suspend fun search(query: String, maxResults: Int): List<FoodHit> {
        if (query.isBlank()) return emptyList()

        val response: FdcSearchResponse = client.get("https://api.nal.usda.gov/fdc/v1/foods/search") {
            parameter("api_key", apiKey)
            parameter("query", query)
            parameter("pageSize", maxResults.coerceIn(1, 50))
        }.body()

        return response.foods.mapNotNull { food ->
            val name = food.description?.lowercase()?.replaceFirstChar { it.titlecase() }
                ?: return@mapNotNull null
            FoodHit(
                id = "fdc-${food.fdcId ?: name}",
                name = name,
                brand = food.brandOwner,
                source = source,
                servingLabel = "100 g",
                calories = food.nutrientValue("Energy").roundToInt(),
                proteinGrams = food.nutrientValue("Protein"),
                carbGrams = food.nutrientValue("Carbohydrate"),
                fatGrams = food.nutrientValue("Total lipid")
            )
        }
    }
}

class CalorieNinjasProvider(
    private val client: HttpClient,
    private val apiKey: String?
) : NutritionProvider {
    override val source = NutritionSource.CalorieNinjas

    override suspend fun search(query: String, maxResults: Int): List<FoodHit> {
        if (query.isBlank() || apiKey.isNullOrBlank()) return emptyList()

        val response: CalorieNinjasResponse = client.get("https://api.calorieninjas.com/v1/nutrition") {
            parameter("query", query)
            header("X-Api-Key", apiKey)
        }.body()

        return response.items.take(maxResults).map { item ->
            FoodHit(
                id = "ninja-${item.name}",
                name = item.name.replaceFirstChar { it.titlecase() },
                brand = "CalorieNinjas",
                source = source,
                servingLabel = "${item.servingSizeGrams.roundToInt()} g",
                calories = item.calories.roundToInt(),
                proteinGrams = item.proteinGrams,
                carbGrams = item.carbohydrateGrams,
                fatGrams = item.fatGrams
            )
        }
    }
}

class FatSecretProvider(
    private val client: HttpClient,
    private val oauthBearerToken: String?
) : NutritionProvider {
    override val source = NutritionSource.FatSecret

    override suspend fun search(query: String, maxResults: Int): List<FoodHit> {
        if (query.isBlank() || oauthBearerToken.isNullOrBlank()) return emptyList()

        val text = client.get("https://platform.fatsecret.com/rest/foods/search/v5") {
            header("Authorization", "Bearer $oauthBearerToken")
            parameter("search_expression", query)
            parameter("format", "json")
            parameter("max_results", maxResults.coerceIn(1, 50))
        }.bodyAsText()

        val root = nutritionJson.parseToJsonElement(text).jsonObject
        val search = root["foods_search"]?.jsonObject ?: return emptyList()
        val foodNodes = search["food"].asList()

        return foodNodes.mapNotNull { element ->
            val food = element.jsonObject
            val name = food.string("food_name") ?: return@mapNotNull null
            val brand = food.string("brand_name") ?: food.string("food_type")
            val serving = food["servings"]
                ?.jsonObject
                ?.get("serving")
                .asList()
                .firstOrNull()
                ?.jsonObject

            FoodHit(
                id = "fatsecret-${food.string("food_id") ?: name}",
                name = name,
                brand = brand,
                source = source,
                servingLabel = serving?.string("serving_description") ?: "Serving",
                calories = serving?.double("calories")?.roundToInt() ?: 0,
                proteinGrams = serving?.double("protein") ?: 0.0,
                carbGrams = serving?.double("carbohydrate") ?: 0.0,
                fatGrams = serving?.double("fat") ?: 0.0
            )
        }
    }
}

@Serializable
private data class OffSearchResponse(
    val products: List<OffProduct> = emptyList()
)

@Serializable
private data class OffProduct(
    val code: String? = null,
    @SerialName("product_name") val productName: String? = null,
    val brands: String? = null,
    val nutriments: OffNutriments = OffNutriments()
)

@Serializable
private data class OffNutriments(
    @SerialName("energy-kcal_100g") val energyKcal100g: Double? = null,
    @SerialName("energy-kcal_serving") val energyKcalServing: Double? = null,
    @SerialName("proteins_100g") val proteins100g: Double? = null,
    @SerialName("proteins_serving") val proteinsServing: Double? = null,
    @SerialName("carbohydrates_100g") val carbohydrates100g: Double? = null,
    @SerialName("carbohydrates_serving") val carbohydratesServing: Double? = null,
    @SerialName("fat_100g") val fat100g: Double? = null,
    @SerialName("fat_serving") val fatServing: Double? = null
)

@Serializable
private data class FdcSearchResponse(
    val foods: List<FdcFood> = emptyList()
)

@Serializable
private data class FdcFood(
    val fdcId: Long? = null,
    val description: String? = null,
    val brandOwner: String? = null,
    val foodNutrients: List<FdcNutrient> = emptyList()
) {
    fun nutrientValue(namePart: String): Double {
        return foodNutrients.firstOrNull {
            it.nutrientName?.contains(namePart, ignoreCase = true) == true
        }?.value ?: 0.0
    }
}

@Serializable
private data class FdcNutrient(
    val nutrientName: String? = null,
    val value: Double? = null
)

@Serializable
private data class CalorieNinjasResponse(
    val items: List<CalorieNinjaFood> = emptyList()
)

@Serializable
private data class CalorieNinjaFood(
    val name: String,
    val calories: Double = 0.0,
    @SerialName("serving_size_g") val servingSizeGrams: Double = 100.0,
    @SerialName("protein_g") val proteinGrams: Double = 0.0,
    @SerialName("carbohydrates_total_g") val carbohydrateGrams: Double = 0.0,
    @SerialName("fat_total_g") val fatGrams: Double = 0.0
)

private fun JsonElement?.asList(): List<JsonElement> {
    return when (this) {
        is JsonArray -> jsonArray
        is JsonObject -> listOf(this)
        else -> emptyList()
    }
}

private fun JsonObject.string(key: String): String? {
    return this[key]?.jsonPrimitive?.content
}

private fun JsonObject.double(key: String): Double? {
    return this[key]?.jsonPrimitive?.doubleOrNull
}
