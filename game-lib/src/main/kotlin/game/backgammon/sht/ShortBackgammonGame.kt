package game.backgammon.sht

import game.backgammon.Backgammon
import game.backgammon.dto.*
import org.apache.commons.collections4.CollectionUtils
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.*

class ShortBackgammonGame(
    val zar: Random = Random()
) : Backgammon() {
    private var endFlag = false

    var deck = ArrayList<Int>(26)
    private var testDeck: ArrayList<Int>
    var turn = 0
    var zarResults: ArrayList<Int>
    private var testZar: ArrayList<Int>
    var bar = hashMapOf(
        -1 to 0,
        1 to 0
    )
    private var testBar = HashMap<Int, Int>()


    init {
        for (i in 0..<26) {
            deck.add(0)
        }
        deck[1] = -2
        deck[12] = -5
        deck[17] = -3
        deck[19] = -5

        deck[24] = 2
        deck[13] = 5
        deck[8] = 3
        deck[6] = 5

        zarResults = setStartConfiguration()
        testDeck = ArrayList(deck)
        testZar = ArrayList(zarResults)
        testBar = HashMap(bar)
    }

    override fun getConfiguration(): ConfigDto {
        return ConfigDto(
            zar = zarResults,
            bar = bar,
            turn = turn,
            deck = deck.subList(1, 25),
        )
    }

    override fun move(user: Int, moves: List<MoveDto>): ChangeDto {

        if (zarResults.size != moves.size) {
            throw RuntimeException("incorrect number of moves")
        }

        validateGameState(user)

        testDeck = ArrayList(deck)
        testZar = ArrayList(zarResults)
        testBar = HashMap(bar)

        val res = mutableListOf<Pair<Int?, Int?>>()

        moves.forEach { move ->
            res.addAll(makeMove(user, move))
        }
        turn = -user
        validateEnd()
        deck = ArrayList(testDeck)
        zarResults = ArrayList(testZar)
        bar = HashMap(testBar)
        return ChangeDto(res)
    }

    override fun getEndState(): EndDto? {
        if (!endFlag) {
            return null
        }
        return EndDto(if (deck[0] == 15) 1 else -1)
    }

    override fun tossBothZar(user: Int): TossZarDto {
        validateGameState(user)

        if (zarResults.isNotEmpty()) {
            throw RuntimeException("re toss zar")
        }
        val res1 = tossZar()
        val res2 = tossZar()
        val result = mutableListOf<Int>()
        for (i in 0..<if (res1 == res2) 2 else 1) {
            result.add(res1)
            result.add(res2)
        }
        zarResults = ArrayList(result)

        var maxMoves = 0

        for (zarPermutation in CollectionUtils.permutations(zarResults)) {
            testDeck = ArrayList(deck)
            testZar = ArrayList(zarResults)
            testBar = HashMap(bar)
            maxMoves = max(maxMoves, findMaxFromSequence(zarPermutation))
            if (maxMoves == result.size) {
                break
            } else if (result.size == 4) {
                break
            }
        }
        if (maxMoves == result.size) {
            zarResults = ArrayList(result)
        } else if (result.size == 4 && maxMoves != 0) {
            zarResults = ArrayList(result.subList(0, maxMoves))
        } else if (result.size == 2 && maxMoves == 1) {
            val zar = max(res1, res2)
            val to = getBarFrom(turn) + zar
            zarResults = if (testBar[turn]!! > 0 && canMove(to)) {
                arrayListOf(zar)
            } else if ((1..24).any { checkTurn(it) && canMove(it + zar) }) {
                arrayListOf(zar)
            } else {
                arrayListOf(min(res1, res2))
            }
        } else if (maxMoves == 0) {
            zarResults.clear()
            turn = -turn
        }

        return TossZarDto(result)
    }

    override fun checkEnd(): Boolean {
        return endFlag
    }

    private fun findMaxFromSequence(zar: List<Int>): Int {
        val canMoveHome = checkAllInHome(turn)
        val currZar = zar.firstOrNull() ?: return 0
        val nextZar = zar.subList(1, zar.size)
        var next = 0
        var flag = false
        val dif = -turn * currZar
        if (testBar[turn] != 0) {
            val barIdx = getBarFrom(turn)
            if (testDeck[barIdx + dif].absoluteValue > 1 && testDeck[barIdx + dif].sign != turn) {
                return 0
            }
            testBar[turn] = testBar[turn]!! - turn
            next = findMaxFromSequence(nextZar)
            testBar[turn] = testBar[turn]!! + turn
            return next + 1
        }

        for (i in 1..24) {
            if (testDeck[i] == 0 || testDeck[i].sign != turn) {
                continue
            }
            val pos = i + dif
            if ((pos == 0 || pos == 25) && !canMoveHome) {
                return 0
            }
            if (pos in testDeck.indices) {
                if (testDeck[pos].absoluteValue <= 1 || testDeck[pos].sign == turn) {
                    flag = true
                    val beforePos = testDeck[pos]
                    val beforeI = testDeck[i]
                    testDeck[i] -= turn
                    testDeck[pos] = turn * max(abs(turn), abs(testDeck[pos] + turn))
                    next = max(next, findMaxFromSequence(nextZar))
                    testDeck[pos] = beforePos
                    testDeck[i] = beforeI
                }

            }
        }
        if (!flag && canMoveHome) {
            var idx = 0
            val dist = if (turn == -1) {
                idx = testDeck.indexOfFirst { it.sign == -1 }
                25 - idx
            } else {
                idx = testDeck.indexOfLast { it != 0 && it.sign == 1 }
                idx
            }
            // todo: оптимизировать валидацию -> когда попали вот сюда, не надо выполнять следующий пересчет хода
            if (dist < currZar) {
                testDeck[idx] -= turn
                next = findMaxFromSequence(nextZar)
                testDeck[idx] += turn
                flag = true
            }
        }
        return if (flag) {
            1 + next
        } else {
            0
        }
    }

    private fun makeMove(user: Int, move: MoveDto): List<Pair<Int?, Int?>> {
        validateAll(user, move)
        val knocked = checkKnockOut(move.to, user)
        testZar.remove(abs(move.to - move.from))
        testDeck[move.to] += user
        val moveMap = if (move.from == 0 || move.from == 25) {
            testBar[user] = testBar[user]!!.minus(1)
            null to move.to
        } else {
            testDeck[move.from] -= user
            move.from to move.to
        }
        return if (knocked == null) {
            listOf(moveMap)
        } else {
            listOf(moveMap, knocked)
        }

    }

    private fun checkKnockOut(to: Int, user: Int): Pair<Int, Int?>? {
        if (testDeck[to] == -user) {
            testBar[-user] = testBar[-user]!!.plus(1)
            testDeck[to] = 0
            return to to null
        }
        return null
    }

    private fun validateAll(user: Int, move: MoveDto) {
        validateMoveFromBar(user, move)
        validateZar(user, move)
        validateMove(user, move)
        validateExit(user, move)
    }

    private fun validateGameState(user: Int) {
        if (endFlag) {
            throw RuntimeException("game is over")
        }
        if (user != -1 && user != 1) {
            throw RuntimeException("user should be -1 or 1")
        }
        if (user != turn) {
            throw RuntimeException("user != turn")
        }
    }

    private fun validateMoveFromBar(user: Int, move: MoveDto) {
        val moveFromBar = move.from == 0 || move.from == 25
        if (!moveFromBar && testBar[user]!! > 0) {
            throw RuntimeException("firstly u have to clear bar")
        } else if (moveFromBar && testBar[user]!! == 0) {
            throw RuntimeException("cant move from bar")
        }
    }

    private fun validateZar(user: Int, move: MoveDto) {
        val dif = abs(move.to - move.from)
        if (testZar.contains(dif)) {
            return
        }
        if (checkAllInHome(user)) {
            if (testZar.max() > dif) {
                val farthest = if (move.to > move.from) {
                    testDeck.filterIndexed { idx, _ -> idx > 18 && idx < move.from }
                } else {
                    testDeck.filterIndexed { idx, _ -> idx < 7 && idx > move.from }
                }
                if (farthest.isEmpty()) {
                    testZar.remove(testZar.max())
                }
            }
        }

        throw RuntimeException("zar result not found for some moves")

    }

    private fun validateMove(user: Int, move: MoveDto) {
        if (user * (move.to - move.from) > 0) {
            throw RuntimeException("incorrect direction for move ${move.to}")
        }
        if (move.from < 0 || move.from >= deck.size || move.to < 0 || move.to >= deck.size) {
            throw RuntimeException("move ${move.from} -> ${move.to} out of bounds")
        }
        if (move.from != 0 && move.from != 25 && testDeck[move.from] * user <= 0) {
            throw RuntimeException("cant move from position ${move.from}")
        }
        if (!canMove(move.to)) {
            throw RuntimeException("cant move to position ${move.to}")
        }

    }

    private fun checkAllInHome(user: Int): Boolean {
        return if (user == -1) {
            testDeck.indexOfFirst { it.sign == user } > 18
        } else {
            testDeck.indexOfLast { it != 0 && it.sign == user } < 7
        }
    }

    private fun validateExit(user: Int, move: MoveDto) {
        if (move.to == 0 || move.to == 25) {
            if (checkAllInHome(user)) {
                throw RuntimeException("cant exit")
            }
        }

    }

    private fun getBarFrom(user: Int): Int {
        return when (user) {
            1 -> 25
            -1 -> 0
            else -> throw RuntimeException("user cant make this move")
        }
    }

    private fun validateEnd(): Boolean {
        if (abs(deck[0]) == 15 || abs(deck[25]) == 15) {
            endFlag = true
            return true
        }
        return false
    }

    private fun tossZar(): Int {
        return zar.nextInt(1, 7)
    }

    private fun setStartConfiguration(): ArrayList<Int> {
        var firstZar = tossZar()
        var secondZar = tossZar()
        while (firstZar == secondZar) {
            firstZar = tossZar()
            secondZar = tossZar()
        }
        turn = if (firstZar > secondZar) {
            -1
        } else 1
        return arrayListOf(firstZar, secondZar)
    }

    private fun canMove(to: Int): Boolean {
        return testDeck[to] * turn >= -1
    }

    private fun checkTurn(position: Int): Boolean {
        return position != 0 && testDeck[position].sign == turn
    }

    override fun toString(): String {
        return "turn = $turn, zarResults = $zarResults, bar = $bar"
    }
}