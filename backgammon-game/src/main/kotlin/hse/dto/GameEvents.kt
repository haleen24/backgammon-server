package hse.dto

import game.backgammon.dto.MoveResponseDto
import game.backgammon.enums.Color
import hse.enums.EventType

abstract class GameEvent(val type: EventType)

data class MoveEvent(
    val moves: List<MoveResponseDto>,
    val color: Color,
) : GameEvent(EventType.MOVE_EVENT)

class PlayerConnectedEvent(
    val color: Color,
) : GameEvent(EventType.PLAYER_CONNECTED_EVENT)

class EndEvent(
    val win: Color,
    val lose: Color
) : GameEvent(EventType.END_EVENT)

class TossZarEvent(
    val value: Collection<Int>,
    val tossedBy: Color
) : GameEvent(EventType.TOSS_ZAR_EVENT)
