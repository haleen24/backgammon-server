package hse.menu.service

import game.common.enums.GameType
import game.common.enums.GammonGamePoints
import game.common.enums.TimePolicy
import hse.menu.adapter.GameAdapter
import hse.menu.dao.GameDao
import hse.menu.entity.Game
import hse.menu.enums.GameStatus
import org.springframework.stereotype.Service

@Service
class GameService(
    private val gameDao: GameDao,
    private val gameAdapter: GameAdapter,
) {
    fun storeGame(
        gameType: GameType,
        gamePoints: GammonGamePoints,
        timePolicy: TimePolicy,
        firstPlayerId: Long,
        secondPlayerId: Long
    ): Game? {
        var game = Game(
            gameType = gameType,
            gamePoints = gamePoints,
            timePolicy = timePolicy,
            firstPlayerId = firstPlayerId,
            secondPlayerId = secondPlayerId,
            status = GameStatus.NOT_STARTED
        )
        game = gameDao.save(game)
        gameAdapter.gameCreation(game) ?: return null
        game.status = GameStatus.IN_PROCESS
        return gameDao.save(game)
    }

    fun setGameEnd(matchId: Int) {
        val game = gameDao.findById(matchId.toLong()).orElse(null) ?: return
        game.status = GameStatus.END
        gameDao.save(game)
    }
}