package game.backgammon.response

import game.backgammon.dto.DeckItemDto
import game.backgammon.dto.MoveResponseDto
import game.backgammon.enums.Color


data class MoveResponse(
    val moves: List<MoveResponseDto>,
    val color: Color
)

data class HistoryResponse(
    val moves: List<MoveResponseDto>,
)


data class ConfigResponse(
    val color: Color,
    val turn: Color,
    val bar: Map<Color, Int>,
    val deck: Set<DeckItemDto>,
    val zar: List<Int>,
    val first: Boolean
)