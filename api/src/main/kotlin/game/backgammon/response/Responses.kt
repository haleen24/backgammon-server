package game.backgammon.response

import game.backgammon.dto.ConfigResponseDto
import game.backgammon.dto.MoveResponseDto
import game.backgammon.dto.StartStateDto
import game.backgammon.enums.Color
import game.backgammon.enums.DoubleCubePositionEnum
import jdk.jfr.Threshold


data class MoveResponse(
    val moves: List<MoveResponseDto>,
    val color: Color
)

data class ConfigResponse(
    val gameData: ConfigResponseDto,
    val blackPoints: Int,
    val whitePoints: Int,
    val threshold: Int,
    val players: Map<Color, Int>,
    val doubleCubeValue: Int?,
    val doubleCubePosition: DoubleCubePositionEnum,
    val winner: Color?
)