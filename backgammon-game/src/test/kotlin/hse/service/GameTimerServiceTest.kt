package hse.service

import game.backgammon.enums.Color
import game.common.enums.TimePolicy
import hse.dao.GameTimerDao
import hse.entity.GameTimer
import hse.wrapper.BackgammonWrapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.Clock
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals

class GameTimerServiceTest {
    private val gameTimerDao = mock(GameTimerDao::class.java)
    private val clock = mock(Clock::class.java)
    private val now = Instant.now()

    private val service = GameTimerService(
        gameTimerDao = gameTimerDao,
        clock = clock
    )

    @Test
    fun `validateAndGet do nothing when time police = NO_TIMER`() {
        val wrapper = mock(BackgammonWrapper::class.java)
        `when`(wrapper.timePolicy).thenReturn(TimePolicy.NO_TIMER)

        service.validateAndGet(1, Color.BLACK, wrapper) { throw RuntimeException() }

        verifyNoInteractions(gameTimerDao)
    }

    @Test
    fun `validateAndGet throws NOT_FOUND exception when policy != NO_TIMER and timer does not exists`() {
        val wrapper = mock(BackgammonWrapper::class.java)
        `when`(wrapper.timePolicy).thenReturn(TimePolicy.DEFAULT_TIMER)

        val exception = assertThrows<ResponseStatusException> {
            service.validateAndGet(
                1,
                Color.BLACK,
                wrapper
            ) { throw RuntimeException() }
        }

        verify(gameTimerDao).getByMatchId(1)
        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }

    @Test
    fun `validateAndGet returns timer`() {
        val wrapper = mock(BackgammonWrapper::class.java)
        val increment = Duration.ofSeconds(2)
        val remainTime = Duration.ofSeconds(3)
        val gameTimer = GameTimer(
            1,
            now.minusSeconds(1),
            now.minusSeconds(1),
            remainTime,
            remainTime,
            increment
        )
        `when`(wrapper.timePolicy).thenReturn(TimePolicy.DEFAULT_TIMER)
        `when`(gameTimerDao.getByMatchId(1)).thenReturn(gameTimer)
        `when`(clock.instant()).thenReturn(now)

        val actual = service.validateAndGet(1, Color.BLACK, wrapper) { throw RuntimeException() }

        verify(gameTimerDao).getByMatchId(1)
        assertEquals(gameTimer, actual)
    }

    @Test
    fun `validateAndGet calls onOutOfTime and throws HTTP exception when time is out`() {
        val wrapper = mock(BackgammonWrapper::class.java)
        val increment = Duration.ofSeconds(2)
        val remainTime = Duration.ofSeconds(3)
        val gameTimer = GameTimer(
            1,
            now.minusSeconds(100),
            now.minusSeconds(100),
            remainTime,
            remainTime,
            increment
        )
        `when`(wrapper.timePolicy).thenReturn(TimePolicy.DEFAULT_TIMER)
        `when`(gameTimerDao.getByMatchId(1)).thenReturn(gameTimer)
        `when`(clock.instant()).thenReturn(now)
        val onOutOfTime = mock<() -> Unit>()

        val exception = assertThrows<ResponseStatusException> { service.validateAndGet(1, Color.BLACK, wrapper, onOutOfTime) }

        verify(gameTimerDao).getByMatchId(1)
        verify(gameTimerDao).deleteByMatchId(1)
        verify(onOutOfTime).invoke()
        assertEquals(HttpStatus.FORBIDDEN, exception.statusCode)
    }

    @Test
    fun `update by WHITE set gameTimer in match`() {
       `when`(clock.instant()).thenReturn(now)
        val increment = Duration.ofSeconds(2)
        val remainTime = Duration.ofSeconds(3)
        val gameTimer = GameTimer(
            1,
            now.minusSeconds(1),
            now.minusSeconds(1),
            remainTime,
            remainTime,
            increment
        )

        service.update(1, Color.WHITE,  gameTimer)

        verify(gameTimerDao).setByMatchId(1, gameTimer)
        assertEquals(Duration.ofSeconds(4), gameTimer.remainWhiteTime)
        assertEquals(now.minusSeconds(1), gameTimer.lastBlackAction)
        assertEquals(now, gameTimer.lastWhiteAction)
    }

    @Test
    fun `update by BLACK set gameTimer in match`() {
        `when`(clock.instant()).thenReturn(now)
        val increment = Duration.ofSeconds(2)
        val remainTime = Duration.ofSeconds(3)
        val gameTimer = GameTimer(
            1,
            now.minusSeconds(1),
            now.minusSeconds(1),
            remainTime,
            remainTime,
            increment
        )

        service.update(1, Color.BLACK,  gameTimer)

        verify(gameTimerDao).setByMatchId(1, gameTimer)
        assertEquals(Duration.ofSeconds(4), gameTimer.remainBlackTime)
        assertEquals(now.minusSeconds(1), gameTimer.lastWhiteAction)
        assertEquals(now, gameTimer.lastBlackAction)
    }
}