package hse.service

import game.backgammon.enums.Color
import game.common.enums.TimePolicy
import hse.dao.GameTimerDao
import hse.dto.TimerActionContext
import hse.entity.GameTimer
import hse.wrapper.BackgammonWrapper
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Clock
import java.time.Duration.between
import java.time.ZonedDateTime

@Service
class GameTimerService(
    private val gameTimerDao: GameTimerDao,
    private val clock: Clock,
) {
    fun validateAndGet(
        matchId: Int,
        currentTurn: Color,
        wrapper: BackgammonWrapper,
        onOutOfTime: () -> Unit
    ): GameTimer? {
        if (wrapper.timePolicy == TimePolicy.NO_TIMER) {
            return null
        }
        val now = ZonedDateTime.now(clock)
        val gameTimer =
            gameTimerDao.getByMatchId(matchId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No timer found")
        val timerActionContext = getActionContext(currentTurn, gameTimer)
        val actionTime = between(timerActionContext.opponentLastAction, now)
        if (actionTime.toMillis() > timerActionContext.playerRemainTime.toMillis()) {
            onOutOfTime()
            gameTimerDao.deleteByMatchId(matchId)
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Out of time")
        }
        return gameTimer
    }

    fun update(matchId: Int, currentTurn: Color, gameTimer: GameTimer) {
        val now = ZonedDateTime.now(clock)
        if (currentTurn == Color.WHITE) {
            val remainTime = between(gameTimer.lastBlackAction, now)
            gameTimer.lastWhiteAction = now
            gameTimer.remainWhiteTime = remainTime.plus(gameTimer.increment)
        } else {
            val remainTime = between(gameTimer.lastWhiteAction, now)
            gameTimer.lastBlackAction = now
            gameTimer.remainBlackTime = remainTime.plus(gameTimer.increment)
        }
        gameTimerDao.setByMatchId(matchId, gameTimer)
    }

    private fun getActionContext(currentTurn: Color, gameTimer: GameTimer): TimerActionContext {
        return if (currentTurn == Color.BLACK) {
            TimerActionContext(
                opponentLastAction = gameTimer.lastWhiteAction,
                playerRemainTime = gameTimer.remainBlackTime
            )
        } else {
            TimerActionContext(
                opponentLastAction = gameTimer.lastBlackAction,
                playerRemainTime = gameTimer.remainWhiteTime
            )
        }
    }
}