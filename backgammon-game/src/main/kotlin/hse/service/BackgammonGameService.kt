package hse.service

import game.backgammon.dto.MoveDto
import game.backgammon.dto.MoveResponseDto
import game.backgammon.enums.BackgammonType
import game.backgammon.enums.Color
import game.backgammon.response.ConfigResponse
import game.backgammon.response.MoveResponse
import hse.dao.BackgammonGameRuntimeDao
import hse.dto.*
import org.springframework.stereotype.Service

@Service
class BackgammonGameService(
    private val backgammonGameRuntimeDao: BackgammonGameRuntimeDao,
    private val emitterService: EmitterService
) {


    fun createGameRoom(roomId: Int, gameType: BackgammonType): Int {
        return backgammonGameRuntimeDao.createGame(roomId, gameType)
    }

    fun connectToGameRoom(playerId: Int, gameId: Int): Color {
        val game = backgammonGameRuntimeDao.getGame(gameId)
        val res = game.connect(playerId)
        if (!res) {
            throw RuntimeException("game already occupied")
        }
        emitterService.sendEventExceptUser(playerId, gameId, PlayerConnectedEvent(game.getPlayerColor(playerId)))
        return game.getPlayerColor(playerId)
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
        return response
    }

    fun getConfiguration(userId: Int, gameId: Int): ConfigResponse {
        val game = backgammonGameRuntimeDao.getGame(gameId)
        return game.getConfiguration(userId)
    }

    fun tossZar(userId: Int, gameId: Int): Collection<Int> {
        val game = backgammonGameRuntimeDao.getGame(gameId)
        val res = game.tossZar(userId).value
        emitterService.sendForAll(gameId, TossZarEvent(res, game.getPlayerColor(userId)))
        return res
    }

    fun getColor(userId: Int, gameId: Int): Color {
        val game = backgammonGameRuntimeDao.getGame(userId)
        return game.getPlayerColor(userId)
    }
}