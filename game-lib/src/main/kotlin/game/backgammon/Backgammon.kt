package game.backgammon

import game.backgammon.dto.*

abstract class Backgammon {
    abstract fun getConfiguration(): ConfigDto
    abstract fun move(user: Int, moves: List<MoveDto>): ChangeDto
    abstract fun getEndState(): EndDto?
    abstract fun tossBothZar(): TossZarDto
    abstract fun checkEnd(): Boolean
}