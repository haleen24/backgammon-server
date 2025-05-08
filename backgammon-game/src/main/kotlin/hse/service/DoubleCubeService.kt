package hse.service

import game.backgammon.enums.Color
import game.backgammon.enums.DoubleCubePositionEnum
import hse.dao.DoubleCubeDao
import hse.dto.AcceptDoubleEvent
import hse.dto.DoubleEvent
import hse.entity.DoubleCube
import hse.wrapper.BackgammonWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Clock

@Service
// TODO продумать взаимодействие с кешом
class DoubleCubeService(
    val gammonStoreService: GammonStoreService,
    val emitterService: EmitterService,
    val doubleCubeDao: DoubleCubeDao,
    val clock: Clock,
) {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    fun doubleCube(matchId: Int, userId: Int, game: BackgammonWrapper) {
        val doubles = getAllDoubles(matchId, game.gameId)
        logger.info("double: $doubles")
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
        if (doubles.size >= 6) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "maximum double already reached")
        }
        createDoubleRequest(matchId, game.gameId, game.numberOfMoves, userId, userColor)
    }

    fun acceptDouble(matchId: Int, userId: Int, game: BackgammonWrapper) {
        val doubles = getAllDoubles(matchId, game.gameId)
        logger.info("accept double: $doubles")
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
        acceptDouble(matchId, last)
        emitterService.sendEventExceptUser(userId, matchId, AcceptDoubleEvent(game.getPlayerColor(userId)))
    }

    fun getDoubleCubePosition(
        matchId: Int,
        game: BackgammonWrapper,
        doubles: List<DoubleCube>
    ): DoubleCubePositionEnum {
        val winners = gammonStoreService.getWinnersInMatch(matchId)
        if (game.thresholdPoints == 1) {
            return DoubleCubePositionEnum.UNAVAILABLE
        }
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


    fun getAllDoubles(matchId: Int, gameId: Int): List<DoubleCube> {
//        val fromCache =
//            redisAdapter.lrange(getCacheKey(matchId)) ?: return doubleCubeDao.getAllDoubles(matchId, gameId)
//                .sortedBy { it.moveId }
//        return fromCache.map { objectMapper.reader().readValue(it, DoubleCube::class.java) }
        return doubleCubeDao.getAllDoubles(matchId, gameId)
    }

    fun acceptDouble(matchId: Int, last: DoubleCube) {
//        val cacheKey = getCacheKey(matchId)
        doubleCubeDao.acceptDouble(matchId, last.gameId, last.moveId)
//        putToCache(cacheKey, last.copy(isAccepted = true))
    }

    private fun createDoubleRequest(matchId: Int, gameId: Int, moveId: Int, userId: Int, by: Color) {
        val doubleCube = DoubleCube(gameId, moveId, by, false, clock.instant())
        doubleCubeDao.saveDouble(matchId, doubleCube)
//        putToCache(matchId, doubleCube)
        emitterService.sendEventExceptUser(userId, matchId, DoubleEvent(by))
    }

//    private fun putToCache(matchId: Any, doubleCube: DoubleCube) {
//        redisAdapter.rpush(getCacheKey(matchId), objectMapper.writeValueAsString(doubleCube))
//    }

//    private fun getCacheKey(matchId: Any): String {
//        return "dc-$matchId"
//    }
}