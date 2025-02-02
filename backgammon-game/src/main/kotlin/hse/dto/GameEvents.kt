package hse.dto

import game.backgammon.dto.MoveResponseDto
import game.backgammon.enums.Color
import hse.enums.EventType

abstract class GameEvent(val type: EventType)

class GameStartedEvent : GameEvent(EventType.GAME_STARTED_EVENT)

data class MoveEvent(
    val moves: List<MoveResponseDto>,
    val color: Color,
) : GameEvent(EventType.MOVE_EVENT)

data class PlayerConnectedEvent(
    val color: Color,
) : GameEvent(EventType.PLAYER_CONNECTED_EVENT)

data class EndGameEvent(
    val win: Color,
    val blackPoints: Int,
    val whitePoints: Int,
    val isMatchEnd: Boolean,
) : GameEvent(EventType.END_GAME_EVENT)


data class TossZarEvent(
    val value: Collection<Int>,
    val tossedBy: Color
) : GameEvent(EventType.TOSS_ZAR_EVENT)
