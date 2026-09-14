package com.foodlense.shared.contracts.identity

/** Platform-neutral identity capability. Implementations may use passkeys. */
interface IdentityProvider {
    suspend fun signIn(): IdentityResult
}

data class IdentityResult(
    val userId: String,
)

/** Minimal device registration information; avoid collecting hardware identifiers. */
data class DeviceRegistration(
    val deviceId: String,
    val platform: Platform,
    val appVersion: String,
)

enum class Platform {
    ANDROID,
    IOS,
    WEB,
}
