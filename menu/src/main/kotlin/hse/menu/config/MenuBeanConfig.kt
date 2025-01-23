package hse.menu.config

import game.common.enums.GameType
import hse.menu.dto.ConnectQueueHolder
import hse.menu.dto.ConnectionDto
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap

@Configuration
class MenuBeanConfig {


    @Bean
    fun connectionQueueHolder(): ConnectQueueHolder {
        val map = mapOf(
            GameType.SHORT_BACKGAMMON to ArrayBlockingQueue<ConnectionDto>(10),
            GameType.REGULAR_GAMMON to ArrayBlockingQueue<ConnectionDto>(10),
        )

        return ConnectQueueHolder(
            ConcurrentHashMap(map)
        )
    }
}