package game.backgammon.request

import game.backgammon.dto.MoveDto
import game.backgammon.enums.BackgammonType
import game.common.enums.GameType

data class CreateGameRequest(
    val type: GameType
)

data class MoveRequest(
    val moves: List<MoveDto>
)

data class CreateBackgammonGameRequest(
    val type: BackgammonType,
    val firstUserId: Int,
    val secondUserId: Int,
)