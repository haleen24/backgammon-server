package game.backgammon.response

import game.backgammon.dto.DeckItemDto
import game.backgammon.dto.MoveDto
import game.backgammon.enums.Color

data class MoveResponse(
    val moves: Map<Int?, Int?>,
    val user: Int
)

data class ConfigResponse(
    val color: Color,
    val turn: Color,
    val bar: Map<Color, Int>,
    val deck: Set<DeckItemDto>,
    val store: Map<Color, Int>,
    val zar: List<Int>
)