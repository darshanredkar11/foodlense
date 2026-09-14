package com.foodlense.android.ai

interface FoodAnswerProvider {
    suspend fun answer(ingredients: String, question: String): String
}
