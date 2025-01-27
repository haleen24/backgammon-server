package player.response

data class JwtResponse(
    val token: String,
    val userId: Long
)

data class GetFriendResponse(
    val username: String,
    val friendId: Long
)