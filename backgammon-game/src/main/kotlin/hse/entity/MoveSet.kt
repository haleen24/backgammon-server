package hse.entity

import game.backgammon.dto.ChangeDto

data class MoveSet(
    val moves: ChangeDto,
    val gameId: Int,
    val moveId: Int,
    val nextZar: List<Int>,
)