package hse.service

import game.backgammon.Gammon
import game.backgammon.enums.Color
import game.backgammon.response.HistoryResponse
import game.backgammon.response.HistoryResponseItem
import hse.adapter.dto.AnalyzeMatchRequest
import hse.adapter.GameEngineAdapter
import hse.dto.AcceptDoubleHistoryResponseItem
import hse.dto.GameEndHistoryResponseItem
import hse.dto.MoveHistoryResponseItem
import hse.dto.OfferDoubleHistoryResponseItem
import hse.entity.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Lookup
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import kotlin.math.pow

@Service
class GammonHistoryService(
    private val gammonStoreService: GammonStoreService,
    private val gameEngineAdapter: GameEngineAdapter
) {
    private final val logger = LoggerFactory.getLogger(GammonHistoryService::class.java)

    @Lookup
    fun lookUp(): GammonHistoryService = this

    fun getLastGameHistory(matchId: Int): HistoryResponse {
        val gameId = gammonStoreService.getCurrentGameId(matchId)
//        lookUp().clearHistoryCache()
        return getHistory(matchId, gameId)
    }

//    @CacheEvict(value = ["history"], allEntries = true)
//    fun clearHistoryCache() {
//    }

    @Cacheable("history")
    fun getHistory(matchId: Int, gameId: Int): HistoryResponse {
        val history = ArrayList(gammonStoreService.getAllInGameInOrderByInsertionTime(matchId, gameId))
        if (history.isEmpty()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No game found")
        }
        val startState = try {
            history[0] as GameWithId
        } catch (exception: RuntimeException) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "В игре нет начального состояния")
        }
        history[0] = Zar(gameId, 0, startState.restoreContextDto.game.zarResult)
        val firstToMove = if (startState.restoreContextDto.game.turn == Gammon.BLACK) Color.BLACK else Color.WHITE
        val responseHistoryItems = mutableListOf<HistoryResponseItem>()
        var doubleCubeCounter = 0
        var i = 0
        while (i in 0 until history.size) {
            val entity = history[i]
            if (entity is Zar) {
                if (i + 1 == history.size) {
                    break
                }
                if (history[i + 1] is MoveWithId) {
                    responseHistoryItems.add(addDoubleCubeActionsToHistory(entity, history[i + 1] as MoveWithId))
                    i += 2
                    continue
                }
                logger.warn("После броска зара не ход! игра $matchId-$gameId; ход ${entity.moveId}")
            } else if (entity is DoubleCube) {
                ++doubleCubeCounter
                addDoubleCubeActionsToHistory(entity, doubleCubeCounter, responseHistoryItems)
            } else if (entity is GameWinner) {
                responseHistoryItems.add(
                    addDoubleCubeActionsToHistory(
                        entity, startState.restoreContextDto.whitePoints,
                        startState.restoreContextDto.blackPoints
                    )
                )
            } else {
                logger.warn("Не найден подходящий маппинг в историю для $entity")
            }
            ++i
        }
        return HistoryResponse(
            items = responseHistoryItems,
            firstToMove = firstToMove,
            gameId = gameId,
            thresholdPoints = startState.restoreContextDto.thresholdPoints,
        )
    }

    fun getAnalysis(matchId: Int): Map<Any, Any> {
        val gameIds = gammonStoreService.getAllGamesId(matchId)
        val lookup = lookUp()
        val request =
            AnalyzeMatchRequest(matchId = matchId, games = gameIds.map { gameId -> lookup.getHistory(matchId, gameId) })
        return gameEngineAdapter.getAnalysis(request)
    }

    private fun addDoubleCubeActionsToHistory(zar: Zar, moveWithId: MoveWithId): HistoryResponseItem {
        return MoveHistoryResponseItem(
            dice = zar.z,
            moves = moveWithId.moveSet.moves.changes.map { MoveHistoryResponseItem.MoveItem(it.first, it.second) }
        )
    }

    private fun addDoubleCubeActionsToHistory(
        doubleCube: DoubleCube,
        n: Int,
        responseHistoryItems: MutableList<HistoryResponseItem>
    ) {
        responseHistoryItems.add(
            OfferDoubleHistoryResponseItem(
                by = doubleCube.by,
                newValue = 2.0.pow(n).toInt(),
            )
        )
        if (doubleCube.isAccepted) {
            responseHistoryItems.add(AcceptDoubleHistoryResponseItem())
        }
    }

    private fun addDoubleCubeActionsToHistory(
        gameWinner: GameWinner,
        initialWhitePoints: Int,
        initialBlackPoints: Int
    ): HistoryResponseItem {
        return GameEndHistoryResponseItem(
            white = if (gameWinner.color == Color.WHITE) gameWinner.points else initialWhitePoints,
            black = if (gameWinner.color == Color.BLACK) gameWinner.points else initialBlackPoints,
            winner = gameWinner.color,
            isSurrendered = gameWinner.surrender
        )
    }
}