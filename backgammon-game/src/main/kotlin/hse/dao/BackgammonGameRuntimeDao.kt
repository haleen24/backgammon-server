package hse.dao

import game.backgammon.enums.BackgammonType
import game.backgammon.sht.ShortBackgammonGame
import hse.config.CacheConfig.Companion.BACKGAMMON_RUNTIME_DAO_CACHE_NAME
import hse.wrapper.BackgammonWrapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Scope
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.ConcurrentHashMap

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
class BackgammonGameRuntimeDao(
    private val games: ConcurrentHashMap<Int, BackgammonWrapper> = ConcurrentHashMap(),
) {
    private val logger = LoggerFactory.getLogger(BackgammonGameRuntimeDao::class.java)

    fun createGame(roomId: Int, gameType: BackgammonType): Int {
        if (games.containsKey(roomId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Game $roomId already exists")
        }
        val game = when (gameType) {
            BackgammonType.SHORT_BACKGAMMON -> BackgammonWrapper(ShortBackgammonGame())
        }
        games.putIfAbsent(roomId, game) ?: return roomId
        throw ResponseStatusException(HttpStatus.CONFLICT, "Game $roomId already exists")
    }

    @Cacheable(BACKGAMMON_RUNTIME_DAO_CACHE_NAME)
    fun getGame(roomId: Int): BackgammonWrapper {
        logger.info("Get game $roomId ignores cache")
        return games[roomId] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Game $roomId not found")
    }

    @CacheEvict(BACKGAMMON_RUNTIME_DAO_CACHE_NAME, key = "#roomId")
    fun closeGame(roomId: Int): Boolean {
        logger.info("Evicting game $roomId")
        return games.remove(roomId) != null
    }
}