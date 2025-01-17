package game.backgammon

import game.backgammon.sht.ShortBackgammonGame

class GammonRestorer {

    data class GammonRestoreContext(
        val deck: Map<Int, Int>,
        val turn: Int,
        val zarResult: List<Int>,
        val bar: Map<Int, Int>,
        val endFlag: Boolean,
    )

    companion object {
        fun restoreBackgammon(
            context: GammonRestoreContext
        ): ShortBackgammonGame {
            return ShortBackgammonGame(context)
        }
    }
}