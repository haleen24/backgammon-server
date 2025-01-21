package hse.service

import game.backgammon.GammonRestorer
import game.backgammon.dto.ChangeDto
import game.backgammon.enums.BackgammonType
import hse.dao.GammonMoveDao
import hse.dto.GammonRestoreContextDto
import hse.entity.MoveSet
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.TestConstructor
import kotlin.test.assertEquals


@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class RegularGammonGameStoreServiceTest(
    private val gammonStoreService: GammonStoreService,
    @SpyBean private val gammonMoveDao: GammonMoveDao,
) {

    @Test
    fun restoreBlackTest() {
        val state = GammonRestoreContextDto(
            game = GammonRestorer.GammonRestoreContext(
                deck = mapOf(
                    3 to 10
                ),
                turn = -1,
                zarResult = listOf(),
                bar = mapOf(-1 to -2, 1 to 0),
                endFlag = false
            ),
            firstUserId = 1,
            secondUserId = 2,
            type = BackgammonType.SHORT_BACKGAMMON,
            numberOfMoves = 1
        )
        val moves = listOf(
            MoveSet(
                moves = ChangeDto(
                    changes = listOf(Pair(0, 1), Pair(0, 2))
                ),
                gameId = 1,
                moveId = 1,
                nextZar = listOf(1, 2)
            )
        )

        val actualGame = gammonStoreService.restoreBackgammon(state, moves).getRestoreContext()

        assertEquals(0, actualGame.game.bar[-1])
        assertEquals(-1, actualGame.game.deck[1])
        assertEquals(-1, actualGame.game.deck[2])
        assertEquals(10, actualGame.game.deck[3])
    }

    @Test
    fun restoreWhiteTest() {
        val state = GammonRestoreContextDto(
            game = GammonRestorer.GammonRestoreContext(
                deck = mapOf(
                    22 to -10
                ),
                turn = 1,
                zarResult = listOf(),
                bar = mapOf(-1 to 0, 1 to 2),
                endFlag = false
            ),
            firstUserId = 1,
            secondUserId = 2,
            type = BackgammonType.SHORT_BACKGAMMON,
            numberOfMoves = 1
        )
        val moves = listOf(
            MoveSet(
                moves = ChangeDto(
                    changes = listOf(Pair(25, 24), Pair(25, 23))
                ),
                gameId = 1,
                moveId = 1,
                nextZar = listOf(1, 2)
            )
        )

        val actualGame = gammonStoreService.restoreBackgammon(state, moves).getRestoreContext()

        assertEquals(0, actualGame.game.bar[1])
        assertEquals(1, actualGame.game.deck[24])
        assertEquals(1, actualGame.game.deck[23])
        assertEquals(-10, actualGame.game.deck[22])
    }
}