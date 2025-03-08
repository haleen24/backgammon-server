package hse.dao

import game.backgammon.enums.Color
import hse.dto.GammonRestoreContextDto
import hse.entity.DoubleCube
import hse.entity.GameWinner
import hse.entity.MoveSet
import hse.entity.SurrenderEntity
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Repository

@Repository
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class GammonMoveRuntimeDao(
    private val contextMap: MutableMap<Int, MutableList<GammonRestoreContextDto>> = mutableMapOf(),
    private val moveSetMap: MutableMap<Int, MutableList<MutableList<MoveSet>>> = mutableMapOf(),
    private val winnerMap: MutableMap<Int, MutableList<Int>> = mutableMapOf(),
) : GammonMoveDao {
    override fun saveMoves(matchId: Int, gameId: Int, moveSet: MoveSet) {
        moveSetMap.computeIfAbsent(matchId) { arrayListOf() }
        val moveSets = moveSetMap[matchId]!!
        if (gameId >= moveSets.size) {
            moveSets.add(gameId, arrayListOf())
        }
        moveSets[gameId].add(moveSet)
    }

    override fun saveZar(matchId: Int, gameId: Int, moveId: Int, zar: List<Int>) {
        TODO("Not yet implemented")
    }

    override fun getMoves(matchId: Int, gameId: Int): List<MoveSet> {
        return moveSetMap.getOrDefault(matchId, arrayListOf(arrayListOf()))[gameId]
    }

    override fun getZar(matchId: Int, gameId: Int, lastMoveId: Int): List<Int> {
        TODO("Not yet implemented")
    }

    override fun getAllDoubles(matchId: Int, gameId: Int): List<DoubleCube> {
        TODO("Not yet implemented")
    }

    override fun checkMatchExists(matchId: Int): Boolean {
        return matchId in contextMap.keys
    }

    override fun saveStartGameContext(matchId: Int, gameId: Int, context: GammonRestoreContextDto) {
        contextMap.putIfAbsent(matchId, arrayListOf())
        val contexts = contextMap[matchId]!!
        if (gameId >= contexts.size) {
            contexts.add(gameId, context)
        }
        contexts[gameId] = context
    }

    override fun getStartGameContext(matchId: Int, gameId: Int): GammonRestoreContextDto? {
        return contextMap[matchId]!![gameId]
    }

    override fun getCurrentGameInMathId(matchId: Int): Int? {
        return contextMap[matchId]?.size
    }

    override fun storeWinner(winner: GameWinner) {
        winnerMap.putIfAbsent(winner.matchId, arrayListOf())
        winnerMap[winner.matchId]!!.add(winner.gameId, winner.gameId)
    }

    override fun saveDouble(matchId: Int, doubleCube: DoubleCube) {
        TODO("Not yet implemented")
    }

    override fun acceptDouble(matchId: Int, gameId: Int, moveId: Int) {
        TODO("Not yet implemented")
    }

    override fun getWinners(matchId: Int): List<GameWinner> {
        TODO("Not yet implemented")
    }

    override fun surrender(matchId: Int, surrenderEntity: SurrenderEntity) {
        TODO("Not yet implemented")
    }

    override fun getSurrenderInfo(matchId: Int): List<SurrenderEntity> {
        TODO("Not yet implemented")
    }

}