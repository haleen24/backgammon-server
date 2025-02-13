package hse.entity

import hse.dto.GammonRestoreContextDto
import hse.enums.GameEntityType

data class GameWithId(
    val matchId: Int,
    val gameId: Int,
    val restoreContextDto: GammonRestoreContextDto,
) : TypedMongoEntity(GameEntityType.START_STATE)