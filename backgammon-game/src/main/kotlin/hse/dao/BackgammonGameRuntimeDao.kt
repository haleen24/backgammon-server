package hse.dao

import game.backgammon.enums.BackgammonType
import game.backgammon.sht.ShortBackgammonGame
import hse.wrapper.BackgammonWrapper
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
class BackgammonGameRuntimeDao(
    private val games: ConcurrentHashMap<Int, BackgammonWrapper> = ConcurrentHashMap(),
    private val notConnectedUser: AtomicReference<Int?> = AtomicReference()
) {
    fun createGame(roomId: Int, gameType: BackgammonType): Int {
        if (games.containsKey(roomId)) {
            throw RuntimeException("game with id $roomId already exists")
        }
        val game = when (gameType) {
            BackgammonType.SHORT_BACKGAMMON -> BackgammonWrapper(ShortBackgammonGame())
        }
        games.putIfAbsent(roomId, game) ?: return roomId
        throw ResponseStatusException(HttpStatus.CONFLICT, "$roomId уже занят")
    }

    fun getGame(roomId: Int): BackgammonWrapper {
        return games[roomId] ?: throw RuntimeException("game not found")
    }
}