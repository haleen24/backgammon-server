package game.backgammon.response

import game.backgammon.enums.Color

data class HistoryResponse(
    val items: List<HistoryResponseItem>,
    val firstToMove: Color,
)

open class HistoryResponseItem(
    val type: HistoryResponseItemType,
)

enum class HistoryResponseItemType {
    MOVE,
    OFFER_DOUBLE,
    ACCEPT_DOUBLE,
    GAME_END,
}