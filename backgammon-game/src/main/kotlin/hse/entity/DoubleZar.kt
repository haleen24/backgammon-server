package hse.entity

import game.backgammon.enums.Color
import hse.enums.GameEntityType

data class DoubleZar(
    val gameId: Int,
    val moveId: Int,
    val by: Color,
    val isAccepted: Boolean
) : TypedMongoEntity(GameEntityType.DOUBLE)