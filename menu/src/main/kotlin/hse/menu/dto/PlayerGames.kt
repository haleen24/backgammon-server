package hse.menu.dto

import game.common.enums.GammonGamePoints
import game.common.enums.TimePolicy
import hse.menu.enums.GameStatus

data class PlayerGames (
    val gameId: Long,
    val gameStatus: GameStatus,
    val timePolicy: TimePolicy,
    val gamePoints: GammonGamePoints,
)