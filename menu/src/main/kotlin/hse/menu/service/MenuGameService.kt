package hse.menu.service

import game.backgammon.request.CreateGameRequest
import game.common.enums.GameType
import game.common.enums.GammonGamePoints
import hse.menu.adapter.GameAdapter
import hse.menu.dao.GameDao
import hse.menu.dto.ConnectionDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Service
class MenuGameService(
    private val menuGameDao: GameDao,
    private val connectionService: ConnectionService,
    private val gameAdapter: GameAdapter,

    @Value("\${disable-job}") isTest: Boolean = false
) {

    private final val logger = LoggerFactory.getLogger(MenuGameService::class.java)


    private var executor: ExecutorService = Executors.newVirtualThreadPerTaskExecutor()

    init {
        if (!isTest) {
            GameType.entries.forEach { type ->
                GammonGamePoints.entries.forEach { points ->
                    executor.execute { connectionJob(type, points) }
                }
            }
        }
    }

    fun storeRoom(gameType: GameType): Int {
        return menuGameDao.storeGame(gameType)
    }


    fun connect(userId: Int, request: CreateGameRequest): Int {
        val connectionDto = ConnectionDto(userId, CountDownLatch(1), request.type)
        val points = GammonGamePoints.of(request.points) ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        connectionService.connect(connectionDto, points)
        connectionDto.latch.await()
        return connectionDto.gameId ?: throw ResponseStatusException(
            HttpStatus.CONFLICT,
            "Упал коннект - очередь поиска не отдала id игры"
        )
    }

    fun disconnect(userId: Int) {
        connectionService.disconnect(userId)
    }

    private fun connectionJob(gameType: GameType, points: GammonGamePoints) {
        var firstPlayerConnection: ConnectionDto
        var secondPlayerConnection: ConnectionDto
        while (true) {
            firstPlayerConnection = connectionService.take(gameType, points)
            secondPlayerConnection = connectionService.take(gameType, points)
            if (connectionService.checkInBan(firstPlayerConnection.userId)) {
                firstPlayerConnection.latch.countDown()
                connectionService.connect(secondPlayerConnection, points)
                continue
            }

            if (firstPlayerConnection.userId == secondPlayerConnection.userId) {
                firstPlayerConnection.latch.countDown()
                connectionService.connect(secondPlayerConnection, points)
                continue
            }
            val gameId = storeRoom(gameType)
            val realRoomId = gameAdapter.gameCreation(
                gameId,
                firstPlayerConnection.userId,
                secondPlayerConnection.userId,
                firstPlayerConnection.gameType,
                points
            )
            if (realRoomId != gameId) {
                logger.info("Не удалось сгенерить игру с id $gameId")
                firstPlayerConnection.latch.countDown()
                secondPlayerConnection.latch.countDown()
                continue
            }
            firstPlayerConnection.gameId = gameId
            secondPlayerConnection.gameId = gameId
            firstPlayerConnection.latch.countDown()
            secondPlayerConnection.latch.countDown()
        }
    }
}