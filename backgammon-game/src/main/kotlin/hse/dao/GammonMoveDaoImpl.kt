package hse.dao

import hse.dto.GammonRestoreContextDto
import hse.entity.*
import hse.enums.GameEntityType
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class GammonMoveDaoImpl(
    private val mongoTemplate: MongoTemplate
) : GammonMoveDao {

    companion object {
        const val GAME_ID = "gameId"
        const val ENTITY_TYPE = "type"
        const val MOVE_ID = "moveId"
    }

    override fun saveMoves(matchId: Int, gameId: Int, moveSet: MoveSet) {
        mongoTemplate.save(MoveWithId(matchId, gameId, moveSet), getCollectionName(matchId))
    }

    override fun saveZar(matchId: Int, gameId: Int, moveId: Int, zar: List<Int>, isDouble: Boolean) {
        mongoTemplate.save(Zar(gameId, moveId, zar, isDouble), getCollectionName(matchId))
    }

    override fun getMoves(matchId: Int, gameId: Int): List<MoveSet> {
        val query = Query().addCriteria(
            Criteria.where(ENTITY_TYPE).`is`(GameEntityType.MOVE.name).and(GAME_ID).`is`(gameId)
        )
        query.fields().exclude(ENTITY_TYPE)
        return mongoTemplate.find(
            query,
            MoveWithId::class.java,
            getCollectionName(matchId)
        )
            .map { it.moveSet }
    }

    override fun getZar(matchId: Int, gameId: Int, lastMoveId: Int): List<Int> {
        val query =
            Query().addCriteria(
                Criteria.where(ENTITY_TYPE).`is`(GameEntityType.ZAR.name).and(GAME_ID).`is`(gameId).and(
                    MOVE_ID
                ).`is`(lastMoveId)
            )
                .with(Sort.by(Sort.Direction.DESC, GAME_ID))
                .limit(1)
        query.fields().exclude(ENTITY_TYPE)


        return mongoTemplate.find(
            query,
            Zar::class.java,
            getCollectionName(matchId)
        ).firstOrNull()?.z ?: listOf()
    }

    override fun checkMatchExists(matchId: Int): Boolean {
        return mongoTemplate.collectionExists(getCollectionName(matchId))
    }

    override fun saveStartGameContext(matchId: Int, gameId: Int, context: GammonRestoreContextDto) {
        mongoTemplate.save(GameWithId(matchId, gameId, context), getCollectionName(matchId))
    }

    override fun getStartGameContext(matchId: Int, gameId: Int): GammonRestoreContextDto? {
        val query =
            Query().addCriteria(
                Criteria.where(ENTITY_TYPE).`is`(GameEntityType.START_STATE.name).and(GAME_ID).`is`(gameId)
            )
        query.fields().exclude(ENTITY_TYPE)
        return mongoTemplate.find(
            query,
            GameWithId::class.java,
            getCollectionName(matchId)
        ).firstOrNull()?.restoreContextDto
    }

    override fun getCurrentGameInMathId(matchId: Int): Int? {
        val query =
            Query().addCriteria(Criteria.where(ENTITY_TYPE).`is`(GameEntityType.START_STATE.name))
                .with(Sort.by(Sort.Direction.DESC, GAME_ID))
                .limit(1)
        query.fields().exclude(ENTITY_TYPE)


        return mongoTemplate.find(
            query,
            GameWithId::class.java,
            getCollectionName(matchId)
        ).firstOrNull()?.gameId
    }

    override fun storeWinner(winner: GameWinner) {
        mongoTemplate.save(winner, getCollectionName(winner.matchId))
    }

    private fun getCollectionName(matchId: Int): String {
        return "match$matchId"
    }
}