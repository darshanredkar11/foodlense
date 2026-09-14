package com.foodlense.android.ai

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IngredientKnowledgeBaseTest {
    private val knowledge = IngredientKnowledgeBase()

    @Test
    fun recognizesIndianInsAliases() {
        val result = knowledge.inspect("Refined wheat flour, sugar, INS 621, INS 322")
        assertEquals(listOf("e621", "e322", "sugar"), result.map { it.key })
    }

    @Test
    fun recognizesCommonNames() {
        val result = knowledge.inspect("Monosodium glutamate, palmolein oil, sucralose")
        assertEquals(listOf("e621", "palm", "e955"), result.map { it.key })
    }

    @Test
    fun doesNotTurnUnknownIngredientIntoAHealthClaim() {
        val result = knowledge.inspect("maltodextrin, wheat flour, spices")
        assertTrue(result.isEmpty())
    }

    @Test
    fun hydrogenatedFatGetsStrongerCautionThanOrdinaryAdditives() {
        val result = knowledge.inspect("hydrogenated vegetable fat, E621")
        assertEquals(RiskLevel.LIMIT, result.first { it.key == "hydrogenated" }.risk)
        assertEquals(RiskLevel.GENERALLY_OK, result.first { it.key == "e621" }.risk)
    }
}
