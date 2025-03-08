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
import hse.dto.*
import hse.entity.DoubleCube
import hse.wrapper.BackgammonWrapper
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import kotlin.math.pow

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
        if (game.numberOfMoves != 0) {
            validateZarState(matchId, game.gameId, game.numberOfMoves)
        }

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
        val doubles = gammonStoreService.getAllDoubles(matchId, game.gameId)

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

    fun doubleZar(matchId: Int, userId: Int) {
        val game = gammonStoreService.getMatchById(matchId)
        val doubles = gammonStoreService.getAllDoubles(matchId, game.gameId)
        if (!game.isTurn(userId)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "incorrect turn")
        }
        if (game.getZar().isNotEmpty()) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "zar already thrown")
        }
        val userColor = game.getPlayerColor(userId)
        val doubleCubePosition = getDoubleCubePosition(matchId, game, doubles)
        if (doubleCubePosition == DoubleCubePositionEnum.UNAVAILABLE) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Decline by Crawford rule")
        }
        if (doubleCubePosition == DoubleCubePositionEnum.FREE) {
            return createDoubleRequest(matchId, game.gameId, game.numberOfMoves, userId, userColor)
        }
        val last = doubles.last()
        if (last.by == userColor) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "cant do 2 doubles in a row")
        }
        if (!last.isAccepted) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "last double wasnt accepted")
        }
        createDoubleRequest(matchId, game.gameId, game.numberOfMoves, userId, userColor)
    }

    fun acceptDouble(matchId: Int, userId: Int) {
        val game = gammonStoreService.getMatchById(matchId)
        val doubles = gammonStoreService.getAllDoubles(matchId, game.gameId)

        if (doubles.isEmpty()) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "there are no doubles")
        }
        val last = doubles.last()
        if (last.isAccepted) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "no double for accepting")
        }
        if (last.by == game.getPlayerColor(userId)) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "cant accept own double")
        }
        gammonStoreService.acceptDouble(matchId, game.gameId, game.numberOfMoves)
        emitterService.sendEventExceptUser(userId, matchId, AcceptDoubleEvent(game.getPlayerColor(userId)))
    }

    fun getConfiguration(userId: Int, matchId: Int): ConfigResponse {
        val game = gammonStoreService.getMatchById(matchId)
        val configData = game.getConfiguration(userId)
        val doubleCubes = gammonStoreService.getAllDoubles(matchId, game.gameId)
        val doubleCubePosition = getDoubleCubePosition(matchId, game, doubleCubes)
        val doubleCubeValue =
            if (doubleCubePosition == DoubleCubePositionEnum.UNAVAILABLE) null else 2.0.pow(doubleCubes.size.toDouble())
                .toInt()

        return ConfigResponse(
            gameData = configData,
            blackPoints = game.blackPoints,
            whitePoints = game.whitePoints,
            threshold = game.thresholdPoints,
            players = game.getPlayers(),
            doubleCubeValue = doubleCubeValue,
            doubleCubePosition = doubleCubePosition,
            end = game.checkEnd()
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
        val winner = endState[true]!!
        val winnerPoints = wrapper.addPointsTo(winner)
        val endMatch = winnerPoints >= wrapper.thresholdPoints
        gammonStoreService.storeWinner(roomId, wrapper.gameId, winner)

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

    fun surrender(userId: Int, matchId: Int, endMatch: Boolean) {
        val game = gammonStoreService.getMatchById(matchId)
        if (!game.isTurn(userId)) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "incorrect turn")
        }
        gammonStoreService.surrender(userId, matchId, game, endMatch)
        emitterService.sendForAll(
            matchId, EndGameEvent(
                win = game.getPlayerColor(userId),
                blackPoints = game.blackPoints,
                whitePoints = game.whitePoints,
                isMatchEnd = endMatch,
            )
        )
    }

    private fun validateZarState(matchId: Int, gameId: Int, moves: Int) {
        val zar = gammonStoreService.getLastZar(matchId, gameId, moves)
        if (zar.isEmpty()) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Should toss zar")
        }
    }

    private fun createDoubleRequest(matchId: Int, gameId: Int, moveId: Int, userId: Int, by: Color) {
        gammonStoreService.createDoubleRequest(matchId, gameId, moveId, by)
        emitterService.sendEventExceptUser(userId, matchId, DoubleEvent(by))
    }

    private fun getDoubleCubePosition(
        matchId: Int,
        game: BackgammonWrapper,
        doubles: List<DoubleCube>
    ): DoubleCubePositionEnum {
        val winners = gammonStoreService.getWinnersInMatch(matchId)

        if (winners.isNotEmpty()) {
            if (game.blackPoints == game.thresholdPoints - 1 && winners.last() == Color.BLACK) {
                return DoubleCubePositionEnum.UNAVAILABLE
            } else if (game.whitePoints == game.thresholdPoints - 1 && winners.last() == Color.WHITE) {
                return DoubleCubePositionEnum.UNAVAILABLE
            }
        }

        if (doubles.isEmpty()) {
            return DoubleCubePositionEnum.FREE
        }


        val last = doubles.last()

        return when (last.isAccepted) {
            true -> when (last.by) {
                Color.BLACK -> DoubleCubePositionEnum.BELONGS_TO_WHITE
                Color.WHITE -> DoubleCubePositionEnum.BELONGS_TO_BLACK
            }

            false -> when (last.by) {
                Color.BLACK -> DoubleCubePositionEnum.OFFERED_TO_WHITE
                Color.WHITE -> DoubleCubePositionEnum.OFFERED_TO_BLACK
            }
        }
    }
}