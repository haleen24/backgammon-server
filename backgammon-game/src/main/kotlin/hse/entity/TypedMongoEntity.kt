package hse.entity

import hse.enums.GameEntityType
import java.time.ZonedDateTime

open class TypedMongoEntity(
    var type: GameEntityType,
    val at: ZonedDateTime,
)