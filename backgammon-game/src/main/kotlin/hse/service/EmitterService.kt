package hse.service

import hse.dao.EmitterRuntimeDao
import hse.dto.GameEvent
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Service
class EmitterService(
    val emitterRuntimeDao: EmitterRuntimeDao
) {
    fun create(gameId: Int, userId: Int): SseEmitter {
        return emitterRuntimeDao.add(gameId, userId)
    }

    fun sendEventExceptUser(userId: Int, gameId: Int, event: GameEvent) {
        emitterRuntimeDao.getAllInRoom(gameId)
            .filter { it.userId != userId }
            .forEach { it.emitter.send(event) }
    }

    fun sendForAll(gameId: Int, event: GameEvent) {
        emitterRuntimeDao.getAllInRoom(gameId)
            .forEach { kotlin.runCatching { it.emitter.send(event) } }
    }
}