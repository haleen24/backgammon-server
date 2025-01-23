package hse.menu.dao

import game.common.enums.GameType
import hse.menu.dto.ConnectQueueHolder
import hse.menu.dto.ConnectionDto
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(SCOPE_SINGLETON)
class ConnectDao(
    private val connectionContext: ConnectQueueHolder
) {

    fun connect(connectionDto: ConnectionDto, gameType: GameType) {
        connectionContext.connectionQueues[gameType]!!.put(connectionDto)
    }

    fun removeFromConnectionQueue(gameType: GameType): ConnectionDto {
        return connectionContext.connectionQueues[gameType]!!.take()
    }
}