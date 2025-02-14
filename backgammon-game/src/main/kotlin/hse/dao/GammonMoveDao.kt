package hse.dao

import hse.dto.GammonRestoreContextDto
import hse.entity.DoubleZar
import hse.entity.GameWinner
import hse.entity.MoveSet

interface GammonMoveDao {
    fun saveMoves(matchId: Int, gameId: Int, moveSet: MoveSet)

    fun saveZar(matchId: Int, gameId: Int, moveId: Int, zar: List<Int>)

    fun getMoves(matchId: Int, gameId: Int): List<MoveSet>

    fun getZar(matchId: Int, gameId: Int, lastMoveId: Int): List<Int>

    fun getAllDoubles(matchId: Int, gameId: Int): List<DoubleZar>

    fun checkMatchExists(matchId: Int): Boolean

    fun saveStartGameContext(matchId: Int, gameId: Int, context: GammonRestoreContextDto)

    fun getStartGameContext(matchId: Int, gameId: Int): GammonRestoreContextDto?

    fun getCurrentGameInMathId(matchId: Int): Int?

    fun storeWinner(winner: GameWinner)

    fun saveDouble(matchId: Int, doubleZar: DoubleZar)

    fun acceptDouble(matchId: Int, gameId: Int, moveId: Int)
}