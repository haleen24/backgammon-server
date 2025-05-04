package hse.playerservice.mapper

import hse.playerservice.entity.User
import org.springframework.stereotype.Component
import player.InvitePolicy
import player.response.UserInfoResponse

@Component
class UserMapper {
    fun toUserInfoResponse(user: User): UserInfoResponse {
        return UserInfoResponse(
            user.id,
            user.username,
            user.login,
            InvitePolicy.ofCode(user.invitePolicyCode)
        )
    }
}