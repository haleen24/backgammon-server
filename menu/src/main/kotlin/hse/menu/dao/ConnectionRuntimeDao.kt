package hse.menu.dao

import game.common.enums.GameType
import game.common.enums.GammonGamePoints
import game.common.enums.TimePolicy
import hse.menu.dto.ConnectQueueHolder
import hse.menu.dto.ConnectionDto
import hse.menu.dto.GameSearchDetails
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Repository
import java.util.concurrent.LinkedBlockingQueue

@Repository
@Scope(SCOPE_SINGLETON)
class ConnectionRuntimeDao(
    val connectionContext: ConnectQueueHolder,
) : ConnectionDao {
    override fun enqueue(connection: ConnectionDto, points: GammonGamePoints, timePolicy: TimePolicy) {
        val gameSearchDetails = GameSearchDetails(connection.gameType, points, timePolicy)
        connectionContext.connectionQueues.putIfAbsent(gameSearchDetails, LinkedBlockingQueue())
        connectionContext.connectionQueues[gameSearchDetails]!!.put(connection)
    }

    override fun dequeue(gameType: GameType, points: GammonGamePoints, timePolicy: TimePolicy): ConnectionDto {
        val gameSearchDetails = GameSearchDetails(gameType, points, timePolicy)
        connectionContext.connectionQueues.putIfAbsent(gameSearchDetails, LinkedBlockingQueue())
        return connectionContext.connectionQueues[gameSearchDetails]!!.take()
    }
}