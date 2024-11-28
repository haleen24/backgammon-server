package hse.menu.dao

import hse.menu.dto.ConnectionDto
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.concurrent.ArrayBlockingQueue

@Component
@Scope(SCOPE_SINGLETON)
class ConnectDao(
    private val connectionQueue: ArrayBlockingQueue<ConnectionDto> = ArrayBlockingQueue<ConnectionDto>(10)
) {

    fun connect(connectionDto: ConnectionDto) {
        connectionQueue.put(connectionDto)
    }

    fun removeFromConnectionQueue(): ConnectionDto {
        return connectionQueue.take()
    }
}