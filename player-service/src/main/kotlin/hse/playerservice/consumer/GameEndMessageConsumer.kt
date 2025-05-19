package hse.playerservice.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import hse.playerservice.service.UserRatingService
import kafka.GameEndMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class GameEndMessageConsumer(
    private val userRatingService: UserRatingService,
    private val objectMapper: ObjectMapper,
) {
    val logger: Logger = LoggerFactory.getLogger(UserRatingService::class.java)
    @KafkaListener(topics = ["\${kafka.topic.narde.event.game-end}"], groupId = "player")
    fun process(data: String) {
        logger.info("handle rating change: $data")
        val gameEndMessage = objectMapper.readValue(data, GameEndMessage::class.java)
        userRatingService.changeRating(gameEndMessage)
    }
}