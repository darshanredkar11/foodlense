package com.foodlense.android.ai

import kotlin.test.Test
import kotlin.test.assertContains

class LocalFoodAnswerProviderTest {
    private val provider = LocalFoodAnswerProvider()

    @Test
    fun answersMsgQuestionWithoutNetwork() = kotlinx.coroutines.test.runTest {
        val answer = provider.answer("Sugar, E621, salt", "Is E621 bad?")
        assertContains(answer, "E621")
        assertContains(answer, "MSG")
    }

    @Test
    fun dailyQuestionUsesFrequencyContext() = kotlinx.coroutines.test.runTest {
        val answer = provider.answer("Sugar, palm oil", "Can I eat this every day?")
        assertContains(answer, "everyday")
        assertContains(answer, "nutrition")
    }

    @Test
    fun asksForNutritionPanelWhenSodiumIsQuestioned() = kotlinx.coroutines.test.runTest {
        val answer = provider.answer("Wheat flour, salt", "How much sodium is okay?")
        assertContains(answer, "nutrition panel")
    }

    @Test
    fun unknownQuestionStaysGroundedInScannedText() = kotlinx.coroutines.test.runTest {
        val answer = provider.answer("Sugar, palm oil", "Tell me something interesting")
        assertContains(answer, "added sugars")
    }
}
