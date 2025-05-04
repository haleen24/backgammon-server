package hse.entity

import hse.dto.GammonRestoreContextDto
import hse.enums.GameEntityType
import java.time.ZonedDateTime

class GameWithId(
    val matchId: Int,
    val gameId: Int,
    val restoreContextDto: GammonRestoreContextDto,
    at: ZonedDateTime,
) : TypedMongoEntity(GameEntityType.START_STATE, at)