package hse.service

import game.backgammon.Gammon
import game.backgammon.enums.Color
import game.backgammon.response.HistoryResponse
import game.backgammon.response.HistoryResponseItem
import hse.dto.AcceptDoubleHistoryResponseItem
import hse.dto.GameEndHistoryResponseItem
import hse.dto.MoveHistoryResponseItem
import hse.dto.OfferDoubleHistoryResponseItem
import hse.entity.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import kotlin.math.pow

@Service
class GammonHistoryService(
    private val gammonStoreService: GammonStoreService
) {

    private final val logger = LoggerFactory.getLogger(GammonHistoryService::class.java)

    fun getLastGameHistory(matchId: Int): HistoryResponse {
        val gameId = gammonStoreService.getCurrentGameId(matchId)
        return getHistory(matchId, gameId)
    }

    fun getHistory(matchId: Int, gameId: Int): HistoryResponse {
        val history = gammonStoreService.getAllInGameInOrderByInsertionTime(matchId, gameId)
        if (history.isEmpty()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No game found")
        }
        val startState = try {
            history[0] as GameWithId
        } catch (exception: RuntimeException) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "В игре нет начального состояния")
        }
        val firstToMove = if (startState.restoreContextDto.game.turn == Gammon.BLACK) Color.BLACK else Color.WHITE
        val responseHistoryItems = mutableListOf<HistoryResponseItem>()
        var doubleCubeCounter = 0
        var i = 1
        while (i in 1 until history.size) {
            val entity = history[i]
            if (entity is Zar) {
                if (i + 1 == history.size) {
                    break
                }
                if (history[i + 1] is MoveWithId) {
                    responseHistoryItems.add(toMoveHistoryItem(entity, history[i + 1] as MoveWithId))
                    i += 2
                    continue
                }
                logger.warn("После броска зара не ход! игра $matchId-$gameId; ход ${entity.moveId}")
            } else if (entity is DoubleCube) {
                ++doubleCubeCounter
                responseHistoryItems.add(toMoveHistoryItem(entity, doubleCubeCounter))
            } else if (entity is GameWinner) {
                responseHistoryItems.add(
                    toMoveHistoryItem(
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
            firstToMove = firstToMove
        )
    }

    private fun toMoveHistoryItem(zar: Zar, moveWithId: MoveWithId): HistoryResponseItem {
        return MoveHistoryResponseItem(
            dice = zar.z,
            moves = moveWithId.moveSet.moves.changes.map { MoveHistoryResponseItem.MoveItem(it.first, it.second) }
        )
    }

    private fun toMoveHistoryItem(doubleCube: DoubleCube, n: Int): HistoryResponseItem {
        return if (!doubleCube.isAccepted) {
            OfferDoubleHistoryResponseItem(
                by = doubleCube.by,
                newValue = 2.0.pow(n).toInt(),
            )
        } else {
            AcceptDoubleHistoryResponseItem()
        }
    }

    private fun toMoveHistoryItem(
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