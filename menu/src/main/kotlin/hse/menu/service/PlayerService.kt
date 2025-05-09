package hse.menu.service

import game.common.enums.GameType
import game.common.enums.TimePolicy
import hse.menu.adapter.PlayerAdapter
import org.springframework.stereotype.Service

@Service
class PlayerService(
    private val playerAdapter: PlayerAdapter,
) {
    fun getUserRating(userId: Int, gameType: GameType, timePolicy: TimePolicy): Long {
        val rating = playerAdapter.getUserInfo(userId.toLong()).rating
        return (if (gameType == GameType.SHORT_BACKGAMMON) {
            if (timePolicy == TimePolicy.BLITZ) {
                rating.nardeBlitz
            } else {
                rating.nardeDefault
            }
        } else {
            if (timePolicy == TimePolicy.BLITZ) {
                rating.nardeBlitz
            } else {
                rating.nardeDefault
            }
        }).toLong()
    }
}