package hse.controller

import game.backgammon.enums.BackgammonType
import game.backgammon.enums.Color
import game.backgammon.request.MoveRequest
import game.backgammon.response.ConfigResponse
import game.backgammon.response.MoveResponse
import hse.service.BackgammonGameService
import hse.service.EmitterService
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter


@RestController
@RequestMapping("backgammon")
class BackgammonGameController(
    private val backgammonGameService: BackgammonGameService,
    private val emitterService: EmitterService,
) {

    companion object {
        private const val USER_ID_HEADER = "auth-user"
    }

    @PostMapping("create-room/{roomId}")
    fun createGameRoom(
        @PathVariable roomId: Int,
        @RequestBody gameType: BackgammonType
    ): Int {
        return backgammonGameService.createGameRoom(roomId, gameType)
    }

    @PostMapping("connect/{roomId}")
    fun connect(@RequestHeader(USER_ID_HEADER) user: Int, @PathVariable roomId: Int) {
        backgammonGameService.connectToGameRoom(user, roomId)
    }

    @GetMapping("сonfig/{roomId}")
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

    @PostMapping("zar/{roomId}")
    fun tossZar(@RequestHeader(USER_ID_HEADER) userId: Int, @PathVariable("roomId") roomId: Int): Collection<Int> {
        return backgammonGameService.tossZar(userId, roomId)
    }

    @GetMapping("colors/{roomId}")
    fun getColor(@RequestHeader(USER_ID_HEADER) userId: Int, @PathVariable roomId: Int): Color {
        return backgammonGameService.getColor(userId, roomId)
    }

    @PostMapping("view/{roomId}")
    fun connectView(@RequestHeader(USER_ID_HEADER) userId: Int, @PathVariable roomId: Int): SseEmitter {
        return emitterService.create(roomId, userId)
    }
}