package hse.entity

import hse.enums.GameEntityType
import java.time.ZonedDateTime

class MoveWithId(
    val matchId: Int,
    val gameId: Int,
    val moveSet: MoveSet,
    at: ZonedDateTime
): TypedMongoEntity(GameEntityType.MOVE, at)