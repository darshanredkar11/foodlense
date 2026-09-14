package com.foodlense.android.ai

import kotlin.test.Test
import kotlin.test.assertContains
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
        assertContains(answer, "added sugars")
        assertContains(answer, "palm / palmolein oil")
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
}
