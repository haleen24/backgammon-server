package hse.controller

import game.backgammon.enums.Color
import game.backgammon.request.CreateBackgammonGameRequest
import game.backgammon.request.MoveRequest
import game.backgammon.response.ConfigResponse
import game.backgammon.response.HistoryResponse
import game.backgammon.response.MoveResponse
import hse.service.BackgammonGameService
import hse.service.DoubleCubeService
import hse.service.EmitterService
import hse.service.GammonHistoryService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter


@RestController
@RequestMapping("backgammon")
class BackgammonGameController(
    private val backgammonGameService: BackgammonGameService,
    private val emitterService: EmitterService,
    private val doubleCubeService: DoubleCubeService,
    private val gammonHistoryService: GammonHistoryService
) {

    companion object {
        private const val USER_ID_HEADER = "auth-user"
    }

    @PostMapping("create-room/{roomId}")
    fun createGameRoom(
        @PathVariable roomId: Int,
        @RequestBody request: CreateBackgammonGameRequest
    ): Int {
        return backgammonGameService.createAndConnect(roomId, request)
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
        @PathVariable roomId: Int,
        @RequestBody request: MoveRequest
    ): MoveResponse {
        return backgammonGameService.moveInGame(roomId, userId, request.moves)
    }

    @PostMapping("zar/{roomId}")
    fun tossZar(@RequestHeader(USER_ID_HEADER) userId: Int, @PathVariable roomId: Int) {
        backgammonGameService.tossZar(roomId, userId)
    }

    @PostMapping("/double/{roomId}")
    fun double(@RequestHeader(USER_ID_HEADER) userId: Int, @PathVariable roomId: Int) {
        doubleCubeService.doubleCube(roomId, userId)
    }

    @PostMapping("/double/accept/{roomId}")
    fun acceptDouble(@RequestHeader(USER_ID_HEADER) userId: Int, @PathVariable roomId: Int) {
        doubleCubeService.acceptDouble(roomId, userId)
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
        return emitterService.create(roomId, userId)
    }

    @GetMapping("history/{matchId}")
    fun getHistory(
        @RequestHeader(USER_ID_HEADER) userId: Int,
        @PathVariable matchId: Int,
        @RequestParam(required = false) gameId: Int? = null
    ): HistoryResponse {
        return if (gameId == null) {
            gammonHistoryService.getLastGameHistory(matchId)
        } else {
            gammonHistoryService.getHistory(matchId, gameId)
        }
    }

    @PostMapping("surrender/{matchId}")
    fun surrender(
        @RequestHeader(USER_ID_HEADER) userId: Int,
        @PathVariable matchId: Int,
        @RequestBody endMatch: Boolean
    ) {
        return backgammonGameService.surrender(userId, matchId, endMatch)
    }
}