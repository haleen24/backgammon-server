package hse.adapter

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

@FeignClient("menu-service")
interface MenuAdapter {
    @PostMapping("/menu/game-status/{matchId}")
    fun sendEndGame(@PathVariable("matchId") matchId: Int)
}