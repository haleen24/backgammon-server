package hse.menu.service

import game.common.enums.GameType
import hse.menu.adapter.GameAdapter
import hse.menu.dao.ConnectDao
import hse.menu.dao.GameDao
import hse.menu.dto.ConnectionDto
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.CountDownLatch

@Service
class MenuGameService(
    private val menuGameDao: GameDao,
    private val connectionDao: ConnectDao,
    private val gameAdapter: GameAdapter
) {

    private final val logger = LoggerFactory.getLogger(MenuGameService::class.java)

    init {
        val job = configureJob().start()
    }

    fun storeRoom(gameType: GameType): Int {
        return menuGameDao.storeGame(gameType)
    }


    fun connect(userId: Int, gameType: GameType): Int {
        val connectionDto = ConnectionDto(userId, CountDownLatch(1), gameType)
        connectionDao.connect(connectionDto)
        connectionDto.latch.await()
        return connectionDto.gameId ?: throw ResponseStatusException(
            HttpStatus.CONFLICT,
            "Упал коннект - очередь поиска не отдала id игры"
        )
    }

    private fun configureJob(): Thread {
        val job = Thread { connectionJob() }
        job.setUncaughtExceptionHandler { thread, ex ->
            logger.error("Uncaught exception $ex in thread $thread \n restarting...")
            Thread { connectionJob() }.start()
        }
        return job
    }

    private fun connectionJob() {
        var firstPlayerConnection: ConnectionDto
        var secondPlayerConnection: ConnectionDto
        while (true) {
            logger.info("Начал ждать юзеров")
            firstPlayerConnection = connectionDao.removeFromConnectionQueue()
            logger.info("Зашел первый: ${firstPlayerConnection.userId}")
            secondPlayerConnection = connectionDao.removeFromConnectionQueue()
            logger.info("Зашел второй: ${secondPlayerConnection.userId}")
            if (firstPlayerConnection.userId == secondPlayerConnection.userId) {
                logger.info("Нельзя играть с самим собой")
                connectionDao.connect(firstPlayerConnection)
                secondPlayerConnection.latch.countDown()
                continue
            }
            // Пока только 1 тип игры
            val gameId = storeRoom(GameType.SHORT_BACKGAMMON)
            logger.info("Сохраняю комнату")
            gameAdapter.gameCreation(
                gameId,
                firstPlayerConnection.userId,
                secondPlayerConnection.userId,
                firstPlayerConnection.gameType,
            )
            firstPlayerConnection.gameId = gameId
            secondPlayerConnection.gameId = gameId
            firstPlayerConnection.latch.countDown()
            secondPlayerConnection.latch.countDown()
            logger.info("Отпустил юзеров")
        }
    }
}