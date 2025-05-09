package hse.playerservice.controller

import hse.playerservice.service.UserRatingService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import player.request.ChangeRatingRequest

@RestController
@RequestMapping("/rating")
class UserRatingController(
    val userRatingService: UserRatingService
) {
    @PostMapping
    fun changeRating(changeRatingRequest: ChangeRatingRequest) {
        userRatingService.changeRating(changeRatingRequest)
    }
}