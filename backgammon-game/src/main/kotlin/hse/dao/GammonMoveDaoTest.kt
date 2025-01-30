package hse.dao

import hse.dto.GammonRestoreContextDto
import hse.entity.MoveSet
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Repository

@Repository
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class GammonMoveDaoTest(
    private val contextMap: MutableMap<Int, MutableList<GammonRestoreContextDto>> = mutableMapOf(),
    private val moveSetMap: MutableMap<Int, MutableList<MutableList<MoveSet>>> = mutableMapOf(),
) : GammonMoveDao {
    override fun saveMoves(matchId: Int, gameId: Int, moveSet: MoveSet) {
        moveSetMap.computeIfAbsent(matchId) { arrayListOf() }
        val moveSets = moveSetMap[matchId]!!
        if (gameId >= moveSets.size) {
            moveSets.add(gameId, arrayListOf())
        }
        moveSets[gameId].add(moveSet)
    }

    override fun getMoves(matchId: Int, gameId: Int): List<MoveSet> {
        return moveSetMap.getOrDefault(matchId, arrayListOf(arrayListOf()))[gameId]
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

}