package hse.menu.service

import game.backgammon.request.CreateGameRequest
import game.common.enums.GameType
import game.common.enums.GammonGamePoints
import game.common.enums.TimePolicy
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
class MenuService(
    private val connectionService: ConnectionService,
    private val gameService: GameService,
    private val playerService: PlayerService,
    @Value("\${disable-job}") isTest: Boolean = false
) {

    private final val logger = LoggerFactory.getLogger(MenuService::class.java)


    private var executor: ExecutorService = Executors.newVirtualThreadPerTaskExecutor()

    init {
        if (!isTest) {
            TimePolicy.entries.forEach { timePolicy ->
                GameType.entries.forEach { type ->
                    GammonGamePoints.entries.forEach { points ->
                        executor.execute { connectionJob(type, points, timePolicy) }
                    }
                }
            }
        }
    }

    fun connect(userId: Int, request: CreateGameRequest): Int {
        val points = GammonGamePoints.of(request.points) ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        val userRating = playerService.getUserRating(userId, request.type, request.timePolicy)
        logger.info("connect: $userId, userRating: $userRating")
        val connectionDto = ConnectionDto(userId, CountDownLatch(1), request.type, userRating.toInt())
        connectionService.connect(connectionDto, points, request.timePolicy)
        connectionDto.latch.await()
        return connectionDto.gameId ?: throw ResponseStatusException(
            HttpStatus.CONFLICT,
            "Упал коннект - очередь поиска не отдала id игры"
        )
    }

    fun disconnect(userId: Int) {
        connectionService.disconnect(userId)
    }

    private fun connectionJob(gameType: GameType, points: GammonGamePoints, timePolicy: TimePolicy) {
        while (true) {
            val connections = connectionService.take(gameType, points, timePolicy)
                .sortedBy { it.userRating }
            for (i in 0..<connections.size - connections.size % 2) {
                val first = connections[i]
                val second = connections[i + 1]
                connect(first, second, gameType, points, timePolicy)
            }
            if (connections.size % 2 != 0) {
                connectionService.connect(connections.last(), points, timePolicy)
            }
        }
    }

    private fun connect(
        firstPlayerConnection: ConnectionDto,
        secondPlayerConnection: ConnectionDto,
        gameType: GameType,
        points: GammonGamePoints,
        timePolicy: TimePolicy
    ) {
        if (connectionService.checkInBan(firstPlayerConnection.userId)) {
            firstPlayerConnection.latch.countDown()
            connectionService.connect(secondPlayerConnection, points, timePolicy)
            return
        }

        if (firstPlayerConnection.userId == secondPlayerConnection.userId) {
            firstPlayerConnection.latch.countDown()
            connectionService.connect(firstPlayerConnection, points, timePolicy)
            return
        }

        val game = gameService.storeGame(
            gameType,
            points,
            timePolicy,
            firstPlayerConnection.userId.toLong(),
            secondPlayerConnection.userId.toLong()
        )
        if (game == null) {
            logger.warn("Не удалось сгенерить игру")
            firstPlayerConnection.latch.countDown()
            secondPlayerConnection.latch.countDown()
            return
        }
        firstPlayerConnection.gameId = game.id.toInt()
        secondPlayerConnection.gameId = game.id.toInt()
        firstPlayerConnection.latch.countDown()
        secondPlayerConnection.latch.countDown()
    }
}