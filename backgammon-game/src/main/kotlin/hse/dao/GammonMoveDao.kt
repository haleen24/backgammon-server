package hse.dao

import hse.dto.GammonRestoreContextDto
import hse.entity.GameWinner
import hse.entity.MoveSet

interface GammonMoveDao {
    fun saveMoves(matchId: Int, gameId: Int, moveSet: MoveSet)

    fun getMoves(matchId: Int, gameId: Int): List<MoveSet>

    fun checkMatchExists(matchId: Int): Boolean
    fun saveStartGameContext(matchId: Int, gameId: Int, context: GammonRestoreContextDto)

    fun getStartGameContext(matchId: Int, gameId: Int): GammonRestoreContextDto?

    fun getCurrentGameInMathId(matchId: Int): Int?

    fun storeWinner(winner: GameWinner)
}