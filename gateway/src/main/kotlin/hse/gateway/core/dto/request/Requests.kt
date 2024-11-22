package hse.gateway.core.dto.request

data class AuthRequest(
    val username: String,
    val password: String
)


data class JwtRequest(
    val accessToken: String,
    val token: String
)

