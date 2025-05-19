package hse.menu.dto

import game.common.enums.GameType
import game.common.enums.TimePolicy
import hse.menu.enums.GameStatus
import player.response.UserInfoResponse

data class PlayerGames(
    val gameId: Long,
    val gameStatus: GameStatus,
    val timePolicy: TimePolicy,
    val gameType: GameType,
    val opponentUserInfo: UserInfoResponse
)