package hse.menu.dao

import game.common.enums.GameType
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Repository
class GameRuntimeDao : GameDao {

    private val games: ConcurrentHashMap<Int, GameType> = ConcurrentHashMap()

    private var id: AtomicInteger = AtomicInteger(0)

    override fun storeGame(gameType: GameType): Int {
        val gameId = getId()
        games[gameId] = gameType
        return gameId
    }

    private fun getId(): Int {
        return id.incrementAndGet()
    }
}