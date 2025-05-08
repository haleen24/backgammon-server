package hse.scheduler

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