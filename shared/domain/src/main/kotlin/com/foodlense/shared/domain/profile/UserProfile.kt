package com.foodlense.shared.domain.profile

/**
 * Explicit preferences and lightweight personalization signals.
 * Identity and device credentials intentionally live outside the domain model.
 */
data class UserProfile(
    val userId: String,
    val language: LanguagePreference = LanguagePreference.AUTO,
    val diet: DietPreference = DietPreference.NOT_SET,
    val goals: Set<FoodGoal> = emptySet(),
    val preferredTone: TonePreference = TonePreference.LIGHT,
)

enum class LanguagePreference {
    AUTO,
    ENGLISH,
    HINDI,
    MARATHI,
}

enum class DietPreference {
    NOT_SET,
    VEGETARIAN,
    VEGAN,
    EGGETARIAN,
    NON_VEGETARIAN,
}

enum class FoodGoal {
    BALANCED_EATING,
    HIGH_PROTEIN,
    LOWER_SUGAR,
    LOWER_SALT,
    WEIGHT_MANAGEMENT,
}

enum class TonePreference {
    LIGHT,
    DIRECT,
}
