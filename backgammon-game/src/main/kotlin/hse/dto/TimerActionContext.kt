package hse.dto

import java.time.Duration
import java.time.ZonedDateTime

data class TimerActionContext(
    val opponentLastAction: ZonedDateTime,
    val playerRemainTime: Duration
)