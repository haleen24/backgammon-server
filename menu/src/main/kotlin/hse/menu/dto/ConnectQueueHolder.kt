package hse.menu.dto

import game.common.enums.GameType
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap

data class ConnectQueueHolder(
    val connectionQueues: ConcurrentHashMap<GameType, ArrayBlockingQueue<ConnectionDto>>
)