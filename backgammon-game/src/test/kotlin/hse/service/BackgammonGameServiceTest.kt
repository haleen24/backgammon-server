package hse.service

import game.backgammon.Gammon
import game.backgammon.enums.BackgammonType
import game.backgammon.enums.Color
import hse.dto.EndMatchEvent
import hse.wrapper.BackgammonWrapper
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import kotlin.test.assertEquals

class BackgammonGameServiceTest {


    private var service: BackgammonGameService

    private val emitterService: EmitterService = Mockito.mock(EmitterService::class.java)
    private val gammonStoreService: GammonStoreService = Mockito.mock(GammonStoreService::class.java)

    init {
        service = BackgammonGameService(emitterService, gammonStoreService)
    }

    @Test
    fun `handle game end happy path`() {
        val wrapper = Mockito.spy(
            BackgammonWrapper(
                game = Mockito.mock(Gammon::class.java),
                type = BackgammonType.SHORT_BACKGAMMON,
                gameId = 1,
                blackPoints = 0,
                whitePoints = 0,
                thresholdPoints = 2
            )
        )
        Mockito.doReturn(mapOf(true to Color.BLACK, false to Color.WHITE)).`when`(wrapper).gameEndStatus()
        Mockito.doReturn(1).`when`(wrapper).getPointsForGame()

        service.handleGameEnd(1, wrapper)

        Mockito.verify(wrapper, Mockito.times(1)).restore()
        Mockito.verify(gammonStoreService, Mockito.times(1)).saveGameOnCreation(1, 2, wrapper)
        assertEquals(1, wrapper.blackPoints)
    }

    @Test
    fun `handle match end happy path`() {
        val wrapper = Mockito.spy(
            BackgammonWrapper(
                game = Mockito.mock(Gammon::class.java),
                type = BackgammonType.SHORT_BACKGAMMON,
                gameId = 1,
                blackPoints = 1,
                whitePoints = 0,
                thresholdPoints = 2
            )
        )
        Mockito.doReturn(mapOf(true to Color.BLACK, false to Color.WHITE)).`when`(wrapper).gameEndStatus()
        Mockito.doReturn(1).`when`(wrapper).getPointsForGame()

        service.handleGameEnd(1, wrapper)

        Mockito.verify(wrapper, Mockito.times(0)).restore()
        Mockito.verify(gammonStoreService, Mockito.times(0)).saveGameOnCreation(1, 2, wrapper)
        Mockito.verify(emitterService, Mockito.times(1)).sendForAll(1, EndMatchEvent(Color.BLACK, Color.WHITE))
        assertEquals(2, wrapper.blackPoints)
    }
}