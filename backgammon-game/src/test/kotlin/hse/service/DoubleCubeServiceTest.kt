package hse.service

import com.fasterxml.jackson.databind.ObjectMapper
import game.backgammon.Gammon
import game.backgammon.GammonRestorer
import game.backgammon.enums.BackgammonType
import game.backgammon.enums.Color
import hse.adapter.RedisAdapter
import hse.dao.DoubleCubeDao
import hse.dto.GammonRestoreContextDto
import hse.entity.DoubleCube
import hse.wrapper.BackgammonWrapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestConstructor
import org.springframework.web.server.ResponseStatusException
import kotlin.test.assertEquals


@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class DoubleCubeServiceTest {
    @MockBean
    lateinit var redisAdapter: RedisAdapter

    @MockBean
    lateinit var doubleCubeDao: DoubleCubeDao

    @MockBean
    lateinit var gammonStoreService: GammonStoreService

    @Autowired
    lateinit var doubleCubeService: DoubleCubeService

    @Autowired
    lateinit var objectMapper: ObjectMapper


    @Test
    fun doubleZarCreateTest() {
        val res = BackgammonWrapper.buildFromContext(
            restoreContextDto = GammonRestoreContextDto(
                game = GammonRestorer.GammonRestoreContext(
                    deck = mapOf(10 to -1),
                    turn = Gammon.BLACK,
                    zarResult = listOf(),
                    bar = mapOf(Gammon.BLACK to 0, Gammon.WHITE to 0),
                    endFlag = false
                ),
                firstUserId = 0,
                secondUserId = 1,
                type = BackgammonType.SHORT_BACKGAMMON,
                numberOfMoves = 0,
                blackPoints = 0,
                whitePoints = 0,
                thresholdPoints = 7,
                gameNumber = 1,
            )
        )
        Mockito.`when`(gammonStoreService.getMatchById(Mockito.anyInt())).thenReturn(res)
        doubleCubeService.doubleCube(0, 0)

        Mockito.verify(redisAdapter).rpush(Mockito.anyString(), Mockito.anyString())
    }

    @Test
    fun doubleZarAlreadyHaveOfferedOneTest() {
        val res = BackgammonWrapper.buildFromContext(
            restoreContextDto = GammonRestoreContextDto(
                game = GammonRestorer.GammonRestoreContext(
                    deck = mapOf(10 to -1),
                    turn = Gammon.BLACK,
                    zarResult = listOf(),
                    bar = mapOf(Gammon.BLACK to 0, Gammon.WHITE to 0),
                    endFlag = false
                ),
                firstUserId = 0,
                secondUserId = 1,
                type = BackgammonType.SHORT_BACKGAMMON,
                numberOfMoves = 0,
                blackPoints = 0,
                whitePoints = 0,
                thresholdPoints = 7,
                gameNumber = 1,
            )
        )
        val doubleCube = objectMapper.writeValueAsString(
            DoubleCube(
                gameId = 0,
                moveId = 1,
                by = Color.BLACK,
                isAccepted = false
            )
        )
        Mockito.`when`(gammonStoreService.getMatchById(Mockito.anyInt())).thenReturn(res)
        Mockito.`when`(redisAdapter.lrange((Mockito.anyString()))).thenReturn(
            mutableListOf(
                doubleCube
            )
        )
        val thrown = assertThrows<ResponseStatusException> { doubleCubeService.doubleCube(0, 0) }

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, thrown.statusCode)
    }

    @Test
    fun doubleZarOffered2InARowTest() {
        val res = BackgammonWrapper.buildFromContext(
            restoreContextDto = GammonRestoreContextDto(
                game = GammonRestorer.GammonRestoreContext(
                    deck = mapOf(10 to -1),
                    turn = Gammon.BLACK,
                    zarResult = listOf(),
                    bar = mapOf(Gammon.BLACK to 0, Gammon.WHITE to 0),
                    endFlag = false
                ),
                firstUserId = 0,
                secondUserId = 1,
                type = BackgammonType.SHORT_BACKGAMMON,
                numberOfMoves = 0,
                blackPoints = 0,
                whitePoints = 0,
                thresholdPoints = 7,
                gameNumber = 1,
            )
        )
        val doubleCube = objectMapper.writeValueAsString(
            DoubleCube(
                gameId = 0,
                moveId = 1,
                by = Color.BLACK,
                isAccepted = true
            )
        )
        Mockito.`when`(gammonStoreService.getMatchById(Mockito.anyInt())).thenReturn(res)
        Mockito.`when`(redisAdapter.lrange((Mockito.anyString()))).thenReturn(
            mutableListOf(
                doubleCube
            )
        )
        val thrown = assertThrows<ResponseStatusException> { doubleCubeService.doubleCube(0, 0) }

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, thrown.statusCode)
    }
}