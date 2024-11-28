package hse.menu.adapter

import game.backgammon.enums.BackgammonType
import game.backgammon.request.CreateBackgammonGameRequest
import game.common.enums.GameType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.net.URI


@Component
class GameAdapter {

    private val restTemplate = RestTemplate()

    private val logger: Logger = LoggerFactory.getLogger(GameAdapter::class.java)

    companion object {
        private const val GAME_ADDR = "game"

        private const val CREATE_ROOM_TEMPLATE = "http://localhost:82/$GAME_ADDR/%s/create-room/%d"
    }

    fun gameCreation(gameId: Int, firstUserId: Int, secondUserId: Int, gameType: GameType): Int? {
        val uri = URI(
            when (gameType.type) {
                GameType.GeneralGameType.BACKGAMMON -> CREATE_ROOM_TEMPLATE.format("backgammon", gameId)
            }
        )
        val request = when (gameType.type) {
            GameType.GeneralGameType.BACKGAMMON -> CreateBackgammonGameRequest(
                type = BackgammonType.valueOf(gameType.toString()),
                firstUserId = firstUserId,
                secondUserId = secondUserId,
            )
        }

        return try {
            restTemplate.postForObject(uri, request, Int::class.java)
                ?: -1
        } catch (e: Exception) {
            logger.error(e.message)
            -1
        }
    }

}