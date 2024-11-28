package hse.menu.controller

import game.backgammon.request.CreateGameRequest
import hse.menu.adapter.GameAdapter
import hse.menu.service.MenuGameService
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("menu")
class MenuController(
    private val menuGameService: MenuGameService,
    private val gameAdapter: GameAdapter,
) {
    companion object {
        private const val AUTH_USER = "auth-user"
    }

    @PostMapping("create-room")
    fun createRoom(
        @RequestBody requestBody: CreateGameRequest,
    ): Int? {
        val id = menuGameService.storeRoom(requestBody.type)

        return gameAdapter.gameCreation(requestBody.type, id)
    }

    @PostMapping("connect")
    fun connect(
        @RequestHeader(AUTH_USER) user: Int,
        @RequestBody request: CreateGameRequest,
    ): Int {
        return menuGameService.connect(user, request.type)
    }
}