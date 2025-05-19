package hse.playerservice.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import hse.playerservice.service.UserRatingService
import kafka.GameEndMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronizationManager

@Component
class GameEndMessageConsumer(
    private val userRatingService: UserRatingService,
    private val objectMapper: ObjectMapper,
) {
    val logger: Logger = LoggerFactory.getLogger(UserRatingService::class.java)

    @Transactional
    @KafkaListener(topics = ["\${kafka.topic.narde.event.game-end}"], groupId = "player")
    fun process(data: String) {
        logger.info("handle rating change: $data, transaction: ${TransactionSynchronizationManager.isActualTransactionActive()}")
        val gameEndMessage = objectMapper.readValue(data, GameEndMessage::class.java)
        userRatingService.changeRating(gameEndMessage)
    }
}