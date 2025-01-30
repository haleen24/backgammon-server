package hse.changelogs

import com.mongodb.client.MongoDatabase
import de.hdi.mongobumblebee.changeset.ChangeLog
import de.hdi.mongobumblebee.changeset.ChangeSet
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index

@ChangeLog
class GammonMoveChangelog {

    companion object {
        const val MOVE_COLLECTION = "moveWithId"
        const val MATCH_COLLECTION = "matchWithId"
        const val MATCH_ID_FIELD = "matchId"
    }

    @ChangeSet(order = "001", author = "haleen24", id = "moveCollection")
    fun createMoveCollection(db: MongoDatabase) {
        db.getCollection(MOVE_COLLECTION) // create without options
    }

    @ChangeSet(order = "002", author = "haleen24", id = "gameIndexForMoves")
    fun createMoveGameIndex(template: MongoTemplate) {
        template.indexOps(MOVE_COLLECTION).ensureIndex(Index(MATCH_ID_FIELD, Sort.Direction.DESC))
    }


    @ChangeSet(order = "003", author = "haleen24", id = "gameCollection")
    fun createGameCollection(db: MongoDatabase) {
        db.getCollection(MATCH_COLLECTION) // create without options
    }

    @ChangeSet(order = "004", author = "haleen24", id = "gameIndex")
    fun createGameIndex(template: MongoTemplate) {
        template.indexOps(MATCH_COLLECTION).ensureIndex(Index(MATCH_ID_FIELD, Sort.Direction.DESC))
    }


}