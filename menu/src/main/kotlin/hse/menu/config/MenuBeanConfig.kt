package hse.menu.config

import game.common.enums.GameType
import game.common.enums.GammonGamePoints
import hse.menu.dto.ConnectQueueHolder
import hse.menu.dto.ConnectionDto
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue

@Configuration
class MenuBeanConfig {


    @Bean
    fun connectionQueueHolder(): ConnectQueueHolder {
        val map = mutableMapOf<Pair<GameType, GammonGamePoints>, LinkedBlockingQueue<ConnectionDto>>()

        GameType.entries.forEach { type ->
            GammonGamePoints.entries.forEach { points ->
                map[type to points] = LinkedBlockingQueue<ConnectionDto>()
            }
        }

        return ConnectQueueHolder(
            ConcurrentHashMap(map)
        )
    }
}