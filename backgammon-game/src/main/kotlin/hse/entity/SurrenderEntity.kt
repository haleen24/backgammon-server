package hse.entity

import game.backgammon.enums.Color
import hse.enums.GameEntityType

class SurrenderEntity(
    val gameId: Int,
    val who: Color,
    val endMatch: Boolean
): TypedMongoEntity(GameEntityType.SURRENDER)
