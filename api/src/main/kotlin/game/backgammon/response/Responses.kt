package game.backgammon.response

import com.fasterxml.jackson.annotation.JsonInclude
import game.backgammon.dto.DeckItemDto
import game.backgammon.dto.MoveResponseDto
import game.backgammon.dto.StartStateDto
import game.backgammon.enums.BackgammonType
import game.backgammon.enums.Color


data class MoveResponse(
    val moves: List<MoveResponseDto>,
    val color: Color
)

data class HistoryResponse(
    val allMoves: List<MoveResponse>,
    val startState: StartStateDto,
)

data class ConfigResponse(
    val color: Color,
    val turn: Color,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val bar: Map<Color, Int>,
    val deck: Set<DeckItemDto>,
    val zar: List<Int>,
    val first: Boolean,
    val type: BackgammonType
)