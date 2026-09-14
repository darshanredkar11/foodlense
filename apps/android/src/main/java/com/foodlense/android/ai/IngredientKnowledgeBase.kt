package com.foodlense.android.ai

/** Deterministic, offline ingredient knowledge for the demo. Aliases cover common Indian labels. */
class IngredientKnowledgeBase {
    fun inspect(rawText: String): List<IngredientInsight> {
        val text = rawText.lowercase()
        return entries.filter { entry -> entry.aliases.any { alias -> text.contains(alias) } }
            .map { it.insight }
            .distinctBy { it.key }
    }

    private data class Entry(val aliases: List<String>, val insight: IngredientInsight)

    private val entries = listOf(
        Entry(listOf("e621", "ins 621", "monosodium glutamate", "msg"), IngredientInsight("e621", "E621 · MSG", "Flavour enhancer", RiskLevel.GENERALLY_OK, "Generally considered safe at normal dietary levels. It can contribute to the overall flavour/sodium profile of a processed food.")),
        Entry(listOf("e322", "ins 322", "lecithin"), IngredientInsight("e322", "E322 · Lecithin", "Emulsifier", RiskLevel.GENERALLY_OK, "Commonly used to help ingredients mix. Its presence alone isn't a reason to consider a food harmful.")),
        Entry(listOf("palmolein", "palm oil", "palmolein oil"), IngredientInsight("palm", "Palm / palmolein oil", "Vegetable fat", RiskLevel.WORTH_KNOWING, "Worth considering in the context of saturated fat and how often the product is eaten.")),
        Entry(listOf("hydrogenated", "partially hydrogenated"), IngredientInsight("hydrogenated", "Hydrogenated fat", "Processed fat", RiskLevel.LIMIT, "This is more important to investigate than many ordinary additives. Check the nutrition panel and the product's declared trans-fat and saturated-fat values.")),
        Entry(listOf("e950", "ins 950", "acesulfame potassium", "acesulfame k"), IngredientInsight("e950", "E950 · Acesulfame-K", "Sweetener", RiskLevel.WORTH_KNOWING, "A high-intensity sweetener. Its presence doesn't by itself mean the product is unsafe; context and frequency matter.")),
        Entry(listOf("e951", "ins 951", "aspartame"), IngredientInsight("e951", "E951 · Aspartame", "Sweetener", RiskLevel.WORTH_KNOWING, "A regulated high-intensity sweetener. People with phenylketonuria need to avoid phenylalanine-containing sources.")),
        Entry(listOf("e955", "ins 955", "sucralose"), IngredientInsight("e955", "E955 · Sucralose", "Sweetener", RiskLevel.WORTH_KNOWING, "A high-intensity sweetener. It should be judged in the context of the whole product rather than as a simple good/bad ingredient.")),
        Entry(listOf("sugar", "sucrose", "glucose syrup", "fructose syrup"), IngredientInsight("sugar", "Added sugars", "Sweetener", RiskLevel.WORTH_KNOWING, "Ingredient lists tell us presence, not quantity. The nutrition panel is needed to understand how much sugar the serving contains.")),
    )
}

data class IngredientInsight(
    val key: String,
    val name: String,
    val category: String,
    val risk: RiskLevel,
    val explanation: String,
)

enum class RiskLevel { GENERALLY_OK, WORTH_KNOWING, LIMIT, POTENTIAL_CONCERN }
