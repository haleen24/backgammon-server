package hse.entity

import hse.enums.GameEntityType
import java.time.ZonedDateTime

class Zar(
    val gameId: Int,
    val moveId: Int,
    val z: List<Int>,
    at: ZonedDateTime
) : TypedMongoEntity(GameEntityType.ZAR, at)