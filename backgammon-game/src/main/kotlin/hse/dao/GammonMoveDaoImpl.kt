package hse.dao

import hse.dto.GammonRestoreContextDto
import hse.entity.GameWinner
import hse.entity.GameWithId
import hse.entity.MoveSet
import hse.entity.MoveWithId
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
        const val START_STATE_COLLECTION = "matchWithId"
        const val MATCH_ID = "matchId"
        const val GAME_ID = "gameId"
    }

    override fun saveMoves(matchId: Int, gameId: Int, moveSet: MoveSet) {
        mongoTemplate.save(MoveWithId(matchId, gameId, moveSet))
    }

    override fun getMoves(matchId: Int, gameId: Int): List<MoveSet> {
        return mongoTemplate.find(
            Query().addCriteria(
                Criteria.where(MATCH_ID).`is`(matchId).and(GAME_ID).`is`(gameId)
            ), MoveWithId::class.java
        )
            .map { it.moveSet }
    }

    override fun checkMatchExists(matchId: Int): Boolean {
        return mongoTemplate.exists(
            Query().addCriteria(Criteria.where(MATCH_ID).`is`(matchId)),
            START_STATE_COLLECTION
        )
    }

    override fun saveStartGameContext(matchId: Int, gameId: Int, context: GammonRestoreContextDto) {
        mongoTemplate.save(GameWithId(matchId, gameId, context), START_STATE_COLLECTION)
    }

    override fun getStartGameContext(matchId: Int, gameId: Int): GammonRestoreContextDto? {
        return mongoTemplate.find(
            Query().addCriteria(Criteria.where(MATCH_ID).`is`(matchId).and(GAME_ID).`is`(gameId)),
            GameWithId::class.java,
            START_STATE_COLLECTION
        ).firstOrNull()?.restoreContextDto
    }

    override fun getCurrentGameInMathId(matchId: Int): Int? {
        val query =
            Query().addCriteria(Criteria.where(MATCH_ID).`is`(matchId)).with(Sort.by(Sort.Direction.DESC, GAME_ID))
                .limit(1)


        return mongoTemplate.find(
            query,
            GameWithId::class.java,
            START_STATE_COLLECTION
        ).firstOrNull()?.gameId
    }

    override fun storeWinner(winner: GameWinner) {
        mongoTemplate.save(winner)
    }
}