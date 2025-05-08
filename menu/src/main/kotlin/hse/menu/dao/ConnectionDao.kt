package hse.menu.dao

import game.common.enums.GameType
import game.common.enums.GammonGamePoints
import game.common.enums.TimePolicy
import hse.menu.dto.ConnectionDto

interface ConnectionDao {
    fun enqueue(connection: ConnectionDto, points: GammonGamePoints, timePolicy: TimePolicy)

    fun dequeue(gameType: GameType, points: GammonGamePoints, timePolicy: TimePolicy): ConnectionDto
}