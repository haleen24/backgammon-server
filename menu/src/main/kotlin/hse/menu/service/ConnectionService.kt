package hse.menu.service

import game.common.enums.GameType
import game.common.enums.GammonGamePoints
import hse.menu.dao.ConnectionDao
import hse.menu.dto.ConnectionDto
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
@Scope(SCOPE_SINGLETON)
class ConnectionService(
    val connectionDao: ConnectionDao,
    val inQueueFilter: MutableSet<Int> = ConcurrentHashMap.newKeySet(),
    val cancelledFilter: MutableSet<Int> = ConcurrentHashMap.newKeySet(),
) {

    fun connect(connectionDto: ConnectionDto, points: GammonGamePoints) {
        val userId = connectionDto.userId
        cancelledFilter.remove(userId)
        if (userId in inQueueFilter) {
            return
        }
        inQueueFilter.add(userId)
        connectionDao.enqueue(connectionDto, points)
    }

    fun take(gameType: GameType, points: GammonGamePoints): ConnectionDto {
        var res: ConnectionDto? = null

        while (res == null) {
            res = connectionDao.dequeue(gameType, points)
            val userId = res.userId

            inQueueFilter.remove(userId)

            if (userId in cancelledFilter) {
                res = null
            }
        }

        return res
    }

    fun checkInBan(userId: Int): Boolean {
        return cancelledFilter.contains(userId)
    }

    fun disconnect(userId: Int) {
        inQueueFilter.remove(userId)
        cancelledFilter.add(userId)
    }
}