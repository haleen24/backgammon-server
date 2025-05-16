package hse.menu.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import hse.menu.service.GameService
import kafka.GameEndMessage
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class GameEndMessageConsumer(
    private val gameService: GameService,
    private val objectMapper: ObjectMapper,
) {
    @KafkaListener(topics = ["\${kafka.topic.narde.event.game-end}"], groupId = "\${spring.kafka.consumer.group-id}")
    fun consume(data: String) {
        val gameEndMessage = objectMapper.readValue(data, GameEndMessage::class.java)
        gameService.setGameEnd(gameEndMessage.matchId)
    }
}