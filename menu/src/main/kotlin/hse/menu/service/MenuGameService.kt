package hse.menu.service

import game.common.enums.GameType
import hse.menu.dao.GameDao
import org.springframework.stereotype.Service

@Service
class MenuGameService(
    private val menuGameDao: GameDao
) {
    fun storeRoom(gameType: GameType, buUser: Int): Int {
        return menuGameDao.storeGame(gameType, buUser)
    }
}