package player.response

import player.InvitePolicy

data class JwtResponse(
    val token: String,
    val userId: Long
)

data class GetFriendResponse(
    val username: String,
    val friendId: Long
)

data class UserInfoResponse(
    val id: Long,
    val username: String,
    val login: String,
    val invitePolicy: InvitePolicy
)