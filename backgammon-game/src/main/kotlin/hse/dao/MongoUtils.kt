package hse.dao

class MongoUtils {

    companion object {
        const val GAME_ID = "gameId"
        const val ENTITY_TYPE = "type"
        const val MOVE_ID = "moveId"

        fun getCollectionName(matchId: Int): String {
            return "match$matchId"
        }
    }
}