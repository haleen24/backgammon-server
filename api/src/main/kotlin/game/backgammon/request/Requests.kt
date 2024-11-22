package game.backgammon.request

import game.backgammon.dto.MoveDto
import game.common.enums.GameType

data class CreateGameRequest(
    val type: GameType
)

data class MoveRequest(
    val moves: List<MoveDto>
)