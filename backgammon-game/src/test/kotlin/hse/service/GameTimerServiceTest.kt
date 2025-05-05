package hse.service

import game.backgammon.enums.Color
import game.common.enums.TimePolicy
import hse.dao.GameTimerDao
import hse.wrapper.BackgammonWrapper
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import java.time.Clock

class GameTimerServiceTest {
    private val gameTimerDao = mock(GameTimerDao::class.java)
    private val clock = mock(Clock::class.java)

    val service = GameTimerService(
        gameTimerDao = gameTimerDao,
        clock = clock
    )

    @Test
    fun `validateAndGet do nothing when time police = NO_TIMER`() {
        val wrapper = mock(BackgammonWrapper::class.java)
        Mockito.`when`(wrapper.timePolicy).thenReturn(TimePolicy.NO_TIMER)

        service.validateAndGet(1, Color.BLACK, wrapper) { throw RuntimeException() }

        verifyNoInteractions(gameTimerDao)
    }
}