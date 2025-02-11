package hse.menu.dto

import game.common.enums.GameType
import game.common.enums.GammonGamePoints
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue

data class ConnectQueueHolder(
    val connectionQueues: ConcurrentHashMap<Pair<GameType, GammonGamePoints>, LinkedBlockingQueue<ConnectionDto>>
)