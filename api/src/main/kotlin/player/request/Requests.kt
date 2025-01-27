package player.request

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import player.InvitePolicy

data class AuthRequest(
    val login: String,
    val password: String
)


data class JwtRequest(
    val accessToken: String,
    val token: String
)

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

data class DeleteUserRequest(
    val password: String
)

data class UpdateUsernameRequest(
    val newUsername: String
)

data class CreateUserRequest(
    val login: String,
    val password: String,
    val username: String = login,
)

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes(
    value =
    [
        JsonSubTypes.Type(value = AddFriendRequest.AddFriendByLogin::class, name = "byLogin"),
        JsonSubTypes.Type(value = AddFriendRequest.AddFriendById::class, name = "byId")
    ]
)
open class AddFriendRequest(
    val type: AddType
) {
    enum class AddType {
        BY_ID,
        BY_LOGIN
    }

    data class AddFriendById(val friendId: Long) : AddFriendRequest(AddType.BY_ID)
    data class AddFriendByLogin(val friendLogin: String) : AddFriendRequest(AddType.BY_LOGIN)
}


@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes(
    value =
    [
        JsonSubTypes.Type(value = RemoveFriendRequest.RemoveFriendByLogin::class, name = "byLogin"),
        JsonSubTypes.Type(value = RemoveFriendRequest.RemoveFriendById::class, name = "byId")
    ]
)
open class RemoveFriendRequest(
    val type: RemoveType
) {
    enum class RemoveType {
        BY_ID,
        BY_LOGIN
    }

    data class RemoveFriendById(val friendId: Long) : RemoveFriendRequest(RemoveType.BY_ID)
    data class RemoveFriendByLogin(val friendLogin: String) : RemoveFriendRequest(RemoveType.BY_LOGIN)
}

data class ChangeInvitePolicyRequest(
    val userId: Long,
    val newPolicy: InvitePolicy
)