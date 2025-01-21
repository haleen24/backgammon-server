package hse.service

import game.backgammon.dto.MoveDto
import game.backgammon.dto.MoveResponseDto
import game.backgammon.enums.BackgammonType
import game.backgammon.enums.Color
import game.backgammon.response.ConfigResponse
import game.backgammon.response.HistoryResponse
import game.backgammon.response.MoveResponse
import game.backgammon.sht.ShortGammonGame
import hse.adapter.RedisAdapter
import hse.dto.EndEvent
import hse.dto.GameStartedEvent
import hse.dto.MoveEvent
import hse.dto.TossZarEvent
import hse.wrapper.BackgammonWrapper
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class BackgammonGameService(
    private val emitterService: EmitterService,
//    private val gameScheduler: GameScheduler,
    private val redisAdapter: RedisAdapter,
    private val gammonStoreService: GammonStoreService
) {

    private val logger = LoggerFactory.getLogger(BackgammonGameService::class.java)

    fun createAndConnect(roomId: Int, firstPlayer: Int, secondPlayer: Int, gameType: BackgammonType): Int {
        val commonGameType = when (gameType) {
            BackgammonType.SHORT_BACKGAMMON -> BackgammonType.SHORT_BACKGAMMON
        }
        val game = createGame(roomId, commonGameType)
        game.connect(firstPlayer, secondPlayer)
        emitterService.sendForAll(roomId, GameStartedEvent())
        gammonStoreService.saveGameOnCreation(roomId, game)
        return roomId
    }

    fun moveInGame(gameId: Int, playerId: Int, moves: List<MoveDto>): MoveResponse {
        val game = gammonStoreService.getGameById(gameId)
        val res = game.move(playerId, moves)
        val playerColor = game.getPlayerColor(playerId)

        val response = MoveResponse(
            moves = res.changes.map { MoveResponseDto(it.first, it.second) },
            color = playerColor,
        )
        emitterService.sendEventExceptUser(playerId, gameId, MoveEvent(response.moves, playerColor))
        if (game.checkEnd()) {
            val endState = game.gameEndStatus()
            val event = EndEvent(lose = endState[false]!!, win = endState[true]!!)
            emitterService.sendForAll(gameId, event)
//            gameScheduler.closeGame(gameId)
        }
        val tossZarRes = game.tossZar()
        gammonStoreService.saveAfterMove(gameId, game, res)
        emitterService.sendForAll(gameId, TossZarEvent(tossZarRes.value, playerColor.getOpponent()))
        return response
    }

    fun getConfiguration(userId: Int, gameId: Int): ConfigResponse {
        val game = gammonStoreService.getGameById(gameId)
        return game.getConfiguration(userId)
    }

    fun getColor(userId: Int, gameId: Int): Color {
        val game = gammonStoreService.getGameById(userId)
        return game.getPlayerColor(userId)
    }

    fun getHistory(roomId: Int): List<HistoryResponse> {
        return gammonStoreService.getAllMovesInGame(roomId)
            .sortedBy { it.moveId }
            .map { HistoryResponse(it.moves.changes.map { pair -> MoveResponseDto(pair.first, pair.second) }) }
    }

    fun closeGame(roomId: Int): Boolean {
        logger.info("Evicting game $roomId")
        return redisAdapter.del(roomId.toString()) != 0L
    }


    private fun createGame(roomId: Int, gameType: BackgammonType): BackgammonWrapper {
        if (gammonStoreService.checkGameExists(roomId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Game $roomId already exists")
        }
        val game = when (gameType) {
            BackgammonType.SHORT_BACKGAMMON -> BackgammonWrapper(ShortGammonGame(), gameType)
        }
        return game
    }
}