package game.backgammon

import game.backgammon.dto.*
import java.util.*

abstract class Gammon(
    val zar: Random = Random()
) {

    var turn = 0
    var zarResults: ArrayList<Int> = arrayListOf()
    var foolZar = ArrayList<Int>()
    var endFlag: Boolean = false

    companion object {
        const val BLACK = -1
        const val WHITE = 1
    }

    abstract fun reload(): Gammon
    abstract fun getConfiguration(): ConfigDto
    abstract fun move(user: Int, moves: List<MoveDto>): ChangeDto
    abstract fun getEndState(): EndDto?
    abstract fun tossBothZar(): TossZarDto
    abstract fun checkEnd(): Boolean


}