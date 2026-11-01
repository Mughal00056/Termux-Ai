package com.example.domain.model

enum class AuthType {
    PASSWORD,
    PRIVATE_KEY
}

data class Server(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val host: String,
    val port: Int = 22,
    val username: String,
    val authType: AuthType = AuthType.PASSWORD,
    val encryptedCredentialKey: String = "", // Reference in Android Keystore
    val fingerprint: String = "",
    val isTrusted: Boolean = false,
    val lastConnected: Long = 0L,
    val isOnline: Boolean = true
)
