package hse.menu.service

import game.common.enums.GameType
import game.common.enums.GammonGamePoints
import game.common.enums.TimePolicy
import hse.menu.adapter.GameAdapter
import hse.menu.dao.GameDao
import hse.menu.dto.PlayerGames
import hse.menu.entity.Game
import hse.menu.enums.GameStatus
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
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

    fun storeGameByInvitation(
        gameType: GameType,
        gamePoints: GammonGamePoints,
        timePolicy: TimePolicy,
        firstPlayerId: Long,
        secondPlayerId: Long
    ): Game {
        val game = Game(
            gameType = gameType,
            gamePoints = gamePoints,
            timePolicy = timePolicy,
            firstPlayerId = firstPlayerId,
            secondPlayerId = secondPlayerId,
            status = GameStatus.NOT_STARTED
        )
        return gameDao.save(game)
    }

    fun startGame(game: Game) {
        gameAdapter.gameCreation(game) ?: return
        game.status = GameStatus.IN_PROCESS
        gameDao.save(game)
    }

    fun declineGameFromInvitation(game: Game) {
        gameDao.deleteAllById(listOf(game.id))
    }

    fun setGameEnd(matchId: Long) {
        val game = gameDao.findById(matchId).orElse(null) ?: return
        game.status = GameStatus.END
        gameDao.save(game)
    }

    fun getGamesByPlayer(playerId: Long, pageNumber: Int, pageSize: Int): List<PlayerGames> {
        val pageable = PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC, "id")
        val games = gameDao.findAll(pageable)
        return games.toList().map { PlayerGames(it.id, it.status, it.timePolicy, it.gamePoints) }
    }

    fun findByPlayersAndStatus(invitedUser: Long, invitedBy: Long, status: GameStatus): Game? {
        return gameDao.findByFirstPlayerIdAndSecondPlayerIdAndStatus(invitedBy, invitedUser, status).firstOrNull()
    }
}