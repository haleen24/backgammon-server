package game.backgammon.response

import game.backgammon.dto.DeckItemDto
import game.backgammon.dto.MoveDto
import game.backgammon.enums.Color

data class MoveResponse(
    val moves: List<MoveResponseDto>,
    val user: Int
) {
    data class MoveResponseDto(
        private val from: Int?,
        private val to: Int?
    )
}

data class ConfigResponse(
    val color: Color,
    val turn: Color,
    val bar: Map<Color, Int>,
    val deck: Set<DeckItemDto>,
    val zar: List<Int>
)