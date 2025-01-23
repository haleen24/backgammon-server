package hse.service

import com.fasterxml.jackson.databind.ObjectMapper
import game.backgammon.dto.ChangeDto
import game.backgammon.enums.BackgammonType
import hse.adapter.RedisAdapter
import hse.dao.GammonMoveDao
import hse.dto.GammonRestoreContextDto
import hse.entity.MoveSet
import hse.wrapper.BackgammonWrapper
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import kotlin.math.sign


@Component
class GammonStoreService(
    private val gammonMoveDao: GammonMoveDao,
    private val redisAdapter: RedisAdapter,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(GammonStoreService::class.java)

    fun getGameById(gameId: Int): BackgammonWrapper {
        return getGameFromCache(gameId) ?: getGameFromDataBase(gameId) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Game $gameId not found"
        )
    }

    fun checkGameExists(gameId: Int): Boolean {
        return redisAdapter.exists(gameId.toString()) || gammonMoveDao.checkGameExists(gameId)
    }

    fun saveGameOnCreation(gameId: Int, game: BackgammonWrapper) {
        val restoreContext = game.getRestoreContext()
        putGameToCache(gameId, restoreContext)
        gammonMoveDao.saveStartGameContext(gameId, restoreContext)
    }

    fun saveAfterMove(gameId: Int, playerId: Int, game: BackgammonWrapper, moves: ChangeDto) {
        val restoreContext = game.getRestoreContext()
        putGameToCache(gameId, restoreContext)
        val moveSet = MoveSet(
            moves = moves,
            gameId = gameId,
            moveId = restoreContext.numberOfMoves,
            nextZar = restoreContext.game.zarResult,
            color = game.getPlayerColor(playerId)
        )
        gammonMoveDao.saveMoves(gameId, moveSet)
    }

    fun getAllMovesInGame(gameId: Int): List<MoveSet> {
        return gammonMoveDao.getMoves(gameId)
    }

    fun getStartGameContext(gameId: Int): GammonRestoreContextDto? {
        return gammonMoveDao.getStartGameContext(gameId)
    }

    private fun getGameFromCache(gameId: Int): BackgammonWrapper? {
        logger.info("Get game $gameId from cache")
        val json = redisAdapter.get(gameId.toString()) ?: return null
        val restoreContext = objectMapper.readValue(json, GammonRestoreContextDto::class.java)

        return BackgammonWrapper.buildFromContext(restoreContext)
    }


    private fun getGameFromDataBase(gameId: Int): BackgammonWrapper? {
        val movesPerChange = gammonMoveDao.getMoves(gameId)
        val startState = gammonMoveDao.getStartGameContext(gameId) ?: return null

        return when (startState.type) {
            BackgammonType.SHORT_BACKGAMMON -> restoreBackgammon(startState, movesPerChange)
            BackgammonType.REGULAR_GAMMON -> restoreGammon(startState, movesPerChange)
        }
    }

    private fun putGameToCache(roomId: Int, context: GammonRestoreContextDto) {
        logger.info("Putting game $roomId")
        redisAdapter.set(roomId.toString(), objectMapper.writeValueAsString(context))
    }


    fun restoreBackgammon(
        startState: GammonRestoreContextDto,
        movesPerChange: List<MoveSet>
    ): BackgammonWrapper {
        var turn = startState.game.turn
        val deck = ArrayList<Int>(28)
        for (i in 0..<28) {
            deck.add(0)
        }
        for (item in startState.game.deck) {
            deck[item.key + 1] = item.value
        }
        deck[0] = startState.game.bar[-1]!!
        deck[deck.size - 1] = startState.game.bar[1]!!

        for (moves in movesPerChange) {
            for (move in moves.moves.changes) {
                val shiftedFirst = if (move.first == 0) 0 else if (move.first == 25) deck.size - 1 else move.first + 1
                val shiftedSecond = move.second + 1
                val realSignOfMove = deck[shiftedFirst].sign
                if (realSignOfMove == 0) {
                    logger.error("move $move, sign = 0!")
                }
                deck[shiftedFirst] -= realSignOfMove.sign
                deck[shiftedSecond] += realSignOfMove.sign
            }
            turn = -turn
        }

        val nextZar = if (movesPerChange.isEmpty()) {
            startState.game.zarResult
        } else {
            movesPerChange.last().nextZar
        }

        return BackgammonWrapper.buildFromContext(
            startState.copy(
                game = startState.game.copy(
                    turn = turn,
                    bar = mapOf(-1 to deck.first, 1 to deck.last),
                    deck = deck.subList(1, deck.size - 1).mapIndexed { index, i -> index to i }.toMap()
                        .filterValues { it != 0 },
                    zarResult = nextZar
                ),
                numberOfMoves = movesPerChange.size
            )
        )
    }

    fun restoreGammon(
        startState: GammonRestoreContextDto,
        movesPerChange: List<MoveSet>
    ): BackgammonWrapper {
        var turn = startState.game.turn
        val deck = ArrayList<Int>(26)
        for (i in 0..<26) {
            deck.add(0)
        }

        for (i in startState.game.deck) {
            deck[i.key] = i.value
        }

        for (moves in movesPerChange) {
            for (move in moves.moves.changes) {
                val realSignOfMove = deck[move.first].sign
                if (realSignOfMove == 0) {
                    logger.error("move $move, sign = 0")
                }
                deck[move.first] -= realSignOfMove.sign
                deck[move.second] += realSignOfMove.sign
            }
            turn = -turn
        }

        val nextZar = if (movesPerChange.isEmpty()) {
            startState.game.zarResult
        } else {
            movesPerChange.last().nextZar
        }

        return BackgammonWrapper.buildFromContext(
            startState.copy(
                game = startState.game.copy(
                    turn = turn,
                    deck = deck.mapIndexed { index, i -> index to i }.toMap()
                        .filterValues { it != 0 },
                    zarResult = nextZar
                ),
                numberOfMoves = movesPerChange.size
            )
        )
    }


}