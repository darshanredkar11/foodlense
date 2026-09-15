package com.foodlense.android.ai

/**
 * Turns a complete ingredient-list OCR result into a plain-English explanation.
 * The demo is deliberately conservative: when an ingredient is not in the
 * verified offline knowledge base, it says so instead of inventing a safety claim.
 */
class IngredientExplainer(
    private val knowledge: IngredientKnowledgeBase = IngredientKnowledgeBase(),
) {
    fun explainAll(rawText: String): String {
        val ingredients = parseIngredients(rawText)
        if (ingredients.isEmpty()) {
            return "I couldn't separate a complete ingredient list from this read yet."
        }

        val lines = ingredients.mapIndexed { index, ingredient ->
            val insight = knowledge.inspect(ingredient).firstOrNull()
            "${index + 1}. $ingredient — ${insight?.let(::explainKnown) ?: explainHeuristically(ingredient)}"
        }

        return "Here’s the whole ingredient list, one by one:\n\n" + lines.joinToString("\n\n")
    }

    private fun explainKnown(insight: IngredientInsight): String =
        "${insight.category}: ${insight.explanation}"

    private fun explainHeuristically(ingredient: String): String {
        val value = ingredient.lowercase()
        return when {
            value == "water" || value == "aqua" ->
                "Base/solvent: usually used to dissolve and carry the other ingredients."
            value.contains("sugar") || value.contains("syrup") || value.contains("sucrose") ->
                "Sugar source: contributes sweetness and carbohydrates. The nutrition panel is needed to know how much is present."
            value.contains("oil") || value.contains("fat") ->
                "Fat/oil: contributes texture and fat. The type and the nutrition panel matter more than the name alone."
            value.contains("acid") || value.contains("citrate") || value.contains("lactate") ->
                "Acidity regulator: commonly used to control pH and tartness. Its name alone is not a reason to call the food unsafe."
            value.contains("salt") || value.contains("sodium") ->
                "Sodium-containing ingredient: can contribute to total sodium intake. The nutrition panel is the better measure of how much."
            value.contains("starch") || value.contains("flour") ->
                "Carbohydrate/thickening ingredient: often used for structure, texture or bulk."
            value.contains("preserv") || value.contains("sorbate") || value.contains("benzoate") ->
                "Preservative: helps slow spoilage and extend shelf life. The specific compound and amount determine the relevant context."
            value.contains("emuls") || value.contains("gum") || value.contains("thickener") ->
                "Texture/stability ingredient: helps ingredients stay mixed or gives the product its intended consistency."
            value.contains("colour") || value.contains("color") ->
                "Colour additive: used to give or restore the product's appearance."
            value.contains("flavour") || value.contains("flavor") ->
                "Flavouring: used to provide or reinforce the product's taste or aroma."
            else ->
                "I can read the ingredient name, but this offline demo doesn't have a verified explanation for it yet, so I won't invent one."
        }
    }

    private fun parseIngredients(rawText: String): List<String> {
        return rawText
            .replace(Regex("(?i)ingredients?\\s*[:\\-]?"), "")
            .replace(Regex("(?i)contains\\s*[:\\-]?"), "")
            .split(',', ';', '\n')
            .map { it.trim().trim('.', ':', '-', '•') }
            .filter { it.length >= 2 }
            .filterNot { it.equals("product barcode", ignoreCase = true) }
            .distinctBy { it.lowercase() }
    }
}
