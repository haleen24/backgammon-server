package hse.playerservice.service

import hse.playerservice.entity.FriendRecord
import hse.playerservice.entity.FriendRequest
import hse.playerservice.repository.FriendRecordRepository
import hse.playerservice.repository.FriendRequestRepository
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import player.request.AddFriendRequest
import player.request.RemoveFriendRequest
import player.response.GetFriendResponse
import java.time.Clock
import kotlin.math.max
import kotlin.math.min

@Service
class FriendService(
    val friendRequestRepository: FriendRequestRepository,
    val friendRecordRepository: FriendRecordRepository,
    val userService: UserService,
    val clock: Clock
) {
    companion object {
        const val NO_ID = 0L
    }

    @Transactional
    fun addFriend(userId: Long, request: AddFriendRequest): ResponseEntity<Void> {
        when (request.type) {
            AddFriendRequest.AddType.BY_ID -> addFriendById(
                userId,
                (request as AddFriendRequest.AddFriendById).friendId
            )

            AddFriendRequest.AddType.BY_LOGIN -> addFriendByLogin(
                userId,
                (request as AddFriendRequest.AddFriendByLogin).friendLogin
            )
        }
        return ResponseEntity(HttpStatus.OK)
    }

    fun removeFriend(userId: Long, request: RemoveFriendRequest): ResponseEntity<Void> {
        when (request.type) {
            RemoveFriendRequest.RemoveType.BY_ID -> removeFriendById(
                userId,
                (request as RemoveFriendRequest.RemoveFriendById).friendId
            )

            RemoveFriendRequest.RemoveType.BY_LOGIN -> removeFriendByLogin(
                userId,
                (request as RemoveFriendRequest.RemoveFriendByLogin).friendLogin
            )
        }
        return ResponseEntity(HttpStatus.OK)
    }

    fun getFriends(userId: Long, offset: Int, limit: Int): List<GetFriendResponse> {
        val pageable = PageRequest.of(offset, limit)
        val friendRelations = friendRecordRepository.findAllFriends(userId, pageable)
        val friendIds = userService.findAllUsers(friendRelations.map {
            if (it.firstUser == userId) {
                it.secondUser
            } else {
                it.firstUser
            }
        })

        return friendIds.map { GetFriendResponse(it.username, it.id) }
    }

    fun getFriendRequests(userId: Long): List<GetFriendResponse> {
        val friendRequests = friendRequestRepository.findByTo(userId).sortedBy { it.from }
        val usernames = userService.getAllUsernames(friendRequests.map { it.to })

        return friendRequests.zip(usernames).map { pair ->
            GetFriendResponse(
                username = pair.second,
                id = pair.first.id
            )
        }
    }

    fun isFriends(firstUserId: Long, secondUserId: Long): Boolean {
        val first = min(firstUserId, secondUserId)
        val second = max(firstUserId, secondUserId)
        return friendRecordRepository.existsFriendRecordByFirstUserAndSecondUser(first, second)
    }

    private fun addFriendById(userId: Long, friendRequestId: Long) {
        if (userId == friendRequestId) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "cant be friend for yourself")
        }
        val first = min(userId, friendRequestId)
        val second = max(userId, friendRequestId)
        checkFriendsAlready(first, second)
        val request = friendRequestRepository.findFirstByFromAndTo(friendRequestId, userId)

        if (request == null) {
            friendRequestRepository.save(FriendRequest(NO_ID, userId, friendRequestId, clock.instant()))
        } else {
            friendRecordRepository.save(FriendRecord(NO_ID, first, second, clock.instant()))
            friendRequestRepository.deleteById(request.id)
        }

    }

    private fun addFriendByLogin(userId: Long, friendRequestLogin: String) {
        val friend = userService.findUserNotNull(friendRequestLogin)
        addFriendById(userId, friend.id)
    }

    private fun checkFriendsAlready(firstUserId: Long, secondUserId: Long) {
        if (isFriends(firstUserId, secondUserId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Already friends")
        }
    }

    private fun removeFriendById(userId: Long, friendRequestId: Long) {
        val first = min(userId, friendRequestId)
        val second = max(userId, friendRequestId)

        friendRecordRepository.deleteFriendRecordByFirstUserAndSecondUser(first, second)
    }

    private fun removeFriendByLogin(userId: Long, friendLogin: String) {
        removeFriendById(userId, userService.findUserNotNull(friendLogin).id)
    }
}