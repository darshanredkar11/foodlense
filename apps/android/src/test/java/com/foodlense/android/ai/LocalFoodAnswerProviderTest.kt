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
    }

    @Test
    fun dailyQuestionUsesFrequencyContext() = runBlocking {
        val answer = provider.answer("Sugar, palm oil", "Can I eat this every day?")
        assertContains(answer, "everyday")
        assertContains(answer, "nutrition")
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
    }
}
