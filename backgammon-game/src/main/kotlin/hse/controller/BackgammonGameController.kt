package hse.controller

import game.backgammon.enums.Color
import game.backgammon.request.CreateBackgammonGameRequest
import game.backgammon.request.MoveRequest
import game.backgammon.response.ConfigResponse
import game.backgammon.response.HistoryResponse
import game.backgammon.response.MoveResponse
import hse.service.BackgammonGameService
import hse.service.EmitterService
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter


@RestController
@RequestMapping("backgammon")
class BackgammonGameController(
    private val backgammonGameService: BackgammonGameService,
    private val emitterService: EmitterService,
) {

    val logger = LoggerFactory.getLogger(BackgammonGameController::class.java)

    companion object {
        private const val USER_ID_HEADER = "auth-user"
    }

    @PostMapping("create-room/{roomId}")
    fun createGameRoom(
        @PathVariable roomId: Int,
        @RequestBody request: CreateBackgammonGameRequest
    ): Int {
        return backgammonGameService.createAndConnect(roomId, request.firstUserId, request.secondUserId, request.type)
    }

    @GetMapping("config/{roomId}")
    fun getConfiguration(
        @RequestHeader(USER_ID_HEADER) user: Int,
        @PathVariable("roomId") roomId: Int
    ): ConfigResponse {
        return backgammonGameService.getConfiguration(user, roomId)
    }

    @PostMapping("move/{roomId}")
    fun move(
        @RequestHeader(USER_ID_HEADER) userId: Int,
        @PathVariable("roomId") roomId: Int,
        @RequestBody request: MoveRequest
    ): MoveResponse {
        return backgammonGameService.moveInGame(roomId, userId, request.moves)
    }

    @GetMapping("colors/{roomId}")
    fun getColor(@RequestHeader(USER_ID_HEADER) userId: Int, @PathVariable roomId: Int): Color {
        return backgammonGameService.getColor(userId, roomId)
    }

    @GetMapping("view/{roomId}")
    fun connectView(
        @RequestHeader(USER_ID_HEADER) userId: Int,
        @PathVariable roomId: Int,
        httpServletResponse: HttpServletResponse
    ): SseEmitter {
        httpServletResponse.addHeader("Content-Type", "text/event-stream")
        httpServletResponse.addHeader("Cache-Control", "no-cache")
        httpServletResponse.addHeader("X-Accel-Buffering", "no")
        logger.info("Зашли")
        val res = emitterService.create(roomId, userId)
        logger.info("Вышли")
        return res
    }

    @GetMapping("history/{roomId}")
    fun getHistory(@PathVariable roomId: Int): HistoryResponse {
        return backgammonGameService.getHistory(roomId)
    }
}