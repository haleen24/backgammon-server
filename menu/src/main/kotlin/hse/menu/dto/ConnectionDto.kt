package hse.menu.dto

import game.common.enums.GameType
import java.util.concurrent.CountDownLatch

data class ConnectionDto(
    val userId: Int,
    val latch: CountDownLatch,
    val gameType: GameType,
    var gameId: Int? = null
)