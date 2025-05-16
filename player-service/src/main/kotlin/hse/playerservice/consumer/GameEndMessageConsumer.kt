package hse.playerservice.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import hse.playerservice.service.UserRatingService
import kafka.GameEndMessage
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class GameEndMessageConsumer(
    private val userRatingService: UserRatingService,
    private val objectMapper: ObjectMapper,
) {
    @KafkaListener(topics = ["\${kafka.topic.narde.event.game-end}"], groupId = "player")
    fun process(data: String) {
        val gameEndMessage = objectMapper.readValue(data, GameEndMessage::class.java)
        userRatingService.changeRating(gameEndMessage)
    }
}