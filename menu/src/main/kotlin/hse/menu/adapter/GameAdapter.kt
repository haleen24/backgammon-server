package hse.menu.adapter

import game.backgammon.enums.BackgammonType
import game.backgammon.request.CreateBackgammonGameRequest
import game.common.enums.GameType
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import java.net.URI


@Component
class GameAdapter {

    private val restTemplate = RestTemplate()

    companion object {
        private const val GAME_ADDR = "game"

        private const val CREATE_ROOM_TEMPLATE = "http://localhost:82/$GAME_ADDR/%s/create-room/%d"
    }

    fun gameCreation(gameId: Int, firstUserId: Int, secondUserId: Int, gameType: GameType): Int {
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

        return restTemplate.postForObject(uri, request, Int::class.java)
            ?: throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Сервис с игрой не доступен")
    }

}