package hse.entity

import java.time.Duration
import java.time.ZonedDateTime

class GameTimer(
    val matchId: Int,
    var lastBlackAction: ZonedDateTime,
    var lastWhiteAction: ZonedDateTime,
    var remainBlackTime: Duration,
    var remainWhiteTime: Duration,
    var increment: Duration,
)