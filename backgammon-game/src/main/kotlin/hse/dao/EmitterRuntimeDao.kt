package hse.dao

import hse.dto.EmitterDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

@Component
class EmitterRuntimeDao(
    private val emitters: ConcurrentHashMap<Int, HashSet<EmitterDto>> = ConcurrentHashMap(),
    @Value("\${config.sse.time-out}") private val sseTimeOut: Long
) {

    val logger = LoggerFactory.getLogger(this.javaClass)
    @Synchronized
    fun add(gameId: Int, userId: Int): SseEmitter {
        if (!emitters.containsKey(gameId)) {
            emitters[gameId] = HashSet()
        }
        val emitter = SseEmitter(sseTimeOut)
        emitter.onCompletion { remove(gameId, userId) }
        emitter.onError { remove(gameId, userId) }
        emitter.onTimeout { remove(gameId, userId) }
        emitters[gameId]!!.add(EmitterDto(userId, emitter))
        logger.info("Добавили с таймаутом $sseTimeOut")
        return emitter
    }

    @Synchronized
    fun remove(gameId: Int, userId: Int) {
        emitters[gameId]?.removeIf { it.userId == userId }
    }

    fun getAllInRoom(gameId: Int): Set<EmitterDto> {
        return emitters[gameId] ?: HashSet()
    }
}