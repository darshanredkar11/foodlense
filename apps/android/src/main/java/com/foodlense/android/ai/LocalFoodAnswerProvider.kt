package com.foodlense.android.ai

/** Offline answer engine. It uses structured ingredient knowledge but never equates a match with "harmful". */
class LocalFoodAnswerProvider(
    private val knowledge: IngredientKnowledgeBase = IngredientKnowledgeBase(),
) : FoodAnswerProvider {
    override suspend fun answer(ingredients: String, question: String): String {
        val q = question.trim().lowercase()
        val insights = knowledge.inspect(ingredients)
        val first = insights.firstOrNull()

        return when {
            q.isBlank() -> "Tell me what you're wondering about and I'll break it down from the label."
            q.contains("msg") || q.contains("e621") -> {
                val msg = insights.firstOrNull { it.key == "e621" }
                msg?.let { "${it.name} is a flavour enhancer and is generally considered safe at normal dietary levels. The more useful question is the overall food — especially sodium and how often you eat it." }
                    ?: "I don't see E621/MSG in the label text I could read. If the print is tiny, try scanning that section again."
            }
            q.contains("daily") || q.contains("every day") || q.contains("often") ->
                "If this is an everyday food, I'd zoom out from one additive and look at sugar, sodium, saturated fat, protein and fibre too. Frequency changes the picture."
            q.contains("sugar") ->
                if (insights.any { it.key == "sugar" }) "Sugar appears in the ingredient list. The ingredient list tells us presence, not quantity — the nutrition panel is needed to judge how much you're getting." else "I don't see sugar in the ingredient text I received. The nutrition panel would give us a more reliable answer."
            q.contains("salt") || q.contains("sodium") ->
                "Sodium is better judged from the nutrition panel than the ingredient list. If you scan that panel too, I can put the number into everyday context."
            q.contains("bad") || q.contains("harm") || q.contains("safe") || q.contains("healthy") ->
                if (first != null) "I wouldn't call the whole product harmful from this label alone. ${first.name} is ${riskPhrase(first.risk)}. The full nutrition panel and how often you eat it matter too." else "I can't make a reliable health judgement from the text I could read yet. The nutrition panel plus the complete ingredient list would give us much more context."
            q.contains("ingredient") || q.contains("what is") || q.contains("what's") ->
                "I can explain individual ingredients one by one. Pick the name that caught your eye and I'll translate the food-science language into plain English."
            else ->
                if (first != null) "Good question. From the label I could read, ${first.name} stands out. It's ${first.category.lowercase()}; ${first.explanation.lowercase()}" else "Good question. I don't have enough recognized ingredients from this scan yet. Try holding the camera closer to the ingredients list."
        }
    }

    private fun riskPhrase(risk: RiskLevel): String = when (risk) {
        RiskLevel.GENERALLY_OK -> "generally okay at normal dietary levels"
        RiskLevel.WORTH_KNOWING -> "worth knowing about, rather than automatically avoiding"
        RiskLevel.LIMIT -> "something I'd pay closer attention to, especially if eaten frequently"
        RiskLevel.POTENTIAL_CONCERN -> "something that deserves a closer look before making a judgement"
    }
}
