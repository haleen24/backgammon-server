package hse.menu.dao

import game.common.enums.GameType
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Repository
class GameRuntimeDao(
    @Value("\${cfg.start-game-id}") private val realId: Int
) : GameDao {

    private val games: ConcurrentHashMap<Int, GameType> = ConcurrentHashMap()

    private var id: AtomicInteger = AtomicInteger(realId)

    override fun storeGame(gameType: GameType): Int {
        val gameId = getId()
        games[gameId] = gameType
        return gameId
    }

    private fun getId(): Int {
        return id.incrementAndGet()
    }
}