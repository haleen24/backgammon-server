package hse.menu.dao

import game.common.enums.GameType

interface GameDao {
    fun storeGame(gameType: GameType): Int
}