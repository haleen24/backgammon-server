package player.response

data class JwtResponse(
    val token: String,
    val userId: Long
)