package hse.service

import game.backgammon.dto.MoveDto
import game.backgammon.dto.MoveResponseDto
import game.backgammon.dto.StartStateDto
import game.backgammon.enums.BackgammonType
import game.backgammon.enums.Color
import game.backgammon.lng.RegularGammonGame
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
        val game = createGame(roomId, gameType)
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
        gammonStoreService.saveAfterMove(gameId, playerId, game, res)
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

    fun getHistory(roomId: Int): HistoryResponse {
        val moves = gammonStoreService.getAllMovesInGame(roomId)
            .sortedBy { it.moveId }
            .map { MoveResponse(it.moves.changes.map { pair -> MoveResponseDto(pair.first, pair.second) }, it.color) }

        val startState =
            gammonStoreService.getStartGameContext(roomId) ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Game $roomId not found"
            )
        return HistoryResponse(
            allMoves = moves,
            startState = StartStateDto(
                userMap = getColorMap(startState.firstUserId, startState.secondUserId, roomId),
                type = startState.type,
                deck = startState.game.deck,
                turn = startState.game.turn,
                zarResult = startState.game.zarResult,
            )
        )
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
            BackgammonType.SHORT_BACKGAMMON -> ShortGammonGame()
            BackgammonType.REGULAR_GAMMON -> RegularGammonGame()
        }
        return BackgammonWrapper(game, gameType)
    }

    private fun getColorMap(firstPlayer: Int, secondPlayer: Int, roomId: Int): Map<Color, Int> {
        val firstColor = getColor(firstPlayer, roomId)
        return mapOf(firstColor to firstPlayer, firstColor.getOpponent() to secondPlayer)
    }
}