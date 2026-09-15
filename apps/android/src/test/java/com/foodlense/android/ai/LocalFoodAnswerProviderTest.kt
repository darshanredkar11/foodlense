package com.foodlense.android.ai

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlinx.coroutines.runBlocking

class LocalFoodAnswerProviderTest {
    private val provider = LocalFoodAnswerProvider()

    @Test
    fun answersMsgQuestionWithoutNetwork() = runBlocking {
        val answer = provider.answer("Sugar, E621, salt", "Is E621 bad?")
        assertContains(answer, "E621")
        assertContains(answer, "MSG")
        assertContains(answer, "generally considered safe")
    }

    @Test
    fun dailyQuestionUsesFrequencyContext() = runBlocking {
        val answer = provider.answer("Sugar, palm oil", "Can I eat this every day?")
        assertContains(answer, "everyday food")
        assertContains(answer, "nutrition picture")
    }

    @Test
    fun asksForNutritionPanelWhenSodiumIsQuestioned() = runBlocking {
        val answer = provider.answer("Wheat flour, salt", "How much sodium is okay?")
        assertContains(answer, "nutrition panel")
    }

    @Test
    fun unknownQuestionStaysGroundedInScannedText() = runBlocking {
        val answer = provider.answer("Sugar, palm oil", "Tell me something interesting")
        assertContains(answer, "palm / palmolein oil")
    }

    @Test
    fun sugarQuestionRecognizesSugarFromScannedText() = runBlocking {
        val answer = provider.answer("Sugar, palm oil", "Does this contain sugar?")
        assertContains(answer, "Sugar appears in the ingredient list")
    }

    @Test
    fun recognizesIndianInsAlias() = runBlocking {
        val answer = provider.answer("Wheat flour, INS 621, salt", "Is this ingredient safe?")
        assertContains(answer, "E621")
        assertContains(answer, "generally okay")
    }

    @Test
    fun unknownLabelDoesNotInventAnIngredient() = runBlocking {
        val answer = provider.answer("Water, wheat flour", "Is this healthy?")
        assertContains(answer, "don't have enough recognized ingredients")
        assertContains(answer, "nutrition panel")
    }

    @Test
    fun explainAllQuestionReturnsEveryParsedIngredient() = runBlocking {
        val answer = provider.answer(
            "Ingredients: sugar, wheat flour, palm oil, E322, potassium sorbate",
            "Explain all ingredients",
        )
        assertContains(answer, "1. sugar")
        assertContains(answer, "2. wheat flour")
        assertContains(answer, "3. palm oil")
        assertContains(answer, "4. E322")
        assertContains(answer, "5. potassium sorbate")
        assertContains(answer, "Added sugars")
        assertContains(answer, "E322 · Lecithin")
    }

    @Test
    fun explainEveryIngredientDoesNotAskUserToPickOne() = runBlocking {
        val answer = provider.answer(
            "Ingredients: sugar, wheat flour, palm oil",
            "Can you explain every ingredient in this list?",
        )
        assertFalse(answer.contains("Pick the name that caught your eye"))
        assertContains(answer, "whole ingredient list")
    }
}
