package hse.service

import game.backgammon.enums.BackgammonType
import game.common.enums.GameType
import game.common.enums.TimePolicy
import hse.adapter.PlayerServiceAdapter
import org.springframework.stereotype.Service
import player.request.ChangeRatingRequest

@Service
class PlayerService(
    private val playerServiceAdapter: PlayerServiceAdapter
) {
    fun changeRating(winnerId: Long, loserId: Long, gameType: BackgammonType, gameTimePolicy: TimePolicy) {
        val realGameType = GameType.valueOf(gameType.name)
        val changeRatingRequest = ChangeRatingRequest(
            winnerId = winnerId,
            loserId = loserId,
            gameType = realGameType,
            gameTimePolicy = gameTimePolicy
        )
        kotlin.runCatching { playerServiceAdapter.changeRating(changeRatingRequest) }
    }
}