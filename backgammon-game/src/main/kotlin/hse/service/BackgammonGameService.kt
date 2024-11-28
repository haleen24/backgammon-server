package hse.service

import game.backgammon.dto.MoveDto
import game.backgammon.dto.MoveResponseDto
import game.backgammon.enums.BackgammonType
import game.backgammon.enums.Color
import game.backgammon.response.ConfigResponse
import game.backgammon.response.MoveResponse
import hse.dao.BackgammonGameRuntimeDao
import hse.dto.EndEvent
import hse.dto.GameStartedEvent
import hse.dto.MoveEvent
import hse.dto.TossZarEvent
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class BackgammonGameService(
    private val backgammonGameRuntimeDao: BackgammonGameRuntimeDao,
    private val emitterService: EmitterService
) {


    fun createAndConnect(roomId: Int, firstPlayer: Int, secondPlayer: Int, gameType: BackgammonType): Int {
        val resId = backgammonGameRuntimeDao.createGame(roomId, gameType)
        val game = backgammonGameRuntimeDao.getGame(resId)
        if (game.connect(firstPlayer) && game.connect(secondPlayer)) {
            emitterService.sendForAll(roomId, GameStartedEvent())
            return resId
        }
        throw ResponseStatusException(HttpStatus.CONFLICT, "Невозможно присоединить игроков к игре")
    }

    fun moveInGame(gameId: Int, playerId: Int, moves: List<MoveDto>): MoveResponse {
        val game = backgammonGameRuntimeDao.getGame(gameId)
        val res = game.move(playerId, moves)
        val playerColor = game.getPlayerColor(playerId)

        val response = MoveResponse(
            moves = res.changes.map { MoveResponseDto(it.first, it.second) },
            color = playerColor,
        )
        emitterService.sendEventExceptUser(playerId, gameId, MoveEvent(response.moves, playerColor))
        if (game.checkEnd()) {
            val endState = game.getEndState()
            val event = EndEvent(lose = endState[false]!!, win = endState[true]!!)
            emitterService.sendForAll(gameId, event)
        }
        val tossZarRes = game.tossZar()
        emitterService.sendForAll(gameId, TossZarEvent(tossZarRes.value, playerColor.getOpponent()))
        return response
    }

    fun getConfiguration(userId: Int, gameId: Int): ConfigResponse {
        val game = backgammonGameRuntimeDao.getGame(gameId)
        return game.getConfiguration(userId)
    }

    fun getColor(userId: Int, gameId: Int): Color {
        val game = backgammonGameRuntimeDao.getGame(userId)
        return game.getPlayerColor(userId)
    }

    fun isGameStarted(gameId: Int): Boolean {
        return backgammonGameRuntimeDao.getGame(gameId).checkIsGameStarted()
    }
}