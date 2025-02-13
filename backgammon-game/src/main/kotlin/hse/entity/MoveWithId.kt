package hse.entity

import hse.enums.GameEntityType

data class MoveWithId(
    val matchId: Int,
    val gameId: Int,
    val moveSet: MoveSet,
): TypedMongoEntity(GameEntityType.MOVE)