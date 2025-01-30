package hse.entity

import hse.dto.GammonRestoreContextDto

data class GameWithId(
    val matchId: Int,
    val gameId: Int,
    val restoreContextDto: GammonRestoreContextDto,
)