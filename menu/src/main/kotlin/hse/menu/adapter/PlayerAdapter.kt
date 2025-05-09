package hse.menu.adapter

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import player.response.UserInfoResponse

@FeignClient("player-service")
interface PlayerAdapter {
    @GetMapping("/player/userinfo")
    fun getUserInfo(@RequestParam("userId") userId: Long): UserInfoResponse
}