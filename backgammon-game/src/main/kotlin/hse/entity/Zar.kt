package hse.entity

import hse.enums.GameEntityType

data class Zar(
    val gameId: Int,
    val moveId: Int,
    val z: List<Int>,
    val isDouble: Boolean = false,
) : TypedMongoEntity(GameEntityType.ZAR)