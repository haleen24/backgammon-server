package hse.dao

import hse.dto.GammonRestoreContextDto
import hse.entity.MoveSet

interface GammonMoveDao {
    fun saveMoves(gameId: Int, moveSet: MoveSet)

    fun getMoves(gameId: Int): List<MoveSet>

    fun checkGameExists(gameId: Int): Boolean
    fun saveStartGameContext(gameId: Int, context: GammonRestoreContextDto)

    fun getStartGameContext(gameId: Int): GammonRestoreContextDto?
}