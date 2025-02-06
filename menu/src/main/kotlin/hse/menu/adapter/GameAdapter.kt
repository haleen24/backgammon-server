package hse.menu.adapter

import game.backgammon.enums.BackgammonType
import game.backgammon.request.CreateBackgammonGameRequest
import game.common.enums.GameType
import game.common.enums.GammonGamePoints
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.net.URI


@Component
class GameAdapter(
    @Value("\${route.config.backgammon-game.uri}") private val gameUri: String
) {

    private val restTemplate = RestTemplate()

    private val logger: Logger = LoggerFactory.getLogger(GameAdapter::class.java)

    private val gameAddr = "game"

    private val createRoomTemplate = "$gameUri/$gameAddr/%s/create-room/%d"
    fun gameCreation(
        gameId: Int,
        firstUserId: Int,
        secondUserId: Int,
        gameType: GameType,
        points: GammonGamePoints
    ): Int? {
        val uri = URI(
            when (gameType.type) {
                GameType.GeneralGameType.BACKGAMMON -> createRoomTemplate.format("backgammon", gameId)
            }
        )
        val request = when (gameType.type) {
            GameType.GeneralGameType.BACKGAMMON -> CreateBackgammonGameRequest(
                type = BackgammonType.valueOf(gameType.toString()),
                firstUserId = firstUserId,
                secondUserId = secondUserId,
                points = points.value
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