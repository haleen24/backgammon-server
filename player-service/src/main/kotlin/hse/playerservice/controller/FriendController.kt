package hse.playerservice.controller

import hse.playerservice.service.FriendService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import player.request.AddFriendRequest
import player.request.RemoveFriendRequest
import player.response.CheckFriend
import player.response.GetFriendResponse

@RestController
@RequestMapping("/friends")
class FriendController(
    private val friendService: FriendService
) {

    companion object {
        const val AUTH_HEADER = "auth-user"
    }

    @PostMapping("/add")
    fun addFriend(
        @RequestHeader(AUTH_HEADER) userId: Long,
        @RequestBody request: AddFriendRequest
    ): ResponseEntity<Void> {
        return friendService.addFriend(userId, request)
    }

    @DeleteMapping("/remove")
    fun removeFriend(
        @RequestHeader(AUTH_HEADER) userId: Long,
        @RequestBody request: RemoveFriendRequest
    ): ResponseEntity<Void> {
        return friendService.removeFriend(userId, request)
    }

    @GetMapping
    fun getFriends(
        @RequestParam userId: Long,
        @RequestParam offset: Int,
        @RequestParam limit: Int
    ): List<GetFriendResponse> {
        return friendService.getFriends(userId, offset, limit)
    }

    @GetMapping("/requests")
    fun getFriendRequests(@RequestHeader(AUTH_HEADER) userId: Long): List<GetFriendResponse> {
        return friendService.getFriendRequests(userId)
    }

    @GetMapping("/invite-check")
    fun isFriend(@RequestParam firsUser: Long, @RequestParam secondUser: Long): CheckFriend {
        return CheckFriend(friendService.isFriends(firsUser, secondUser))
    }
}