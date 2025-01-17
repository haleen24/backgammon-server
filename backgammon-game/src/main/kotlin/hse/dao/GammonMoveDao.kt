package hse.dao

import hse.dto.GammonRestoreContextDto
import hse.entity.GameWithId
import hse.entity.MoveSet
import hse.entity.MoveWithId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class GammonMoveDao(
    private val mongoTemplate: MongoTemplate
) {

    fun saveMoves(gameId: Int, moveSet: MoveSet) {
        mongoTemplate.save(MoveWithId(gameId, moveSet))
    }

    fun getMoves(gameId: Int): List<MoveSet> {
        return mongoTemplate.find(Query().addCriteria(Criteria.where("gameId").`is`(gameId)), MoveWithId::class.java)
            .map { it.moveSet }
    }

    fun checkGameExists(gameId: Int): Boolean {
        return mongoTemplate.exists(Query().addCriteria(Criteria.where("gameId").`is`(gameId)), MoveWithId::class.java)
    }

    fun saveStartGameContext(gameId: Int, context: GammonRestoreContextDto) {
        mongoTemplate.save(GameWithId(gameId, context))
    }

    fun getStartGameContext(gameId: Int): GammonRestoreContextDto? {
        return mongoTemplate.find(
            Query().addCriteria(Criteria.where("gameId").`is`(gameId)),
            GameWithId::class.java
        ).firstOrNull()?.restoreContextDto
    }
}