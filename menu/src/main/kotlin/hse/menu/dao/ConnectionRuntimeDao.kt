package hse.menu.dao

import game.common.enums.GameType
import game.common.enums.GammonGamePoints
import hse.menu.dto.ConnectQueueHolder
import hse.menu.dto.ConnectionDto
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Repository

@Repository
@Scope(SCOPE_SINGLETON)
class ConnectionRuntimeDao(
    val connectionContext: ConnectQueueHolder,
) : ConnectionDao {
    override fun enqueue(connection: ConnectionDto, points: GammonGamePoints) {
        connectionContext.connectionQueues[Pair(connection.gameType, points)]!!.put(connection)
    }

    override fun dequeue(gameType: GameType, points: GammonGamePoints): ConnectionDto {
        return connectionContext.connectionQueues[Pair(gameType, points)]!!.take()
    }
}