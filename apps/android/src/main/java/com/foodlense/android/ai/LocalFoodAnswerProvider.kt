package com.foodlense.android.ai

/**
 * Zero-cost on-device answer engine used until an external AI provider is configured.
 * It deliberately avoids claiming medical certainty and keeps the answer grounded in
 * the ingredient text the scanner actually observed.
 */
class LocalFoodAnswerProvider : FoodAnswerProvider {
    override suspend fun answer(ingredients: String, question: String): String {
        val q = question.trim().lowercase()
        val normalized = ingredients.lowercase()
        val flags = detectedFlags(normalized)
        val firstFlag = flags.firstOrNull() ?: "the ingredients I could read"

        return when {
            q.isBlank() -> "Tell me what you're wondering about and I'll break it down from the label."
            "msg" in q || "e621" in q ->
                "E621 (MSG) is generally considered safe at normal dietary levels. The more useful question is the overall food — especially sodium and how often you eat it."
            "daily" in q || "every day" in q || "often" in q ->
                "If this is an everyday food, I'd zoom out from one additive and look at sugar, sodium, saturated fat, protein and fibre too. Frequency changes the picture."
            "sugar" in q ->
                if ("sugar" in normalized) {
                    "Sugar is on the label I could read. If you send me the nutrition panel too, I can help put the amount into context rather than judging the ingredient alone."
                } else {
                    "I don't see the word sugar in the ingredient text I received. The nutrition panel would give us a more reliable answer."
                }
            "salt" in q || "sodium" in q ->
                "Sodium is better judged from the nutrition panel than the ingredient list. If you scan that panel too, I can put the number into everyday context."
            "bad" in q || "harm" in q || "safe" in q || "healthy" in q ->
                "I wouldn't label the whole product as harmful or healthy from this ingredient list alone. $firstFlag is something to understand, not automatically fear. The full nutrition panel and how often you eat it matter too."
            "ingredient" in q || "what is" in q || "what's" in q ->
                "I can explain individual ingredients one by one. Pick the name that caught your eye and I'll translate the food-science language into plain English."
            else ->
                "Good question. From the label I could read, $firstFlag stands out. I can go deeper if you tell me what you're most curious about — safety, nutrition, additives, or whether it makes sense as an everyday food."
        }
    }

    private fun detectedFlags(text: String): List<String> = buildList {
        if (listOf("e621", "monosodium glutamate", "msg").any(text::contains)) add("E621 · MSG")
        if (listOf("palmolein", "palm oil", "hydrogenated", "partially hydrogenated").any(text::contains)) add("palm / hydrogenated fat")
        if (listOf("aspartame", "sucralose", "acesulfame", "e951", "e955", "e950").any(text::contains)) add("sweeteners")
        if (listOf("sugar", "glucose syrup", "fructose").any(text::contains)) add("added sugars")
    }
}
