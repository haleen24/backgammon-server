package hse.dto

import game.backgammon.dto.MoveDto
import game.backgammon.enums.Color
import hse.enums.EventType

abstract class GameEvent(type: EventType)

data class MoveEvent(
    val moves: Map<Int?, Int?>
) : GameEvent(EventType.MOVE_EVENT)

class PlayerConnectedEvent(
    val color: Color,
) : GameEvent(EventType.PLAYER_CONNECTED_EVENT)

class EndEvent(
    val win: Color,
    val lose: Color
) : GameEvent(EventType.END_EVENT)

class TossZarEvent(
    val value: Collection<Int>
) : GameEvent(EventType.TOSS_ZAR_EVENT)
