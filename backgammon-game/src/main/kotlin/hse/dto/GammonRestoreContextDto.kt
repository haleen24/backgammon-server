package hse.dto

import game.backgammon.GammonRestorer
import game.backgammon.enums.BackgammonType

data class GammonRestoreContextDto(
    val game: GammonRestorer.GammonRestoreContext,
    val firstUserId: Int,
    val secondUserId: Int,
    val type: BackgammonType,
    val numberOfMoves: Int,
    val blackPoints: Int,
    val whitePoints: Int,
    val thresholdPoints: Int,
    val gameNumber: Int,
    val endFlag: Boolean,
)
