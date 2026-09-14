package com.foodlense.shared.domain.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class UserProfileTest {
    @Test
    fun `profile defaults to lightweight personalization`() {
        val profile = UserProfile(userId = "user-1")

        assertEquals(LanguagePreference.AUTO, profile.language)
        assertEquals(DietPreference.NOT_SET, profile.diet)
        assertEquals(emptySet(), profile.goals)
        assertEquals(TonePreference.LIGHT, profile.preferredTone)
    }

    @Test
    fun `explicit preferences are represented without inferred sensitive data`() {
        val profile = UserProfile(
            userId = "user-1",
            language = LanguagePreference.MARATHI,
            diet = DietPreference.VEGETARIAN,
            goals = setOf(FoodGoal.HIGH_PROTEIN, FoodGoal.LOWER_SUGAR),
        )

        assertEquals(LanguagePreference.MARATHI, profile.language)
        assertEquals(DietPreference.VEGETARIAN, profile.diet)
        assertEquals(setOf(FoodGoal.HIGH_PROTEIN, FoodGoal.LOWER_SUGAR), profile.goals)
    }
}
