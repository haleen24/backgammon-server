package hse.entity

import game.backgammon.enums.Color
import hse.enums.GameEntityType


data class GameWinner(
    val matchId: Int,
    val gameId: Int,
    val winner: Int,
): TypedMongoEntity(GameEntityType.WINNER_INFO) {

    val color: Color = if (winner == BLACK) Color.BLACK else Color.WHITE

    companion object {

        private const val BLACK = -1
        private const val WHITE = 1

        fun of(matchId: Int, gameId: Int, color: Color): GameWinner {
            val intColor = if (color == Color.BLACK) BLACK else WHITE
            return GameWinner(matchId, gameId, intColor)
        }
    }
}