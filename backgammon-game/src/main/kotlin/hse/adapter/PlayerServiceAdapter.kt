package hse.adapter

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import player.request.ChangeRatingRequest

@FeignClient("player-service")
interface PlayerServiceAdapter {
    @PostMapping("rating")
    fun changeRating(@RequestBody changeRatingRequest: ChangeRatingRequest): ResponseEntity<Unit>
}