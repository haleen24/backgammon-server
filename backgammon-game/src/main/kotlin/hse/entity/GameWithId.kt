package hse.entity

import hse.dto.GammonRestoreContextDto

data class GameWithId(
    val gameId: Int,
    val restoreContextDto: GammonRestoreContextDto,
)