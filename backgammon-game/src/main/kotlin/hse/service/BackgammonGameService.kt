package hse.service

import game.backgammon.dto.MoveDto
import game.backgammon.dto.MoveResponseDto
import game.backgammon.dto.StartStateDto
import game.backgammon.enums.BackgammonType
import game.backgammon.enums.Color
import game.backgammon.lng.RegularGammonGame
import game.backgammon.request.CreateBackgammonGameRequest
import game.backgammon.response.ConfigResponse
import game.backgammon.response.HistoryResponse
import game.backgammon.response.MoveResponse
import game.backgammon.sht.ShortGammonGame
import hse.dto.EndGameEvent
import hse.dto.GameStartedEvent
import hse.dto.MoveEvent
import hse.dto.TossZarEvent
import hse.wrapper.BackgammonWrapper
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class BackgammonGameService(
    private val emitterService: EmitterService,
    private val gammonStoreService: GammonStoreService
) {

    fun createAndConnect(roomId: Int, request: CreateBackgammonGameRequest): Int {
        val game = createMatch(roomId, request.points, request.type)
        game.connect(request.firstUserId, request.secondUserId)
        gammonStoreService.saveGameOnCreation(roomId, 1, game)
        emitterService.sendForAll(roomId, GameStartedEvent())
        return roomId
    }

    fun moveInGame(matchId: Int, playerId: Int, moves: List<MoveDto>): MoveResponse {
        val game = gammonStoreService.getMatchById(matchId)
        val res = game.move(playerId, moves)
        val playerColor = game.getPlayerColor(playerId)

        val response = MoveResponse(
            moves = res.changes.map { MoveResponseDto(it.first, it.second) },
            color = playerColor,
        )
        emitterService.sendEventExceptUser(playerId, matchId, MoveEvent(response.moves, playerColor))
        val tossZarRes = game.tossZar()
        gammonStoreService.saveAfterMove(matchId, game.gameId, playerId, game, res)
        if (game.checkEnd()) {
            handleGameEnd(matchId, game)
        }
        emitterService.sendForAll(matchId, TossZarEvent(tossZarRes.value, playerColor.getOpponent()))
        return response
    }

    fun getConfiguration(userId: Int, gameId: Int): ConfigResponse {
        val game = gammonStoreService.getMatchById(gameId)
        return game.getConfiguration(userId)
    }

    fun getColor(userId: Int, gameId: Int): Color {
        val game = gammonStoreService.getMatchById(gameId)
        return game.getPlayerColor(userId)
    }

    fun getHistory(matchId: Int, gameId: Int): HistoryResponse {
        val moves = gammonStoreService.getAllMovesInGame(matchId, gameId)
            .sortedBy { it.moveId }
            .map { MoveResponse(it.moves.changes.map { pair -> MoveResponseDto(pair.first, pair.second) }, it.color) }

        val startState =
            gammonStoreService.getStartGameContext(matchId, gameId) ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Game $gameId in match $matchId not found"
            )
        return HistoryResponse(
            allMoves = moves,
            startState = StartStateDto(
                userMap = getColorMap(startState.firstUserId, startState.secondUserId, matchId),
                type = startState.type,
                deck = startState.game.deck,
                turn = startState.game.turn,
                zarResult = startState.game.zarResult,
            )
        )
    }

    private fun createMatch(roomId: Int, points: Int, gameType: BackgammonType): BackgammonWrapper {
        if (gammonStoreService.checkMatchExists(roomId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Game $roomId already exists")
        }
        val game = when (gameType) {
            BackgammonType.SHORT_BACKGAMMON -> ShortGammonGame()
            BackgammonType.REGULAR_GAMMON -> RegularGammonGame()
        }
        return BackgammonWrapper(
            game = game,
            type = gameType,
            blackPoints = 0,
            whitePoints = 0,
            thresholdPoints = points,
            gameId = 1
        )
    }

    private fun getColorMap(firstPlayer: Int, secondPlayer: Int, roomId: Int): Map<Color, Int> {
        val firstColor = getColor(firstPlayer, roomId)
        return mapOf(firstColor to firstPlayer, firstColor.getOpponent() to secondPlayer)
    }

    // public for testing
    fun handleGameEnd(roomId: Int, wrapper: BackgammonWrapper) {
        val endState = wrapper.gameEndStatus()
        val winnerPoints = wrapper.addPointsTo(endState[true]!!)
        val endMatch = winnerPoints >= wrapper.thresholdPoints

        if (!endMatch) {
            wrapper.restore()
            gammonStoreService.saveGameOnCreation(roomId, wrapper.gameId, wrapper)
        }
        emitterService.sendForAll(
            roomId, EndGameEvent(
                win = endState[true]!!,
                blackPoints = wrapper.blackPoints,
                whitePoints = wrapper.whitePoints,
                isMatchEnd = endMatch,
            )
        )
    }
}