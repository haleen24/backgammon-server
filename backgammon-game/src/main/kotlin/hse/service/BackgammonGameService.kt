package hse.service

import game.backgammon.dto.MoveDto
import game.backgammon.dto.MoveResponseDto
import game.backgammon.dto.StartStateDto
import game.backgammon.enums.BackgammonType
import game.backgammon.enums.Color
import game.backgammon.enums.DoubleCubePositionEnum
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
import kotlin.math.pow

@Service
class BackgammonGameService(
    private val emitterService: EmitterService,
    private val gammonStoreService: GammonStoreService,
    private val doubleCubeService: DoubleCubeService,
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
        checkGameState(game)
        val res = game.move(playerId, moves)
        val playerColor = game.getPlayerColor(playerId)
        val response = MoveResponse(
            moves = res.changes.map { MoveResponseDto(it.first, it.second) },
            color = playerColor,
        )
        gammonStoreService.saveAfterMove(matchId, game.gameId, playerId, game, res)

        emitterService.sendEventExceptUser(playerId, matchId, MoveEvent(response.moves, playerColor))

        if (game.checkEnd()) {
            handleGameEnd(matchId, game)
        }
        return response
    }

    fun tossZar(matchId: Int, userId: Int) {
        val game = gammonStoreService.getMatchById(matchId)
        checkGameState(game)
        val doubles = doubleCubeService.getAllDoubles(matchId, game.gameId)

        if (doubles.isNotEmpty()) {
            if (!doubles.last().isAccepted) {
                throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "there is no response on double request")
            }
        }

        val res = game.tossZar(userId)
        gammonStoreService.storeZar(
            matchId,
            game,
            res.value,
        )
        emitterService.sendForAll(matchId, TossZarEvent(res.value, game.getPlayerColor(userId)))
    }

    fun getConfiguration(userId: Int, matchId: Int): ConfigResponse {
        val game = gammonStoreService.getMatchById(matchId)
        val configData = game.getConfiguration(userId)
        val doubleCubes = doubleCubeService.getAllDoubles(matchId, game.gameId)
        val doubleCubePosition = doubleCubeService.getDoubleCubePosition(matchId, game, doubleCubes)
        val doubleCubeValue =
            if (doubleCubePosition == DoubleCubePositionEnum.UNAVAILABLE) null else 2.0.pow(doubleCubes.size.toDouble())
                .toInt()
        val winner = if (game.checkEnd()) gammonStoreService.getWinnersInMatch(matchId).last() else null

        return ConfigResponse(
            gameData = configData,
            blackPoints = game.blackPoints,
            whitePoints = game.whitePoints,
            threshold = game.thresholdPoints,
            players = game.getPlayers(),
            doubleCubeValue = doubleCubeValue,
            doubleCubePosition = doubleCubePosition,
            winner = winner,
        )
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

    fun handleGameEnd(roomId: Int, wrapper: BackgammonWrapper) {
        val endState = wrapper.gameEndStatus()
        val doubles = doubleCubeService.getAllDoubles(roomId, wrapper.gameId).count { it.isAccepted }
        val winner = endState[true]!!
        val winnerPoints = wrapper.getPointsForGame() * 2.0.pow(doubles).toInt()
        addPointsToWinner(wrapper, winnerPoints, winner)
        val endMatch = wrapper.blackPoints >= wrapper.thresholdPoints || wrapper.whitePoints >= wrapper.thresholdPoints
        gammonStoreService.storeWinner(roomId, wrapper.gameId, winner, winnerPoints, endMatch)
        if (!endMatch) {
            wrapper.restore()
            gammonStoreService.saveGameOnCreation(roomId, wrapper.gameId, wrapper)
        }
        emitterService.sendForAll(
            roomId, EndGameEvent(
                win = winner,
                blackPoints = wrapper.blackPoints,
                whitePoints = wrapper.whitePoints,
                isMatchEnd = endMatch,
            )
        )
    }

    fun surrender(userId: Int, matchId: Int, surrenderMatch: Boolean) {
        val game = gammonStoreService.getMatchById(matchId)
        checkGameState(game)
        val surrenderedColor = game.getPlayerColor(userId)
        val winnerColor = surrenderedColor.getOpponent()
        val doubles = doubleCubeService.getAllDoubles(matchId, game.gameId)
        if (doubles.isEmpty() || doubles.last().isAccepted) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "cant surrender without double cube")
        }
        val winnerPoints =
            game.getPointsForGame() * 2.0.pow(doubles.count { it.isAccepted }).toInt()
        addPointsToWinner(game, winnerPoints, winnerColor)
        val endMatch = surrenderMatch || game.blackPoints >= game.whitePoints || game.whitePoints >= game.thresholdPoints
        gammonStoreService.surrender(surrenderedColor, matchId, game, winnerPoints, endMatch)
        if (!endMatch) {
            game.restore()
            gammonStoreService.saveGameOnCreation(matchId, game.gameId, game)
        }
        emitterService.sendForAll(
            matchId, EndGameEvent(
                win = winnerColor,
                blackPoints = game.blackPoints,
                whitePoints = game.whitePoints,
                isMatchEnd = endMatch,
            )
        )
    }

    fun checkGameState(game: BackgammonWrapper) {
        if (game.checkEnd()) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Game is already end")
        }
    }

    private fun addPointsToWinner(game: BackgammonWrapper, points: Int, winner: Color) {
        if (winner == Color.BLACK) {
            game.blackPoints += points
        } else {
            game.whitePoints += points
        }
    }
}