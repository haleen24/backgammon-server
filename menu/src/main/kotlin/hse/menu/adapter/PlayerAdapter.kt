package hse.menu.adapter

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import player.response.UserInfoResponse

@FeignClient("player-service")
interface PlayerAdapter {
    @GetMapping("/userinfo")
    fun getUserInfo(userId: Long): UserInfoResponse
}