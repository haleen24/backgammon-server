package hse.menu.dto

import game.common.enums.GameType
import game.common.enums.GammonGamePoints
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap

data class ConnectQueueHolder(
    val connectionQueues: ConcurrentHashMap<Pair<GameType, GammonGamePoints>, ArrayBlockingQueue<ConnectionDto>>
)