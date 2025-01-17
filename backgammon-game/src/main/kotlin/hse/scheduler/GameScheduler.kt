package hse.scheduler

import hse.service.BackgammonGameService
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Component
import java.time.Duration

//@Component
//class GameScheduler(
//    private val scheduler: ThreadPoolTaskScheduler = ThreadPoolTaskScheduler(),
//    private val backgammonGameService: BackgammonGameService,
//
//    @Value("\${config.game.after-close-delay}")
//    private val closeGameDelaySeconds: Long,
//) {
//
//    init {
//        scheduler.poolSize = 10
//    }
//
//    fun closeGame(roomId: Int) {
//        scheduler.scheduleWithFixedDelay(
//            { backgammonGameService.closeGame(roomId) },
//            Duration.ofSeconds(closeGameDelaySeconds)
//        )
//    }
//}