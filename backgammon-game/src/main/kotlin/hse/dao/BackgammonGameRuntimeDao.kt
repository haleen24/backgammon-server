package hse.dao

import game.backgammon.enums.BackgammonType
import game.backgammon.sht.ShortBackgammonGame
import hse.wrapper.BackgammonWrapper
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
class BackgammonGameRuntimeDao(
    private val games: ConcurrentHashMap<Int, BackgammonWrapper> = ConcurrentHashMap()
) {
    fun createGame(roomId: Int, gameType: BackgammonType): Int {
        if (games.containsKey(roomId)) {
            throw RuntimeException("game with id $roomId already exists")
        }
        games[roomId] = when (gameType) {
            BackgammonType.SHORT_BACKGAMMON -> BackgammonWrapper(ShortBackgammonGame())
        }
        return roomId
    }

    fun getGame(roomId: Int): BackgammonWrapper {
        return games[roomId] ?: throw RuntimeException("game not found")
    }
}