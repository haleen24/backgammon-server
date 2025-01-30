package hse.entity

data class MoveWithId(
    val matchId: Int,
    val gameId: Int,
    val moveSet: MoveSet,
)