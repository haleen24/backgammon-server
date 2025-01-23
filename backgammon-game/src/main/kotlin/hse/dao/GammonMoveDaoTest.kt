package hse.dao

import hse.dto.GammonRestoreContextDto
import hse.entity.MoveSet
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Repository

@Repository
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class GammonMoveDaoTest(
    private val contextMap: MutableMap<Int, GammonRestoreContextDto> = mutableMapOf(),
    private val moveSetMap: MutableMap<Int, MutableList<MoveSet>> = mutableMapOf(),
) : GammonMoveDao {
    override fun saveMoves(gameId: Int, moveSet: MoveSet) {
        moveSetMap.computeIfAbsent(gameId) { arrayListOf() }.add(moveSet)
    }

    override fun getMoves(gameId: Int): List<MoveSet> {
        return moveSetMap.getOrDefault(gameId, arrayListOf())
    }

    override fun checkGameExists(gameId: Int): Boolean {
        return gameId in contextMap.keys
    }

    override fun saveStartGameContext(gameId: Int, context: GammonRestoreContextDto) {
        contextMap.putIfAbsent(gameId, context)
    }

    override fun getStartGameContext(gameId: Int): GammonRestoreContextDto? {
        return contextMap[gameId]
    }

}