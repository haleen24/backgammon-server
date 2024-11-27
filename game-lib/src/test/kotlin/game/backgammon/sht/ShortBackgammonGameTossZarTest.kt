package game.backgammon.sht

import jdk.jfr.Description
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mockito
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ShortBackgammonGameTossZarTest {

    private var game: ShortBackgammonGame = ShortBackgammonGame()

    @BeforeEach
    fun setUp() {
        val random = Mockito.mock(Random::class.java)
        Mockito.`when`(random.nextInt(Mockito.anyInt(), Mockito.anyInt())).thenReturn(2, 1)
        game = ShortBackgammonGame(random)
        for (i in game.deck.indices) {
            game.deck[i] = 0
        }
    }

    @Test
    fun cantMoveFromBar() {
        Mockito.`when`(game.zar.nextInt(Mockito.anyInt(), Mockito.anyInt())).thenReturn(1)
        game.turn = -1
        game.zarResults = arrayListOf()
        game.bar[-1] = -1
        game.deck[1] = 2
        game.deck[2] = -1

        game.tossBothZar(-1)

        assertEquals(1, game.turn)
        assertEquals(2, game.deck[1])
        assertEquals(-1, game.bar[-1])
        assertEquals(0, game.zarResults.size)
    }

    @Test
    fun cantDoRegularMove() {
        Mockito.`when`(game.zar.nextInt(Mockito.anyInt(), Mockito.anyInt())).thenReturn(1, 2)
        game.turn = -1
        game.zarResults = arrayListOf()
        game.deck[1] = -1
        game.deck[2] = 2
        game.deck[3] = 2

        game.tossBothZar(-1)

        assertEquals(1, game.turn)
        assertEquals(-1, game.deck[1])
        assertEquals(2, game.deck[2])
        assertEquals(2, game.deck[3])
        assertEquals(0, game.zarResults.size)
    }

    @Test
    fun canDoMoveTroughKnockOut() {
        Mockito.`when`(game.zar.nextInt(Mockito.anyInt(), Mockito.anyInt())).thenReturn(2, 1)
        game.turn = -1
        game.zarResults = arrayListOf()
        game.deck[1] = -1
        game.deck[2] = 1
        game.deck[3] = 1

        game.tossBothZar(-1)

        assertEquals(-1, game.turn)
        assertEquals(-1, game.deck[1])
        assertEquals(1, game.deck[2])
        assertEquals(1, game.deck[3])
        assertEquals(2, game.zarResults.size)
    }

    @Test
    fun canMove3from4ForLuckyZar() {
        Mockito.`when`(game.zar.nextInt(Mockito.anyInt(), Mockito.anyInt())).thenReturn(1)
        game.turn = 1
        game.zarResults = arrayListOf()
        game.deck[24] = 1
        game.deck[20] = -2
        game.deck[19] = -2

        game.tossBothZar(1)

        assertEquals(1, game.turn)
        assertEquals(1, game.deck[24])
        assertEquals(-2, game.deck[20])
        assertEquals(-2, game.deck[19])
        assertEquals(3, game.zarResults.size)
    }

    @Test
    fun canMoveHome() {
        Mockito.`when`(game.zar.nextInt(Mockito.anyInt(), Mockito.anyInt())).thenReturn(2)
        game.turn = 1
        game.zarResults = arrayListOf()
        game.deck[0] = 0
        game.deck[1] = 1
        game.deck[2] = 3

        game.tossBothZar(1)

        assertEquals(1, game.turn)
        assertEquals(0, game.deck[0])
        assertEquals(1, game.deck[1])
        assertEquals(3, game.deck[2])
        assertEquals(4, game.zarResults.size)
    }

    @Test
    @Description("Можно два хода: 18 -> 19 и сброс 23")
    fun advancedMoveHome() {
        Mockito.`when`(game.zar.nextInt(Mockito.anyInt(), Mockito.anyInt())).thenReturn(1, 2)
        game.turn = -1
        game.zarResults = arrayListOf()
        game.deck[24] = 2
        game.deck[23] = -1
        game.deck[21] = 2
        game.deck[18] = -1

        game.tossBothZar(-1)

        assertEquals(-1, game.turn)
        assertEquals(2, game.deck[24])
        assertEquals(2, game.deck[21])
        assertEquals(-1, game.deck[23])
        assertEquals(-1, game.deck[18])
        assertEquals(2, game.zarResults.size)
    }

    @Test
    @Description("Проверка 10 -> 4/5 -> 0")
    fun advancedMoveHomeRuleOfBiggestZar() {
        Mockito.`when`(game.zar.nextInt(Mockito.anyInt(), Mockito.anyInt())).thenReturn(5, 6)
        game.turn = 1
        game.zarResults = arrayListOf()
        game.deck[10] = 1
        game.deck[1] = 1

        game.tossBothZar(1)

        assertEquals(1, game.turn)
        assertEquals(1, game.deck[10])
        assertEquals(1, game.deck[1])
        assertEquals(2, game.zarResults.size)
    }

    @Test
    fun canMoveFromBarOnlyOnes() {
        Mockito.`when`(game.zar.nextInt(Mockito.anyInt(), Mockito.anyInt())).thenReturn(1, 2)
        game.turn = -1
        game.zarResults = arrayListOf()
        game.bar[-1] = -1
        game.deck[1] = 2
        game.deck[3] = 2

        game.tossBothZar(-1)

        assertEquals(-1, game.turn)
        assertEquals(1, game.zarResults.size)
    }

    @ParameterizedTest
    @CsvSource("1, 2", "2, 1")
    fun canMoveOnlyOne(firstZar: Int, secondZar: Int) {
        Mockito.`when`(game.zar.nextInt(Mockito.anyInt(), Mockito.anyInt())).thenReturn(firstZar, secondZar)
        game.turn = -1
        game.zarResults = arrayListOf()
        game.deck[1] = -1
        game.deck[4] = 2

        game.tossBothZar(-1)

        assertEquals(-1, game.turn)
        assertEquals(1, game.zarResults.size)
        assertEquals(2, game.zarResults.first())
    }

    @Test
    fun moveAllFromBar() {
        Mockito.`when`(game.zar.nextInt(Mockito.anyInt(), Mockito.anyInt())).thenReturn(3)
        game.turn = 1
        game.zarResults = arrayListOf()
        game.bar[1] = 4
        game.deck[24] = -2

        game.tossBothZar(1)

        assertEquals(1, game.turn)
        assertEquals(4, game.zarResults.size)
    }

    @Test
    // Тест покрывает баг, когда нельзя сходить в стор из дома, из-за чего игнорировались ходы из других секций
    fun moveSeveralTimes() {
        Mockito.`when`(game.zar.nextInt(Mockito.anyInt(), Mockito.anyInt())).thenReturn(5, 6)
        game.turn = -1
        game.zarResults = arrayListOf()
        game.bar[-1] = -1
        game.deck[1] = 1
        game.deck[4] = -1
        game.deck[5] = -1
        game.deck[6] = 4
        game.deck[8] = 3
        game.deck[12] = -5
        game.deck[13] = 5
        game.deck[18] = 1
        game.deck[19] = -5
        game.deck[21] = -2
        game.deck[24] = 1

        game.tossBothZar(-1)

        assertEquals(-1, game.turn)
        assertEquals(2, game.zarResults.size)
    }
}