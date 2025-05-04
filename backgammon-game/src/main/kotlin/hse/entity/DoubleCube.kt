package hse.entity

import game.backgammon.enums.Color
import hse.enums.GameEntityType
import java.time.ZonedDateTime

class DoubleCube(
    val gameId: Int,
    val moveId: Int,
    val by: Color,
    val isAccepted: Boolean,
    at: ZonedDateTime
) : TypedMongoEntity(GameEntityType.DOUBLE, at)